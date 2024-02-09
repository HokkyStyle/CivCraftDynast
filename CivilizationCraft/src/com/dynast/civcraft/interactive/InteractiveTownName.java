
package com.dynast.civcraft.interactive;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.command.town.TownCommand;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.CivColor;

public class InteractiveTownName implements InteractiveResponse {

	@Override
	public void respond(String message, Resident resident) {

		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}

		if (message.equalsIgnoreCase("cancel")) {
			CivMessage.send(player, CivSettings.localize.localizedString("interactive_town_cancelled"));
			resident.clearInteractiveMode();
			return;
		}
		
		if (!StringUtils.isAlpha(message) || !StringUtils.isAsciiPrintable(message)) {
			CivMessage.send(player, CivColor.Rose+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_town_nameInvalid"));
			return;
		}
		
		message = message.replace(" ", "_");
		message = message.replace("\"", "");
		message = message.replace("\'", "");
		
		resident.desiredTownName = message;
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_interactive_town_confirmName",CivColor.Yellow+resident.desiredTownName+CivColor.LightGreen));
		
		class SyncTask implements Runnable {
			Player player;
			Resident resident;
			
			public SyncTask(Resident resident) {
				this.resident = resident;
			}
			
			
			@Override
			public void run() {
				
				try {
				player = CivGlobal.getPlayer(resident);
					
				ItemStack stack = player.getInventory().getItemInMainHand();
				LoreMaterial craftMat = LoreMaterial.getMaterial(stack);
				if (craftMat == null) {
					throw new CivException(CivSettings.localize.localizedString("settler_missingItem"));						
				}
				 
				
				CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_town_surveyResults"));
				CivMessage.send(player, TownCommand.survey(player.getLocation()));
				
				Location capLoc = resident.getCiv().getCapitolTownHallLocation();
				if (capLoc == null) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_town_noCapitol"));
					resident.clearInteractiveMode();
					return;
				}
				
				CivMessage.send(player, CivColor.LightGreen+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_town_confirm"));
				
				resident.setInteractiveMode(new InteractiveConfirmTownCreation());	
				}catch (CivException e) {
					CivMessage.sendError(player, e.getMessage());
				}
			}
		} 
		
		TaskMaster.syncTask(new SyncTask(resident));

		return;
		
		
	}

}
