package com.dynast.civcraft.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigUniverLevel {
	public int level;			/* Current level number */
	public Map<Integer, Integer> consumes; /* A map of block ID's and amounts required for this level to progress */
	public int count; /* Number of times that consumes must be met to level up */
	public double beakers; /* Coins generated each time for the cottage */
	
	public ConfigUniverLevel() {
		
	}
	
	public ConfigUniverLevel(ConfigUniverLevel currentlvl) {
		this.level = currentlvl.level;
		this.count = currentlvl.count;
		this.beakers = currentlvl.beakers;
		
		this.consumes = new HashMap<>();
		for (Entry<Integer, Integer> entry : currentlvl.consumes.entrySet()) {
			this.consumes.put(entry.getKey(), entry.getValue());
		}
		
	}


	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigUniverLevel> univer_levels) {
		univer_levels.clear();
		List<Map<?, ?>> univer_list = cfg.getMapList("univer_levels");
		Map<Integer, Integer> consumes_list = null;
		for (Map<?,?> cl : univer_list ) {
			List<?> consumes = (List<?>)cl.get("consumes");
			if (consumes != null) {
				consumes_list = new HashMap<>();
                for (Object consume : consumes) {
                    String line = (String) consume;
                    String split[];
                    split = line.split(",");
                    consumes_list.put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
                }
			}
			
			
			ConfigUniverLevel univerlevel = new ConfigUniverLevel();
			univerlevel.level = (Integer)cl.get("level");
			univerlevel.consumes = consumes_list;
			univerlevel.count = (Integer)cl.get("count");
			univerlevel.beakers = (Double)cl.get("beakers");
			
			univer_levels.put(univerlevel.level, univerlevel);
			
		}
		CivLog.info("Loaded "+univer_levels.size()+" univer levels");		
	}
	
}
