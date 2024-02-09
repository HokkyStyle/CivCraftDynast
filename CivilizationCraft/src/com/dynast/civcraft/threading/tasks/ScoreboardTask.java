package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.object.Civilization;
import org.bukkit.scoreboard.Score;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScoreboardTask implements Runnable {
    public static HashMap<Civilization, HashMap<Score, Integer>> timer = new HashMap<>();
    private Set<Score> toDel = new HashSet<>();

    @Override
    public void run() {
        for (HashMap<Score, Integer> hm : timer.values()) {
            for (Map.Entry<Score, Integer> e : hm.entrySet()) {
                e.setValue(e.getValue() - 1);
                if (e.getValue() <= 0) {
                    toDel.add(e.getKey());
                }
            }
        }
        for (Civilization civ : timer.keySet()) {
            for (Score sc : toDel) {
                civ.scoreboard.resetScores(sc.getEntry());
            }
        }
        toDel.clear();
    }
}
