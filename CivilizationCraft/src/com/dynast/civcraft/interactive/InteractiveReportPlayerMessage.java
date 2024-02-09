package com.dynast.civcraft.interactive;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.global.reports.ReportManager;
import com.dynast.global.reports.ReportManager.ReportType;

public class InteractiveReportPlayerMessage implements InteractiveResponse {

	ReportType type;
	String playerName;
	
	public InteractiveReportPlayerMessage(String playerName, ReportType type) {
		this.type = type;
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
		
		ReportManager.reportPlayer(playerName, type, message, resident.getName());
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_interactive_report_success",playerName));
		resident.clearInteractiveMode();
	}

}
