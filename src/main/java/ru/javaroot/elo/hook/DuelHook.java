package ru.javaroot.elo.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.javaroot.elo.cfg.CfgLoad;
import ru.javaroot.elo.svc.EloSvc;

import java.lang.reflect.Method;
import java.util.UUID;

public final class DuelHook implements Listener {
    private static final String[] EVENT_CLASSES = {
            "me.realized.duels.api.event.match.MatchEndEvent",
            "com.meteordevelopments.duels.api.event.match.MatchEndEvent"
    };

    private final CfgLoad cfgLoad;
    private final EloSvc svc;

    private DuelHook(CfgLoad cfgLoad, EloSvc svc) {
        this.cfgLoad = cfgLoad;
        this.svc = svc;
    }

    public static boolean register(Plugin plug, CfgLoad cfgLoad, EloSvc svc) {
        Class<? extends Event> eventClass = resolveMatchEndClass();
        if (eventClass == null) {
            return false;
        }
        DuelHook hook = new DuelHook(cfgLoad, svc);
        Bukkit.getPluginManager().registerEvent(
                eventClass,
                hook,
                EventPriority.MONITOR,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        hook.handleMatchEnd(event);
                    }
                },
                plug,
                true
        );
        return true;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Event> resolveMatchEndClass() {
        for (String name : EVENT_CLASSES) {
            try {
                Class<?> raw = Class.forName(name);
                if (Event.class.isAssignableFrom(raw)) {
                    return (Class<? extends Event>) raw;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private void handleMatchEnd(Event event) {
        if (!cfgLoad.cfg().onlyDuels()) {
            return;
        }
        UUID winner = extractUuid(event, "getWinner");
        UUID loser = extractUuid(event, "getLoser");
        if (winner != null && loser != null) {
            svc.recordMatch(winner, loser);
        }
    }

    private UUID extractUuid(Object source, String methodName) {
        Object val = invokeNoArg(source, methodName);
        if (val == null) {
            Object match = invokeNoArg(source, "getMatch");
            if (match != null && match != source) {
                val = invokeNoArg(match, methodName);
            }
        }
        return unwrapUuid(val);
    }

    private UUID unwrapUuid(Object obj) {
        if (obj == null) return null;
        if (obj instanceof UUID uuid) return uuid;
        if (obj instanceof Player p) return p.getUniqueId();
        if (obj instanceof String name) {
            Player p = Bukkit.getPlayerExact(name);
            return p != null ? p.getUniqueId() : null;
        }

        Object pObj = invokeNoArg(obj, "getPlayer");
        if (pObj instanceof Player p) return p.getUniqueId();

        Object uObj = invokeNoArg(obj, "getUuid");
        if (uObj instanceof UUID uuid) return uuid;

        uObj = invokeNoArg(obj, "getUniqueId");
        if (uObj instanceof UUID uuid) return uuid;
        return null;
    }

    private Object invokeNoArg(Object target, String name) {
        try {
            Method m = target.getClass().getMethod(name);
            return m.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
