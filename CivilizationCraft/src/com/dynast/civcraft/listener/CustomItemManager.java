package com.dynast.civcraft.listener;

import com.dynast.civcraft.cache.LightningFiredCache;
import com.dynast.civcraft.interactive.InteractiveSpyMission;
import com.dynast.civcraft.items.units.Spy;
import gpl.AttributeUtil;
import gpl.HorseModifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.dynast.civcraft.cache.ArrowFiredCache;
import com.dynast.civcraft.cache.CivCache;
import com.dynast.civcraft.camp.CampBlock;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigRemovedRecipes;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.items.ItemDurabilityEntry;
import com.dynast.civcraft.items.components.Catalyst;
import com.dynast.civcraft.listener.armor.ArmorType;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.ItemChangeResult;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.lorestorage.LoreGuiItem;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureBlock;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;

@SuppressWarnings("deprecation")
public class CustomItemManager implements Listener {
	
	public static HashMap<String, LinkedList<ItemDurabilityEntry>> itemDuraMap = new HashMap<>();
	public static boolean duraTaskScheduled = false;
	
	//@EventHandler(priority = EventPriority.NORMAL)
	//public void onBlockBreak(BlockBreakEvent event) {
	//	this.onItemDurabilityChange(event.getPlayer(), event.getPlayer().getInventory().getItemInMainHand());
	//}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreakSpawnItems(BlockBreakEvent event) {
		BlockCoord bcoord = new BlockCoord("world", 0, 0, 0);
		Location loc = event.getBlock().getLocation();
		bcoord.setFromLocation(loc);
		StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
		CampBlock cb = CivGlobal.getCampBlock(bcoord);

		BlockCoord bcoord1 = new BlockCoord(event.getBlock());
		//Block block = event.getBlock();

		Resident resident = CivGlobal.getResident(event.getPlayer());

		if (sb != null || cb != null) {
			return;
		}

    	if (event.getBlock().getType() == Material.WEB) {
    		if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.SHEARS ||
				event.getPlayer().getInventory().getItemInOffHand().getType() == Material.SHEARS) {
				if (event.isCancelled()) {
					return;
				}

    			event.setCancelled(true);
    			ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);

    			Random rand = new Random();

    			int amount = rand.nextInt(3)+2;

				for (int i = 0; i < amount; i++) {
    				ItemStack stack = new ItemStack(Material.STRING);
    				event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), stack);
				}
			}
		}
		
		if (event.getBlock().getType() == Material.LAPIS_ORE) {
			if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
				return;
			}

			if (event.isCancelled()) {
				return;
			}

			if (CivGlobal.placedBlocks.contains(bcoord1)) {
				return;
			}

			//event.setCancelled(true);

			//ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);
			
			try {
				Random rand = new Random();

				int min = CivSettings.getInteger(CivSettings.materialsConfig, "tungsten_min_drop");
				int max;
				if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
					max = CivSettings.getInteger(CivSettings.materialsConfig, "tungsten_max_drop")
							+ (CivSettings.getInteger(CivSettings.materialsConfig, "tungsten_max_drop_with_fortune")
					        * event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
					min += (CivSettings.getInteger(CivSettings.materialsConfig, "tungsten_max_drop_with_fortune")
							* event.getPlayer().getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS));
				} else {
					max = CivSettings.getInteger(CivSettings.materialsConfig, "tungsten_max_drop");
				}
				
				int randAmount = rand.nextInt(min + max);
				randAmount -= min;
				if (randAmount <= 0) {
					randAmount = min;
				}
				
				for (int i = 0; i < randAmount; i++) {
					ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_tungsten_ore"));
					event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), stack);
				}
				
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}

		if (event.getBlock().getType().equals(Material.IRON_ORE)) {
			if (event.isCancelled()) {
				return;
			}

			if (event.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
				return;
			}

			if (CivGlobal.placedBlocks.contains(bcoord1)) {
				return;
			}

			Random rand = new Random();
			//event.setCancelled(true);

			//ItemManager.setTypeIdAndData(event.getBlock(), CivData.AIR, (byte)0, true);

			int amount1 = rand.nextInt(2)+1;

			for (int i = 0; i < amount1; i++) {
				ItemStack stack = LoreMaterial.spawn(LoreMaterial.materialMap.get("mat_chromium_ore"));
				event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), stack);
			}

			//ItemStack iore = new ItemStack(Material.IRON_ORE, 1);
			//event.getPlayer().getWorld().dropItem(event.getBlock().getLocation(), iore);

			if (CivGlobal.placedBlocks.contains(bcoord)) {
				CivGlobal.placedBlocks.remove(bcoord);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) 
	public void onBlockPlace(BlockPlaceEvent event) {
		ItemStack stackMain = event.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOff = event.getPlayer().getInventory().getItemInOffHand();

		ItemStack stack = event.getHand() == EquipmentSlot.OFF_HAND ? stackOff : stackMain;

		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);

		if (craftMat != null) {
			craftMat.onBlockPlaced(event);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		ItemStack stack = null;
		if (event.getHand() == EquipmentSlot.OFF_HAND) {
			 stack = event.getPlayer().getInventory().getItemInOffHand();
		} else {
			 stack = event.getPlayer().getInventory().getItemInMainHand();
		}
		if (stack == null) {
			return;
		}
		
		LoreMaterial material = LoreMaterial.getMaterial(stack);
		if (material != null) {
			material.onInteract(event);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW) 
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
		if (stack == null) {
			return;
		}

		LoreMaterial material = LoreMaterial.getMaterial(stack);
		if (material != null) {
			material.onInteractEntity(event);
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onItemHeld(PlayerItemHeldEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		ItemStack stack = event.getPlayer().getInventory().getItemInMainHand();
		if (stack == null) {
			return;
		}

		LoreMaterial material = LoreMaterial.getMaterial(stack);
		if (material != null) {
			material.onHold(event);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) {
			return;
		}
		ItemStack stack = event.getItemDrop().getItemStack();
		LoreMaterial loreItem = LoreMaterial.getMaterial(event.getItemDrop().getItemStack());
		if (loreItem instanceof Spy) {
			Resident res = CivGlobal.getResident(event.getPlayer());
			if (res.getInteractiveResponse() instanceof InteractiveSpyMission) {
				event.setCancelled(true);
				return;
			}
		}

		if (LoreMaterial.isCustom(stack)) {
			LoreMaterial.getMaterial(stack).onItemDrop(event);
			return;
		}
		
		String custom = isCustomDrop(stack);
		
		if (custom != null) {
			event.setCancelled(true);
		}
	}	
	
	private static String isCustomDrop(ItemStack stack) {
		if (stack == null || ItemManager.getId(stack) != 166) {
			return null;
		}
		
		if (LoreGuiItem.isGUIItem(stack)) {
			return null;
		}
		
		return stack.getItemMeta().getDisplayName();
	}
	
	/*
	 * Prevent the player from using goodies in crafting recipies.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnCraftItemEvent(CraftItemEvent event) {	
		for (ItemStack stack : event.getInventory().getMatrix()) {
			if (stack != null) {

				if (LoreMaterial.isCustom(stack)) {
					LoreMaterial.getMaterial(stack).onItemCraft(event);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnPlayerItemPickup(PlayerPickupItemEvent event) {
		ItemStack stack = event.getItem().getItemStack();

		if (LoreMaterial.isCustom(stack)) {
			LoreMaterial.getMaterial(stack).onItemPickup(event);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnItemSpawn(ItemSpawnEvent event) {
		ItemStack stack = event.getEntity().getItemStack();

		if (LoreMaterial.isCustom(stack)) {
			LoreMaterial.getMaterial(stack).onItemSpawn(event);
			return;
		}
		
		String custom = isCustomDrop(stack);
		
		if (custom != null) {
			ItemStack newStack = LoreMaterial.spawn(LoreMaterial.materialMap.get(custom), stack.getAmount());
			event.getEntity().getWorld().dropItemNaturally(event.getLocation(), newStack);
			event.setCancelled(true);
			return;
		}
		
		if (isUnwantedVanillaItem(stack)) {
			if (!stack.getType().equals(Material.HOPPER) && 
					!stack.getType().equals(Material.HOPPER_MINECART)) {		
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDefenseAndAttack(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getDamager() instanceof Giant) {
			event.setDamage(28);
		}

		if (event.getDamager() instanceof EnderDragon) {
			event.setDamage(36);
		}

		if (event.getDamager() instanceof WitherSkull || event.getDamager() instanceof Wither) {
			event.setDamage(28);
		}

		Player defendingPlayer = null;
		if (event.getEntity() instanceof Player) {
			defendingPlayer = (Player)event.getEntity();
		}
		
		if (event.getDamager() instanceof LightningStrike) {
			LightningFiredCache lfc = CivCache.lightningFired.get(event.getDamager().getUniqueId());
			if (lfc != null) {
				lfc.destroy(event.getDamager());
				if (defendingPlayer != null) {
					Resident defenderResident = CivGlobal.getResident(defendingPlayer);
					if (defenderResident != null && defenderResident.hasTown() && defenderResident.getTown().getCiv() == lfc.getFromTower().getTown().getCiv()) {
							/* Prevent friendly fire from arrow towers. */
						event.setCancelled(true);
						return;
					}
					event.setDamage((double)lfc.getFromTower().getDamage());
					return;
				}
			}
		}
		
		if (event.getDamager() instanceof Arrow) {
			if (defendingPlayer instanceof HumanEntity) {
				ItemStack stackMain = defendingPlayer.getInventory().getItemInMainHand();
				ItemStack stackOff = defendingPlayer.getInventory().getItemInOffHand();

				if (defendingPlayer.isHandRaised() && (stackMain.getType() == Material.SHIELD || stackOff.getType() == Material.SHIELD) &&
						(stackMain.getType() != Material.BOW && stackOff.getType() != Material.BOW)) {
					defendingPlayer.setCooldown(Material.SHIELD, 100);

					Material mat = defendingPlayer.getInventory().getItemInMainHand().getType();
					ItemStack stack = mat == Material.SHIELD ? stackMain : stackOff;

					ItemStack stackNull = new ItemStack(Material.AIR);
					if (mat == Material.SHIELD) {
						defendingPlayer.getInventory().setItemInMainHand(stackNull);
						defendingPlayer.getInventory().setItemInMainHand(stack);
						defendingPlayer.updateInventory();
					} else {
						defendingPlayer.getInventory().setItemInOffHand(stackNull);
						defendingPlayer.getInventory().setItemInOffHand(stack);
						defendingPlayer.updateInventory();
					}
					event.setCancelled(true);
				}
			}
			
			if ( ((Arrow)event.getDamager()).getShooter() instanceof Player) {
				LivingEntity shooter = (LivingEntity) ((Arrow)event.getDamager()).getShooter();
				ItemStack inHand = ((Player)shooter).getInventory().getItemInMainHand();

				if (LoreMaterial.isCustom(inHand)) {
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(inHand);
					craftMat.onRangedAttack(event, inHand);
				}
				if (event.getEntity() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
					LivingEntity mob = (LivingEntity) event.getEntity();
					double mobdef = mob.getAttribute(Attribute.GENERIC_ARMOR).getValue();
					if (mobdef > 1.0) {
						event.setDamage(event.getDamage() - mobdef);
					}
				}
			} else {
				ArrowFiredCache afc = CivCache.arrowsFired.get(event.getDamager().getUniqueId());
				if (afc != null) {
					/* Arrow was fired by a tower. */
					afc.setHit(true);
					afc.destroy(event.getDamager());
					if (defendingPlayer != null) {
						Resident defenderResident = CivGlobal.getResident(defendingPlayer);
						if (defenderResident != null && defenderResident.hasTown() && 
								defenderResident.getTown().getCiv() == afc.getFromTower().getTown().getCiv()) {
							/* Prevent friendly fire from arrow towers. */
							event.setCancelled(true);
							return;
						}
					}
					/* Return after arrow tower does damage, do not apply armor defense. */
					event.setDamage((double)afc.getFromTower().getDamage());
					return;
				}
				if (((Arrow)event.getDamager()).getShooter() instanceof LivingEntity && !(((Arrow)event.getDamager()).getShooter() instanceof Player)) {
					LivingEntity mob = (LivingEntity) ((Arrow) event.getDamager()).getShooter();
					double mobdmg = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();

					if (mobdmg > 1.0) {
						event.setDamage(mobdmg);
					}

					if (event.getDamage() < 0) {
						event.setDamage(0);
					}
				}
			}
		} else if (event.getDamager() instanceof Player) {
			ItemStack inHand = ((Player)event.getDamager()).getInventory().getItemInMainHand();
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(inHand);
			
			if (defendingPlayer instanceof HumanEntity && craftMat != null) {
				ItemStack stackMain = defendingPlayer.getInventory().getItemInMainHand();
				ItemStack stackOff = defendingPlayer.getInventory().getItemInOffHand();

                if (defendingPlayer.isHandRaised() && (stackMain.getType() == Material.SHIELD || stackOff.getType() == Material.SHIELD) &&
						(stackMain.getType() != Material.BOW && stackOff.getType() != Material.BOW)) {
					defendingPlayer.setCooldown(Material.SHIELD, 100);

					Material mat = defendingPlayer.getInventory().getItemInMainHand().getType();
					ItemStack stack = mat == Material.SHIELD ? stackMain : stackOff;

					ItemStack stackNull = new ItemStack(Material.AIR);
					if (mat == Material.SHIELD) {
						defendingPlayer.getInventory().setItemInMainHand(stackNull);
						defendingPlayer.getInventory().setItemInMainHand(stack);
						defendingPlayer.updateInventory();
					} else {
						defendingPlayer.getInventory().setItemInOffHand(stackNull);
						defendingPlayer.getInventory().setItemInOffHand(stack);
						defendingPlayer.updateInventory();
					}
					event.setCancelled(true);
				}
			}
			
			
			if (craftMat != null) {
				craftMat.onAttack(event, inHand);
			} else {
				/* Non-civcraft items only do 0.5 damage. */
				event.setDamage(0.5);
			}
		}
		
		if (event.getEntity() instanceof Horse) {
			if (HorseModifier.isCivCraftHorse((LivingEntity) event.getEntity())) {
				//Horses take 50% damage from all sources.
				event.setDamage(event.getDamage()/2.0);
			}
		}
		
		if (defendingPlayer == null) {
			if (event.getDamager() instanceof Player && event.getEntity() instanceof LivingEntity) {
				LivingEntity mob = (LivingEntity) event.getEntity();
				double mobdef = mob.getAttribute(Attribute.GENERIC_ARMOR).getValue();
				if (mobdef > 1.0) {
					event.setDamage(event.getDamage() - mobdef);
					if (event.getDamage() < 0.5) {
						event.setDamage(0.5);
					}
				}

				if (event.getEntity() instanceof Giant) {
					event.setDamage(event.getDamage() - 22);
				}

				/*for (ItemStack stack : mob.getEquipment().getArmorContents()) {
					if (LoreMaterial.isCustom(stack)) {
						LoreMaterial.getMaterial(stack).onDefense(event, stack);
					}
				}*/

				if (event.getDamage() < 0.5) {
					event.setDamage(0.5);
				}
			}

//			if (event.getEntity() instanceof LivingEntity) {
//				if (event.getEntity() instanceof CustomMob) {
//					ItemStack inHand = ((Player)event.getDamager()).getInventory().getItemInMainHand();
//					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(inHand);
//
//					if (craftMat != null) {
//						craftMat.onAttack(event, inHand);
//					}
//				}
//			}
        } else {
			/* Search equipt items for defense event. */
			if (event.getDamager() instanceof LivingEntity && !(event.getEntity() instanceof Player)) {
				LivingEntity mob = (LivingEntity) event.getDamager();
				double mobdmg = mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
				if (mobdmg > 1.0) {
					event.setDamage(mobdmg);
					if (event.getDamage() < 0.5) {
						event.setDamage(0.5);
					}
				}
			}



			for (ItemStack stack : defendingPlayer.getEquipment().getArmorContents()) {
				if (LoreMaterial.isCustom(stack)) {
					LoreMaterial.getMaterial(stack).onDefense(event, stack);
				}
			}
		}

		if (event.getDamager() instanceof AreaEffectCloud) {
			event.setDamage(2);
		}
	}
		
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnInventoryClose(InventoryCloseEvent event) {
		for (ItemStack stack : event.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}

			if (LoreMaterial.isCustom(stack)) {
				LoreMaterial.getMaterial(stack).onInventoryClose(event);
			}
		}
		
		for (ItemStack stack : event.getPlayer().getInventory()) {
			if (stack == null) {
				continue;
			}

			if (LoreMaterial.isCustom(stack)) {
				LoreMaterial.getMaterial(stack).onInventoryClose(event);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnInventoryOpen(InventoryOpenEvent event) {
		for (ItemStack stack : event.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}

			if (LoreMaterial.isCustom(stack)) {
				LoreCraftableMaterial.getMaterial(stack).onInventoryOpen(event, stack);
			}
		}
		
		for (ItemStack stack : event.getPlayer().getInventory()) {
			if (stack == null) {
				continue;
			}

			if (LoreMaterial.isCustom(stack)) {
				LoreMaterial.getMaterial(stack).onInventoryOpen(event, stack);
			}
		}
		
		for (ItemStack stack : event.getPlayer().getInventory().getArmorContents()) {
			if (stack == null) {
				continue;
			}

			if (LoreMaterial.isCustom(stack)) {
				LoreMaterial.getMaterial(stack).onInventoryOpen(event, stack);
			}
		}
	}
	
	/* 
	 * Returns false if item is destroyed.
	 */
	private boolean processDurabilityChanges(PlayerDeathEvent event, ItemStack stack, int i) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat != null) {
			ItemChangeResult result = craftMat.onDurabilityDeath(event, stack);
			if (result != null) {
				if (!result.destroyItem) {
					event.getEntity().getInventory().setItem(i, result.stack);
				} else {
					event.getEntity().getInventory().setItem(i, new ItemStack(Material.AIR));
					event.getDrops().remove(stack);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean processArmorDurabilityChanges(PlayerDeathEvent event, ItemStack stack, int i) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat != null) {
			ItemChangeResult result = craftMat.onDurabilityDeath(event, stack);
			if (result != null) {
				if (!result.destroyItem) {
					replaceItem(event, stack, result.stack);
				} else {
					replaceItem(event, stack, new ItemStack(Material.AIR));
					event.getDrops().remove(stack);
					return false;
				}
			}
		}
		
		return true;
	}
	
	private void replaceItem(PlayerDeathEvent event, ItemStack oldItem, ItemStack newItem) {
		ArmorType type = ArmorType.matchType(oldItem);
		switch (type) {
		case HELMET:{
			event.getEntity().getInventory().setHelmet(newItem);
			break;}
		case CHESTPLATE: {
			event.getEntity().getInventory().setChestplate(newItem);
			break;}
		case LEGGINGS: {
			event.getEntity().getInventory().setLeggings(newItem);
			break;}
		case BOOTS: {
			event.getEntity().getInventory().setBoots(newItem);
			break;}
		}
	
	}
	
	@EventHandler(priority = EventPriority.LOW) 
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		HashMap<Integer, ItemStack> noDrop = new HashMap<>();
		ItemStack[] armorNoDrop = new ItemStack[4];
		
		/* Search and execute any enhancements */
		for (int i = 0; i < event.getEntity().getInventory().getSize(); i++) {
			ItemStack stack = event.getEntity().getInventory().getItem(i);
			if (stack == null) {
				continue;
			}
						
			if(!processDurabilityChanges(event, stack, i)) {
				/* Don't process anymore more enhancements on items after its been destroyed. */
				continue;
			}
			
			if (!LoreMaterial.hasEnhancements(stack)) {
				continue;
			}
			
			AttributeUtil attrs = new AttributeUtil(stack);
			for (LoreEnhancement enhance : attrs.getEnhancements()) {
				if (enhance.onDeath(event, stack)) {
					/* Stack is not going to be dropped on death. */
					noDrop.put(i, stack);
				}
			}
		}
		
		/* Search for armor, apparently it doesnt show up in the normal inventory. */
		ItemStack[] contents = event.getEntity().getInventory().getArmorContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack stack = contents[i];
			if (stack == null) {
				continue;
			}

			if(!processArmorDurabilityChanges(event, stack, i)) {
				/* Don't process anymore more enhancements on items after its been destroyed. */
				continue;
			}

			if (!LoreMaterial.hasEnhancements(stack)) {
				continue;
			}
			
			AttributeUtil attrs = new AttributeUtil(stack);
			for (LoreEnhancement enhance : attrs.getEnhancements()) {
				if (enhance.onDeath(event, stack)) {
					/* Stack is not going to be dropped on death. */
					armorNoDrop[i] = stack;
				}
			}
		}

		
		//event.getEntity().getInventory().getArmorContents()	
		class SyncRestoreItemsTask implements Runnable {
			private HashMap<Integer, ItemStack> restore;
			private String playerName;
			private ItemStack[] armorContents;
			
			private SyncRestoreItemsTask(HashMap<Integer, ItemStack> restore,
										 ItemStack[] armorContents, String playerName) {
				this.restore = restore;
				this.playerName = playerName;
				this.armorContents = armorContents;
			}
			
			@Override
			public void run() {
				try {
					Player player = CivGlobal.getPlayer(playerName);					
					PlayerInventory inv = player.getInventory();
					for (Integer slot : restore.keySet()) {
						ItemStack stack = restore.get(slot);
						inv.setItem(slot, stack);
					}	
					
					inv.setArmorContents(this.armorContents);
				} catch (CivException e) {
					e.printStackTrace();
				}
			}
			
		}
		Boolean keepInventory = Boolean.valueOf(Bukkit.getWorld("world").getGameRuleValue("keepInventory"));
		if (!keepInventory) {
			TaskMaster.syncTask(new SyncRestoreItemsTask(noDrop, armorNoDrop, event.getEntity().getName()));
		}
		
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}
				
		/* Remove any vanilla item IDs that can't be crafted from vanilla drops. */
		LinkedList<ItemStack> removed = new LinkedList<>();
		for (ItemStack stack : event.getDrops()) {
			Integer key = ItemManager.getId(stack);
			
			if (CivSettings.removedRecipies.containsKey(key)) {
				if (!LoreMaterial.isCustom(stack)) {
					removed.add(stack);
				}
			}
		}
		
		event.getDrops().removeAll(removed);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemPickup(PlayerPickupItemEvent event) {

		if (ItemManager.getId(event.getItem().getItemStack()) == ItemManager.getId(Material.SLIME_BALL)) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getItem().getItemStack());
			if (craftMat == null) {
				/* Found a vanilla slime ball. */
				LoreCraftableMaterial slime = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_slime");
				ItemStack newStack = LoreCraftableMaterial.spawn(slime);
				newStack.setAmount(event.getItem().getItemStack().getAmount());
				event.getPlayer().getInventory().addItem(newStack);
				event.getPlayer().updateInventory();
				event.getItem().remove();
				event.setCancelled(true);
			}
		} else if (ItemManager.getId(event.getItem().getItemStack()) == ItemManager.getId(Material.ENDER_PEARL)) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getItem().getItemStack());
			if (craftMat == null) {
				/* Found a vanilla ender peral. */
				LoreCraftableMaterial slime = LoreCraftableMaterial.getCraftMaterialFromId("mat_ender_pearl");
				ItemStack newStack = LoreCraftableMaterial.spawn(slime);
				newStack.setAmount(event.getItem().getItemStack().getAmount());
				event.getPlayer().getInventory().addItem(newStack);
				event.getPlayer().updateInventory();
				event.getItem().remove();
				event.setCancelled(true);
			}
		} else if (ItemManager.getId(event.getItem().getItemStack()) == ItemManager.getId(Material.TNT)) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getItem().getItemStack());
			if (craftMat == null) {
				/* Found a vanilla tnt. */
				LoreCraftableMaterial slime = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_tnt");
				ItemStack newStack = LoreCraftableMaterial.spawn(slime);
				newStack.setAmount(event.getItem().getItemStack().getAmount());
				event.getPlayer().getInventory().addItem(newStack);
				event.getPlayer().updateInventory();
				event.getItem().remove();
				event.setCancelled(true);
			}
		} else if (ItemManager.getId(event.getItem().getItemStack()) == ItemManager.getId(Material.RAW_FISH)
				&& ItemManager.getData(event.getItem().getItemStack()) == 
					ItemManager.getData(ItemManager.getMaterialData(CivData.FISH_RAW, CivData.CLOWNFISH))) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getItem().getItemStack());
			if (craftMat == null) {
				/* Found a vanilla clownfish. */
				LoreCraftableMaterial clown = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_clownfish");
				ItemStack newStack = LoreCraftableMaterial.spawn(clown);
				newStack.setAmount(event.getItem().getItemStack().getAmount());
				event.getPlayer().getInventory().addItem(newStack);
				event.getPlayer().updateInventory();
				event.getItem().remove();
				event.setCancelled(true);
			}
		} else if (ItemManager.getId(event.getItem().getItemStack()) == ItemManager.getId(Material.RAW_FISH)
				&& ItemManager.getData(event.getItem().getItemStack()) == 
					ItemManager.getData(ItemManager.getMaterialData(CivData.FISH_RAW, CivData.PUFFERFISH))) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getItem().getItemStack());
			if (craftMat == null) {
				/* Found a vanilla pufferfish. */
				LoreCraftableMaterial clown = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_pufferfish");
				ItemStack newStack = LoreCraftableMaterial.spawn(clown);
				newStack.setAmount(event.getItem().getItemStack().getAmount());
				event.getPlayer().getInventory().addItem(newStack);
				event.getPlayer().updateInventory();
				event.getItem().remove();
				event.setCancelled(true);			
			}
		}
	}
	
	/* Called when we click on an object, used for conversion to fix up reverse compat problems. */
	public void convertLegacyItem(InventoryClickEvent event) {
		boolean currentEmpty = (event.getCurrentItem() == null) || (ItemManager.getId(event.getCurrentItem()) == CivData.AIR);

		if (currentEmpty) {
			return;
		}
		
		if (ItemManager.getId(event.getCurrentItem()) == ItemManager.getId(Material.SLIME_BALL)) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getCurrentItem());
			if (craftMat == null) {
				/* Found a vanilla slime ball. */
				LoreCraftableMaterial slime = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_slime");
				ItemStack newStack = LoreCraftableMaterial.spawn(slime);
				newStack.setAmount(event.getCurrentItem().getAmount());
				event.setCurrentItem(newStack);
			}
		}
		
		if (ItemManager.getId(event.getCurrentItem()) == ItemManager.getId(Material.RAW_FISH)
				&& ItemManager.getData(event.getCurrentItem()) == 
					ItemManager.getData(ItemManager.getMaterialData(CivData.FISH_RAW, CivData.CLOWNFISH))) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getCurrentItem());
			if (craftMat == null) {
				/* Found a vanilla slime ball. */
				LoreCraftableMaterial clown = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_clownfish");
				ItemStack newStack = LoreCraftableMaterial.spawn(clown);
				newStack.setAmount(event.getCurrentItem().getAmount());
				event.setCurrentItem(newStack);
			}
		}
		
		if (ItemManager.getId(event.getCurrentItem()) == ItemManager.getId(Material.RAW_FISH)
				&& ItemManager.getData(event.getCurrentItem()) == 
					ItemManager.getData(ItemManager.getMaterialData(CivData.FISH_RAW, CivData.PUFFERFISH))) {
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getCurrentItem());
			if (craftMat == null) {
				/* Found a vanilla slime ball. */
				LoreCraftableMaterial clown = LoreCraftableMaterial.getCraftMaterialFromId("mat_vanilla_pufferfish");
				ItemStack newStack = LoreCraftableMaterial.spawn(clown);
				newStack.setAmount(event.getCurrentItem().getAmount());
				event.setCurrentItem(newStack);
			}
		}
	}
	
	/*
	 * Track the location of the goodie.
	 */
	@EventHandler(priority = EventPriority.HIGHEST) 
	public void OnInventoryClick(InventoryClickEvent event) {
		boolean currentEmpty = (event.getCurrentItem() == null) || (ItemManager.getId(event.getCurrentItem()) == CivData.AIR);
		boolean cursorEmpty = (event.getCursor() == null) || (ItemManager.getId(event.getCursor()) == CivData.AIR);
		
		if (currentEmpty && cursorEmpty) {
			return;
		}
		
		convertLegacyItem(event);
		
		if (event.getRawSlot() < 0) {
			//I guess this means "drop the item?"
			
			//CivLog.debug("GET RAW WAS NEGATIVE, cancel this event it should be invalid.");
			//event.setResult(Result.DENY);
			//event.setCancelled(true);
			
			//try {
			//	Player player = CivGlobal.getPlayer(event.getWhoClicked().getName());
			//	player.updateInventory();
			//} catch (CivException e) {
			//}
			
			return;
		}
		
		InventoryView view = event.getView();
		Inventory clickedInv;
		Inventory otherInv;
		
		if (view.getType().equals(InventoryType.CRAFTING)) {
			//This is the player's own inventory. For some reason it requires
			//special treatment. The 'top' inventory is the 2x2 crafting
			//area plus the output. During shift click, items do not go there
			//so the otherInv should always be the player's inventory aka the bottom.
			if (event.getRawSlot() <= 4) {
				clickedInv = view.getTopInventory();
				otherInv = view.getBottomInventory();
			} else {
				clickedInv = view.getBottomInventory();
				otherInv = view.getBottomInventory();
			}
		} else {
			if (event.getRawSlot() == view.convertSlot(event.getRawSlot())) {
				//Clicked in the top holder
				clickedInv = view.getTopInventory();
				otherInv = view.getBottomInventory();
			} else {
				clickedInv = view.getBottomInventory();
				otherInv = view.getTopInventory();
			}
		}
		
		LoreMaterial current = LoreMaterial.getMaterial(event.getCurrentItem());
		LoreMaterial cursor = LoreMaterial.getMaterial(event.getCursor());
		
		if (event.isShiftClick()) {
			// Shift click is _always_ current item.
		//	CustomItemStack is = new CustomItemStack(event.getCurrentItem());
			if (current != null) {
			//if (is.isCustomItem() && (is.getMaterial() instanceof CustomMaterialExtended)) {
				// Calling onInvShiftClick Event.
				//((CustomMaterialExtended)is.getMaterial()).onInvShiftClick(event, clickedInv, otherInv, is.getItem());
				current.onInvShiftClick(event, clickedInv, otherInv, event.getCurrentItem());
			//}
			}
			
		} else {
			
			if (!currentEmpty && !cursorEmpty) {
				//CustomItemStack currentIs = new CustomItemStack(event.getCurrentItem());
				//CustomItemStack cursorIs = new CustomItemStack(event.getCursor());
				
				if (current != null) {
					current.onInvItemSwap(event, clickedInv, event.getCursor(), event.getCurrentItem());
				}
				
				if (cursor != null) {
					cursor.onInvItemSwap(event, clickedInv, event.getCursor(), event.getCurrentItem());
				}
			} else if (!currentEmpty) {
				// This is a pickup event.
				//CustomItemStack is = new CustomItemStack(event.getCurrentItem());
				if (current != null) {
					// Calling onInvItemPickup Event.
					current.onInvItemPickup(event, clickedInv, event.getCurrentItem());
				}
			} else {
				// Implied !cursorEmpty
				if (cursor != null) {
					// Calling onInvItemDrop Event.
					cursor.onInvItemDrop(event, clickedInv, event.getCursor());
				}
				
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnPlayerInteractEntityEvent (PlayerInteractEntityEvent event) {
			
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getPlayer().getInventory().getItemInMainHand());
		if (craftMat == null) {
			return;
		}
		
		craftMat.onPlayerInteractEntityEvent(event);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnPlayerLeashEvent(PlayerLeashEntityEvent event) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getPlayer().getInventory().getItemInMainHand());
		if (craftMat == null) {
			return;
		}
		
		craftMat.onPlayerLeashEvent(event);
	}
	
	
	@EventHandler(priority = EventPriority.LOW)
	public void onItemDurabilityChange(PlayerItemDamageEvent event) {
		ItemStack stack = event.getItem();
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) {
			return;
		}
		craftMat.onItemDurabilityChange(event);
	}
	
	private static boolean isUnwantedVanillaItem(ItemStack stack) {
		if (stack == null) {
			return false;
		}
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat != null) {
			/* Assume that if it's custom. It's good to go. */			
			return false;
		}
		
		if(LoreGuiItem.isGUIItem(stack)) {
			return false;
		}
		
		ConfigRemovedRecipes removed = CivSettings.removedRecipies.get(ItemManager.getId(stack));
		if (removed == null && !stack.getType().equals(Material.ENCHANTED_BOOK)) {
			/* Check for badly enchanted tools */
			if (stack.containsEnchantment(Enchantment.DAMAGE_ALL) ||
				stack.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS) ||
				stack.containsEnchantment(Enchantment.KNOCKBACK) ||
				stack.containsEnchantment(Enchantment.DAMAGE_UNDEAD) ||
				stack.containsEnchantment(Enchantment.DURABILITY)) {					
			} else if (stack.containsEnchantment(Enchantment.FIRE_ASPECT) && 
					   stack.getEnchantmentLevel(Enchantment.FIRE_ASPECT) > 2) {
				// Remove any fire aspect above this amount
			} else if (stack.containsEnchantment(Enchantment.LOOT_BONUS_MOBS) &&
					   stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) > 1) {
				// Only allow looting 1
			} else if (stack.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS) &&
				   stack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) > 2) {
				// Only allow fortune 1
			} else if (stack.containsEnchantment(Enchantment.DIG_SPEED) &&
					   stack.getEnchantmentLevel(Enchantment.DIG_SPEED) > 5) {
				// only allow effiencey 5
			} else {
				/* Not in removed list, so allow it. */
				return false;				
			}
		}
		return true;
	}
	
	public static void removeUnwantedVanillaItems(Player player, Inventory inv) {
		if (player.isOp()) {
			/* Allow OP to carry vanilla stuff. */
			return;
		}
		boolean sentMessage = false;
		
		for (ItemStack stack : inv.getContents()) {
			if (!isUnwantedVanillaItem(stack)) {
				continue;
			}
			
			inv.remove(stack);
			if (player != null) {
				CivLog.info("Removed vanilla item:"+stack+" from "+player.getName());
			}
			if (!sentMessage) {
				if (player != null) {
					CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("customItem_restrictedItemsRemoved"));
				}
				sentMessage = true;
			}
		}
		
		/* Also check the player's equipt. */
		if (player != null) {
			ItemStack[] contents = player.getEquipment().getArmorContents();
			boolean foundBad = false;
			for (int i = 0; i < contents.length; i++) {
				ItemStack stack = contents[i];
				if (stack == null) {
					continue;
				}
				
				LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
				if (craftMat != null) {
					/* Assume we are good if we are custom. */
					continue;
				}
				
				ConfigRemovedRecipes removed = CivSettings.removedRecipies.get(stack.getTypeId());
				if (removed == null && !stack.getType().equals(Material.ENCHANTED_BOOK)) {
					/* Not in removed list, so allow it. */
					continue;
				}
				
				CivLog.info("Removed vanilla item:"+stack+" from "+player.getName()+" from armor.");
				contents[i] = new ItemStack(Material.AIR);
				foundBad = true;
				if (!sentMessage) {
					CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("customItem_restrictedItemsRemoved"));
					sentMessage = true;
				}
			}		
			if (foundBad) {
				player.getEquipment().setArmorContents(contents);
			}
		}
		
		if (sentMessage) {
			if (player != null) {
				player.updateInventory();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void OnInventoryHold(PlayerItemHeldEvent event) {
		
		ItemStack stack = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (stack == null) {
			return;
		}
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) {
			return;
		}
		
		craftMat.onHold(event);
	}
	
//	/* Prevent books from being inside an inventory. */
	/* Prevent vanilla gear from being used. */
/*	@EventHandler(priority = EventPriority.LOWEST)
	public void OnInventoryOpenRemove(InventoryOpenEvent event) {
		//CivLog.debug("open event.");
		if (event.getPlayer() instanceof Player) {
			
			//for (ItemStack stack : event.getInventory()) {
			for (int i = 0; i < event.getInventory().getSize(); i++) {
				ItemStack stack = event.getInventory().getItem(i);
				//CivLog.debug("stack cleanup");
				
				AttributeUtil attrs = ItemCleanup(stack);
				if (attrs != null) {
					event.getInventory().setItem(i, attrs.getStack());
				}
			}
		}
	}*/
	
/*	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerLogin(PlayerLoginEvent event) {
		
		class SyncTask implements Runnable {
			String playerName;
			
			public SyncTask(String name) {
				playerName = name;
			}

			@Override
			public void run() {
				try {
					Player player = CivGlobal.getPlayer(playerName);
										
					for (int i = 0; i < player.getInventory().getSize(); i++) {
						ItemStack stack = player.getInventory().getItem(i);

						AttributeUtil attrs = ItemCleanup(stack);
						if (attrs != null) {
							player.getInventory().setItem(i, attrs.getStack());
						}
					}
					
					ItemStack[] contents = new ItemStack[player.getInventory().getArmorContents().length];
					for (int i = 0; i < player.getInventory().getArmorContents().length; i++) {
						ItemStack stack = player.getInventory().getArmorContents()[i];
						
						AttributeUtil attrs = ItemCleanup(stack);
						if (attrs != null) {
							contents[i] = attrs.getStack();
						} else {
							contents[i] = stack;
						}
					}
					
					player.getInventory().setArmorContents(contents);
					
				} catch (CivException e) {
					return;
				}
				
			}
		}
		
		TaskMaster.syncTask(new SyncTask(event.getPlayer().getName()));
	
	}*/
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void OnInventoryClickEvent(InventoryClickEvent event) {
		//if (event.getWhoClicked() instanceof Player) {
		//	removeUnwantedVanillaItems((Player)event.getWhoClicked(), event.getView().getBottomInventory());			
		//}
	}
		
	public LoreCraftableMaterial getCompatibleCatalyst(LoreCraftableMaterial craftMat) {
		/* Setup list of catalysts to refund. */
		LinkedList<LoreMaterial> cataList = new LinkedList<>();
		cataList.add(LoreMaterial.materialMap.get("mat_common_attack_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_common_defense_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_uncommon_attack_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_uncommon_defense_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_rare_attack_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_rare_defense_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_legendary_attack_catalyst"));
		cataList.add(LoreMaterial.materialMap.get("mat_legendary_defense_catalyst"));
		
		for (LoreMaterial mat : cataList) {
			LoreCraftableMaterial cMat = (LoreCraftableMaterial)mat;
			
			Catalyst cat = (Catalyst)cMat.getComponent("Catalyst");
			String allowedMats = cat.getString("allowed_materials");
			String[] matSplit = allowedMats.split(",");
			
			for (String mid : matSplit) {
				if (mid.trim().equalsIgnoreCase(craftMat.getId())) {
					return cMat;
				}
			}
			
		}
		return null;
	}
	
	
//	/*
//	 * Checks a players inventory and inventories that are opened for items.
//	 *   - Currently looks for old catalyst enhancements and marks them so
//	 *     they can be refunded.
//	 *
//	 */
//	public AttributeUtil ItemCleanup(ItemStack stack) {
//		
//		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
//		if (craftMat == null) {
//			return null;
//		}
//			
//		AttributeUtil attrs = new AttributeUtil(stack);
//		if (!attrs.hasLegacyEnhancements()) {
//			return null;
//		}
//		
//		/* Found a legacy catalysts. Repair it. */
//		ItemStack cleanItem = LoreCraftableMaterial.spawn(craftMat);
//		AttributeUtil attrsClean = new AttributeUtil(cleanItem);
//		
//		double level = 0;
//		for (LoreEnhancement enh : LoreCraftableMaterial.getLegacyEnhancements(stack)) {
//			if (enh instanceof LoreEnhancementDefense) {
//				level = Double.valueOf(attrs.getLegacyEnhancementData("LoreEnhancementDefense"));
//				LoreCraftableMaterial compatCatalyst = getCompatibleCatalyst(craftMat);
//				attrs.setCivCraftProperty("freeCatalyst", ""+level+":"+compatCatalyst.getId());
//				attrs.removeLegacyEnhancement("LoreEnhancementDefense");
//			} else if (enh instanceof LoreEnhancementAttack) {
//				level = Double.valueOf(attrs.getLegacyEnhancementData("LoreEnhancementAttack"));
//				LoreCraftableMaterial compatCatalyst = getCompatibleCatalyst(craftMat);
//				attrs.setCivCraftProperty("freeCatalyst", ""+level+":"+compatCatalyst.getId());
//				attrs.removeLegacyEnhancement("LoreEnhancementAttack");
//			} 
//		}
//		
//		attrs.setLore(attrsClean.getLore());
//		attrs.setName(attrsClean.getName());
//		attrs.add(Attribute.newBuilder().name("Attack").
//				type(AttributeType.GENERIC_ATTACK_DAMAGE).
//				amount(0).
//				build());
//		
//		if (level != 0) {
//			attrs.addLore(CivColor.LightBlue+level+" free enhancements! Redeem at blacksmith.");
//			CivLog.cleanupLog("Converted stack:"+stack+" with enhancement level:"+level);
//		
//		}
//		
//		for (LoreEnhancement enh : LoreCraftableMaterial.getLegacyEnhancements(stack)) {
//			if (enh instanceof LoreEnhancementSoulBound) {	
//				LoreEnhancementSoulBound soulbound = (LoreEnhancementSoulBound)LoreEnhancement.enhancements.get("LoreEnhancementSoulBound");
//				soulbound.add(attrs);
//			}
//		}
//		
//		
//
//		return attrs;
//	}
	
}
