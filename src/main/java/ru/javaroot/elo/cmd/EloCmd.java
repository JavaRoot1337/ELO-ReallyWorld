package ru.javaroot.elo.cmd;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.cfg.CfgLoad;
import ru.javaroot.elo.cfg.Msg;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.db.mdl.EloRow;
import ru.javaroot.elo.db.mdl.TopRow;
import ru.javaroot.elo.plat.sched.Exec;
import ru.javaroot.elo.plat.txt.Txt;
import ru.javaroot.elo.svc.EloSvc;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class EloCmd {
    private static final String PERM_USE = "elo.use";
    private static final String PERM_TOP = "elo.top";
    private static final String PERM_ADMIN = "elo.admin";

    private final CfgLoad cfgLoad;
    private final EloRepo repo;
    private final EloSvc svc;
    private final Exec exec;
    private final Runnable reloadTask;

    public EloCmd(CfgLoad cfgLoad, EloRepo repo, EloSvc svc, Exec exec, Runnable reloadTask) {
        this.cfgLoad = cfgLoad;
        this.repo = repo;
        this.svc = svc;
        this.exec = exec;
        this.reloadTask = reloadTask;
    }

    public void register(Commands cmds) {
        cmds.register(buildElo().build(), "Статистика и управление ELO");
        cmds.register(buildTop().build(), "Топ игроков по ELO");
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildElo() {
        return Commands.literal("elo")
                .requires(s -> s.getSender().hasPermission(PERM_USE) || s.getSender().hasPermission(PERM_ADMIN))
                .executes(ctx -> {
                    CommandSender snd = ctx.getSource().getSender();
                    if (!(snd instanceof Player p)) {
                        Txt.send(snd, cfgLoad.msg().usageElo());
                        return Command.SINGLE_SUCCESS;
                    }
                    EloRow row = repo.getCached(p.getUniqueId());
                    if (row == null) {
                        Txt.send(snd, cfgLoad.msg().playerNotFound(), Txt.text("%player%", p.getName()));
                        return Command.SINGLE_SUCCESS;
                    }
                    sendStats(snd, row);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(s -> s.getSender().hasPermission(PERM_ADMIN))
                        .executes(ctx -> {
                            long start = System.currentTimeMillis();
                            reloadTask.run();
                            long ms = System.currentTimeMillis() - start;
                            Txt.send(ctx.getSource().getSender(), cfgLoad.msg().adminReload(),
                                    Txt.text("%ms%", ms));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(adminModify("give", Action.GIVE))
                .then(adminModify("take", Action.TAKE))
                .then(adminModify("set", Action.SET))
                .then(Commands.argument("player", StringArgumentType.word())
                        .requires(s -> s.getSender().hasPermission(PERM_USE))
                        .suggests(this::suggestPlayers)
                        .executes(ctx -> {
                            CommandSender snd = ctx.getSource().getSender();
                            String targetName = StringArgumentType.getString(ctx, "player");
                            lookupAndReply(snd, targetName);
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> adminModify(String literal, Action action) {
        int minAmount = action == Action.SET ? 0 : 1;
        return Commands.literal(literal)
                .requires(s -> s.getSender().hasPermission(PERM_ADMIN))
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(this::suggestPlayers)
                        .then(Commands.argument("amount", IntegerArgumentType.integer(minAmount))
                                .executes(ctx -> {
                                    CommandSender snd = ctx.getSource().getSender();
                                    String targetName = StringArgumentType.getString(ctx, "player");
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    modifyPlayer(snd, targetName, amount, action);
                                    return Command.SINGLE_SUCCESS;
                                })));
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildTop() {
        return Commands.literal("elotop")
                .requires(s -> s.getSender().hasPermission(PERM_TOP))
                .executes(ctx -> {
                    CommandSender snd = ctx.getSource().getSender();
                    Cfg cfg = cfgLoad.cfg();
                    Msg msg = cfgLoad.msg();

                    Txt.send(snd, msg.topHeader(), Txt.text("%limit%", cfg.topLimit()));

                    List<TopRow> top = repo.topCache();
                    if (top.isEmpty()) {
                        Txt.send(snd, msg.topEmpty());
                        return Command.SINGLE_SUCCESS;
                    }

                    int limit = Math.min(top.size(), cfg.topLimit());
                    for (int i = 0; i < limit; i++) {
                        TopRow row = top.get(i);
                        Txt.send(snd, msg.topFormat(),
                                Txt.text("%num%", i + 1),
                                Txt.text("%player%", row.name()),
                                Txt.text("%elo%", row.elo()),
                                Txt.color("%rank%", svc.rankOf(row.elo())),
                                Txt.text("%kills%", row.kills()),
                                Txt.text("%deaths%", row.deaths()),
                                Txt.text("%kd%", row.kd()));
                    }
                    return Command.SINGLE_SUCCESS;
                });
    }

    private void lookupAndReply(CommandSender snd, String targetName) {
        EloRow cached = repo.getCachedByName(targetName);
        if (cached != null) {
            sendStats(snd, cached);
            return;
        }
        repo.findProfile(targetName).thenAccept(row -> exec.runAtSender(snd, () -> {
            if (row == null) {
                Txt.send(snd, cfgLoad.msg().playerNotFound(), Txt.text("%player%", targetName));
                return;
            }
            sendStats(snd, row);
        }));
    }

    private void modifyPlayer(CommandSender snd, String targetName, int amount, Action action) {
        repo.findProfile(targetName).thenAccept(row -> exec.runAtSender(snd, () -> {
            Msg msg = cfgLoad.msg();
            if (row == null) {
                Txt.send(snd, msg.playerNotFound(), Txt.text("%player%", targetName));
                return;
            }
            String template = switch (action) {
                case GIVE -> {
                    svc.giveElo(row, amount);
                    yield msg.adminGive();
                }
                case TAKE -> {
                    svc.takeElo(row, amount);
                    yield msg.adminTake();
                }
                case SET -> {
                    svc.setElo(row, amount);
                    yield msg.adminSet();
                }
            };
            Txt.send(snd, template,
                    Txt.text("%player%", row.name()),
                    Txt.text("%amount%", amount));
        }));
    }

    private void sendStats(CommandSender snd, EloRow row) {
        Msg msg = cfgLoad.msg();
        int elo = row.elo();
        for (String line : msg.eloStats()) {
            Txt.send(snd, line,
                    Txt.text("%player%", row.name()),
                    Txt.text("%elo%", elo),
                    Txt.color("%rank%", svc.rankOf(elo)),
                    Txt.text("%kills%", row.kills()),
                    Txt.text("%deaths%", row.deaths()),
                    Txt.text("%kd%", row.kd()));
        }
    }

    private CompletableFuture<Suggestions> suggestPlayers(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
            SuggestionsBuilder builder
    ) {
        String rem = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (Player p : Bukkit.getOnlinePlayers()) {
            String name = p.getName();
            if (name.toLowerCase(Locale.ROOT).startsWith(rem)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    private enum Action {
        GIVE,
        TAKE,
        SET
    }
}
