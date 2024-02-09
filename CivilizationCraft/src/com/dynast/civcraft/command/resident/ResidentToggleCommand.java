
package com.dynast.civcraft.command.resident;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class ResidentToggleCommand extends CommandBase {

	@Override
	public void init() {
		command = "/resident toggle";
		displayName = CivSettings.localize.localizedString("cmd_res_toggle_name");	
		
		commands.put("map", CivSettings.localize.localizedString("cmd_res_toggle_mapDesc"));
		commands.put("info", CivSettings.localize.localizedString("cmd_res_toggle_infoDesc"));
		commands.put("showtown", CivSettings.localize.localizedString("cmd_res_toggle_showtownDesc"));
		commands.put("showciv", CivSettings.localize.localizedString("cmd_res_toggle_showcivDesc"));
		commands.put("showscout", CivSettings.localize.localizedString("cmd_res_toggle_showscoutDesc"));
		commands.put("combatinfo", CivSettings.localize.localizedString("cmd_res_toggle_combatinfoDesc"));
		commands.put("itemdrops", CivSettings.localize.localizedString("cmd_res_toggle_itemdropsDesc"));
		commands.put("titles", CivSettings.localize.localizedString("cmd_res_toggle_titleAPIDesc"));
		commands.put("showexp", CivSettings.localize.localizedString("cmd_res_toggle_showExp"));
		
	}
	public void itemdrops_cmd() throws CivException {
		toggle();
	}
	
	public void map_cmd() throws CivException {
		toggle();
	}
	public void showtown_cmd() throws CivException {
		toggle();
	}
	
	public void showciv_cmd() throws CivException  {
		toggle();
	}
	
	public void showscout_cmd() throws CivException  {
		toggle();
	}
	
	public void info_cmd() throws CivException {
		toggle();
	}
	
	public void combatinfo_cmd() throws CivException {
		toggle();
	}
	
	public void titles_cmd() throws CivException {
		toggle();
	}

	public void showexp_cmd() throws CivException {
		toggle();
	}
	
	private void toggle() throws CivException {
		Resident resident = getResident();
	
		boolean result;
		switch(args[0].toLowerCase()) {
		case "map":
			resident.setShowMap(!resident.isShowMap());
			result = resident.isShowMap();
			break;
		case "showtown":
			resident.setShowTown(!resident.isShowTown());
			result = resident.isShowTown();
			break;
		case "showciv":
			resident.setShowCiv(!resident.isShowCiv());
			result = resident.isShowCiv();
			break;
		case "showscout":
			resident.setShowScout(!resident.isShowScout());
			result = resident.isShowScout();
			break;
		case "info":
			resident.setShowInfo(!resident.isShowInfo());
			result = resident.isShowInfo();
			break;
		case "combatinfo":
			resident.setCombatInfo(!resident.isCombatInfo());
			result = resident.isCombatInfo();
			break;
		case "titles":
			resident.setTitleAPI(!resident.isTitleAPI());
			result = resident.isTitleAPI();
			break;
		case "itemdrops":
			resident.toggleItemMode();
			return;
		case "showexp":
			resident.setShowExp(!resident.isShowExp());
			result = resident.isShowExp();
			break;
		default:
			throw new CivException(CivSettings.localize.localizedString("cmd_unkownFlag")+" "+args[0]);
		}
		
		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_toggled")+" "+args[0]+" -> "+result);
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
