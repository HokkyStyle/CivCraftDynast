package com.dynast.civcraft.config;

import com.dynast.civcraft.main.CivLog;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigJobLevels {
    public int level;
    public int amount;
    public String type;
    public ArrayList<String> kitItems = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void loadConfig(FileConfiguration cfg,
                                  Map<Integer, ConfigJobLevels> minerLevels,
                                  Map<Integer, ConfigJobLevels> woodcutterLevels,
                                  Map<Integer, ConfigJobLevels> diggerLevels,
                                  Map<Integer, ConfigJobLevels> fishermanLevels,
                                  Map<Integer, ConfigJobLevels> farmerLevels,
                                  Map<Integer, ConfigJobLevels> hunterLevels) {
        minerLevels.clear();
        woodcutterLevels.clear();
        diggerLevels.clear();
        fishermanLevels.clear();
        farmerLevels.clear();
        hunterLevels.clear();

        List<Map<?, ?>> job_miner = cfg.getMapList("job_miner");
        for (Map<?, ?> type : job_miner) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            minerLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+minerLevels.size()+" job miner levels.");

        List<Map<?, ?>> job_woodcutter = cfg.getMapList("job_woodcutter");
        for (Map<?, ?> type : job_woodcutter) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            woodcutterLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+woodcutterLevels.size()+" job woodcutter levels.");

        List<Map<?, ?>> job_digger = cfg.getMapList("job_digger");
        for (Map<?, ?> type : job_digger) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            diggerLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+diggerLevels.size()+" job digger levels.");

        List<Map<?, ?>> job_fisherman = cfg.getMapList("job_fisherman");
        for (Map<?, ?> type : job_fisherman) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            fishermanLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+fishermanLevels.size()+" job fisherman levels.");

        List<Map<?, ?>> job_farmer = cfg.getMapList("job_farmer");
        for (Map<?, ?> type : job_farmer) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            farmerLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+farmerLevels.size()+" job farmer levels.");

        List<Map<?, ?>> job_hunter = cfg.getMapList("job_hunter");
        for (Map<?, ?> type : job_hunter) {
            ConfigJobLevels job = new ConfigJobLevels();
            job.level = (Integer) type.get("level");
            job.amount = (Integer) type.get("amount");
            job.kitItems = (ArrayList<String>) type.get("kit");
            job.type = (String) type.get("type");
            hunterLevels.put(job.level, job);
        }
        CivLog.info("Loaded "+hunterLevels.size()+" job hunter levels.");
    }
}