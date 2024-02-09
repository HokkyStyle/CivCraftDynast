

package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigBuildableInfo;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.interactive.InteractiveCivName;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.CallbackInterface;
import com.dynast.civcraft.util.CivColor;

public class FoundCivilization extends ItemComponent implements CallbackInterface{
	
	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
		attrUtil.addLore(ChatColor.RESET+CivColor.Gold+CivSettings.localize.localizedString("foundCiv_lore1"));
		attrUtil.addLore(ChatColor.RESET+CivColor.Rose+CivSettings.localize.localizedString("itemLore_RightClickToUse"));
		attrUtil.addEnhancement("LoreEnhancementSoulBound", null, null);
		attrUtil.addLore(CivColor.Gold+CivSettings.localize.localizedString("Soulbound"));
	}
	
	public void foundCiv(Player player) throws CivException {
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) {
			throw new CivException(CivSettings.localize.localizedString("foundCiv_notResident"));
		}
			
		/*
		 * Build a preview for the Capitol structure.
		 */
		CivMessage.send(player, CivColor.LightGreen+CivColor.BOLD+CivSettings.localize.localizedString("build_checking_position"));
		ConfigBuildableInfo info = CivSettings.structures.get("s_capitol");
		Buildable.buildVerifyStatic(player, info, player.getLocation(), this);	
	}
	
	public void onInteract(PlayerInteractEvent event) {
		
		event.setCancelled(true);
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) &&
				!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		
		class SyncTask implements Runnable {
			String name;
				
			public SyncTask(String name) {
				this.name = name;
			}
			
			@Override
			public void run() {
				Player player;
				try {
					player = CivGlobal.getPlayer(name);
					try {
						foundCiv(player);
					} catch (CivException e) {
						CivMessage.sendError(player, e.getMessage());
					}
				} catch (CivException e) {
					return;
				}
				player.updateInventory();
			}
		}
		TaskMaster.syncTask(new SyncTask(event.getPlayer().getName()));
		
	}

	@Override
	public void execute(String playerName) {
		
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		
		Resident resident = CivGlobal.getResident(player);
		
		/* Save the location so we dont have to re-validate the structure position. */
		resident.desiredTownLocation = player.getLocation();
		CivMessage.sendHeading(player, CivSettings.localize.localizedString("foundCiv_Heading"));
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("foundCiv_Prompt1"));
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("foundCiv_Prompt2"));
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("foundCiv_Prompt3"));
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("foundCiv_Prompt4"));
		CivMessage.send(player, " ");
		CivMessage.send(player, CivColor.LightGreen+ChatColor.BOLD+CivSettings.localize.localizedString("foundCiv_Prompt5"));
		CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("build_cancel_prompt"));
		
		resident.setInteractiveMode(new InteractiveCivName());
	}

	
}
