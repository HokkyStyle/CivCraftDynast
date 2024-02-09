
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigStableItem {
	public String name;
	public double cost;
	public int store_id;
	public int item_id;
	public int horse_id;
	
	public static void loadConfig(FileConfiguration cfg, Set<ConfigStableItem> items) {
		items.clear();
		List<Map<?, ?>> cfg_items = cfg.getMapList("stable_items");
		for (Map<?, ?> level : cfg_items) {
			ConfigStableItem itm = new ConfigStableItem();
			
			itm.name = (String)level.get("name");
			itm.cost = (Double)level.get("cost");
			itm.store_id = (Integer)level.get("store_id");
			itm.item_id = (Integer)level.get("item_id");
			itm.horse_id = (Integer)level.get("horse_id");
			
			items.add(itm);
		}
		CivLog.info("Loaded "+items.size()+" stable items.");
	}
	
}
