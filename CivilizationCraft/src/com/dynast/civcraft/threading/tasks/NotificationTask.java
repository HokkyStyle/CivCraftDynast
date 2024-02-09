
package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;

public class NotificationTask implements Runnable {
	//private Server server;
	String message;
	String playerName;
	
	public NotificationTask(String playerName, String msg) {
		message = msg;
		this.playerName = playerName;
	}

	@Override
	public void run() {
		try {
			Player player = CivGlobal.getPlayer(playerName);
			CivMessage.send(player, message);
		} catch (CivException e) {
			//Player not online
		}
		
	}
}