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

        eloWin = config.getInt("elo-win");
        eloLoss = config.getInt("elo-loss");
        eloDefault = config.getInt("elo-default");
        
        ranks.clear();
        if (config.isConfigurationSection("ranks")) {
            for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
                try {
                    int elo = Integer.parseInt(key);
                    ranks.put(elo, c(config.getString("ranks." + key)));
                } catch (NumberFormatException ignored) {}
            }
        }
        defaultRank = c(config.getString("default-rank"));
        
        prefix = c(messages.getString("prefix"));
        playerNotFound = c(messages.getString("player-not-found"));
        usageElo = c(messages.getString("usage-elo"));
        
        eloStats = new ArrayList<>();
        for (String s : messages.getStringList("elo-stats")) {
            eloStats.add(c(s));
        }
        
        topHeader = c(messages.getString("top-header"));
        topEmpty = c(messages.getString("top-empty"));
        topFormat = c(messages.getString("top-format"));
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
