
package com.dynast.civcraft.threading.timers;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.threading.CivAsyncTask;

public class BeakerTimer extends CivAsyncTask {

	//private double beakersPerRun;
	
	public static final int BEAKER_PERIOD = 60;
	
	public BeakerTimer(int periodInSeconds) {
		
	//	this.beakersPerRun = ((double)periodInSeconds/60);
	}
	
	@Override
	public void run() {
		
		for (Civilization civ : CivGlobal.getCivs()) {
			
			if (civ.getCapitolName() == null) {
				CivMessage.sendCiv(civ, CivSettings.localize.localizedString("beaker_ErrorNoCapitol"));
				continue;
			}
			
			Town town = CivGlobal.getTown(civ.getCapitolName());
			if (town == null) {
				CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_beaker_noCapitol",civ.getCapitolName()));
				continue;
			}
			
			TownHall townhall = town.getTownHall();
			if (townhall == null) {
				CivMessage.sendCiv(civ, CivSettings.localize.localizedString("beaker_noCapitolHall"));
			}
			
			try {
				/* 
				 * The base_beakers defines the number of beakers per hour to give.
				 * This timer runs every min, so dividing my 60 will give us the number
				 * of beakers per min.
				 */
				if (civ.getResearchTech() != null) {
					civ.addBeakers(civ.getBeakers() / BEAKER_PERIOD);
				} else {
					civ.processUnusedBeakers();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}

}
