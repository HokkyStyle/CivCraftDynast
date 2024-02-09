
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigEnchant {

	public String id;
	public String name;
	public String description;
	public double cost;
	public String enchant_id;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigEnchant> enchant_map) {
		enchant_map.clear();
		List<Map<?, ?>> techs = cfg.getMapList("enchants");
		for (Map<?, ?> level : techs) {
			ConfigEnchant enchant = new ConfigEnchant();
			
			enchant.id = (String)level.get("id");
			enchant.name = (String)level.get("name");
			enchant.description = (String)level.get("description");
			enchant.cost = (Double)level.get("cost");
			enchant.enchant_id = (String)level.get("enchant_id");			
			enchant_map.put(enchant.id, enchant);
		}
		CivLog.info("Loaded "+enchant_map.size()+" enchantments.");		
	}

	
}
