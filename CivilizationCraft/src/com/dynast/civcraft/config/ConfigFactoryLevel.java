package com.dynast.civcraft.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigFactoryLevel {
	public int level;			/* Current level number */
	public Map<Integer, Integer> consumes; /* A map of block ID's and amounts required for this level to progress */
	public int count; /* Number of times that consumes must be met to level up */
	public double hammers; /* Coins generated each time for the cottage */
	
	public ConfigFactoryLevel() {
		
	}
	
	public ConfigFactoryLevel(ConfigFactoryLevel currentlvl) {
		this.level = currentlvl.level;
		this.count = currentlvl.count;
		this.hammers = currentlvl.hammers;
		
		this.consumes = new HashMap<>();
		for (Entry<Integer, Integer> entry : currentlvl.consumes.entrySet()) {
			this.consumes.put(entry.getKey(), entry.getValue());
		}
		
	}


	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigFactoryLevel> factory_levels) {
		factory_levels.clear();
		List<Map<?, ?>> factory_list = cfg.getMapList("factory_levels");
		Map<Integer, Integer> consumes_list = null;
		for (Map<?,?> cl : factory_list ) {
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
			
			
			ConfigFactoryLevel factorylevel = new ConfigFactoryLevel();
			factorylevel.level = (Integer)cl.get("level");
			factorylevel.consumes = consumes_list;
			factorylevel.count = (Integer)cl.get("count");
			factorylevel.hammers = (Double)cl.get("hammers");
			
			factory_levels.put(factorylevel.level, factorylevel);
			
		}
		CivLog.info("Loaded "+factory_levels.size()+" factory levels");		
	}
	
}
