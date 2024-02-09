
package com.dynast.civcraft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.dynast.civcraft.main.CivLog;

public class ConfigWonderBuff {

	public String id;
	public ArrayList<ConfigBuff> buffs = new ArrayList<>();
	
	public static void loadConfig(FileConfiguration cfg, Map<String, ConfigWonderBuff> wbuffs){
		wbuffs.clear();
		List<Map<?, ?>> ConfigWonderBuff = cfg.getMapList("wonder_buffs");
		for (Map<?, ?> b : ConfigWonderBuff) {
			ConfigWonderBuff buff = new ConfigWonderBuff();
			buff.id = (String)b.get("id");
			
			List<?> buffStrings = (List<?>)b.get("buffs");
			for (Object obj : buffStrings) {
				if (obj instanceof String) {
					String str = (String)obj;
					
					ConfigBuff cfgBuff = CivSettings.buffs.get(str);
					
					if (cfgBuff != null) {
						buff.buffs.add(cfgBuff);
					} else {
						CivLog.warning("Unknown buff id:"+str);
					}
					
				}
			}
			
			wbuffs.put(buff.id, buff);
		}
		
		CivLog.info("Loaded "+wbuffs.size()+" Wonder Buffs.");
	}	
}
