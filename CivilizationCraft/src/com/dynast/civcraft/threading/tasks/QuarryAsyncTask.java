package com.dynast.civcraft.threading.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.structure.Quarry;
import com.dynast.civcraft.structure.Quarry.Mineral;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.sync.request.UpdateInventoryRequest.Action;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.MultiInventory;

public class QuarryAsyncTask extends CivAsyncTask {

	Quarry quarry;
	
	public static HashSet<String> debugTowns = new HashSet<>();

	public static void debug(Quarry quarry, String msg) {
		if (debugTowns.contains(quarry.getTown().getName())) {
			CivLog.warning("QuarryDebug:"+quarry.getTown().getName()+":"+msg);
		}
	}	
	
	public QuarryAsyncTask(Structure quarry) {
		this.quarry = (Quarry)quarry;
	}
	
//	private Boolean hasSilkTouch(ItemStack stack) {
//
//		if (stack.hasItemMeta()) {
//			ItemMeta testEnchantMeta = stack.getItemMeta();
//			if (testEnchantMeta.hasEnchant(Enchantment.SILK_TOUCH)) {
//
//				debug(quarry, "Pickaxe has SILK_TOUCH");
//				return true;
//				
//			}
//		}
//		return false;
//	}
	
	private int checkDigSpeed(ItemStack stack) {						
		if (stack.hasItemMeta()) {
			ItemMeta testEnchantMeta = stack.getItemMeta();
			if (testEnchantMeta.hasEnchant(Enchantment.DIG_SPEED)) {

				debug(quarry, "Pickaxe has DIG_SPEED lvl: "+testEnchantMeta.getEnchantLevel(Enchantment.DIG_SPEED));
				return ((int) Math.pow(testEnchantMeta.getEnchantLevel(Enchantment.DIG_SPEED), 2.0)+1);
				
			}
		}
		return 1;
	}
	
	public void processQuarryUpdate() {

		if (!quarry.isActive()) {
			debug(quarry, "quarry inactive...");
			return;
		}
		
		debug(quarry, "Processing Quarry...");
		// Grab each CivChest object we'll require.
		ArrayList<StructureChest> sources = quarry.getAllChestsById(0);
		ArrayList<StructureChest> destinations = quarry.getAllChestsById(1);
		
//		if (sources.size() != 2 || destinations.size() != 2) {
//			CivLog.error("Bad chests for quarry in town:"+quarry.getTown().getName()+" sources:"+sources.size()+" dests:"+destinations.size());
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
					CivLog.warning("Quarry:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					quarry.skippedCounter++;
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
					CivLog.warning("Quarry:"+e.getMessage());
					return;
				}
				if (tmp == null) {
					quarry.skippedCounter++;
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
				/* Quarry destination chest is full, stop processing. */
				return;
			}
			
		} catch (InterruptedException e) {
			return;
		}

		debug(quarry, "Processing quarry:"+quarry.skippedCounter+1);
		ItemStack[] contents = source_inv.getContents();
		for (int i = 0; i < quarry.skippedCounter+1; i++) {
		
			for(ItemStack stack : contents) {
				if (stack == null) {
					continue;
				}
				int modifier = 4+checkDigSpeed(stack);
				
				if (ItemManager.getId(stack) == CivData.WOOD_PICKAXE) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv, stack);
						damage+= modifier;
						stack.setDurability(damage);
						if (damage < 59 && stack.getAmount() == 1) {
							this.updateInventory(Action.ADD, source_inv, stack);
						}
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					//Random rand = new Random();
					//int randMax = Quarry.MAX_CHANCE;
					//int rand1 = rand.nextInt(randMax);
					ItemStack newItem = ItemManager.createItemStack(CivData.COBBLESTONE, modifier);
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(quarry, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
				if (this.quarry.getLevel() >= 2 && ItemManager.getId(stack) == CivData.STONE_PICKAXE) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv, stack);
						damage+= modifier;
						stack.setDurability(damage);
						if (damage < 131 && stack.getAmount() == 1) {
							this.updateInventory(Action.ADD, source_inv, stack);
						}
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Quarry.MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
					
					if (rand1 < ((int)((quarry.getChance(Mineral.COAL))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.COAL, modifier);
					} else {
						newItem = ItemManager.createItemStack(CivData.COBBLESTONE, modifier);
					} 
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(quarry, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
				if (this.quarry.getLevel() >= 3 && ItemManager.getId(stack) == CivData.IRON_PICKAXE) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv, stack);
						damage+= modifier;
						stack.setDurability(damage);
						if (damage < 250 && stack.getAmount() == 1) {
							this.updateInventory(Action.ADD, source_inv, stack);
						}
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Quarry.MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
					
					if (rand1 < ((int)((quarry.getChance(Mineral.COAL))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.COAL, modifier);
					} else if (rand1 < ((int)((quarry.getChance(Mineral.OTHER))*randMax))) {
						newItem = getOther(modifier);
					} else {
						newItem = ItemManager.createItemStack(CivData.COBBLESTONE, modifier);
					} 
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(quarry, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
				if (this.quarry.getLevel() >= 4 && ItemManager.getId(stack) == CivData.DIAMOND_PICKAXE) {
					try {
						short damage = ItemManager.getData(stack);
						this.updateInventory(Action.REMOVE, source_inv, stack);
						damage+= modifier;
						stack.setDurability(damage);
						if (damage < 1561 && stack.getAmount() == 1) {
							this.updateInventory(Action.ADD, source_inv, stack);
						}
					} catch (InterruptedException e) {
						return;
					}
					
					// Attempt to get special resources
					Random rand = new Random();
					int randMax = Quarry.MAX_CHANCE;
					int rand1 = rand.nextInt(randMax);
					ItemStack newItem;
					
					if (rand1 < ((int)((quarry.getChance(Mineral.OBSIDIAN))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.OBSIDIAN, modifier);
				    } else if (rand1 < ((int)((quarry.getChance(Mineral.COAL))*randMax))) {
						newItem = ItemManager.createItemStack(CivData.COAL, modifier);
					} else if (rand1 < ((int)((quarry.getChance(Mineral.OTHER))*randMax))) {
						newItem = getOther(modifier);
					} else {
						newItem = ItemManager.createItemStack(CivData.COBBLESTONE, modifier);
					} 
					
					//Try to add the new item to the dest chest, if we cant, oh well.
					try {
						debug(quarry, "Updating inventory:"+newItem);
						this.updateInventory(Action.ADD, dest_inv, newItem);
					} catch (InterruptedException e) {
						return;
					}
					break;
				}
			}
		}	
	quarry.skippedCounter = 0;
	}	
	
	private ItemStack getOther(int modifier) {
		int randMax = Quarry.MAX_CHANCE;
		Random rand = new Random();
		int rand2 = rand.nextInt(randMax);
		if (rand2 < (randMax/8)) {
			return ItemManager.createItemStack(CivData.STONE, modifier, (short) CivData.ANDESITE);
		} else if (rand2 < (randMax/5)) {
			return ItemManager.createItemStack(CivData.STONE, modifier, (short) CivData.DIORITE);
		} else {
			return ItemManager.createItemStack(CivData.STONE, modifier, (short) CivData.GRANITE);
		}
	}	
	
	
	@Override
	public void run() {
		if (this.quarry.lock.tryLock()) {
			try {
				try {
					processQuarryUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				this.quarry.lock.unlock();
			}
		} else {
			debug(this.quarry, "Failed to get lock while trying to start task, aborting.");
		}
	}

}
