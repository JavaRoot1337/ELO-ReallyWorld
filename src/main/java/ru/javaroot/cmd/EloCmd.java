package ru.javaroot.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.javaroot.core.EloApp;
import ru.javaroot.data.PlayerData;

public class EloCmd implements CommandExecutor {
    private final EloApp app;

    public EloCmd(EloApp app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        if (args.length >= 1) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(app.getCfg().getPlayerNotFound());
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(app.getCfg().getUsageElo());
                return true;
            }
            target = (Player) sender;
        }

        PlayerData data = app.getDb().getCached(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(app.getCfg().getPlayerNotFound());
            return true;
        }

        for (String line : app.getCfg().getEloStats()) {
            line = line.replace("%player%", data.getName())
                       .replace("%elo%", String.valueOf(data.getElo()))
                       .replace("%rank%", app.getRankUtil().getRank(data.getElo()))
                       .replace("%kills%", String.valueOf(data.getKills()))
                       .replace("%deaths%", String.valueOf(data.getDeaths()))
                       .replace("%kd%", data.getKd());
            sender.sendMessage(line);
        }

        return true;
    }
}
