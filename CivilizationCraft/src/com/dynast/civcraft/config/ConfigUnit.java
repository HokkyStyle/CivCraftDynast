
package com.dynast.civcraft.config;

import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;

public class ConfigUnit {
	public String id;
	public String name;
	public String class_name;
	public String require_tech;
	public String require_struct;
	public String require_upgrade;
	public double cost;
	public double hammer_cost;
	public int limit;
	public int item_id;
	public int item_data;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigUnit> units){
		units.clear();
		List<Map<?, ?>> configUnits = cfg.getMapList("units");
		for (Map<?, ?> b : configUnits) {
			
			ConfigUnit unit = new ConfigUnit();
			
			unit.id = (String)b.get("id");
			unit.name = (String)b.get("name");
			unit.class_name = (String)b.get("class_name");
			unit.require_tech = (String)b.get("require_tech");
			unit.require_struct = (String)b.get("require_struct");
			unit.require_upgrade = (String)b.get("require_upgrade");
			unit.cost = (Double)b.get("cost");
			unit.hammer_cost = (Double)b.get("hammer_cost");
			unit.limit = (Integer)b.get("limit");
			unit.item_id = (Integer)b.get("item_id");
			unit.item_data = (Integer)b.get("item_data");
			
			units.put(unit.id, unit);
		}
		
		CivLog.info("Loaded "+units.size()+" Units.");
	}
	
	public boolean isAvailable(Town town) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (town.hasTechnology(require_tech)) {
			if (town.hasUpgrade(require_upgrade)) {
				if (town.hasStructure(require_struct)) {
					if (limit == 0 || town.getUnitTypeCount(id) < limit) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
