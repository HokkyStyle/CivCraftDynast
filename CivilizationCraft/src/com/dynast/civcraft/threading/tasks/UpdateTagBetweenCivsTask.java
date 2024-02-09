
package com.dynast.civcraft.threading.tasks;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.sync.SyncUpdateTagsBetweenCivs;

public class UpdateTagBetweenCivsTask implements Runnable {

	Civilization civ;
	Civilization otherCiv;
	
	public UpdateTagBetweenCivsTask(Civilization civ, Civilization otherCiv) {
		this.civ = civ;
		this.otherCiv = otherCiv;
	}
	
	@Override
	public void run() {
		Set<Player> civList = new HashSet<>();
		Set<Player> otherCivList = new HashSet<>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			Resident resident = CivGlobal.getResident(player);
			if (resident == null || !resident.hasTown()) {
				continue;
			}
			
			if (resident.getTown().getCiv() == civ) {
				civList.add(player);
			} else if (resident.getTown().getCiv() == otherCiv) {
				otherCivList.add(player);
			}
		}
		
		TaskMaster.syncTask(new SyncUpdateTagsBetweenCivs(civList, otherCivList));		
	}

}
