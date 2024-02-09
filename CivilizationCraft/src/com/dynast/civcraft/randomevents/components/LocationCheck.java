package com.dynast.civcraft.randomevents.components;

import java.util.List;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.randomevents.RandomEventComponent;
import com.dynast.civcraft.util.BlockCoord;

public class LocationCheck extends RandomEventComponent {

	@Override
	public void process() {
	}
	
	public boolean onCheck() { 
		
		String varname = this.getString("varname");
		String locString = this.getParent().componentVars.get(varname);
		
		if (locString == null) {
			//CivLog.warning("Couldn't get var name: "+varname+" for location check component.");
			return false;
		}
		
		BlockCoord bcoord = new BlockCoord(locString);
		double radiusSquared = 2500.0; /* 50 block radius */
		List<PlayerLocationCache> cache = PlayerLocationCache.getNearbyPlayers(bcoord, radiusSquared);
		
		for (PlayerLocationCache pc : cache) {
			Resident resident = CivGlobal.getResident(pc.getName());
			if (resident.getTown() == this.getParentTown()) {
				return true;
			}
		}
		
		return false; 
		
	}

}
