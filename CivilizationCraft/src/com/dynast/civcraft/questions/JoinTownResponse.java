
package com.dynast.civcraft.questions;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.AlreadyRegisteredException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class JoinTownResponse implements QuestionResponseInterface {

	public Town town;
	public Resident resident;
	public Player sender;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinTown_accepted",resident.getName()));
			
			try {
				town.addResident(resident);
			} catch (AlreadyRegisteredException e) {
				CivMessage.sendError(sender, CivSettings.localize.localizedString("var_joinTown_errorInTown",resident.getName()));
				return;
			}

			CivMessage.sendTown(town, CivSettings.localize.localizedString("var_joinTown_alert",resident.getName()));
			resident.save();
		} else {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinTown_Declined",resident.getName()));
		}
	}
	
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
