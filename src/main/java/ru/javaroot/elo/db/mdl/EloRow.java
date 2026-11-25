package ru.javaroot.elo.db.mdl;

import java.util.Locale;
import java.util.UUID;

public final class EloRow {
    private final UUID uuid;
    private String name;
    private int elo;
    private int kills;
    private int deaths;
    private boolean dirty;
    private boolean inDb;

    public EloRow(UUID uuid, String name, int elo, int kills, int deaths, boolean dirty, boolean inDb) {
        this.uuid = uuid;
        this.name = name;
        this.elo = elo;
        this.kills = kills;
        this.deaths = deaths;
        this.dirty = dirty;
        this.inDb = inDb;
    }

    public UUID uuid() {
        return uuid;
    }

    public synchronized String name() {
        return name;
    }

    public synchronized int elo() {
        return elo;
    }

    public synchronized int kills() {
        return kills;
    }

    public synchronized int deaths() {
        return deaths;
    }

    public synchronized boolean dirty() {
        return dirty;
    }

    public synchronized boolean inDb() {
        return inDb;
    }

    public synchronized void updateName(String newName) {
        if (newName != null && !newName.equals(name)) {
            name = newName;
            dirty = true;
        }
    }

    public synchronized void recordWin(int delta, int min, int max) {
        elo = clamp(elo + delta, min, max);
        kills++;
        dirty = true;
    }

    public synchronized void recordLoss(int delta, int min, int max) {
        elo = clamp(elo - delta, min, max);
        deaths++;
        dirty = true;
    }

    public synchronized void addElo(int delta, int min, int max) {
        elo = clamp(elo + delta, min, max);
        dirty = true;
    }

    public synchronized void setElo(int value, int min, int max) {
        elo = clamp(value, min, max);
        dirty = true;
    }

    public synchronized Snap beginSave() {
        if (!dirty && inDb) return null;
        dirty = false;
        return new Snap(uuid, name, elo, kills, deaths);
    }

    public synchronized void markSaved() {
        inDb = true;
    }

    public synchronized void markSaveFailed() {
        dirty = true;
    }

    public synchronized TopRow toTopRow() {
        return new TopRow(uuid, name, elo, kills, deaths);
    }

    public synchronized String kd() {
        if (deaths == 0) return String.valueOf(kills);
        return String.format(Locale.US, "%.1f", (double) kills / deaths);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public record Snap(UUID uuid, String name, int elo, int kills, int deaths) {
    }
}
