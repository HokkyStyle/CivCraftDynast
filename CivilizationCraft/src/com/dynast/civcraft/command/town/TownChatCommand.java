
package com.dynast.civcraft.command.town;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class TownChatCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		//TODO let non players use this command
		if ((sender instanceof Player) == false) {
			return false;
		}
		
		Player player = (Player)sender;
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_civchat_notResident"));
			return false;
		}
	
		if (args.length == 0) {
			resident.setTownChat(!resident.isTownChat());
			resident.setCivChat(false);
			CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_chat_mode")+" "+resident.isTownChat());
			return true;
		}
		
		
		String fullArgs = "";
		for (String arg : args) {
			fullArgs += arg + " ";
		}
	
		if (resident.getTown() == null) {
			player.sendMessage(CivColor.Rose+CivSettings.localize.localizedString("cmd_town_chat_NoTown"));
			return false;
		}
		CivMessage.sendTownChat(resident.getTown(), resident, "<%s> %s", fullArgs);
		return true;
	}

}
