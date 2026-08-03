package ru.javaroot.core;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class CfgMgr {
    private final EloApp app;
    private FileConfiguration config;
    private FileConfiguration messages;
    
    private int eloWin;
    private int eloLoss;
    private int eloDefault;
    private final TreeMap<Integer, String> ranks = new TreeMap<>();
    private String defaultRank;
    
    private String prefix;
    private String playerNotFound;
    private String usageElo;
    private List<String> eloStats;
    private String topHeader;
    private String topEmpty;
    private String topFormat;

    public CfgMgr(EloApp app) {
        this.app = app;
    }

    public void load() {
        app.saveDefaultConfig();
        app.reloadConfig();
        config = app.getConfig();
        
        File msgFile = new File(app.getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            app.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(msgFile);

        eloWin = config.getInt("elo-win", 15);
        eloLoss = config.getInt("elo-loss", 15);
        eloDefault = config.getInt("elo-default", 1000);
        
        ranks.clear();
        if (config.isConfigurationSection("ranks")) {
            for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
                try {
                    int elo = Integer.parseInt(key);
                    ranks.put(elo, c(config.getString("ranks." + key)));
                } catch (NumberFormatException ignored) {}
            }
        }
        defaultRank = c(config.getString("default-rank", "&6Феномен"));
        
        prefix = c(messages.getString("prefix", "&6[ELO] &f"));
        playerNotFound = c(messages.getString("player-not-found", "&cИгрок не найден."));
        usageElo = c(messages.getString("usage-elo", "&cИспользование: /elo <игрок>"));
        
        eloStats = new ArrayList<>();
        for (String s : messages.getStringList("elo-stats")) {
            eloStats.add(c(s));
        }
        
        topHeader = c(messages.getString("top-header", "&6=== Топ 10 по ELO ==="));
        topEmpty = c(messages.getString("top-empty", "&7Топ пока пуст."));
        topFormat = c(messages.getString("top-format", "&6#%num% &e%player% &7- &f%elo% &7ELO"));
    }

    private String c(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public int getEloWin() { return eloWin; }
    public int getEloLoss() { return eloLoss; }
    public int getEloDefault() { return eloDefault; }
    public TreeMap<Integer, String> getRanks() { return ranks; }
    public String getDefaultRank() { return defaultRank; }

    public String getPrefix() { return prefix; }
    public String getPlayerNotFound() { return playerNotFound; }
    public String getUsageElo() { return usageElo; }
    public List<String> getEloStats() { return eloStats; }
    public String getTopHeader() { return topHeader; }
    public String getTopEmpty() { return topEmpty; }
    public String getTopFormat() { return topFormat; }
}
