package ru.javaroot.elo.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.javaroot.elo.db.EloRepo;
import ru.javaroot.elo.db.mdl.EloRow;
import ru.javaroot.elo.db.mdl.TopRow;
import ru.javaroot.elo.plat.txt.Txt;
import ru.javaroot.elo.svc.EloSvc;

import java.util.List;
import java.util.Locale;

public final class PapiHook extends PlaceholderExpansion {
    private final Plugin plug;
    private final EloRepo repo;
    private final EloSvc svc;

    public PapiHook(Plugin plug, EloRepo repo, EloSvc svc) {
        this.plug = plug;
        this.repo = repo;
        this.svc = svc;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "elo";
    }

    @Override
    public @NotNull String getAuthor() {
        return "javaroot";
    }

    @Override
    public @NotNull String getVersion() {
        return plug.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, @NotNull String params) {
        String id = params.toLowerCase(Locale.ROOT);
        if (id.startsWith("top_")) {
            return resolveTop(id);
        }
        if (p == null) {
            return "";
        }
        EloRow row = repo.getCached(p.getUniqueId());
        if (row == null) {
            return "";
        }
        return switch (id) {
            case "rating" -> String.valueOf(row.elo());
            case "rank" -> Txt.toLegacy(svc.rankOf(row.elo()));
            case "kills" -> String.valueOf(row.kills());
            case "deaths" -> String.valueOf(row.deaths());
            case "kd" -> row.kd();
            default -> null;
        };
    }

    private String resolveTop(String id) {
        String[] parts = id.split("_");
        if (parts.length != 3) {
            return null;
        }
        int idx;
        try {
            idx = Integer.parseInt(parts[2]) - 1;
        } catch (NumberFormatException ex) {
            return null;
        }
        List<TopRow> top = repo.topCache();
        if (idx < 0 || idx >= top.size()) {
            return "";
        }
        TopRow row = top.get(idx);
        return switch (parts[1]) {
            case "name" -> row.name() != null ? row.name() : "";
            case "rating" -> String.valueOf(row.elo());
            case "rank" -> Txt.toLegacy(svc.rankOf(row.elo()));
            case "kills" -> String.valueOf(row.kills());
            case "deaths" -> String.valueOf(row.deaths());
            case "kd" -> row.kd();
            default -> null;
        };
    }
}
