
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigGrocerLevel {
	public int level;
	public String itemName;
	public int itemId;
	public int itemData;
	public int amount;
	public double price;
	
	public static void loadConfig(FileConfiguration cfg, Map<Integer, ConfigGrocerLevel> levels) {
		levels.clear();
		List<Map<?, ?>> culture_levels = cfg.getMapList("grocer_levels");
		for (Map<?, ?> level : culture_levels) {
			ConfigGrocerLevel grocer_level = new ConfigGrocerLevel();
			grocer_level.level = (Integer)level.get("level");
			grocer_level.itemName = (String)level.get("itemName");
			grocer_level.itemId = (Integer)level.get("itemId");
			grocer_level.itemData = (Integer)level.get("itemData");
			grocer_level.amount = (Integer)level.get("amount");
			grocer_level.price = (Double)level.get("price");
			
			levels.put(grocer_level.level, grocer_level);
		}
		CivLog.info("Loaded "+levels.size()+" grocer levels.");
	}
	
}
