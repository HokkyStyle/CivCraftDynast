
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigHemisphere {
	public String id;
	public int x_min;
	public int x_max;
	public int z_min;
	public int z_max;
	
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigHemisphere> hemis){
		hemis.clear();
		List<Map<?, ?>> configHemis = cfg.getMapList("hemispheres");
		for (Map<?, ?> b : configHemis) {
			ConfigHemisphere buff = new ConfigHemisphere();
			buff.id = (String)b.get("id");
			buff.x_min = (Integer)b.get("x_min");
			buff.x_max = (Integer)b.get("x_max");
			buff.z_min = (Integer)b.get("z_min");
			buff.z_max = (Integer)b.get("z_max");
			hemis.put(buff.id, buff);
		}
		
		CivLog.info("Loaded "+hemis.size()+" Hemispheres.");
	}	
	
}
