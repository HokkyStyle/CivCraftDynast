package com.dynast.civcraft.threading.tasks;


import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.SimpleBlock;


public class BuildPreviewAsyncTask extends CivAsyncTask {
	/*
	 * This task slow-builds a struct block-by-block based on the 
	 * town's hammer rate. This task is per-structure building and will
	 * use the CivAsynTask interface to send synchronous requests to the main
	 * thread to build individual blocks.
	 */
	
	public Template tpl;
	public Block centerBlock;
	public UUID playerUUID;
	public Boolean aborted = false;
	public ReentrantLock lock = new ReentrantLock();
	private int blocksPerTick;
	private int speed;
	private Resident resident;
		
	public BuildPreviewAsyncTask(Template t, Block center, UUID playerUUID) {
		tpl = t;
		centerBlock = center;
		this.playerUUID = playerUUID;
		resident = CivGlobal.getResidentViaUUID(playerUUID);
		//this.blocksPerTick = getBlocksPerTick();
		//this.speed = getBuildSpeed();
		this.blocksPerTick = 100;
		this.speed = 600;
	}
	
	public Player getPlayer() throws CivException {
		Player player = Bukkit.getPlayer(playerUUID);
		if (player == null) {
			throw new CivException("Player offline");
		}
		return player;
	}
		
	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		try {
			int count = 0;
			
			for (int y = 0; y < tpl.size_y; y++) {
				for (int x = 0; x < tpl.size_x; x++) {
					for (int z = 0; z < tpl.size_z; z++) {
						Block b = centerBlock.getRelative(x, y, z);
						
						if (tpl.blocks[x][y][z].isAir()) {
							continue;
						}
						
						lock.lock();
						try {
							if (aborted) {
								return;
							}
							//ItemManager.getId(CivSettings.previewMaterial), 5
							ItemManager.sendBlockChange(getPlayer(), b.getLocation(), ItemManager.getId(CivSettings.previewMaterial), 5);
							resident.previewUndo.put(new BlockCoord(b.getLocation()),
									new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
							count++;			
						} finally {
							lock.unlock();
						}
						
						
						if (count < blocksPerTick) {
							continue;
						}
						
						count = 0;
						int timeleft = speed;
						while (timeleft > 0) {
							int min = Math.min(10000, timeleft);
							Thread.sleep(min);
							timeleft -= 10000;
						}
					}
				}
			}
		} catch (CivException | InterruptedException e) {
			//abort task.
		}
	}
	


}