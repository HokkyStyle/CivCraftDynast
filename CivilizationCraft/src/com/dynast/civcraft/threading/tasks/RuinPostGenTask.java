package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.config.ConfigRuin;
import com.dynast.civcraft.config.ConfigTradeGood;
import com.dynast.civcraft.database.SQL;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Ruin;
import com.dynast.civcraft.populators.RuinPick;
import com.dynast.civcraft.populators.RuinPopulator;
import com.dynast.civcraft.populators.TradeGoodPick;
import com.dynast.civcraft.populators.TradeGoodPopulator;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class RuinPostGenTask implements Runnable {
    String playerName;
    int start;

    public RuinPostGenTask(String playerName, int start) {
        this.playerName = playerName;
        this.start = 0;
    }

    public void deleteAllRuinsFromDB() {
		/* Delete all existing trade goods from DB. */
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            try {
                conn = SQL.getGameConnection();
                String code = "TRUNCATE TABLE "+ Ruin.TABLE_NAME;
                ps = conn.prepareStatement(code);
                ps.execute();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void run() {
        CivLog.info("Generating/Clearing Ruins...");
        CivLog.info("|- Organizing ruin picks into a Queue.");

        deleteAllRuinsFromDB();

		/* Generate Trade Good Pillars. */
        Queue<RuinPick> picksQueue = new LinkedList<>();
        for (RuinPick pick : CivGlobal.ruinPreGenerator.ruinPicks.values()) {
            picksQueue.add(pick);
        }

        int count = 0;
        int amount = 20;
        int totalSize = picksQueue.size();
        while (picksQueue.peek() != null) {
            CivLog.info("|- Placing/Picking Ruins:"+count+"/"+totalSize+" current size:"+picksQueue.size());

            Queue<RuinPick> processQueue = new LinkedList<>();
            for (int i = 0; i < amount; i++) {
                RuinPick pick = picksQueue.poll();
                if (pick == null) {
                    break;
                }

                count++;
                processQueue.add(pick);
            }

            TaskMaster.syncTask(new RuinPostGenTask.SyncTradeGenTask(processQueue, amount));

            try {
                while (processQueue.peek() != null) {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                return;
            }
        }


        CivLog.info("Finished!");
    }

    class SyncTradeGenTask implements Runnable {
        public Queue<RuinPick> picksQueue;
        public int amount;

        public SyncTradeGenTask(Queue<RuinPick> picksQueue, int amount) {
            this.picksQueue = picksQueue;
            this.amount = amount;
        }

        @Override
        public void run() {
            World world = Bukkit.getWorld("world");

            for(int i = 0; i < amount; i++) {
                RuinPick pick = picksQueue.poll();
                if (pick == null) {
                    return;
                }

                ChunkCoord coord = pick.chunkCoord;
                Chunk chunk = world.getChunkAt(coord.getX(), coord.getZ());

                int centerX = (chunk.getX() << 4);
                int centerZ = (chunk.getZ() << 4);
                int centerY = world.getHighestBlockYAt(centerX, centerZ);

                if (ItemManager.getBlockTypeIdAt(world, centerX, centerY-1, centerZ) == CivData.WATER ||
                        ItemManager.getBlockTypeIdAt(world, centerX, centerY-1, centerZ) == CivData.WATER_RUNNING) {
                    return;
                }

                ConfigRuin ruin = null;
                if (pick.jungleRuin != null) {
                    ruin = pick.jungleRuin;
                } else if (pick.landRuin != null) {
                    ruin = pick.landRuin;
                } else if (pick.desertRuin != null) {
                    ruin = pick.desertRuin;
                }

                // Randomly choose a land or water good.
                if (ruin == null) {
                    System.out.println("Could not find suitable good type during populate! aborting.");
                    continue;
                }

                // Create a copy and save it in the global hash table.
                BlockCoord bcoord = new BlockCoord(world.getName(), centerX, centerY, centerZ);
                RuinPopulator.buildRuin(ruin, bcoord, world, true);

            }
        }
    }
}
