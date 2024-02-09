
package com.dynast.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.structure.Trommel;
import com.dynast.civcraft.structure.Trommel.Mineral;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.MultiInventory;

public class TrommelAsyncTask extends CivAsyncTask {

	Trommel trommel;
	private static final int GRAVEL_RATE = 1; //0.10%
	private static final int COBBLESTONE_RATE = 1;
	
	public static HashSet<String> debugTowns = new HashSet<>();

	public static void debug(Trommel trommel, String msg) {
		if (debugTowns.contains(trommel.getTown().getName())) {
			CivLog.warning("TrommelDebug:"+trommel.getTown().getName()+":"+msg);
		}
	}	
	
	public TrommelAsyncTask(Structure trommel) {
		this.trommel = (Trommel)trommel;
	}
	
	public void processTrommelUpdate() {
		if (!trommel.isActive()) {
			debug(trommel, "trommel inactive...");
			return;
		}
		
		debug(trommel, "Processing trommel...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = trommel.getAllChestsById(1);
		ArrayList<StructureChest> destinations = trommel.getAllChestsById(2);
		
//		if (sources.size() != 2 || destinations.size() != 2) {
//			CivLog.error("Bad chests for trommel in town:"+trommel.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
//			return;
//		}
		
		// Make sure the chunk is loaded before continuing. Also, add get chest and add it to inventory.
		MultiInventory source_inv = new MultiInventory();
		MultiInventory dest_inv = new MultiInventory();

		try {
			for (StructureChest src : sources) {
				//this.syncLoadChunk(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getZ());				
				Inventory tmp;
				try {
					tmp = this.getChestInventory(src.getCoord().getWorldname(), src.getCoord().getX(), src.getCoord().getY(), src.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Trommel:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
					return;
				}
				source_inv.addInventory(tmp);
			}
			
			boolean full = true;
			for (StructureChest dst : destinations) {
				//this.syncLoadChunk(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getZ());
				Inventory tmp;
				try {
					tmp = this.getChestInventory(dst.getCoord().getWorldname(), dst.getCoord().getX(), dst.getCoord().getY(), dst.getCoord().getZ(), false);
				} catch (CivTaskAbortException e) {
					//e.printStackTrace();
					CivLog.warning("Trommel:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					trommel.skippedCounter++;
					return;
				}
				dest_inv.addInventory(tmp);
				
				for (ItemStack stack : tmp.getContents()) {
					if (stack == null) {
						full = false;
						break;
					}
				}
			}
			
			if (full) {
				/* Trommel destination chest is full, stop processing. */
				return;
			}
			
		} catch (InterruptedException e) {
			return;
		}
		
		debug(trommel, "Processing trommel:"+trommel.skippedCounter+1);
		ItemStack[] contents = source_inv.getContents();
		for (int i = 0; i < trommel.skippedCounter+1; i++) {
		
			for(ItemStack stack : contents) {
				if (stack == null) {
					continue;
				}
				
				if (ItemManager.getId(stack) == CivData.COBBLESTONE) {
					try {
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.COBBLESTONE, 1));
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Trommel.GRAVEL_MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
									
					if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
					} else {
						newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
					}
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(trommel, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
				if (ItemManager.getId(stack) == CivData.SANDSTONE) {
				if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.SANDSTONE, CivData.SANDSTONE_COMMON))) {
					try {
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.SANDSTONE, 1, (short) CivData.SANDSTONE_COMMON));
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Trommel.GRAVEL_MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
									
					if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
					} else {
						newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 0);
					}
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(trommel, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
				}				
				if (this.trommel.getLevel() >= 5 && ItemManager.getId(stack) == CivData.OBSIDIAN) {
					try {
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.OBSIDIAN, 1));
					} catch (InterruptedException e) {
						return;
					}
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Trommel.OBSIDIAN_MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
					
				    if (rand1 < ((int)((trommel.getObsidianChance(Mineral.REFINED_CHROMIUM))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.REFINED_TUNGSTEN))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));							
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.CHROMIUM))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.TUNGSTEN))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.EMERALD_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD_ORE, 1);	
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.EMERALD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.DIAMOND_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND_ORE, 1);	
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.DIAMOND))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.GOLD_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_ORE, 1);	
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.GOLD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.IRON_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.REDSTONE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);
					} else if (rand1 < ((int)((trommel.getObsidianChance(Mineral.IRON))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
					} else {
						newItem = ItemManager.createItemStack(CivData.COBBLESTONE, (Integer)COBBLESTONE_RATE);
					}
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(trommel, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;												
			    }				
				if (ItemManager.getId(stack) == CivData.HARD_CLAY) {
					try {
						this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(172, 1));
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Trommel.GRAVEL_MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
									
					if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
						newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
					} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
					} else {
						newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 1);
					}
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(trommel, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}				
				if (ItemManager.getId(stack) == CivData.STAINED_CLAY) {
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.LIGHTGRAY_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.LIGHTGRAY_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.RED_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.RED_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 1);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.BROWN_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.BROWN_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.YELLOW_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.YELLOW_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 0);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.ORANGE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.ORANGE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 1);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.WHITE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.WHITE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 0);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.LIGHTPURPLE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.LIGHTPURPLE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.LIGHTBLUE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.LIGHTBLUE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.LIME_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.LIME_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.ROSE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.ROSE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 1);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.GRAY_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.GRAY_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.BIRUZE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.BIRUZE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.PURPLE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.PURPLE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.SAND, (Integer)GRAVEL_RATE, (short) 1);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.BLUE_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.BLUE_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.GREEN_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.GREEN_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					
					if (ItemManager.getData(stack) == ItemManager.getData(ItemManager.getMaterialData(CivData.STAINED_CLAY, CivData.BLACK_CLAY))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STAINED_CLAY, 1, (short) CivData.BLACK_CLAY));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRAVEL_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
										
						if (rand1 < ((int)((trommel.getGravelChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);	
						} else if (rand1 < ((int)((trommel.getGravelChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);	
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
				}
				if (ItemManager.getId(stack) == CivData.STONE) {										

					if (this.trommel.getLevel() >= 2 && ItemManager.getData(stack) == 
							ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.GRANITE))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.GRANITE));
						} catch (InterruptedException e) {
							return;
						}
						
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.GRANITE_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
						
						if (rand1 < ((int)((trommel.getGraniteChance(Mineral.REFINED_CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.REFINED_TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));	
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.GOLD_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);
						} else if (rand1 < ((int)((trommel.getGraniteChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					if (this.trommel.getLevel() >= 3 && ItemManager.getData(stack) == 
							ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.DIORITE))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.DIORITE));
						} catch (InterruptedException e) {
							return;
						}
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.DIORITE_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
						
						if (rand1 < ((int)((trommel.getDioriteChance(Mineral.REFINED_CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.REFINED_TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));	
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.DIAMOND_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND_ORE, 1);
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.GOLD_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);
						} else if (rand1 < ((int)((trommel.getDioriteChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}
					if (this.trommel.getLevel() >= 4 && ItemManager.getData(stack) == 
							ItemManager.getData(ItemManager.getMaterialData(CivData.STONE, CivData.ANDESITE))) {
						try {
							this.updateInventory(Action.REMOVE, source_inv, ItemManager.createItemStack(CivData.STONE, 1, (short) CivData.ANDESITE));
						} catch (InterruptedException e) {
							return;
						}
						// Attempt to get special resources
						Random rand = new Random();
						int randMax = Trommel.ANDESITE_MAX_CHANCE;
						int rand1 = rand.nextInt(randMax);
						ItemStack newItem;
						

						if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.REFINED_CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_chromium"));
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.REFINED_TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_forged_tungsten"));							
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.CHROMIUM))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.TUNGSTEN))*randMax))) {
							newItem = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.EMERALD_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.EMERALD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.EMERALD, 1);
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.DIAMOND_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.DIAMOND))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.DIAMOND, 1);
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.GOLD_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_ORE, 1);	
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.GOLD))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.GOLD_INGOT, 1);
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.IRON_ORE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_ORE, 1);
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.REDSTONE))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.REDSTONE_DUST, 1);
						} else if (rand1 < ((int)((trommel.getAndesiteChance(Mineral.IRON))*randMax))) {
							newItem = ItemManager.createItemStack(CivData.IRON_INGOT, 1);
						} else {
							newItem = ItemManager.createItemStack(CivData.GRAVEL, (Integer)GRAVEL_RATE);
						}
						
						//Try to add the new item to the dest chest, if we cant, oh well.
						try {
							debug(trommel, "Updating inventory:"+newItem);
							this.updateInventory(Action.ADD, dest_inv, newItem);
						} catch (InterruptedException e) {
							return;
						}
						break;
					}					
				}
			}	
		}
		trommel.skippedCounter = 0;
	}
	
	
	
	@Override
	public void run() {
		if (this.trommel.lock.tryLock()) {
			try {
				try {
					if (this.trommel.getTown().getGovernment().id.equals("gov_theocracy") || this.trommel.getTown().getGovernment().id.equals("gov_monarchy") || this.trommel.getTown().getGovernment().id.equals("gov_oligarchy")){
						Random rand = new Random();
						int randMax = 100;
						int rand1 = rand.nextInt(randMax);
						Double chance = CivSettings.getDouble(CivSettings.structureConfig, "trommel.penalty_rate") * 100;
						if (rand1 < chance) {
							processTrommelUpdate();
							debug(this.trommel, "Not penalized");
						} else {
							debug(this.trommel, "Skip Due to Penalty");
						}
					} else {
						processTrommelUpdate();
						if (this.trommel.getTown().getGovernment().id.equals("gov_despotism")) {
							debug(this.trommel, "Doing Bonus");
							processTrommelUpdate();
						}
					}					
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				this.trommel.lock.unlock();
			}
		} else {
			debug(this.trommel, "Failed to get lock while trying to start task, aborting.");
		}
	}

}
