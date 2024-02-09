
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigTechItem {

	public int id;
	public String name;
	public String require_tech;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigTechItem> tech_maps) {
		tech_maps.clear();
		List<Map<?, ?>> techs = cfg.getMapList("items");
		for (Map<?, ?> confTech : techs) {
			ConfigTechItem tech = new ConfigTechItem();
			
			tech.id = (Integer)confTech.get("id");
			tech.name = (String)confTech.get("name");
			tech.require_tech = (String)confTech.get("require_tech");			
			tech_maps.put(tech.id, tech);
		}
		CivLog.info("Loaded "+tech_maps.size()+" technologies.");		
	}
	
}
