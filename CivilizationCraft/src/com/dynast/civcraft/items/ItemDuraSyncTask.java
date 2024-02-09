
package com.dynast.civcraft.items;

import java.util.LinkedList;

import org.bukkit.entity.Player;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.listener.CustomItemManager;
import com.dynast.civcraft.main.CivGlobal;

public class ItemDuraSyncTask implements Runnable {

	@Override
	public void run() {
		
		for (String playerName : CustomItemManager.itemDuraMap.keySet()) {
			Player player;
			try {
				player = CivGlobal.getPlayer(playerName);
			} catch (CivException e) {
				continue;
			}
			
			LinkedList<ItemDurabilityEntry> entries = CustomItemManager.itemDuraMap.get(playerName);
			
			for (ItemDurabilityEntry entry : entries) {
				entry.stack.setDurability(entry.oldValue);
			}
			
			player.updateInventory();
		}
		
		CustomItemManager.duraTaskScheduled = false;
	}
}