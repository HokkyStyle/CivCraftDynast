
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigCultureLevel {
	public int level;
	public int amount;
	public int chunks;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigCultureLevel> levels) {
		levels.clear();
		List<Map<?, ?>> culture_levels = cfg.getMapList("culture_levels");
		for (Map<?, ?> level : culture_levels) {
			ConfigCultureLevel culture_level = new ConfigCultureLevel();
			culture_level.level = (Integer)level.get("level");
			culture_level.amount = (Integer)level.get("amount");
			culture_level.chunks = (Integer)level.get("chunks");
			levels.put(culture_level.level, culture_level);
		}
		CivLog.info("Loaded "+levels.size()+" culture levels.");
	}
}
