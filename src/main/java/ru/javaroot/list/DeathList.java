package ru.javaroot.list;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.javaroot.core.Elo;

public class DeathList implements Listener {
    private final Elo app;

    public DeathList(Elo app) {
        this.app = app;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (app.getCfg().isOnlyDuels()) {
            return;
        }

        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            app.getSvc().processDeath(killer, victim);
        }
    }
}
