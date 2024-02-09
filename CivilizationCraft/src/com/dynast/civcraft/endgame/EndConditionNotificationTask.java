package com.dynast.civcraft.endgame;

import java.util.ArrayList;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.util.CivColor;

public class EndConditionNotificationTask implements Runnable {

	@Override
	public void run() {
		
		for (EndGameCondition endCond : EndGameCondition.endConditions) {
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(endCond.getSessionKey());
			if (entries.size() == 0) {
				continue;
			}
			
			for (SessionEntry entry : entries) {
				Civilization civ = EndGameCondition.getCivFromSessionData(entry.value);
				if (civ != null)
				{
					Integer daysLeft = endCond.getDaysToHold() - endCond.getDaysHeldFromSessionData(entry.value);
					if (daysLeft == 0) {
						CivMessage.global(CivSettings.localize.localizedString("var_cmd_civ_info_victory",
								CivColor.LightBlue+CivColor.BOLD+civ.getName()+CivColor.White, CivColor.LightPurple+CivColor.BOLD+endCond.getVictoryName()+CivColor.White));
						break;
					} else {
						CivMessage.global(CivSettings.localize.localizedString("var_cmd_civ_info_daysTillVictoryNew",
								CivColor.LightBlue+CivColor.BOLD+civ.getName()+CivColor.White, CivColor.Yellow+CivColor.BOLD+daysLeft+CivColor.White,CivColor.LightPurple+CivColor.BOLD+endCond.getVictoryName()+CivColor.White));
					}
				}
			}
		}
		
	}

}
