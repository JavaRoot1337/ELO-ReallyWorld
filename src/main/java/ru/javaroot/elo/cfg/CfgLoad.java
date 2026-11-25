package ru.javaroot.elo.cfg;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public final class CfgLoad {
    private final Plugin plug;
    private volatile Cfg cfg;
    private volatile Msg msg;

    public CfgLoad(Plugin plug) {
        this.plug = plug;
    }

    public void load() {
        plug.saveDefaultConfig();
        plug.reloadConfig();
        YamlConfiguration rawCfg = loadWithDefaults("config.yml");
        YamlConfiguration rawMsg = loadWithDefaults("messages.yml");

        this.cfg = readCfg(rawCfg);
        this.msg = readMsg(rawMsg);
    }

    public Cfg cfg() {
        return cfg;
    }

    public Msg msg() {
        return msg;
    }

    private YamlConfiguration loadWithDefaults(String name) {
        File file = new File(plug.getDataFolder(), name);
        if (!file.exists()) {
            plug.saveResource(name, false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = plug.getResource(name)) {
            if (in != null) {
                YamlConfiguration def = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8)
                );
                yaml.setDefaults(def);
            }
        } catch (Exception ex) {
            plug.getLogger().warning("Failed to read bundled defaults for " + name + ": " + ex.getMessage());
        }
        return yaml;
    }

    private Cfg readCfg(YamlConfiguration y) {
        String rawType = y.getString("storage.type", "sqlite").toUpperCase(Locale.ROOT);
        Cfg.DbType dbType = "MYSQL".equals(rawType) ? Cfg.DbType.MYSQL : Cfg.DbType.SQLITE;
        Cfg.DbCfg dbCfg = new Cfg.DbCfg(
                dbType,
                y.getString("storage.host", "localhost"),
                Math.max(1, y.getInt("storage.port", 3306)),
                y.getString("storage.database", "elo"),
                y.getString("storage.user", "root"),
                y.getString("storage.password", ""),
                Math.max(2, y.getInt("storage.pool-size", 6))
        );

        int eloMin = y.getInt("elo-min", 0);
        int eloMax = Math.max(eloMin, y.getInt("elo-max", 100000));
        int eloDefault = clamp(y.getInt("elo-default", 1000), eloMin, eloMax);

        TreeMap<Integer, String> ranks = new TreeMap<>();
        ConfigurationSection sec = y.getConfigurationSection("ranks");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                try {
                    int score = Integer.parseInt(key);
                    String val = sec.getString(key);
                    if (val != null) {
                        ranks.put(score, val);
                    }
                } catch (NumberFormatException ignored) {
                    plug.getLogger().warning("Invalid rank threshold in config.yml: " + key);
                }
            }
        }

        return new Cfg(
                dbCfg,
                Math.max(0, y.getInt("elo-win", 15)),
                Math.max(0, y.getInt("elo-loss", 15)),
                eloDefault,
                eloMin,
                eloMax,
                Math.max(0, y.getInt("kill-cooldown-sec", 300)),
                Math.max(5, y.getInt("top-update-sec", 60)),
                Math.max(1, y.getInt("top-limit", 10)),
                Math.max(10, y.getInt("autosave-sec", 60)),
                y.getBoolean("only-duels", false),
                Collections.unmodifiableNavigableMap(ranks),
                y.getString("default-rank", "&6Феномен")
        );
    }

    private Msg readMsg(YamlConfiguration y) {
        List<String> stats = y.getStringList("elo-stats");
        return new Msg(
                y.getString("no-perm", ""),
                y.getString("player-not-found", ""),
                y.getString("only-player", ""),
                y.getString("invalid-amount", ""),
                y.getString("usage-elo", ""),
                y.getString("usage-admin", ""),
                List.copyOf(stats),
                y.getString("top-header", ""),
                y.getString("top-empty", ""),
                y.getString("top-format", ""),
                y.getString("admin-give", ""),
                y.getString("admin-take", ""),
                y.getString("admin-set", ""),
                y.getString("admin-reload", "")
        );
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
