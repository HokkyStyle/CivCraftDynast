
package com.dynast.civcraft.threading.sync;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.SimpleBlock;


public class SyncBuildUpdateTask implements Runnable {
	
	public static int UPDATE_LIMIT = Integer.MAX_VALUE;
	public static final int QUEUE_SIZE = 4096;
	
//	public static BlockingQueue<SimpleBlock> updateBlocks = new ArrayBlockingQueue<SimpleBlock>(QUEUE_SIZE);
	private static Queue<SimpleBlock> updateBlocks = new LinkedList<>();

	
	public static ReentrantLock buildBlockLock = new ReentrantLock();
	
	public static void queueSimpleBlock(Queue<SimpleBlock> sbList) {
		buildBlockLock.lock();
		try {
			updateBlocks.addAll(sbList);
		} finally {
			buildBlockLock.unlock();
		}
	}
	
	public SyncBuildUpdateTask() {
	}
	
	/*
	 * Runs once, per tick and changes the blocks represented by SimpleBlock 
	 * up to UPDATE_LIMIT times.
	 */
	@Override
	public void run() {
		
		if (buildBlockLock.tryLock()) {
			try {
			
				int i = 0;
				for (i = 0; i < UPDATE_LIMIT; i++) {
					SimpleBlock next = updateBlocks.poll();
					if (next == null) {
						break;
					}
					
					Block block = Bukkit.getWorld(next.worldname).getBlockAt(next.x, next.y, next.z);			
					ItemManager.setTypeId(block, next.getType());
					ItemManager.setData(block, next.getData());
					
					/* Handle Special Blocks */
					Sign s;
					switch (next.specialType) {
					case COMMAND:
						ItemManager.setTypeId(block, CivData.AIR);
						ItemManager.setData(block, 0);
						break;
					case LITERAL:
						if (block.getState() instanceof Sign) {

						s = (Sign)block.getState();
						for (int j = 0; j < 4; j++) {
							s.setLine(j, next.message[j]);
						}
						
						s.update();
						} else {
							ItemManager.setTypeId(block, CivData.AIR);
							ItemManager.setData(block, 0);
						}
						break;
					case NORMAL:
						break;
					}
					
					if (next.buildable != null) {
						next.buildable.savedBlockCount++;
					}
				}
			} finally {
				buildBlockLock.unlock();
			}
		} else {
			CivLog.warning("Couldn't get sync build update lock, skipping until next tick.");
		}		
	}

}
