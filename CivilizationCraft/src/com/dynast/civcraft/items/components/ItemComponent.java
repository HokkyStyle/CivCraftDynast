package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.lorestorage.ItemChangeResult;
import com.dynast.civcraft.object.BuildableDamageBlock;

public abstract class ItemComponent {

	//public static ConcurrentHashMap<String, ArrayList<ItemComponent>> componentsByType = new ConcurrentHashMap<String, ArrayList<ItemComponent>>();
	public static ReentrantLock lock = new ReentrantLock();
	
	private HashMap<String, String> attributes = new HashMap<>();
	private String name;
	
	public void createComponent() {
		
	}
	
	public void destroyComponent() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getString(String key) {
		return attributes.get(key);
	}
	
	public double getDouble(String key) {
		return Double.valueOf(attributes.get(key));
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public abstract void onPrepareCreate(AttributeUtil attrUtil);
	public void onDurabilityChange(PlayerItemDamageEvent event) {}
	public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {}
	public void onInteract(PlayerInteractEvent event) {}
	public int onStructureBlockBreak(BuildableDamageBlock sb, int damage) { return damage; }
	public void onItemSpawn(ItemSpawnEvent event) {}
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {}
	public void onPlayerLeashEvent(PlayerLeashEntityEvent event) {}
	public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {}
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack stack) { return result; }
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {}
	public boolean onBlockPlaced(BlockPlaceEvent event) { return false;	}
	public void onInventoryOpen(InventoryOpenEvent event, ItemStack stack) {	}
	public void onHold(PlayerItemHeldEvent event) {	}
	public void onBlock(EntityDamageByEntityEvent event) { }

}
