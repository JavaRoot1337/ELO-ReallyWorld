package ru.javaroot.core;

import ru.javaroot.data.PlayerData;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DbMgr {
    private final EloApp app;
    private Connection conn;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final List<PlayerData> topCache = new CopyOnWriteArrayList<>();

    public DbMgr(EloApp app) {
        this.app = app;
    }

    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbFile = new File(app.getDataFolder(), "elo.db");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                        "uuid VARCHAR(36) PRIMARY KEY, " +
                        "name VARCHAR(16), " +
                        "elo INTEGER, " +
                        "kills INTEGER, " +
                        "deaths INTEGER)");
            }

            app.getServer().getScheduler().runTaskTimerAsynchronously(app, this::updateTopCache, 20L, 1200L); // Update every minute
        } catch (Exception e) {
            app.getLogger().severe("DB Init Err: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PlayerData getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public void loadPlayerAsync(UUID uuid, String name) {
        app.getServer().getScheduler().runTaskAsynchronously(app, () -> {
            PlayerData pd = null;
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pd = new PlayerData(
                        rs.getString("name"),
                        rs.getInt("elo"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        true
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (pd == null) {
                pd = new PlayerData(name, app.getCfg().getEloDefault(), 0, 0, false);
            } else {
                pd.setName(name);
            }
            cache.put(uuid, pd);
        });
    }

    public void savePlayerAsync(UUID uuid, boolean remove) {
        PlayerData pd = cache.get(uuid);
        if (pd == null) return;

        app.getServer().getScheduler().runTaskAsynchronously(app, () -> {
            try {
                if (pd.isInDb()) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE players SET name=?, elo=?, kills=?, deaths=? WHERE uuid=?")) {
                        ps.setString(1, pd.getName());
                        ps.setInt(2, pd.getElo());
                        ps.setInt(3, pd.getKills());
                        ps.setInt(4, pd.getDeaths());
                        ps.setString(5, uuid.toString());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO players (uuid, name, elo, kills, deaths) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, pd.getName());
                        ps.setInt(3, pd.getElo());
                        ps.setInt(4, pd.getKills());
                        ps.setInt(5, pd.getDeaths());
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (remove) {
                cache.remove(uuid);
            }
        });
    }

    public void saveAllSync() {
        for (UUID uuid : cache.keySet()) {
            PlayerData pd = cache.get(uuid);
            if (pd == null) continue;
            try {
                if (pd.isInDb()) {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE players SET name=?, elo=?, kills=?, deaths=? WHERE uuid=?")) {
                        ps.setString(1, pd.getName());
                        ps.setInt(2, pd.getElo());
                        ps.setInt(3, pd.getKills());
                        ps.setInt(4, pd.getDeaths());
                        ps.setString(5, uuid.toString());
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO players (uuid, name, elo, kills, deaths) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, pd.getName());
                        ps.setInt(3, pd.getElo());
                        ps.setInt(4, pd.getKills());
                        ps.setInt(5, pd.getDeaths());
                        ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void getTopAsync(int limit, TopCallback cb) {
        app.getServer().getScheduler().runTaskAsynchronously(app, () -> {
            List<PlayerData> top = fetchTop(limit);
            app.getServer().getScheduler().runTask(app, () -> cb.onResult(top));
        });
    }

    private void updateTopCache() {
        List<PlayerData> top = fetchTop(20);
        topCache.clear();
        topCache.addAll(top);
    }

    private List<PlayerData> fetchTop(int limit) {
        List<PlayerData> top = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM players ORDER BY elo DESC LIMIT ?")) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                top.add(new PlayerData(
                    rs.getString("name"),
                    rs.getInt("elo"),
                    rs.getInt("kills"),
                    rs.getInt("deaths"),
                    true
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    public List<PlayerData> getTopCache() {
        return topCache;
    }

    public interface TopCallback {
        void onResult(List<PlayerData> top);
    }
}
