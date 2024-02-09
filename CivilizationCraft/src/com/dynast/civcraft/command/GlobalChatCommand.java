
package com.dynast.civcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class GlobalChatCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		//TODO let non players use this command
		if ((sender instanceof Player) == false) {
			return false;
		}
		
		Player player = (Player)sender;
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_gc_notResident"));
			return false;
		}
	
		if (args.length == 0) {
			resident.setCivChat(false);
			resident.setTownChat(false);
			CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_gc_enabled"));
			return true;
		}
		
		CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_gc_disabled"));
		return true;
		
//		String fullArgs = "";
//		for (String arg : args) {
//			fullArgs += arg + " ";
//		}
//		
//		CivMessage.sendChat(resident, "<%s> %s", fullArgs);
//		return true;
	}
}
