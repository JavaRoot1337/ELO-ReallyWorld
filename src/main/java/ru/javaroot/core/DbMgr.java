package ru.javaroot.core;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbMgr {
    private final Elo app;
    private Connection conn;

    public DbMgr(Elo app) {
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

    public Connection getConn() {
        return conn;
    }
}
