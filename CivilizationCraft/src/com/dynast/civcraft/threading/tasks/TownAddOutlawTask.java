
package com.dynast.civcraft.threading.tasks;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class TownAddOutlawTask implements Runnable {

	String name;
	Town town;
	
	
	public TownAddOutlawTask(String name, Town town) {
		this.name = name;
		this.town = town;
	}

	@Override
	public void run() {
		
		try {
			Player player = CivGlobal.getPlayer(name);
			CivMessage.send(player, CivColor.Yellow+ChatColor.BOLD+CivSettings.localize.localizedString("var_TownAddOutlawTask_Notify",town.getName()));
		} catch (CivException e) {
		}
		
		town.addOutlaw(name);
		town.save();
		CivMessage.sendTown(town, CivColor.Yellow+CivSettings.localize.localizedString("var_TownAddOutlawTask_Message",name));
		
	}
	
}
