package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;

public class PlayerArmorSpeedAsyncTask implements Runnable {

	Player player;
	
	public PlayerArmorSpeedAsyncTask(Player player) {
		this.player = player;
	}

	@Override
	public void run() {		
		doArmorSpeedCheck();
	}
	
	public void doArmorSpeedCheck() {
		Resident resident = CivGlobal.getResident(this.player);
		resident.calculateWalkingModifier(this.player);
	}

}
