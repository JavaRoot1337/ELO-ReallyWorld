package ru.javaroot.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

public class EloTopCmd implements CommandExecutor {
    private final Elo app;

    public EloTopCmd(Elo app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getTopHeader());
        
        java.util.List<PlayerData> top = app.getRepo().getTopCache();
        if (top.isEmpty()) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getTopEmpty());
            return true;
        }

        int num = 1;
        for (int i = 0; i < Math.min(top.size(), 10); i++) {
            PlayerData pd = top.get(i);
            String line = app.getCfg().getTopFormat()
                    .replace("%num%", String.valueOf(num++))
                    .replace("%player%", pd.getName())
                    .replace("%elo%", String.valueOf(pd.getElo()));
            sender.sendMessage(line);
        }
        return true;
    }
}
