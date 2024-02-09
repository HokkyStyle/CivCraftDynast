package com.dynast.civcraft.questions;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FoundTownSync;
import com.dynast.civcraft.util.CivColor;

public class TownNewRequest implements QuestionResponseInterface {

	public Resident resident;
	public Resident leader;
	public Civilization civ;
	public String name;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivMessage.send(civ, CivColor.LightGreen+CivSettings.localize.localizedString("newTown_accepted1",leader.getName(),name));
			TaskMaster.syncTask(new FoundTownSync(resident));
		} else {
			CivMessage.send(resident, CivColor.LightGray+CivSettings.localize.localizedString("var_newTown_declined",leader.getName()));
		}		
	}

	@Override
	public void processResponse(String response, Resident responder) {
		this.leader = responder;
		processResponse(response);		
	}
}
