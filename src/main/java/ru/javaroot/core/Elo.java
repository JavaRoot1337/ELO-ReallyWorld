package ru.javaroot.core;

import org.bukkit.plugin.java.JavaPlugin;
import ru.javaroot.cmd.EloAdminCmd;
import ru.javaroot.cmd.EloCmd;
import ru.javaroot.cmd.EloTabComp;
import ru.javaroot.cmd.EloTopCmd;
import ru.javaroot.list.DeathList;
import ru.javaroot.list.DuelList;
import ru.javaroot.list.JoinQuitList;
import ru.javaroot.papi.EloExpansion;
import ru.javaroot.repo.PlayerRepo;
import ru.javaroot.svc.EloSvc;
import ru.javaroot.util.RankUtil;

public class Elo extends JavaPlugin {
    private CfgMgr cfg;
    private DbMgr db;
    private PlayerRepo repo;
    private EloSvc svc;
    private RankUtil rankUtil;

    @Override
    public void onEnable() {
        cfg = new CfgMgr(this);
        cfg.load();

        db = new DbMgr(this);
        db.init();

        repo = new PlayerRepo(this);
        repo.startTopTask();

        svc = new EloSvc(this);
        rankUtil = new RankUtil(cfg);

        EloAdminCmd adminCmd = new EloAdminCmd(this);
        EloTabComp tabComp = new EloTabComp();

        getCommand("elo").setExecutor(new EloCmd(this, adminCmd));
        getCommand("elo").setTabCompleter(tabComp);
        getCommand("elotop").setExecutor(new EloTopCmd(this));

        getServer().getPluginManager().registerEvents(new JoinQuitList(this), this);
        getServer().getPluginManager().registerEvents(new DeathList(this), this);
        getServer().getPluginManager().registerEvents(new DuelList(this), this);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EloExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        if (repo != null) {
            repo.saveAllSync();
        }
        if (db != null) {
            db.close();
        }
    }

    public CfgMgr getCfg() { return cfg; }
    public DbMgr getDb() { return db; }
    public PlayerRepo getRepo() { return repo; }
    public EloSvc getSvc() { return svc; }
    public RankUtil getRankUtil() { return rankUtil; }
}
