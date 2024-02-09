package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigPastureLevel {
	public int level;
	public String consumes;
	public int amount;
	public int cows = 0;
	public int sheeps = 0;
	public int chickens = 0;
	public int pigs = 0;

	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigPastureLevel> pasture_levels) {
		pasture_levels.clear();
		List<Map<?, ?>> pasture_list = cfg.getMapList("pasture.levels");
		Map<Integer, Integer> consumes_list = null;
		for (Map<?,?> cl : pasture_list ) {
			ConfigPastureLevel pasturelevel = new ConfigPastureLevel();
			pasturelevel.level = (Integer)cl.get("level");
			pasturelevel.consumes = (String)cl.get("consume");
			pasturelevel.amount = (Integer)cl.get("amount");

			pasturelevel.cows = (Integer)cl.get("cow");
			pasturelevel.sheeps = (Integer)cl.get("sheep");
			pasturelevel.pigs = (Integer)cl.get("pig");
			pasturelevel.chickens = (Integer)cl.get("chicken");
			
			pasture_levels.put(pasturelevel.level, pasturelevel);
			
		}
		CivLog.info("Loaded "+pasture_levels.size()+" pasture levels");		
	}
	
}
