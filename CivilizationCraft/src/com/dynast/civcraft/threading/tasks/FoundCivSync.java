
package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class FoundCivSync implements Runnable {

	Resident resident;
	
	public FoundCivSync(Resident resident) {
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
			Civilization.newCiv(resident.desiredCivName, resident.desiredCapitolName, resident, player, resident.desiredTownLocation);
		} catch (CivException e) {
			CivMessage.send(player, CivColor.Rose+e.getMessage());
		}
		
	}

	
	
}
