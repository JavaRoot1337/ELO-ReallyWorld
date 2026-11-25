package ru.javaroot.elo.list;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.javaroot.elo.cfg.CfgLoad;
import ru.javaroot.elo.svc.EloSvc;

public final class DeathList implements Listener {
    private final CfgLoad cfgLoad;
    private final EloSvc svc;

    public DeathList(CfgLoad cfgLoad, EloSvc svc) {
        this.cfgLoad = cfgLoad;
        this.svc = svc;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        if (cfgLoad.cfg().onlyDuels()) {
            return;
        }
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) {
            return;
        }
        svc.recordMatch(killer.getUniqueId(), victim.getUniqueId());
    }
}
