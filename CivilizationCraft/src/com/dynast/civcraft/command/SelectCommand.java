
package com.dynast.civcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.threading.tasks.PlayerQuestionTask;
import com.dynast.civcraft.threading.tasks.TemplateSelectQuestionTask;

public class SelectCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_MustBePlayer"));
			return false;
		}
		
		
		if (args.length < 1) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_enterNumber"));
			return false;
		}
		
		Player player = (Player)sender;
		
		PlayerQuestionTask task = (PlayerQuestionTask) CivGlobal.getQuestionTask(player.getName());
		if (task == null) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptError"));
			return false;
		}
		
		if (!(task instanceof TemplateSelectQuestionTask)) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_acceptSomethingWentWrong"));
			return false;
		}
		
		/* We have a question, and the answer was "Accepted" so notify the task. */
		synchronized(task) {
			task.setResponse(args[0]);
			task.notifyAll();
		}
				
		return true;
	}

}
