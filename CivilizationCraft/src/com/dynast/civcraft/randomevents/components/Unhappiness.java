package com.dynast.civcraft.randomevents.components;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.randomevents.RandomEventComponent;

public class Unhappiness extends RandomEventComponent {

	public static String getKey(Town town) {
		return "randomevent:unhappiness:"+town.getId();
	}
	
	
	@Override
	public void process() {
		
		int unhappiness = Integer.valueOf(this.getString("value"));
		int duration = Integer.valueOf(this.getString("duration"));
		
		CivGlobal.getSessionDB().add(getKey(this.getParentTown()), unhappiness+":"+duration, this.getParentTown().getCiv().getId(), this.getParentTown().getId(), 0);	
		sendMessage(CivSettings.localize.localizedString("var_re_unhappiness1",unhappiness,duration));
		
	}

}
