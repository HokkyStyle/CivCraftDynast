package com.dynast.civcraft.tasks;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.TradeGood;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ItemManager;

public class TradeGoodSignCleanupTask implements Runnable {

	String playerName;
	int xoff = 0;
	int zoff = 0;
	
	public TradeGoodSignCleanupTask(String playername, int xoff, int zoff) {
		this.playerName = playername;
		this.xoff = xoff;
		this.zoff = zoff;
	}
	
	@Override
	public void run() {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			e.printStackTrace();
			return;
		}
		
		int count = 0;
		int i = 0;
		//BlockCoord bcoord2 = new BlockCoord();
		World world = Bukkit.getWorld("world");

		
		
		
	//	for(ChunkCoord coord : CivGlobal.preGenerator.goodPicks.keySet()) {
		for (TradeGood tg : CivGlobal.getTradeGoods()) { 
			BlockCoord bcoord2 = tg.getCoord();			
			bcoord2.setX(bcoord2.getX() + xoff);
			bcoord2.setZ(bcoord2.getZ() + zoff);
			bcoord2.setY(0);
			
		//	Chunk chunk = world.getChunkAt(coord.getX(), coord.getZ());
		//	int centerX = (chunk.getX() << 4) + 8;
		//	int centerZ = (chunk.getZ() << 4) + 8;
		//	int centerY = world.getHighestBlockYAt(centerX, centerZ);
			
		//	bcoord2.setWorldname("world");
		//	bcoord2.setX(centerX);
		//	bcoord2.setY(centerY);
		//	bcoord2.setZ(centerZ);
			
			while(bcoord2.getY() < 256) {
				Block top = world.getBlockAt(bcoord2.getX(), bcoord2.getY(), bcoord2.getZ());
				ItemManager.setTypeId(top, CivData.AIR);
	    			ItemManager.setData(top, 0, true);
	    			bcoord2.setY(bcoord2.getY() + 1);
	    			
	    			top = top.getRelative(BlockFace.NORTH);
	    			if (ItemManager.getId(top) == CivData.WALL_SIGN || ItemManager.getId(top) == CivData.SIGN) {
	    				count++;
	    			}
	    			ItemManager.setTypeId(top, CivData.AIR);
		    		ItemManager.setData(top, 0, true);

	    			top = top.getRelative(BlockFace.SOUTH);
	    			if (ItemManager.getId(top) == CivData.WALL_SIGN || ItemManager.getId(top) == CivData.SIGN) {
	    				count++;
	    				ItemManager.setTypeId(top, CivData.AIR);
			    		ItemManager.setData(top, 0, true);
	    			}
	    			
	    		
		    		top = top.getRelative(BlockFace.EAST);
	    			if (ItemManager.getId(top) == CivData.WALL_SIGN || ItemManager.getId(top) == CivData.SIGN) {
	    				count++;
	    				ItemManager.setTypeId(top, CivData.AIR);
			    		ItemManager.setData(top, 0, true);

	    			}
	    			
	    		
		    		top = top.getRelative(BlockFace.WEST);
	    			if (ItemManager.getId(top) == CivData.WALL_SIGN || ItemManager.getId(top) == CivData.SIGN) {
	    				count++;
	    				ItemManager.setTypeId(top, CivData.AIR);
			    		ItemManager.setData(top, 0, true);
	    			}
			}
			
			i++;
			if ((i % 80) == 0) {
				CivMessage.send(player, "Goodie:"+i+" cleared "+count+" signs...");
			//	TaskMaster.syncTask(new TradeGoodPostGenTask(playerName, (i)));
			//	return;
			}
			
		}
		
		CivMessage.send(player, CivSettings.localize.localizedString("Finished"));
	}

}
