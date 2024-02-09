package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.siege.Cannon;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.war.War;

public class BuildCannon extends ItemComponent {

	public void onInteract(PlayerInteractEvent event) {
		try {
			
			if (!War.isWarTime()) {
				throw new CivException(CivSettings.localize.localizedString("buildCannon_NotWar"));
			}
			
			Player player = event.getPlayer();
			ItemStack stack = player.getInventory().getItemInMainHand();
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null || !craftMat.hasComponent("BuildCannon")) {
				throw new CivException(CivSettings.localize.localizedString("cannon_missingItem"));
			}
			
			Resident resident = CivGlobal.getResident(event.getPlayer());
			Cannon.newCannon(resident);
			
			CivMessage.sendCiv(resident.getCiv(), CivSettings.localize.localizedString("var_buildCannon_Success",
					(event.getPlayer().getLocation().getBlockX()+","+
					event.getPlayer().getLocation().getBlockY()+","+
					event.getPlayer().getLocation().getBlockZ())));
			
			ItemStack newStack = new ItemStack(Material.AIR);
			event.getPlayer().getInventory().setItemInMainHand(newStack);
		} catch (CivException e) {
			CivMessage.sendError(event.getPlayer(), e.getMessage());
		}
		
	}

	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
		attrUtil.addLore(ChatColor.RESET+CivColor.Gold+CivSettings.localize.localizedString("buildCannon_Lore1"));
		attrUtil.addLore(ChatColor.RESET+CivColor.Rose+CivSettings.localize.localizedString("itemLore_RightClickToUse"));	
	}
	
}
