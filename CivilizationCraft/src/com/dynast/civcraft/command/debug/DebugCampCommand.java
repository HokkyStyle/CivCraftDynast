
package com.dynast.civcraft.command.debug;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.BlockCoord;

public class DebugCampCommand extends CommandBase {

	@Override
	public void init() {
		command = "/dbg test ";
		displayName = "Test Commands";
		
		commands.put("growth", "[name] - Shows a list of this player's camp growth spots.");
		
	}
	
	public void growth_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		
		if (!resident.hasCamp()) {
			throw new CivException("This guy doesnt have a camp.");
		}
		
		Camp camp = resident.getCamp();
		
		CivMessage.sendHeading(sender, "Growth locations");
		
		String out = "";
		for (BlockCoord coord : camp.growthLocations) {
			boolean inGlobal = CivGlobal.vanillaGrowthLocations.contains(coord);
			out += coord.toString()+" in global:"+inGlobal;
		}
		
		CivMessage.send(sender, out);
		
	}

	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		
	}

}
