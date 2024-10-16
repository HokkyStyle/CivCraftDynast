
package com.dynast.civcraft.command.civ;

import java.util.ArrayList;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigGovernment;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.util.CivColor;

public class CivGovCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ gov";
		displayName = CivSettings.localize.localizedString("cmd_civ_gov_name");
		
		commands.put("info", CivSettings.localize.localizedString("cmd_civ_gov_infoDesc"));
		commands.put("change", CivSettings.localize.localizedString("cmd_civ_gov_changeDesc"));
		commands.put("list", CivSettings.localize.localizedString("cmd_civ_gov_listDesc"));
	}
	
	public void change_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_gov_changePrompt"));
		}
		
		ConfigGovernment gov = ConfigGovernment.getGovernmentFromName(args[1]);
		if (gov == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_gov_changeInvalid")+" "+args[1]);
		}
		
		if (!gov.isAvailable(civ)) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_gov_changeNotHere",gov.displayName));
		}
		
		civ.changeGovernment(civ, gov, false);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_civ_gov_changeSuccess"));
	}
	
	public void list_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_gov_listHeading"));
		ArrayList<ConfigGovernment> govs = ConfigGovernment.getAvailableGovernments(civ);
		
		for (ConfigGovernment gov : govs) {
			if (gov == civ.getGovernment()) {
				CivMessage.send(sender, CivColor.Gold+gov.displayName+" "+"("+CivSettings.localize.localizedString("currentGovernment")+")");
			} else {
				CivMessage.send(sender, CivColor.Green+gov.displayName);
			}
		}
		
	}
	
	public void info_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_gov_infoHading")+" "+civ.getGovernment().displayName);
		CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("cmd_civ_gov_infoTrade")+" "+CivColor.LightGreen+civ.getGovernment().trade_rate+
				CivColor.Green+" "+CivSettings.localize.localizedString("cmd_civ_gov_infoCottage")+" "+CivColor.LightGreen+civ.getGovernment().cottage_rate);
		CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("cmd_civ_gov_infoUpkeep")+" "+CivColor.LightGreen+civ.getGovernment().upkeep_rate+
				CivColor.Green+" "+CivSettings.localize.localizedString("cmd_civ_gov_infoGrowth")+" "+CivColor.LightGreen+civ.getGovernment().growth_rate);
		CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("cmd_civ_gov_infoHammer")+" "+CivColor.LightGreen+civ.getGovernment().hammer_rate+
				CivColor.Green+" "+CivSettings.localize.localizedString("cmd_civ_gov_infoBeaker")+" "+CivColor.LightGreen+civ.getGovernment().beaker_rate);
		CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("cmd_civ_gov_infoCulture")+" "+CivColor.LightGreen+civ.getGovernment().culture_rate+
				CivColor.Green+" "+CivSettings.localize.localizedString("cmd_civ_gov_infoMaxTax")+" "+CivColor.LightGreen+civ.getGovernment().maximum_tax_rate);
				
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
		validLeader();
	}

}
