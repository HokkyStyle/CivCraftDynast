
package com.dynast.civcraft.threading.timers;

import java.util.ArrayList;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;

public class ChangeGovernmentTimer implements Runnable {

	@Override
	public void run() {

		// For each town in anarchy, search the session DB for it's timer.
		for (Civilization civ : CivGlobal.getCivs()) {
			if (civ.getGovernment().id.equalsIgnoreCase("gov_anarchy")) {
				String key = "changegov_"+civ.getId();
				ArrayList<SessionEntry> entries;
				
				entries = CivGlobal.getSessionDB().lookup(key);
				if (entries == null || entries.size() < 1) {
					//We are in anarchy but didn't have a sessiondb entry? huh...
					civ.setGovernment("gov_tribalism");
					return;
					//throw new TownyException("Town "+town.getName()+" in anarchy but cannot find its session DB entry with key:"+key);
				}
				
				SessionEntry se = entries.get(0);
				// Our Hour
				int duration = 3600;
				if (CivGlobal.testFileFlag("debug")) {
					duration = 1;
				}
				
				double memberHours = 0;				
				
				boolean noanarchy = false;
				for (Town t : civ.getTowns()) {
					//Get the Count of Residents in each town
					double residentHours = t.getResidentCount();
					double modifier = 1.0;
					//If the town has a broadcast tower, reduce the modifer by the buff_reduced_anarchy value 
					if (t.getBuffManager().hasBuff("buff_reduced_anarchy")) {
						modifier -= t.getBuffManager().getEffectiveDouble("buff_reduced_anarchy");
					}

					//If the civ has a Notre Dame, reduce the modifer by the buff_noanarchy value 
					if (t.getBuffManager().hasBuff("buff_noanarchy")) {
						modifier -= t.getBuffManager().getEffectiveDouble("buff_noanarchy");
						noanarchy = true;
					}
					//Reduce the number of resident hours by the modifier, then add it to the member hours
					memberHours += (residentHours*modifier);
				}
				//Get the maxAnarchy from the governments.yml (Default 24 hours) 
				double maxAnarchy = CivSettings.getIntegerGovernment("anarchy_duration");

				if (noanarchy)
				{
					//If the civ has completed Notre Dame, reduce the maxAnarchy to the lower penalty from the governments.yml (Default 2 hours)
					maxAnarchy = CivSettings.getIntegerGovernment("notre_dame_max_anarchy");
				}
				//Finally, calculate the number of hours, taking the lower memberHours or maxAnarchy
				double anarchyHours = Math.min(memberHours, maxAnarchy);
				
				//Check if enough time has elapsed in seconds since the anarchy started
				if (CivGlobal.hasTimeElapsed(se, anarchyHours*duration)) {

					civ.setGovernment(se.value);
					CivMessage.global(CivSettings.localize.localizedString("var_gov_emergeFromAnarchy",civ.getName(),CivSettings.governments.get(se.value).displayName));
					
					CivGlobal.getSessionDB().delete_all(key);
					civ.save();
				} 
			}
		}		
	}

}
