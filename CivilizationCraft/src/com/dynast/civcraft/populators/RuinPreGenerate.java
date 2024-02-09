package com.dynast.civcraft.populators;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigRuin;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RuinPreGenerate {

    private int chunks_min;
    private int chunks_max;
    private int chunks_x;
    private int chunks_z;
    private String worldName;
    private int seed;

    public Map<ChunkCoord, RuinPick> ruinPicks = new HashMap<>();

    public RuinPreGenerate() { }

    public boolean validBiome(ConfigRuin ruin, Biome biome) {
        String[] split = ruin.toBiome.split(",");
        Biome b;

        for (String s : split) {
            b = Biome.valueOf(s);
            if (b == biome) {
                return true;
            }
        }
        return false;
    }

    public void preGenerate() {
        try {
            chunks_min = CivSettings.getInteger(CivSettings.ruinCfg, "min_distance");
            chunks_max = CivSettings.getInteger(CivSettings.ruinCfg, "max_distance");
            chunks_x = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_x");
            chunks_z = CivSettings.getInteger(CivSettings.goodsConfig, "generation.chunks_z");
            seed = CivSettings.getInteger(CivSettings.ruinCfg, "seed");
            this.worldName = Bukkit.getWorlds().get(0).getName();

        } catch (InvalidConfiguration e) {
            e.printStackTrace();
        }

        Random rand = new Random();
        rand.setSeed(seed);
        CivLog.info("Generating Ruin Locations.");
        for (int x = -chunks_x; x < chunks_x; x += chunks_min ) {
            for (int z = -chunks_z; z < chunks_z; z += chunks_min) {
                int diff = chunks_max - chunks_min;
                int randX = x;
                int randZ = z;

                if (diff > 0) {
                    if (rand.nextBoolean()) {
                        randX += rand.nextInt(diff);
                    } else {
                        randX -= rand.nextInt(diff);
                    }

                    if (rand.nextBoolean()) {
                        randZ += rand.nextInt(diff);
                    } else {
                        randZ -= rand.nextInt(diff);
                    }
                }


                ChunkCoord cCoord = new ChunkCoord(worldName, randX, randZ);
                pickFromCoord(cCoord);
            }
        }

        CivLog.info("Done.");
    }

    private void pickFromCoord(ChunkCoord cCoord) {
        RuinPick pick = new RuinPick();
        Map<String, ConfigRuin> ruins = CivSettings.ruins;

        int centerX = (cCoord.getX() << 4) + 8;
        int centerZ = (cCoord.getZ() << 4) + 8;

        pick.chunkCoord = cCoord;
        for (String id : ruins.keySet()) {
            ConfigRuin ruin = CivSettings.ruins.get(id);
            if (validBiome(ruin, cCoord.getChunk().getWorld().getBiome(centerX, centerZ))) {
                switch (ruin.id) {
                    case "desert_ruin":
                        pick.desertRuin = ruin;
                        break;
                    case "land_ruin":
                        pick.landRuin = ruin;
                        break;
                    case "jungle_ruin":
                        pick.jungleRuin = ruin;
                        break;
                    default:
                        break;
                }
            }
        }

        this.ruinPicks.put(cCoord, pick);
    }
}
