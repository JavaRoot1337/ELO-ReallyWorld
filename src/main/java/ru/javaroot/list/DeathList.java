package ru.javaroot.list;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import ru.javaroot.core.EloApp;
import ru.javaroot.data.PlayerData;

public class DeathList implements Listener {
    private final EloApp app;

    public DeathList(EloApp app) {
        this.app = app;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        if (killer != null && !killer.equals(victim)) {
            PlayerData wData = app.getDb().getCached(killer.getUniqueId());
            PlayerData lData = app.getDb().getCached(victim.getUniqueId());

            if (wData != null && lData != null) {
                wData.addElo(app.getCfg().getEloWin());
                wData.addKill();

                lData.addElo(-app.getCfg().getEloLoss());
                lData.addDeath();

                app.getDb().savePlayerAsync(killer.getUniqueId(), false);
                app.getDb().savePlayerAsync(victim.getUniqueId(), false);
            }
        }
    }
}
