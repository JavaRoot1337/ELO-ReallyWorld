package ru.javaroot.list;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.javaroot.core.Elo;

public class JoinQuitList implements Listener {
    private final Elo app;

    public JoinQuitList(Elo app) {
        this.app = app;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        app.getRepo().loadPlayerAsync(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        app.getRepo().savePlayerAsync(e.getPlayer().getUniqueId(), true);
    }
}
