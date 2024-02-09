
package com.dynast.civcraft.threading.tasks;


import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import com.dynast.civcraft.threading.TaskMaster;

public class ChunkGenerateTask implements Runnable {

	int startX;
	int startZ;
	int stopX;
	int stopZ;
	
	public ChunkGenerateTask(int startx, int startz, int stopx, int stopz) {
		this.startX = startx;
		this.startZ = startz;
		this.stopX = stopx;
		this.stopZ = stopz;
	}
	
	@Override
	public void run() {
	
		int maxgen = 10;
		int i = 0;

		for (int x = startX; x <= stopX; x++) {
			for (int z = startZ; z <= stopZ; z++) {
				i++;
				
				Chunk chunk = Bukkit.getWorld("world").getChunkAt(x, z);
				if (!chunk.load(true)) {
				}
				
				if (!chunk.unload(true)) {
				}
				
				if (i > maxgen) {
					TaskMaster.syncTask(new ChunkGenerateTask(x, z, stopX, stopZ));
					return;
				}
				
			}
		}
		
		
	}

	
	
}
