package ru.javaroot.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.javaroot.core.EloApp;
import ru.javaroot.data.PlayerData;

public class EloTopCmd implements CommandExecutor {
    private final EloApp app;

    public EloTopCmd(EloApp app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        app.getDb().getTopAsync(10, top -> {
            sender.sendMessage(app.getCfg().getTopHeader());
            if (top.isEmpty()) {
                sender.sendMessage(app.getCfg().getTopEmpty());
                return;
            }

            int num = 1;
            for (PlayerData pd : top) {
                String line = app.getCfg().getTopFormat()
                        .replace("%num%", String.valueOf(num++))
                        .replace("%player%", pd.getName())
                        .replace("%elo%", String.valueOf(pd.getElo()));
                sender.sendMessage(line);
            }
        });
        return true;
    }
}
