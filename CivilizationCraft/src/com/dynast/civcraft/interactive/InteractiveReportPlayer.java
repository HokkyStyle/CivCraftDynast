package com.dynast.civcraft.interactive;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;
import com.dynast.global.reports.ReportManager;
import com.dynast.global.reports.ReportManager.ReportType;

public class InteractiveReportPlayer implements InteractiveResponse {

	String playerName;
	
	public InteractiveReportPlayer(String playerName) {
		this.playerName = playerName;
	}
	
	@Override
	public void respond(String message, Resident resident) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}

		if (message.equalsIgnoreCase("cancel")) {
			CivMessage.send(player, CivColor.LightGreen+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_report_cancel"));
			resident.clearInteractiveMode();
			return;
		}
		
		ReportType selectedType = null;
		for (ReportType type : ReportManager.ReportType.values()) {
			if (message.equalsIgnoreCase(type.name())) {
				selectedType = type;
				break;
			}
		}
		
		if (selectedType == null) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_report_category")+" ("+ReportManager.getReportTypes()+")");
			return;
		}
		
		CivMessage.send(player, CivColor.Yellow+ChatColor.BOLD+CivSettings.localize.localizedString("interactive_report_description"));
		resident.setInteractiveMode(new InteractiveReportPlayerMessage(playerName, selectedType));		
		
	}

}
