package com.dynast.civcraft.war;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.dynast.anticheat.ACManager;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.PlayerKickBan;
import com.dynast.civcraft.util.CivColor;

public class WarAntiCheat {

	
	public static void kickUnvalidatedPlayers() {
		if (CivGlobal.isCasualMode()) {
			return;
		}
		
		if (!ACManager.isEnabled()) {
			return;
		}
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.isOp()) {
				continue;
			}
			
			if (player.hasPermission("civ.ac_exempt")) {
				continue;
			}
			
			Resident resident = CivGlobal.getResident(player);
			onWarTimePlayerCheck(resident);
		}
		
		CivMessage.global(CivColor.LightGray+CivSettings.localize.localizedString("war_kick_atWarNoAnticheat"));
	}
	
	public static void onWarTimePlayerCheck(Resident resident) {
		if (!resident.hasTown()) {
			return;
		}
		
		if (!resident.getCiv().getDiplomacyManager().isAtWar()) {
			return;
		}
		
		try {
			if (!resident.isUsesAntiCheat()) {
				TaskMaster.syncTask(new PlayerKickBan(resident.getName(), true, false, 
						CivSettings.localize.localizedString("war_kick_needAnticheat1")+
						CivSettings.localize.localizedString("war_kick_needAntiCheat2")));
			}
		} catch (CivException e) {
		}
	}
	
}
