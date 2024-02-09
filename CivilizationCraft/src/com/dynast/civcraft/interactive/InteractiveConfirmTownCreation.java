
package com.dynast.civcraft.interactive;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.questions.TownNewRequest;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FoundTownSync;
import com.dynast.civcraft.util.CivColor;

public class InteractiveConfirmTownCreation implements InteractiveResponse {

	@Override
	public void respond(String message, Resident resident) {

		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}

		resident.clearInteractiveMode();

		if (!message.equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivSettings.localize.localizedString("interactive_town_cancel"));
			return;
		}
		
		if (resident.desiredTownName == null) {
			CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("interactive_town_createError"));
			return;
		}
		try {
		TownNewRequest join = new TownNewRequest();		
		join.resident = resident;
		join.civ = resident.getCiv();
		ItemStack stack = player.getInventory().getItemInMainHand();
		LoreMaterial craftMat = LoreMaterial.getMaterial(stack);
		if (craftMat == null) {
			throw new CivException(CivSettings.localize.localizedString("settler_missingItem"));
		} else {				
//			CivGlobal.questionLeaders(player, resident.getCiv(), CivSettings.localize.localizedString("var_interactive_town_alert",player.getName(),resident.desiredTownName,(player.getLocation().getBlockX()+","+player.getLocation().getBlockY()+","+player.getLocation().getBlockZ())),
//					30*1000, join);
			TaskMaster.syncTask(new FoundTownSync(resident));
		} 
		} catch (CivException e) {			
		}
							
//		CivMessage.send(player, CivColor.Yellow+CivSettings.localize.localizedString("interactive_town_request"));  
		return;
//		CivGlobal.questionPlayer(player, CivGlobal.getPlayer(newResident), 
//				"Would you like to join the town of "+town.getName()+"?",
//				INVITE_TIMEOUT, join);
			
		}
}
