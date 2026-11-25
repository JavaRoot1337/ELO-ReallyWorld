package ru.javaroot.elo.cfg;

import java.util.List;

public record Msg(
        String noPerm,
        String playerNotFound,
        String onlyPlayer,
        String invalidAmount,
        String usageElo,
        String usageAdmin,
        List<String> eloStats,
        String topHeader,
        String topEmpty,
        String topFormat,
        String adminGive,
        String adminTake,
        String adminSet,
        String adminReload
) {
}
