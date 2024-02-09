
package com.dynast.civcraft.threading.sync;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.World;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.util.BlockCoord;

public class DamagedStructureTimer implements Runnable {
	
	@Override
	public void run() {
		
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			Structure struct = iter.next().getValue();
			
			if (struct.isDestroyed()) {
				int size = struct.getStructureBlocks().size();
				World world = struct.getCorner().getBlock().getWorld();

				for (int i = 0; i < size/10; i++) {
					Random rand = new Random();
					int index = rand.nextInt(size);
					
					// slower but uses less memory.
					int j = 0;
					for (BlockCoord coord : struct.getStructureBlocks().keySet()) {
						
						if (j < index) {
							j++;
							continue;
						}
						
						world.playEffect(coord.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
						break;
					}
					//BlockCoord coord = (BlockCoord) struct.getStructureBlocks().keySet().toArray()[index];
				}
			}
		}
	}
	
}
