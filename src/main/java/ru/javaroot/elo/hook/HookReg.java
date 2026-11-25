package ru.javaroot.elo.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import ru.javaroot.elo.cfg.CfgLoad;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.svc.EloSvc;

import java.util.logging.Logger;

public final class HookReg {
    private final Plugin plug;
    private final CfgLoad cfgLoad;
    private final EloRepo repo;
    private final EloSvc svc;
    private PapiHook papi;

    public HookReg(Plugin plug, CfgLoad cfgLoad, EloRepo repo, EloSvc svc) {
        this.plug = plug;
        this.cfgLoad = cfgLoad;
        this.repo = repo;
        this.svc = svc;
    }

    public void init() {
        PluginManager pm = Bukkit.getPluginManager();
        Logger log = plug.getLogger();

        if (pm.isPluginEnabled("PlaceholderAPI")) {
            papi = new PapiHook(plug, repo, svc);
            papi.register();
            log.info("Hooked into PlaceholderAPI");
        }

        if (pm.isPluginEnabled("Duels")) {
            if (DuelHook.register(plug, cfgLoad, svc)) {
                log.info("Hooked into Duels");
            } else {
                log.warning("Duels detected, but MatchEndEvent class was not found");
            }
        } else if (cfgLoad.cfg().onlyDuels()) {
            log.warning("only-duels is enabled, but Duels plugin is not installed");
        }
    }

    public void shutdown() {
        if (papi != null) {
            papi.unregister();
            papi = null;
        }
    }
}
