package ru.javaroot.elo.svc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.db.Db;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.db.mdl.EloRow;

import java.nio.file.Path;
import java.util.Collections;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EloSvcTest {

    @TempDir
    Path tempDir;

    private Cfg sampleCfg() {
        TreeMap<Integer, String> ranks = new TreeMap<>();
        ranks.put(1250, "Новичок");
        ranks.put(1500, "Профи");
        return new Cfg(
                new Cfg.DbCfg(Cfg.DbType.SQLITE, "localhost", 3306, "elo", "root", "", 4),
                20, 15, 1000, 0, 2000, 60, 60, 10, 60, false,
                Collections.unmodifiableNavigableMap(ranks), "Феномен"
        );
    }

    @Test
    void matchCooldownBoundsAndRanks() throws Exception {
        Cfg cfg = sampleCfg();
        Db db = new Db(tempDir.toFile(), cfg.db(), Logger.getLogger("test"));
        db.init();
        try {
            EloRepo repo = new EloRepo(db, Runnable::run, () -> cfg, Logger.getLogger("test"));
            AtomicLong clock = new AtomicLong(1_000_000L);
            EloSvc svc = new EloSvc(repo, () -> cfg, clock::get);

            UUID p1 = UUID.randomUUID();
            UUID p2 = UUID.randomUUID();
            EloRow w = repo.loadOnLogin(p1, "Winner").join();
            EloRow l = repo.loadOnLogin(p2, "Loser").join();

            assertTrue(svc.recordMatch(p1, p2));
            assertEquals(1020, w.elo());
            assertEquals(985, l.elo());
            assertEquals(1, w.kills());
            assertEquals(1, l.deaths());

            assertFalse(svc.recordMatch(p1, p2));
            clock.addAndGet(61_000L);
            assertTrue(svc.recordMatch(p1, p2));
            assertEquals(1040, w.elo());

            svc.giveElo(w, 5000);
            assertEquals(2000, w.elo());
            assertEquals("Профи", svc.rankOf(w.elo()));
            assertEquals("Феномен", svc.rankOf(900));

            svc.takeElo(l, 5000);
            assertEquals(0, l.elo());
        } finally {
            db.close();
        }
    }
}
