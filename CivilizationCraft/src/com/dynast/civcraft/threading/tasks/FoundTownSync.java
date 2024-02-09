
package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class FoundTownSync implements Runnable {

	Resident resident;
	
	public FoundTownSync(Resident resident) {
		this.resident = resident;
	}
	
	@Override
	public void run() {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e1) {
			return;
		}
		
		try {
			Town.newTown(resident, resident.desiredTownName, resident.getCiv(), false, false, resident.desiredTownLocation);
		} catch (CivException e) {
			CivMessage.send(player, CivColor.Rose+e.getMessage());
			return;
		}

		//CivMessage.sendSuccess(sender, "Town "+args[1]+" has been founded.");
		CivMessage.global(CivSettings.localize.localizedString("var_FoundTownSync_Success",resident.desiredTownName,resident.getCiv().getName()));
	}

}
