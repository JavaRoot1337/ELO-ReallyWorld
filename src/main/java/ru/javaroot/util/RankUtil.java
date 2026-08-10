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
        TreeMap<Integer, String> ranks = cfg.getRanks();
        for (Map.Entry<Integer, String> entry : ranks.entrySet()) {
            if (elo < entry.getKey()) {
                return entry.getValue();
            }
        }
        return cfg.getDefaultRank();
    }
}
