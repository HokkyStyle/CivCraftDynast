
package com.dynast.civcraft.interactive;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.dynast.civcraft.command.town.TownCommand;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.CivColor;

public class InteractiveCapitolName implements InteractiveResponse {

	@Override
	public void respond(String message, Resident resident) {
		
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}

		if (message.equalsIgnoreCase("cancel")) {
			CivMessage.send(player, CivSettings.localize.localizedString("interactive_capitol_cancel"));
			resident.clearInteractiveMode();
			return;
		}
		
		if (!StringUtils.isAlpha(message) || !StringUtils.isAsciiPrintable(message)) {
			CivMessage.send(player, CivColor.Rose+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_capitol_invalidname"));
			return;
		}
		
		message = message.replace(" ", "_");
		message = message.replace("\"", "");
		message = message.replace("\'", "");
		
		resident.desiredCapitolName = message;
		CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_interactive_capitol_confirm1",CivColor.Yellow+resident.desiredCivName+CivColor.LightGreen,CivColor.Yellow+resident.desiredCapitolName+CivColor.LightGreen));
		CivMessage.sendHeading(player, CivSettings.localize.localizedString("interactive_capitol_confirmSurvey"));
		
		class SyncTask implements Runnable {
			String playerName;
			
			
			public SyncTask(String name) {
				this.playerName = name;
			}

			@Override
			public void run() {
				Player player;
				try {
					player = CivGlobal.getPlayer(playerName);
				} catch (CivException e) {
					return;
				}
				
				Resident resident = CivGlobal.getResident(playerName);
				if (resident == null) {
					return;
				}
				
				CivMessage.send(player, TownCommand.survey(player.getLocation()));
				CivMessage.send(player, "");
				CivMessage.send(player, CivColor.LightGreen+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_capitol_confirmPrompt"));
				resident.setInteractiveMode(new InteractiveConfirmCivCreation());				
			}
		
		}

		TaskMaster.syncTask(new SyncTask(resident.getName())); 

		return;
	}

}
