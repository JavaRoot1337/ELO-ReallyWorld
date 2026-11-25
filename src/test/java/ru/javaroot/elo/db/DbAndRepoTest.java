package ru.javaroot.elo.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.db.mdl.EloRow;
import ru.javaroot.elo.db.mdl.TopRow;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class DbAndRepoTest {

    @TempDir
    Path tempDir;

    private Cfg sampleCfg() {
        TreeMap<Integer, String> ranks = new TreeMap<>();
        ranks.put(1250, "&aНовичок");
        ranks.put(1500, "&cПрофи");
        return new Cfg(
                new Cfg.DbCfg(Cfg.DbType.SQLITE, "localhost", 3306, "elo", "root", "", 4),
                15, 15, 1000, 0, 100000, 300, 60, 10, 60, false,
                Collections.unmodifiableNavigableMap(ranks), "&6Феномен"
        );
    }

    @Test
    void sqliteUpsertLookupTopAndLegacyMigration() throws Exception {
        Path legacyDb = tempDir.resolve("elo.db");
        UUID legacyUuid = UUID.randomUUID();
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + legacyDb.toAbsolutePath());
             Statement st = c.createStatement()) {
            st.execute("CREATE TABLE players (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), elo INTEGER, kills INTEGER, deaths INTEGER)");
            st.execute("INSERT INTO players VALUES ('" + legacyUuid + "', 'OldHero', 1800, 20, 5)");
        }

        Cfg cfg = sampleCfg();
        Db db = new Db(tempDir.toFile(), cfg.db(), Logger.getLogger("test"));
        db.init();
        try {
            EloRepo repo = new EloRepo(db, Runnable::run, () -> cfg, Logger.getLogger("test"));

            EloRow migrated = repo.loadOnLogin(legacyUuid, "OldHero").join();
            assertEquals(1800, migrated.elo());
            assertEquals(20, migrated.kills());
            assertEquals(5, migrated.deaths());

            UUID newUuid = UUID.randomUUID();
            EloRow fresh = repo.loadOnLogin(newUuid, "FreshGuy").join();
            assertEquals(1000, fresh.elo());
            fresh.addElo(350, cfg.eloMin(), cfg.eloMax());

            repo.saveAndUnload(newUuid).join();
            assertNull(repo.getCached(newUuid));

            EloRow offline = repo.findProfile("freshguy").join();
            assertNotNull(offline);
            assertEquals(1350, offline.elo());

            repo.refreshTop();
            List<TopRow> top = repo.topCache();
            assertEquals(2, top.size());
            assertEquals("OldHero", top.get(0).name());
            assertEquals("FreshGuy", top.get(1).name());
        } finally {
            db.close();
        }
    }
}
