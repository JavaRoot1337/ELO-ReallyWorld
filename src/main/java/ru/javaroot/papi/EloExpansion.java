package ru.javaroot.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import ru.javaroot.core.Elo;
import ru.javaroot.data.PlayerData;

import java.util.List;

public class EloExpansion extends PlaceholderExpansion {
    private final Elo app;

    public EloExpansion(Elo app) {
        this.app = app;
    }

    @Override
    public String getIdentifier() {
        return "elo";
    }

    @Override
    public String getAuthor() {
        return "javaroot";
    }

    @Override
    public String getVersion() {
        return app.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer p, String id) {
        String lowerId = id.toLowerCase();
        
        if (lowerId.startsWith("top_")) {
            String[] parts = lowerId.split("_");
            if (parts.length == 3) {
                String stat = parts[1];
                int index;
                try {
                    index = Integer.parseInt(parts[2]) - 1;
                } catch (NumberFormatException e) {
                    return null;
                }
                
                List<PlayerData> top = app.getRepo().getTopCache();
                if (index < 0 || index >= top.size()) {
                    return "";
                }
                
                PlayerData tpd = top.get(index);
                switch (stat) {
                    case "name": return tpd.getName() != null ? tpd.getName() : "";
                    case "rating": return String.valueOf(tpd.getElo());
                    case "rank": return app.getRankUtil().getRank(tpd.getElo());
                    case "kills": return String.valueOf(tpd.getKills());
                    case "deaths": return String.valueOf(tpd.getDeaths());
                    case "kd": return tpd.getKd();
                }
            }
            return null;
        }

        if (p == null) return "";

        PlayerData pd = app.getRepo().getCached(p.getUniqueId());
        if (pd == null) return "";

        switch (lowerId) {
            case "rating":
                return String.valueOf(pd.getElo());
            case "rank":
                return app.getRankUtil().getRank(pd.getElo());
            case "kills":
                return String.valueOf(pd.getKills());
            case "deaths":
                return String.valueOf(pd.getDeaths());
            case "kd":
                return pd.getKd();
        }

        return null;
    }
}
