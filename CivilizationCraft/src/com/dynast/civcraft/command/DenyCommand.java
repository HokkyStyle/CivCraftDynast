
package com.dynast.civcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.tasks.CivLeaderQuestionTask;
import com.dynast.civcraft.threading.tasks.PlayerQuestionTask;

public class DenyCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if (!(sender instanceof Player)) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_MustBePlayer"));
			return false;
		}
		
		Player player = (Player)sender;
		
		PlayerQuestionTask task = (PlayerQuestionTask) CivGlobal.getQuestionTask(player.getName());
		if (task != null) {
			/* We have a question, and the answer was "Accepted" so notify the task. */
			synchronized(task) {
				task.setResponse("deny");
				task.notifyAll();
			}
			return true;
		}

		Resident resident = CivGlobal.getResident(player);
		if (resident.getCiv().getLeaderGroup().hasMember(resident)) {
			CivLeaderQuestionTask civTask = (CivLeaderQuestionTask) CivGlobal.getQuestionTask("civ:"+resident.getCiv().getName());
			if (civTask != null) {
				synchronized(civTask) {
					civTask.setResponse("deny");
					civTask.setResponder(resident);
					civTask.notifyAll();
				}
			}
			return true;
		}
		

		CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptError"));
		return false;
	}
}
