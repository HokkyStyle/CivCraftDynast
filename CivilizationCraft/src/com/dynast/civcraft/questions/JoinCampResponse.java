
package com.dynast.civcraft.questions;

import org.bukkit.entity.Player;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class JoinCampResponse implements QuestionResponseInterface {

	public Camp camp;
	public Resident resident;
	public Player sender;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinCamp_accepted",resident.getName()));
			
			if (!camp.hasMember(resident.getName())) {
				camp.addMember(resident);
				CivMessage.sendCamp(camp, CivSettings.localize.localizedString("var_joinCamp_Alert",resident.getName()));
				resident.save();
			}
		} else {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinCamp_Decline",resident.getName()));
		}
	}
	
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
