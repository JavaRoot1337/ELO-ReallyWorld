package ru.javaroot.elo.db.mdl;

import java.util.Locale;
import java.util.UUID;

public record TopRow(UUID uuid, String name, int elo, int kills, int deaths) {
    public String kd() {
        if (deaths == 0) return String.valueOf(kills);
        return String.format(Locale.US, "%.1f", (double) kills / deaths);
    }
}
