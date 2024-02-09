package com.dynast.civcraft.randomevents.components;


import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.randomevents.RandomEventComponent;

public class PayPlayer extends RandomEventComponent {

	@Override
	public void process() {
		String playerName = this.getParent().componentVars.get(getString("playername_var"));
		if (playerName == null) {
			CivLog.warning("No playername var for pay player.");
			return;
		}

		Resident resident = CivGlobal.getResident(playerName);
		double coins = this.getDouble("amount");
		resident.getTreasury().deposit(coins);
		CivMessage.send(resident, CivSettings.localize.localizedString("resident_paid")+" "+coins+" "+CivSettings.CURRENCY_NAME);	
	}

}
