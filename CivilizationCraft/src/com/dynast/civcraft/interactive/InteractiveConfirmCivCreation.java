
package com.dynast.civcraft.interactive;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FoundCivSync;
import com.dynast.civcraft.util.CivColor;

public class InteractiveConfirmCivCreation implements InteractiveResponse {

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
			CivMessage.send(player, CivSettings.localize.localizedString("interactive_civ_cancelcreate"));
			return;
		}
		
		if (resident.desiredCapitolName == null || resident.desiredCivName == null) {
			CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("interactive_civ_createError"));
			return;
		}
		
		TaskMaster.syncTask(new FoundCivSync(resident));

	}

}
