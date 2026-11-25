package ru.javaroot.elo.db;

import ru.javaroot.elo.cfg.Cfg;
import ru.javaroot.elo.db.mdl.EloRow;
import ru.javaroot.elo.db.mdl.TopRow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EloRepo {
    private static final int PAPI_TOP_MIN = 20;

    private final Db db;
    private final Executor async;
    private final Supplier<Cfg> cfgGet;
    private final Logger log;

    private final ConcurrentHashMap<UUID, EloRow> cache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UUID> nameIdx = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<Void>> pendingSaves = new ConcurrentHashMap<>();
    private final AtomicReference<List<TopRow>> topCache = new AtomicReference<>(List.of());

    public EloRepo(Db db, Executor async, Supplier<Cfg> cfgGet, Logger log) {
        this.db = db;
        this.async = async;
        this.cfgGet = cfgGet;
        this.log = log;
    }

    public CompletableFuture<EloRow> loadOnLogin(UUID uuid, String name) {
        CompletableFuture<Void> prevSave = pendingSaves.get(uuid);
        CompletableFuture<Void> gate = prevSave != null ? prevSave.exceptionally(ex -> null) : CompletableFuture.completedFuture(null);

        return gate.thenApplyAsync(ignored -> {
            EloRow row = null;
            try {
                row = db.findByUuid(uuid);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed to load ELO profile for " + uuid, ex);
            }
            if (row == null) {
                row = new EloRow(uuid, name, cfgGet.get().eloDefault(), 0, 0, true, false);
            } else {
                row.updateName(name);
            }
            cache.put(uuid, row);
            if (name != null) {
                nameIdx.put(name.toLowerCase(Locale.ROOT), uuid);
            }
            return row;
        }, async);
    }

    public EloRow getCached(UUID uuid) {
        return cache.get(uuid);
    }

    public EloRow getCachedByName(String name) {
        if (name == null) return null;
        UUID uuid = nameIdx.get(name.toLowerCase(Locale.ROOT));
        return uuid != null ? cache.get(uuid) : null;
    }

    public CompletableFuture<EloRow> findProfile(String name) {
        EloRow cached = getCachedByName(name);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return db.findByName(name);
            } catch (SQLException ex) {
                log.log(Level.WARNING, "Failed to lookup ELO profile for " + name, ex);
                return null;
            }
        }, async);
    }

    public CompletableFuture<Void> saveRowAsync(EloRow row) {
        if (row == null) return CompletableFuture.completedFuture(null);
        EloRow.Snap snap = row.beginSave();
        if (snap == null) return CompletableFuture.completedFuture(null);

        CompletableFuture<Void> fut = CompletableFuture.runAsync(() -> persistSnap(row, snap), async);
        trackPending(row.uuid(), fut);
        return fut;
    }

    public CompletableFuture<Void> saveAndUnload(UUID uuid) {
        EloRow row = cache.remove(uuid);
        if (row == null) return CompletableFuture.completedFuture(null);

        String name = row.name();
        if (name != null) {
            nameIdx.remove(name.toLowerCase(Locale.ROOT), uuid);
        }

        EloRow.Snap snap = row.beginSave();
        if (snap == null) return CompletableFuture.completedFuture(null);

        CompletableFuture<Void> fut = CompletableFuture.runAsync(() -> persistSnap(row, snap), async);
        trackPending(uuid, fut);
        return fut;
    }

    public void flushDirtyAsync() {
        for (EloRow row : cache.values()) {
            if (row.dirty() || !row.inDb()) {
                saveRowAsync(row);
            }
        }
    }

    public void flushAllSync() {
        List<CompletableFuture<Void>> inflight = new ArrayList<>(pendingSaves.values());
        if (!inflight.isEmpty()) {
            try {
                CompletableFuture.allOf(inflight.toArray(new CompletableFuture[0]))
                        .get(5, TimeUnit.SECONDS);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Timed out waiting for pending saves during shutdown", ex);
            }
        }

        for (EloRow row : cache.values()) {
            EloRow.Snap snap = row.beginSave();
            if (snap != null) {
                persistSnap(row, snap);
            }
        }
    }

    public void refreshTop() {
        int limit = Math.max(PAPI_TOP_MIN, cfgGet.get().topLimit());
        try {
            List<TopRow> rows = db.fetchTop(limit);
            topCache.set(rows);
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to refresh ELO leaderboard", ex);
        }
    }

    public List<TopRow> topCache() {
        return topCache.get();
    }

    private void persistSnap(EloRow row, EloRow.Snap snap) {
        try {
            db.upsert(snap, System.currentTimeMillis());
            row.markSaved();
        } catch (SQLException ex) {
            row.markSaveFailed();
            log.log(Level.SEVERE, "Failed to save ELO profile " + snap.uuid(), ex);
        }
    }

    private void trackPending(UUID uuid, CompletableFuture<Void> fut) {
        CompletableFuture<Void> tracked = pendingSaves.compute(uuid, (k, existing) ->
                existing == null ? fut : CompletableFuture.allOf(existing, fut));
        tracked.whenComplete((v, ex) -> pendingSaves.remove(uuid, tracked));
    }
}
