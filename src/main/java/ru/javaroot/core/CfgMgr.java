package ru.javaroot.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.javaroot.util.ColorUtil;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

public class CfgMgr {
    private final Elo app;
    private FileConfiguration config;
    private FileConfiguration messages;
    
    private int eloWin;
    private int eloLoss;
    private int eloDefault;
    private int topUpdateSec;
    private boolean onlyDuels;
    private final TreeMap<Integer, String> ranks = new TreeMap<>();
    private String defaultRank;
    
    private String prefix;
    private String playerNotFound;
    private String noPerm;
    private String usageElo;
    private String usageAdmin;
    private List<String> eloStats;
    private String topHeader;
    private String topEmpty;
    private String topFormat;
    private String adminGiveSuccess;
    private String adminTakeSuccess;
    private String adminSetSuccess;
    private String adminReloadSuccess;

    public CfgMgr(Elo app) {
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
        topUpdateSec = config.getInt("top-update-sec");
        onlyDuels = config.getBoolean("only-duels");
        
        ranks.clear();
        if (config.isConfigurationSection("ranks")) {
            for (String key : config.getConfigurationSection("ranks").getKeys(false)) {
                try {
                    int elo = Integer.parseInt(key);
                    ranks.put(elo, ColorUtil.color(config.getString("ranks." + key)));
                } catch (NumberFormatException ignored) {}
            }
        }
        defaultRank = ColorUtil.color(config.getString("default-rank"));
        
        prefix = ColorUtil.color(messages.getString("prefix"));
        playerNotFound = ColorUtil.color(messages.getString("player-not-found"));
        noPerm = ColorUtil.color(messages.getString("no-perm"));
        usageElo = ColorUtil.color(messages.getString("usage-elo"));
        usageAdmin = ColorUtil.color(messages.getString("usage-admin"));
        
        eloStats = ColorUtil.color(messages.getStringList("elo-stats"));
        topHeader = ColorUtil.color(messages.getString("top-header"));
        topEmpty = ColorUtil.color(messages.getString("top-empty"));
        topFormat = ColorUtil.color(messages.getString("top-format"));
        
        adminGiveSuccess = ColorUtil.color(messages.getString("admin-give-success"));
        adminTakeSuccess = ColorUtil.color(messages.getString("admin-take-success"));
        adminSetSuccess = ColorUtil.color(messages.getString("admin-set-success"));
        adminReloadSuccess = ColorUtil.color(messages.getString("admin-reload-success"));
    }

    public int getEloWin() { return eloWin; }
    public int getEloLoss() { return eloLoss; }
    public int getEloDefault() { return eloDefault; }
    public int getTopUpdateSec() { return topUpdateSec; }
    public boolean isOnlyDuels() { return onlyDuels; }
    public TreeMap<Integer, String> getRanks() { return ranks; }
    public String getDefaultRank() { return defaultRank; }

    public String getPrefix() { return prefix; }
    public String getPlayerNotFound() { return playerNotFound; }
    public String getNoPerm() { return noPerm; }
    public String getUsageElo() { return usageElo; }
    public String getUsageAdmin() { return usageAdmin; }
    public List<String> getEloStats() { return eloStats; }
    public String getTopHeader() { return topHeader; }
    public String getTopEmpty() { return topEmpty; }
    public String getTopFormat() { return topFormat; }
    public String getAdminGiveSuccess() { return adminGiveSuccess; }
    public String getAdminTakeSuccess() { return adminTakeSuccess; }
    public String getAdminSetSuccess() { return adminSetSuccess; }
    public String getAdminReloadSuccess() { return adminReloadSuccess; }
}
