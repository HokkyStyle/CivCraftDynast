package com.dynast.civcraft.config;

import com.dynast.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Map;

public class ConfigRuin {
    public String id;
    public String toBiome;// must be a "land", "desert" or "jungle" //TODO MORE BIOMES
    public List<String> toDrop;

    @SuppressWarnings("unchecked")
    public static void loadConfig(FileConfiguration cfg, Map<String, ConfigRuin> ruins) {
        ruins.clear();
        List<Map<?, ?>> ruins1 = cfg.getMapList("ruins");
        for (Map<?, ?> r : ruins1) {
            ConfigRuin ruin = new ConfigRuin();
            ruin.id = (String)r.get("id");
            ruin.toBiome = (String)r.get("toBiome");
            ruin.toDrop = (List<String>)r.get("toDrop");

            ruins.put(ruin.id, ruin);
        }

        CivLog.info("Loaded "+ruins.size()+" Ruin Types.");
    }
}
