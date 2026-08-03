package ru.javaroot.core;

import org.bukkit.plugin.java.JavaPlugin;
import ru.javaroot.cmd.EloCmd;
import ru.javaroot.cmd.EloTopCmd;
import ru.javaroot.list.DeathList;
import ru.javaroot.list.JoinQuitList;
import ru.javaroot.papi.EloExpansion;
import ru.javaroot.util.RankUtil;

public class EloApp extends JavaPlugin {
    
    private CfgMgr cfg;
    private DbMgr db;
    private RankUtil rankUtil;

    @Override
    public void onEnable() {
        cfg = new CfgMgr(this);
        cfg.load();

        db = new DbMgr(this);
        db.init();

        rankUtil = new RankUtil(cfg);

        getCommand("elo").setExecutor(new EloCmd(this));
        getCommand("elotop").setExecutor(new EloTopCmd(this));

        getServer().getPluginManager().registerEvents(new JoinQuitList(this), this);
        getServer().getPluginManager().registerEvents(new DeathList(this), this);
        
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EloExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        if (db != null) {
            db.saveAllSync();
            db.close();
        }
    }

    public CfgMgr getCfg() { return cfg; }
    public DbMgr getDb() { return db; }
    public RankUtil getRankUtil() { return rankUtil; }
}
