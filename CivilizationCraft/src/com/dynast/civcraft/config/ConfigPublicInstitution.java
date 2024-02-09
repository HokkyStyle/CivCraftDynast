package com.dynast.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;

public class ConfigPublicInstitution {
	public String id;
	public String displayName;
	public String displayInfo;
	public String requireid;
	public double value;
	public double value2;
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigPublicInstitution> publicinst_map) {
		publicinst_map.clear();
		List<Map<?, ?>> insts = cfg.getMapList("publicinsts");
		for (Map<?, ?> level : insts) {
			ConfigPublicInstitution inst = new ConfigPublicInstitution();
			
			inst.id = (String)level.get("id");
			inst.displayName = (String)level.get("displayName");
			inst.requireid = (String)level.get("requireid");
            inst.displayInfo = (String)level.get("displayInfo");
            inst.value = Double.valueOf((String)level.get("value"));

			if (level.get("value2") != null) {
				inst.value2 = Double.valueOf((String)level.get("value2"));
			}

			publicinst_map.put(inst.id, inst);
		}
		CivLog.info("Loaded "+publicinst_map.size()+" public intitutions.");		
	}
	
	public static ArrayList<ConfigPublicInstitution> getAvailableInstitutes(Civilization civ) {
		ArrayList<ConfigPublicInstitution> insts = new ArrayList<>();
		
		for (ConfigPublicInstitution inst : CivSettings.publicinsts.values()) {
			if (!civ.hasInstitution(inst.id)) {
			    if (inst.isAvailableInst(civ)) {
				    insts.add(inst);
			    }
			}
		}
		return insts;
	}
	
    public static ConfigPublicInstitution getPublicInstitutionFromName(String string) {
		
		for (ConfigPublicInstitution inst : CivSettings.publicinsts.values()) {
			if (inst.displayName.equalsIgnoreCase(string)) {
				return inst;
			}
		}		
		return null;
	}
    
    public boolean isAvailableInst(Civilization civ) {
    	if (requireid == null || requireid.equals("")) {
			return true;
		}
		if (CivGlobal.testFileFlag("debug-norequire")) {
			CivMessage.global("Ignoring requirements! debug-norequire found.");
			return true;
		}
	
		String[] requireInsts = requireid.split(":");
	
		for (String reqInst : requireInsts) {
			if (!civ.hasInstitution(reqInst)) {
				return false;
			}
		}
	    return true;
    }

}
