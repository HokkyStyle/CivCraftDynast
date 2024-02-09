
package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.event.player.PlayerItemDamageEvent;

public class NoVanillaDurability extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
	}
	
	
	//private ConcurrentHashMap<String, String> playersToUpdateInventory = new ConcurrentHashMap<String, String>();
	
	@Override
	public void onDurabilityChange(PlayerItemDamageEvent event) {
		event.setCancelled(true);
		///event.setDamage(0);
		//event.getPlayer().updateInventory();
		
		
		
//		LinkedList<ItemDurabilityEntry> entries = CustomItemManager.itemDuraMap.get(player.getName());
//		
//		if (entries == null) {
//			entries = new LinkedList<ItemDurabilityEntry>();
//		}
//		
//		ItemDurabilityEntry entry = new ItemDurabilityEntry();
//		entry.stack = stack;
//		entry.oldValue = stack.getDurability();
//		
//		entries.add(entry);
//		CustomItemManager.itemDuraMap.put(player.getName(), entries);
//		
//		if (!CustomItemManager.duraTaskScheduled) {
//			TaskMaster.syncTask(new ItemDuraSyncTask());
//		}
	}




//	@SuppressWarnings("deprecation")
//	@Override
//	public void run() {
//		for (String playerName : playersToUpdateInventory.keySet()) {
//			try {
//				Player player = CivGlobal.getPlayer(playerName);
//				player.updateInventory();
//			} catch (CivException e) {
//				e.printStackTrace();
//			}
//		}
//		
//	}
	

}
