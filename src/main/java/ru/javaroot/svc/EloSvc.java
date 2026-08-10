package ru.javaroot.svc;

import org.bukkit.entity.Player;
import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

import java.util.UUID;

public class EloSvc {
    private final Elo app;

    public EloSvc(Elo app) {
        this.app = app;
    }

    public void processDeath(Player killer, Player victim) {
        if (killer == null || victim == null || killer.equals(victim)) return;

        PlayerData wData = app.getRepo().getCached(killer.getUniqueId());
        PlayerData lData = app.getRepo().getCached(victim.getUniqueId());

        if (wData != null && lData != null) {
            wData.addElo(app.getCfg().getEloWin());
            wData.addKill();

            lData.addElo(-app.getCfg().getEloLoss());
            lData.addDeath();

            app.getRepo().savePlayerAsync(killer.getUniqueId(), false);
            app.getRepo().savePlayerAsync(victim.getUniqueId(), false);
        }
    }

    public void giveElo(PlayerData pd, UUID uuid, int amount) {
        if (pd == null) return;
        pd.addElo(amount);
        app.getRepo().savePlayerAsync(uuid, false);
    }

    public void takeElo(PlayerData pd, UUID uuid, int amount) {
        if (pd == null) return;
        pd.addElo(-amount);
        app.getRepo().savePlayerAsync(uuid, false);
    }

    public void setElo(PlayerData pd, UUID uuid, int amount) {
        if (pd == null) return;
        pd.setElo(amount);
        app.getRepo().savePlayerAsync(uuid, false);
    }
}
