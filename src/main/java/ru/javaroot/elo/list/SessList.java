package ru.javaroot.elo.list;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.javaroot.elo.db.EloRepo;

public final class SessList implements Listener {
    private final EloRepo repo;

    public SessList(EloRepo repo) {
        this.repo = repo;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        repo.loadOnLogin(e.getUniqueId(), e.getName()).join();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (repo.getCached(e.getPlayer().getUniqueId()) == null) {
            repo.loadOnLogin(e.getPlayer().getUniqueId(), e.getPlayer().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        repo.saveAndUnload(e.getPlayer().getUniqueId());
    }
}
