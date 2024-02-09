
package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.object.Resident;


public class PlayerDelayedDebtWarning implements Runnable {
	Resident resident;
	
	public PlayerDelayedDebtWarning(Resident resident) {
		this.resident = resident;
	}
	
	@Override
	public void run() {
		resident.warnDebt();
	}
	
	
	
}
