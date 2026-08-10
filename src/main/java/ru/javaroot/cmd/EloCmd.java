package ru.javaroot.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

public class EloCmd implements CommandExecutor {
    private final Elo app;
    private final EloAdminCmd adminCmd;

    public EloCmd(Elo app, EloAdminCmd adminCmd) {
        this.app = app;
        this.adminCmd = adminCmd;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length >= 1) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set") || sub.equals("reload")) {
                return adminCmd.handleAdmin(sender, args);
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getPlayerNotFound());
                return true;
            }
            return showStats(sender, target);
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getUsageElo());
            return true;
        }

        return showStats(sender, (Player) sender);
    }

    private boolean showStats(CommandSender sender, Player target) {
        PlayerData data = app.getRepo().getCached(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getPlayerNotFound());
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
