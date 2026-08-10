package ru.javaroot.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EloTabComp implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            if (sender.hasPermission("elo.admin")) {
                options.addAll(Arrays.asList("give", "take", "set", "reload"));
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                options.add(player.getName());
            }
            StringUtil.copyPartialMatches(args[0], options, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2 && sender.hasPermission("elo.admin")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                List<String> players = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    players.add(player.getName());
                }
                StringUtil.copyPartialMatches(args[1], players, completions);
                Collections.sort(completions);
                return completions;
            }
        }

        if (args.length == 3 && sender.hasPermission("elo.admin")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("give") || sub.equals("take") || sub.equals("set")) {
                List<String> amounts = Arrays.asList("10", "50", "100", "500", "1000");
                StringUtil.copyPartialMatches(args[2], amounts, completions);
                return completions;
            }
        }

        return Collections.emptyList();
    }
}
