package ru.javaroot.list;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.javaroot.core.EloApp;

public class JoinQuitList implements Listener {
    private final EloApp app;

    public JoinQuitList(EloApp app) {
        this.app = app;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        app.getDb().loadPlayerAsync(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        app.getDb().savePlayerAsync(e.getPlayer().getUniqueId(), true);
    }
}
