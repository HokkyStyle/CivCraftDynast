
package com.dynast.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChunkSnapshot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.structure.Windmill;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.MultiInventory;

public class WindmillPreProcessTask extends CivAsyncTask {

	private ArrayList<ChunkSnapshot> snapshots;
	private Windmill windmill;
	
	public WindmillPreProcessTask(Windmill windmill, ArrayList<ChunkSnapshot> snaphots) {
		this.snapshots = snaphots;
		this.windmill = windmill;
	}
	
	@Override
	public void run() {
		int plant_max;
		try {
			plant_max = CivSettings.getInteger(CivSettings.structureConfig, "windmill.plant_max");
			
			if (windmill.getCiv().hasTechnology("tech_machinery")) {
				plant_max *= 2;
			}
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		
		/* Read in the source inventory's contents. Make sure we have seeds to plant. */
		ArrayList<StructureChest> sources = windmill.getAllChestsById(0);
		MultiInventory source_inv = new MultiInventory();

		for (StructureChest src : sources) {
			try {
				this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), true);
				} catch (CivTaskAbortException e) {
				//	e.printStackTrace();
					return;
				}
				source_inv.addInventory(tmp);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}				
		}
		
		int breadCount = 0;
		int carrotCount = 0;
		int potatoCount = 0;
		for (ItemStack stack : source_inv.getContents()) {
			if (stack == null) {
				continue;
			}

			switch (ItemManager.getId(stack)) {
			case CivData.BREAD_SEED:
				breadCount += stack.getAmount();
				break;
			case CivData.CARROT_ITEM:
				carrotCount += stack.getAmount();
				break;
			case CivData.POTATO_ITEM:
				potatoCount += stack.getAmount();
				break;
			default:
				continue;
			}
		}
				
		/* If we've got nothing in the seed basket, nothing to plant! */
		if (breadCount == 0 && carrotCount == 0 && potatoCount == 0) {
			return;
		}
		
		/* Only try to plant as many crops as we have (or the max) */
		plant_max = Math.min((breadCount + carrotCount + potatoCount), plant_max);
		
		/* Read snapshots and find blocks that can be planted. */
		ArrayList<BlockCoord> blocks = new ArrayList<>();
		for (ChunkSnapshot snapshot : this.snapshots) {			
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int y = 0; y < 255; y++) {
						
						
						if (ItemManager.getBlockTypeId(snapshot, x, y, z) == CivData.FARMLAND) {
							if (ItemManager.getBlockTypeId(snapshot, x, y+1, z) == CivData.AIR) {
								int blockx = (snapshot.getX()*16) + x;
								int blocky = y+1;
								int blockz = (snapshot.getZ()*16) + z;
								
								blocks.add(new BlockCoord(this.windmill.getCorner().getWorldname(),
										blockx, blocky, blockz));
							}
						}
					}
				}
			}
		}
		
		ArrayList<BlockCoord> plantBlocks = new ArrayList<>();
		/* Select up to plant_max of these blocks to be planted. */
		Random rand = new Random();
		for (int i = 0; i < plant_max; i++) {
			if (blocks.isEmpty()) {
				break;
			}
			
			BlockCoord coord = blocks.get(rand.nextInt(blocks.size()));
			blocks.remove(coord);
			plantBlocks.add(coord);
		}
				
		// Fire off a sync task to complete the operation.
		TaskMaster.syncTask(new WindmillPostProcessSyncTask(windmill, plantBlocks,
				breadCount, carrotCount, potatoCount, source_inv));

	}

}
