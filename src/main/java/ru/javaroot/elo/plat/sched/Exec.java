package ru.javaroot.elo.plat.sched;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class Exec {
    private final Plugin plug;
    private final Set<ScheduledTask> timers = ConcurrentHashMap.newKeySet();

    public Exec(Plugin plug) {
        this.plug = plug;
    }

    public void runAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plug, st -> task.run());
    }

    public void runGlobal(Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plug, st -> task.run());
    }

    public void runAtEntity(Entity ent, Runnable task) {
        ent.getScheduler().run(plug, st -> task.run(), null);
    }

    public void runAtSender(CommandSender snd, Runnable task) {
        if (snd instanceof Entity ent) {
            runAtEntity(ent, task);
            return;
        }
        runGlobal(task);
    }

    public ScheduledTask runAsyncTimer(Runnable task, long delaySec, long periodSec) {
        long safeDelay = Math.max(1L, delaySec);
        long safePeriod = Math.max(1L, periodSec);
        ScheduledTask st = Bukkit.getAsyncScheduler().runAtFixedRate(
                plug,
                ignored -> task.run(),
                safeDelay,
                safePeriod,
                TimeUnit.SECONDS
        );
        timers.add(st);
        return st;
    }

    public void cancel(ScheduledTask st) {
        if (st == null) return;
        timers.remove(st);
        st.cancel();
    }

    public void cancelAll() {
        for (ScheduledTask st : timers) {
            st.cancel();
        }
        timers.clear();
    }
}
