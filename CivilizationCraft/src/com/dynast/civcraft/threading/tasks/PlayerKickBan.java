
package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;

public class PlayerKickBan implements Runnable {

	String name;
	boolean kick;
	boolean ban;
	String reason;
	
	public PlayerKickBan(String name, boolean kick, boolean ban, String reason) {
		this.name = name;
		this.kick = kick;
		this.ban = ban;
		this.reason = reason;
	}
	
	@Override
	public void run() {
		Player player;
		try {
			player = CivGlobal.getPlayer(name);
		} catch (CivException e) {
			return;
		}
		
		if (ban) {
			Resident resident = CivGlobal.getResident(player);
			resident.setBanned(true);
			resident.save();
		}
		
		if (kick) {
			player.kickPlayer(reason);
		}
	}

}
