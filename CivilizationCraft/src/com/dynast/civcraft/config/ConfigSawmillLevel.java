package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigSawmillLevel {
	public int level;	/* Current level number */
	public int amount; /* Number of redstone this mine consumes */
	public int count; /* Number of times that consumes must be met to level up */
	public double hammers; /* hammers generated each time hour */
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigSawmillLevel> levels) {
		levels.clear();
		List<Map<?, ?>> sawmill_levels = cfg.getMapList("sawmill_levels");
		for (Map<?, ?> level : sawmill_levels) {
			ConfigSawmillLevel sawmill_level = new ConfigSawmillLevel();
			sawmill_level.level = (Integer)level.get("level");
			sawmill_level.amount = (Integer)level.get("amount");
			sawmill_level.hammers = (Double)level.get("hammers");
			sawmill_level.count = (Integer)level.get("count"); 
			levels.put(sawmill_level.level, sawmill_level);
		}
		CivLog.info("Loaded "+levels.size()+" sawmill levels.");
	}
}
