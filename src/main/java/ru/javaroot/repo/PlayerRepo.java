package ru.javaroot.repo;

import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerRepo {
    private final Elo app;
    private final ConcurrentHashMap<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final List<PlayerData> topCache = new CopyOnWriteArrayList<>();

    public PlayerRepo(Elo app) {
        this.app = app;
    }

    public void startTopTask() {
        long ticks = app.getCfg().getTopUpdateSec() * 20L;
        if (ticks <= 0) ticks = 1200L;
        app.getServer().getScheduler().runTaskTimerAsynchronously(app, this::updateTopCache, 20L, ticks);
    }

    public PlayerData getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public void loadPlayerAsync(UUID uuid, String name) {
        app.getServer().getScheduler().runTaskAsynchronously(app, () -> {
            PlayerData pd = null;
            Connection conn = app.getDb().getConn();
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT name, elo, kills, deaths FROM players WHERE uuid = ?")) {
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
            saveSingle(uuid, pd);
            if (remove) {
                cache.remove(uuid);
            }
        });
    }

    public void saveAllSync() {
        for (UUID uuid : cache.keySet()) {
            PlayerData pd = cache.get(uuid);
            if (pd != null) {
                saveSingle(uuid, pd);
            }
        }
    }

    private void saveSingle(UUID uuid, PlayerData pd) {
        Connection conn = app.getDb().getConn();
        if (conn == null) return;
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
        Connection conn = app.getDb().getConn();
        if (conn == null) return top;
        try (PreparedStatement ps = conn.prepareStatement("SELECT name, elo, kills, deaths FROM players ORDER BY elo DESC LIMIT ?")) {
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
