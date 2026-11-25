package ru.javaroot.elo;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.cfg.CfgLoad;
import ru.javaroot.elo.cmd.EloCmd;
import ru.javaroot.elo.db.Db;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.hook.HookReg;
import ru.javaroot.elo.list.DeathList;
import ru.javaroot.elo.list.SessList;
import ru.javaroot.elo.plat.sched.Exec;
import ru.javaroot.elo.svc.EloSvc;

import java.sql.SQLException;
import java.util.logging.Level;

public final class Elo extends JavaPlugin {
    private CfgLoad cfgLoad;
    private Exec exec;
    private Db db;
    private EloRepo repo;
    private EloSvc svc;
    private HookReg hooks;

    private ScheduledTask topTask;
    private ScheduledTask autosaveTask;

    @Override
    public void onEnable() {
        cfgLoad = new CfgLoad(this);
        cfgLoad.load();

        exec = new Exec(this);
        Cfg cfg = cfgLoad.cfg();
        db = new Db(getDataFolder(), cfg.db(), getLogger());
        try {
            db.init();
        } catch (SQLException ex) {
            getLogger().log(Level.SEVERE, "Failed to initialize database (" + cfg.db().type() + ")", ex);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        repo = new EloRepo(db, exec::runAsync, cfgLoad::cfg, getLogger());
        svc = new EloSvc(repo, cfgLoad::cfg);

        hooks = new HookReg(this, cfgLoad, repo, svc);
        hooks.init();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new SessList(repo), this);
        pm.registerEvents(new DeathList(cfgLoad, svc), this);

        EloCmd cmd = new EloCmd(cfgLoad, repo, svc, exec, this::reloadPlugin);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> cmd.register(event.registrar()));

        startTimers();
        exec.runAsync(repo::refreshTop);
    }

    @Override
    public void onDisable() {
        if (exec != null) {
            exec.cancelAll();
        }
        if (hooks != null) {
            hooks.shutdown();
        }
        if (repo != null) {
            repo.flushAllSync();
        }
        if (db != null) {
            db.close();
        }
    }

    private void reloadPlugin() {
        cfgLoad.load();
        startTimers();
        exec.runAsync(repo::refreshTop);
    }

    private void startTimers() {
        exec.cancel(topTask);
        exec.cancel(autosaveTask);

        Cfg cfg = cfgLoad.cfg();
        topTask = exec.runAsyncTimer(repo::refreshTop, cfg.topUpdateSec(), cfg.topUpdateSec());
        autosaveTask = exec.runAsyncTimer(() -> {
            repo.flushDirtyAsync();
            svc.pruneCooldowns();
        }, cfg.autosaveSec(), cfg.autosaveSec());
    }
}
