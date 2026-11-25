package ru.javaroot.elo.svc;

import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.db.mdl.EloRow;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public final class EloSvc {
    private final EloRepo repo;
    private final Supplier<Cfg> cfgGet;
    private final LongSupplier clock;
    private final ConcurrentHashMap<KillPair, Long> killCooldowns = new ConcurrentHashMap<>();

    public EloSvc(EloRepo repo, Supplier<Cfg> cfgGet) {
        this(repo, cfgGet, System::currentTimeMillis);
    }

    public EloSvc(EloRepo repo, Supplier<Cfg> cfgGet, LongSupplier clock) {
        this.repo = repo;
        this.cfgGet = cfgGet;
        this.clock = clock;
    }

    public boolean recordMatch(UUID winnerUuid, UUID loserUuid) {
        if (winnerUuid == null || loserUuid == null || winnerUuid.equals(loserUuid)) {
            return false;
        }
        Cfg cfg = cfgGet.get();
        if (isOnCooldown(winnerUuid, loserUuid, cfg.killCooldownSec())) {
            return false;
        }

        EloRow winner = repo.getCached(winnerUuid);
        EloRow loser = repo.getCached(loserUuid);
        if (winner == null || loser == null) {
            return false;
        }

        winner.recordWin(cfg.eloWin(), cfg.eloMin(), cfg.eloMax());
        loser.recordLoss(cfg.eloLoss(), cfg.eloMin(), cfg.eloMax());
        repo.saveRowAsync(winner);
        repo.saveRowAsync(loser);
        return true;
    }

    public void giveElo(EloRow row, int amount) {
        if (row == null) return;
        Cfg cfg = cfgGet.get();
        row.addElo(amount, cfg.eloMin(), cfg.eloMax());
        repo.saveRowAsync(row);
    }

    public void takeElo(EloRow row, int amount) {
        if (row == null) return;
        Cfg cfg = cfgGet.get();
        row.addElo(-amount, cfg.eloMin(), cfg.eloMax());
        repo.saveRowAsync(row);
    }

    public void setElo(EloRow row, int amount) {
        if (row == null) return;
        Cfg cfg = cfgGet.get();
        row.setElo(amount, cfg.eloMin(), cfg.eloMax());
        repo.saveRowAsync(row);
    }

    public String rankOf(int elo) {
        Cfg cfg = cfgGet.get();
        Map.Entry<Integer, String> entry = cfg.ranks().floorEntry(elo);
        return entry != null ? entry.getValue() : cfg.defaultRank();
    }

    public void pruneCooldowns() {
        int cdSec = cfgGet.get().killCooldownSec();
        if (cdSec <= 0) {
            killCooldowns.clear();
            return;
        }
        long cutoff = clock.getAsLong() - cdSec * 1000L;
        killCooldowns.entrySet().removeIf(e -> e.getValue() < cutoff);
    }

    private boolean isOnCooldown(UUID killer, UUID victim, int cdSec) {
        if (cdSec <= 0) return false;
        long now = clock.getAsLong();
        long cdMs = cdSec * 1000L;
        KillPair pair = new KillPair(killer, victim);
        Long prev = killCooldowns.get(pair);
        if (prev != null && (now - prev) < cdMs) {
            return true;
        }
        killCooldowns.put(pair, now);
        return false;
    }

    private record KillPair(UUID killer, UUID victim) {
    }
}
