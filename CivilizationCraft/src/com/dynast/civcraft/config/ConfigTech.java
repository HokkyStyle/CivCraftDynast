
package com.dynast.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;

public class ConfigTech {
	public String id;
	public String name;
	public double beaker_cost;
	public double cost;
	public String require_techs;
	public int era;
	public Integer points;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigTech> tech_maps) {
		tech_maps.clear();
		List<Map<?, ?>> techs = cfg.getMapList("techs");
		for (Map<?, ?> confTech : techs) {
			ConfigTech tech = new ConfigTech();
			
			tech.id = (String)confTech.get("id");
			tech.name = (String)confTech.get("name");
			tech.beaker_cost = (Double)confTech.get("beaker_cost");
			tech.cost = (Double)confTech.get("cost");
			tech.era = (Integer)confTech.get("era");
			tech.require_techs = (String)confTech.get("require_techs");
			tech.points = (Integer)confTech.get("points");
			
			tech_maps.put(tech.id, tech);
		}
		CivLog.info("Loaded "+tech_maps.size()+" technologies.");
	}
	
	public static double eraRate(Civilization civ) {
		double rate = 0.0;
		double era = (CivGlobal.highestCivEra-1) - civ.getCurrentEra();
		if (era > 0) {
			rate = (era/10);
		}
		return rate;
	}
	
	public double getAdjustedBeakerCost(Civilization civ) {
		double rate = 1.0;
		rate -= eraRate(civ);
		return Math.floor(this.beaker_cost*Math.max(rate, .01));
	}
	
	public double getAdjustedTechCost(Civilization civ) {
		double rate = 1.0;
		
		for (Town town : civ.getTowns())
		{
			if (town.getBuffManager().hasBuff("buff_profit_sharing"))
			{
				rate -= town.getBuffManager().getEffectiveDouble("buff_profit_sharing");
			}
		}
		
		if (civ.hasInstitution("rational_5")) {
			rate -= 0.1;
		}
		rate = Math.max(rate, 0.8);
		rate -= eraRate(civ);
		
		return Math.floor(this.cost * Math.max(rate, .01));
	}
	
	
	public static ArrayList<ConfigTech> getAvailableTechs(Civilization civ) {
		ArrayList<ConfigTech> returnTechs = new ArrayList<>();
		
		for (ConfigTech tech : CivSettings.techs.values()) {
			if (!civ.hasTechnology(tech.id)) {
				if (tech.isAvailable(civ)) {
					returnTechs.add(tech);
				}
				
				
				/*if (tech.require_techs == null || tech.require_techs.equals("")) {
					returnTechs.add(tech);
				} else {
					String[] requireTechs = tech.require_techs.split(":");
					// Search for the prereq techs.
					boolean hasRequirements = true;
					for (String reqTech : requireTechs) {
						if (!civ.hasTech(reqTech)) {
							hasRequirements = false;
							break;
						}
					}
					if (hasRequirements) {
						// If we're here, then we have all the required techs.
						returnTechs.add(tech);
					}
				}*/
			}
		}
		return returnTechs;
	}
	
	public boolean isAvailable(Civilization civ) {
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
		
		if (require_techs == null || require_techs.equals("")) {
			return true;			
		}
		
		String[] requireTechs = require_techs.split(":");
		
		for (String reqTech : requireTechs) {
			if (!civ.hasTechnology(reqTech)) {
				return false;
			}
		}
		return true;
	}
	
}
