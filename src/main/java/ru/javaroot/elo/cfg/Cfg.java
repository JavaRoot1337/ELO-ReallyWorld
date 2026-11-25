package ru.javaroot.elo.cfg;

import java.util.NavigableMap;

public record Cfg(
        DbCfg db,
        int eloWin,
        int eloLoss,
        int eloDefault,
        int eloMin,
        int eloMax,
        int killCooldownSec,
        int topUpdateSec,
        int topLimit,
        int autosaveSec,
        boolean onlyDuels,
        NavigableMap<Integer, String> ranks,
        String defaultRank
) {
    public enum DbType {
        SQLITE,
        MYSQL
    }

    public record DbCfg(
            DbType type,
            String host,
            int port,
            String database,
            String user,
            String password,
            int poolSize
    ) {
    }
}
