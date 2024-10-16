
package com.dynast.civcraft.command.admin;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.PlayerKickBan;
import com.dynast.civcraft.war.War;

public class AdminWarCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad war";
		displayName = CivSettings.localize.localizedString("adcmd_war_name");
		
		commands.put("start", CivSettings.localize.localizedString("adcmd_war_startDesc"));
		commands.put("stop", CivSettings.localize.localizedString("adcmd_war_stopDesc"));
		commands.put("resetstart", CivSettings.localize.localizedString("adcmd_war_resetstartDesc"));
		//commands.put("setlastwar", "takes a date of the form: DAY:MONTH:YEAR:HOUR:MIN (24 hour time)");
		commands.put("onlywarriors", CivSettings.localize.localizedString("adcmd_war_onlywarriorsDesc"));
	}
	
	public void onlywarriors_cmd() {
		
		War.setOnlyWarriors(!War.isOnlyWarriors());
		
		/*if (War.isOnlyWarriors()) {
		
			for (Player player : Bukkit.getOnlinePlayers()) {
				Resident resident = CivGlobal.getResident(player);
				
				if (player.isOp() || player.hasPermission(CivSettings.MINI_ADMIN)) {
					CivMessage.send(sender, CivSettings.localize.localizedString("var_adcmd_war_onlywarriorsSkippedAdmin",player.getName()));
					continue;
				}
				
				if (resident == null || !resident.hasTown() || 
						!resident.getTown().getCiv().getDiplomacyManager().isAtWar()) {
					
					TaskMaster.syncTask(new PlayerKickBan(player.getName(), true, false, CivSettings.localize.localizedString("adcmd_war_onlywarriorsKickMessage")));
				}	
			}
			
			CivMessage.global(CivSettings.localize.localizedString("adcmd_war_onlywarriorsStart"));
		} else {
			CivMessage.global(CivSettings.localize.localizedString("adcmd_war_onlywarriorsEnd"));
		}*/
	}
	
	
//	public void setlastwar_cmd() throws CivException {
//		if (args.length < 2) {
//			throw new CivException("Enter a date like DAY:MONTH:YEAR:HOUR:MIN");
//		}
//		
//		String dateStr = args[1];
//		SimpleDateFormat parser = new SimpleDateFormat("d:M:y:H:m");
//		
//		Date lastwar;
//		try {
//			lastwar = parser.parse(dateStr);
//			War.setLastWarTime(lastwar);
//			CivMessage.sendSuccess(sender, "Set last war date");
//		} catch (ParseException e) {
//			throw new CivException("Couldnt parse "+args[1]+" into a date, use format: DAY:MONTH:YEAR:HOUR:MIN");
//		}
//		
//	}
	
	public void start_cmd() {
		
		War.setWarTime(true);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_war_startSuccess"));
	}
	
	public void stop_cmd() {
		
		War.setWarTime(false);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_war_stopSuccess"));
	}

	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		if (sender.isOp() == false) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_NotAdmin"));			
		}	
	}

}
