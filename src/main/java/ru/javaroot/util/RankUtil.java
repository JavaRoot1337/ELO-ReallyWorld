package ru.javaroot.util;

import ru.javaroot.core.CfgMgr;

import java.util.Map;
import java.util.TreeMap;

public class RankUtil {
    private final CfgMgr cfg;

    public RankUtil(CfgMgr cfg) {
        this.cfg = cfg;
    }

    public String getRank(int elo) {
        Map.Entry<Integer, String> entry = cfg.getRanks().floorEntry(elo);
        if (entry != null) {
            return entry.getValue();
        }
        return cfg.getDefaultRank();
    }
}
