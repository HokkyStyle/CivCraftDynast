package com.dynast.civcraft.questions;

import org.bukkit.entity.Player;

import com.dynast.civcraft.arena.ArenaTeam;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class JoinTeamResponse implements QuestionResponseInterface {

	public ArenaTeam team;
	public Resident resident;
	public Player sender;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinTeam_accepted",resident.getName()));
			
			try {
				ArenaTeam.addMember(team.getName(), resident);
			} catch (CivException e) {
				CivMessage.sendError(sender, e.getMessage());
				return;
			}

			CivMessage.sendTeam(team, CivSettings.localize.localizedString("var_joinTeam_Alert",resident.getName()));
		} else {
			CivMessage.send(sender, CivColor.LightGray+CivSettings.localize.localizedString("var_joinTeam_Declined",resident.getName()));
		}
	}
	
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}

}
