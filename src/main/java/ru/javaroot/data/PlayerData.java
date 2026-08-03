package ru.javaroot.data;

public class PlayerData {
    private String name;
    private int elo;
    private int kills;
    private int deaths;
    private final boolean inDb;

    public PlayerData(String name, int elo, int kills, int deaths, boolean inDb) {
        this.name = name;
        this.elo = elo;
        this.kills = kills;
        this.deaths = deaths;
        this.inDb = inDb;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public boolean isInDb() { return inDb; }

    public void addElo(int amount) {
        this.elo += amount;
        if (this.elo < 0) this.elo = 0;
    }

    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }

    public String getKd() {
        if (deaths == 0) return String.valueOf(kills);
        return String.format(java.util.Locale.US, "%.1f", (double) kills / deaths);
    }
}
