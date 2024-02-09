package com.dynast.civcraft.command;

import org.bukkit.ChatColor;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.interactive.InteractiveReportPlayer;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;
import com.dynast.global.reports.ReportManager;

public class ReportCommand extends CommandBase {

	@Override
	public void init() {
		command = "/report";
		displayName = CivSettings.localize.localizedString("cmd_reprot_Name");
		
		commands.put("player", CivSettings.localize.localizedString("cmd_report_playerDesc"));
	}

	public void player_cmd() throws CivException {
		Resident resident = getResident();
		Resident reportedResident = getNamedResident(1);
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_report_Heading"));
		CivMessage.send(sender, CivColor.Yellow+ChatColor.BOLD+CivSettings.localize.localizedString("cmd_report_1")+" "+reportedResident.getName());
		CivMessage.send(sender, " ");
		CivMessage.send(sender, CivColor.Yellow+ChatColor.BOLD+CivSettings.localize.localizedString("cmd_report_2")+" "+CivColor.LightGreen+ChatColor.BOLD+ReportManager.getReportTypes());
		CivMessage.send(sender, " ");
		CivMessage.send(sender, CivColor.Yellow+ChatColor.BOLD+ CivSettings.localize.localizedString("cmd_report_3")+
				CivSettings.localize.localizedString("cmd_report_4"));
		CivMessage.send(sender, CivColor.LightGray+ChatColor.BOLD+CivSettings.localize.localizedString("cmd_report_5"));
		resident.setInteractiveMode(new InteractiveReportPlayer(reportedResident.getName()));
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
		
	}

}
