package ru.javaroot.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

public class EloAdminCmd {
    private final Elo app;

    public EloAdminCmd(Elo app) {
        this.app = app;
    }

    public boolean handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission("elo.admin")) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getNoPerm());
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            app.getCfg().load();
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getAdminReloadSuccess());
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getUsageAdmin());
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getPlayerNotFound());
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getUsageAdmin());
            return true;
        }

        PlayerData pd = app.getRepo().getCached(target.getUniqueId());
        if (pd == null) {
            sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getPlayerNotFound());
            return true;
        }

        switch (sub) {
            case "give":
                app.getSvc().giveElo(pd, target.getUniqueId(), amount);
                sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getAdminGiveSuccess()
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount)));
                break;
            case "take":
                app.getSvc().takeElo(pd, target.getUniqueId(), amount);
                sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getAdminTakeSuccess()
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount)));
                break;
            case "set":
                app.getSvc().setElo(pd, target.getUniqueId(), amount);
                sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getAdminSetSuccess()
                        .replace("%player%", target.getName())
                        .replace("%amount%", String.valueOf(amount)));
                break;
            default:
                sender.sendMessage(app.getCfg().getPrefix() + app.getCfg().getUsageAdmin());
                break;
        }

        return true;
    }
}
