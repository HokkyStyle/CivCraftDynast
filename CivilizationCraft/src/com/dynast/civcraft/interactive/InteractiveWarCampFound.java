package com.dynast.civcraft.interactive;

import com.dynast.civcraft.camp.WarCamp;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigBuildableInfo;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class InteractiveWarCampFound implements InteractiveResponse {

	ConfigBuildableInfo info;
	
	public InteractiveWarCampFound(ConfigBuildableInfo info) {
		this.info = info;
	}
	
	@Override
	public void respond(String message, Resident resident) {
		resident.clearInteractiveMode();

		if (!message.equalsIgnoreCase("yes")) {
			CivMessage.send(resident, CivSettings.localize.localizedString("interactive_warcamp_Cancel"));
			return;
		}
		
		WarCamp.newCamp(resident, info);
	}

}
