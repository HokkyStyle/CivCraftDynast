package com.dynast.civcraft.interactive;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.structure.Barracks;
import com.dynast.civcraft.util.CivColor;

public class InteractiveRepairItem implements InteractiveResponse {

	double cost;
	String playerName;
	LoreCraftableMaterial craftMat;
	
	public InteractiveRepairItem(double cost, String playerName, LoreCraftableMaterial craftMat) {
		this.cost = cost;
		this.playerName = playerName;
		this.craftMat = craftMat;
	}
	
	public void displayMessage() {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		
		CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_repair_heading"));
		CivMessage.send(player, CivColor.LightGreen+CivColor.BOLD+CivSettings.localize.localizedString("var_interactive_repair_prompt1",craftMat.getName()));
		CivMessage.send(player, CivColor.LightGreen+CivColor.BOLD+CivSettings.localize.localizedString("var_interactive_repair_prompt2",CivColor.Yellow+CivColor.BOLD+cost+CivColor.LightGreen,CivColor.Yellow+CivColor.BOLD+CivSettings.CURRENCY_NAME+CivColor.LightGreen));
		CivMessage.send(player, CivColor.LightGreen+CivColor.BOLD+CivSettings.localize.localizedString("interactive_repair_prompt3"));
		
	}
	
	
	@Override
	public void respond(String message, Resident resident) {
		resident.clearInteractiveMode();

		if (!message.equalsIgnoreCase("yes")) {
			CivMessage.send(resident, CivColor.LightGray+CivSettings.localize.localizedString("interactive_repair_canceled"));
			return;
		}
		
		Barracks.repairItemInHand(cost, resident.getName(), craftMat);
	}

}
