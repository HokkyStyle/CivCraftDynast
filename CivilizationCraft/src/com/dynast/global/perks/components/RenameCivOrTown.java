package com.dynast.global.perks.components;


import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.interactive.InteractiveRenameCivOrTown;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class RenameCivOrTown extends PerkComponent {

	@Override
	public void onActivate(Resident resident) {
		
		if (!resident.hasTown()) {
			CivMessage.sendError(resident, CivSettings.localize.localizedString("RenameCivOrTown_NotResident"));
			return;
		}
		
		resident.setInteractiveMode(new InteractiveRenameCivOrTown(resident, this));
	}
	
}
