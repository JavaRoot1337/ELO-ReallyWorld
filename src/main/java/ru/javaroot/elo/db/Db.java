package ru.javaroot.elo.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.db.mdl.EloRow;
import ru.javaroot.elo.db.mdl.TopRow;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Db {
    private static final int SCHEMA_VERSION = 1;

    private final File dataDir;
    private final Cfg.DbCfg dbCfg;
    private final Logger log;
    private HikariDataSource ds;

    public Db(File dataDir, Cfg.DbCfg dbCfg, Logger log) {
        this.dataDir = dataDir;
        this.dbCfg = dbCfg;
        this.log = log;
    }

    public void init() throws SQLException {
        if (dbCfg.type() == Cfg.DbType.MYSQL) {
            ds = createMysqlPool();
        } else {
            ds = createSqlitePool();
        }
        initSchema();
    }

    private HikariDataSource createSqlitePool() throws SQLException {
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new SQLException("Failed to create plugin data directory: " + dataDir);
        }
        File dbFile = new File(dataDir, "elo.db");
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("elo-sqlite");
        hc.setDriverClassName("org.sqlite.JDBC");
        hc.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        hc.setMaximumPoolSize(1);
        hc.setConnectionTimeout(10_000L);

        HikariDataSource pool = new HikariDataSource(hc);
        try (Connection conn = pool.getConnection(); Statement st = conn.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL;");
            st.execute("PRAGMA synchronous=NORMAL;");
            st.execute("PRAGMA busy_timeout=5000;");
            st.execute("PRAGMA foreign_keys=ON;");
        } catch (SQLException ex) {
            pool.close();
            throw ex;
        }
        return pool;
    }

    private HikariDataSource createMysqlPool() {
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("elo-mysql");
        hc.setDriverClassName(org.mariadb.jdbc.Driver.class.getName());
        hc.setJdbcUrl("jdbc:mariadb://" + dbCfg.host() + ":" + dbCfg.port() + "/" + dbCfg.database()
                + "?useBulkStmts=true&characterEncoding=utf8");
        hc.setUsername(dbCfg.user());
        hc.setPassword(dbCfg.password());
        int maxPool = Math.max(2, dbCfg.poolSize());
        hc.setMaximumPoolSize(maxPool);
        hc.setMinimumIdle(Math.min(2, maxPool));
        hc.setConnectionTimeout(10_000L);
        hc.setMaxLifetime(1_700_000L);
        return new HikariDataSource(hc);
    }

    private void initSchema() throws SQLException {
        try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS schema_version (version INT PRIMARY KEY)");
            st.execute("""
                    CREATE TABLE IF NOT EXISTS elo_players (
                        uuid CHAR(36) PRIMARY KEY,
                        name VARCHAR(16) NOT NULL,
                        elo INT NOT NULL,
                        kills INT NOT NULL DEFAULT 0,
                        deaths INT NOT NULL DEFAULT 0,
                        updated_at BIGINT NOT NULL
                    )""");

            int curVersion = readSchemaVersion(conn);
            if (curVersion < SCHEMA_VERSION) {
                applyV1Migration(conn, st);
            }
        }
    }

    private int readSchemaVersion(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(version) FROM schema_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void applyV1Migration(Connection conn, Statement st) throws SQLException {
        if (dbCfg.type() == Cfg.DbType.SQLITE) {
            st.execute("CREATE INDEX IF NOT EXISTS idx_elo_players_elo ON elo_players (elo DESC, kills DESC)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_elo_players_name ON elo_players (name)");
            migrateLegacySqliteTable(conn);
        } else {
            createMysqlIndexIfAbsent(conn, "idx_elo_players_elo", "elo DESC, kills DESC");
            createMysqlIndexIfAbsent(conn, "idx_elo_players_name", "name");
        }

        String verSql = dbCfg.type() == Cfg.DbType.SQLITE
                ? "INSERT OR IGNORE INTO schema_version (version) VALUES (?)"
                : "INSERT IGNORE INTO schema_version (version) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(verSql)) {
            ps.setInt(1, SCHEMA_VERSION);
            ps.executeUpdate();
        }
    }

    private void createMysqlIndexIfAbsent(Connection conn, String indexName, String cols) throws SQLException {
        String check = """
                SELECT 1 FROM information_schema.statistics
                WHERE table_schema = DATABASE() AND table_name = 'elo_players' AND index_name = ?
                LIMIT 1""";
        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, indexName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        }
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE INDEX " + indexName + " ON elo_players (" + cols + ")");
        }
    }

    private void migrateLegacySqliteTable(Connection conn) {
        String check = "SELECT 1 FROM sqlite_master WHERE type='table' AND name='players' LIMIT 1";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(check)) {
            if (!rs.next()) return;
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to check legacy players table", ex);
            return;
        }

        String copy = """
                INSERT OR IGNORE INTO elo_players (uuid, name, elo, kills, deaths, updated_at)
                SELECT uuid, COALESCE(name, 'Unknown'), elo, kills, deaths, ? FROM players""";
        try (PreparedStatement ps = conn.prepareStatement(copy)) {
            ps.setLong(1, System.currentTimeMillis());
            int migrated = ps.executeUpdate();
            if (migrated > 0) {
                log.info("Migrated " + migrated + " player rows from legacy players table");
            }
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to migrate rows from legacy players table", ex);
        }
    }

    public EloRow findByUuid(UUID uuid) throws SQLException {
        String sql = "SELECT name, elo, kills, deaths FROM elo_players WHERE uuid = ? LIMIT 1";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new EloRow(
                        uuid,
                        rs.getString("name"),
                        rs.getInt("elo"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        false,
                        true
                );
            }
        }
    }

    public EloRow findByName(String name) throws SQLException {
        String sql = """
                SELECT uuid, name, elo, kills, deaths
                FROM elo_players
                WHERE LOWER(name) = LOWER(?)
                ORDER BY updated_at DESC
                LIMIT 1""";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                UUID uuid;
                try {
                    uuid = UUID.fromString(rs.getString("uuid"));
                } catch (IllegalArgumentException ignored) {
                    return null;
                }
                return new EloRow(
                        uuid,
                        rs.getString("name"),
                        rs.getInt("elo"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        false,
                        true
                );
            }
        }
    }

    public void upsert(EloRow.Snap snap, long nowMs) throws SQLException {
        String sql = dbCfg.type() == Cfg.DbType.SQLITE
                ? """
                  INSERT INTO elo_players (uuid, name, elo, kills, deaths, updated_at)
                  VALUES (?, ?, ?, ?, ?, ?)
                  ON CONFLICT(uuid) DO UPDATE SET
                      name = excluded.name,
                      elo = excluded.elo,
                      kills = excluded.kills,
                      deaths = excluded.deaths,
                      updated_at = excluded.updated_at"""
                : """
                  INSERT INTO elo_players (uuid, name, elo, kills, deaths, updated_at)
                  VALUES (?, ?, ?, ?, ?, ?)
                  ON DUPLICATE KEY UPDATE
                      name = VALUES(name),
                      elo = VALUES(elo),
                      kills = VALUES(kills),
                      deaths = VALUES(deaths),
                      updated_at = VALUES(updated_at)""";
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, snap.uuid().toString());
            ps.setString(2, snap.name());
            ps.setInt(3, snap.elo());
            ps.setInt(4, snap.kills());
            ps.setInt(5, snap.deaths());
            ps.setLong(6, nowMs);
            ps.executeUpdate();
        }
    }

    public List<TopRow> fetchTop(int limit) throws SQLException {
        String sql = "SELECT uuid, name, elo, kills, deaths FROM elo_players ORDER BY elo DESC, kills DESC LIMIT ?";
        List<TopRow> rows = new ArrayList<>();
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid;
                    try {
                        uuid = UUID.fromString(rs.getString("uuid"));
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }
                    rows.add(new TopRow(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("elo"),
                            rs.getInt("kills"),
                            rs.getInt("deaths")
                    ));
                }
            }
        }
        return List.copyOf(rows);
    }

    public void close() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}
