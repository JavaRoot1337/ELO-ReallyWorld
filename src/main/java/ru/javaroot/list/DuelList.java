package ru.javaroot.list;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.javaroot.core.Elo;

import java.lang.reflect.Method;
import java.util.UUID;

public class DuelList implements Listener {
    private final Elo app;

    public DuelList(Elo app) {
        this.app = app;
    }

    @EventHandler
    public void onEvent(Event event) {
        if (!app.getCfg().isOnlyDuels()) return;

        String name = event.getClass().getSimpleName();
        if (!name.contains("MatchEnd") && !name.contains("DuelEnd") && !name.contains("MatchComplete")) {
            return;
        }

        Player winner = extractPlayer(event, "getWinner");
        Player loser = extractPlayer(event, "getLoser");

        if (winner != null && loser != null) {
            app.getSvc().processDeath(winner, loser);
        }
    }

    private Player extractPlayer(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            Object obj = method.invoke(target);
            if (obj == null) return null;

            if (obj instanceof Player) {
                return (Player) obj;
            }
            if (obj instanceof UUID) {
                return Bukkit.getPlayer((UUID) obj);
            }
            if (obj instanceof String) {
                return Bukkit.getPlayer((String) obj);
            }

            try {
                Method getPlayerMethod = obj.getClass().getMethod("getPlayer");
                Object playerObj = getPlayerMethod.invoke(obj);
                if (playerObj instanceof Player) {
                    return (Player) playerObj;
                }
            } catch (Exception ignored) {}

            try {
                Method getUuidMethod = obj.getClass().getMethod("getUuid");
                Object uuidObj = getUuidMethod.invoke(obj);
                if (uuidObj instanceof UUID) {
                    return Bukkit.getPlayer((UUID) uuidObj);
                }
            } catch (Exception ignored) {}

            try {
                Method getUniqueIdMethod = obj.getClass().getMethod("getUniqueId");
                Object uuidObj = getUniqueIdMethod.invoke(obj);
                if (uuidObj instanceof UUID) {
                    return Bukkit.getPlayer((UUID) uuidObj);
                }
            } catch (Exception ignored) {}

        } catch (Exception ignored) {
            try {
                Method getMatchMethod = target.getClass().getMethod("getMatch");
                Object match = getMatchMethod.invoke(target);
                if (match != null && !match.equals(target)) {
                    return extractPlayer(match, methodName);
                }
            } catch (Exception ignoredInner) {}
        }
        return null;
    }
}
