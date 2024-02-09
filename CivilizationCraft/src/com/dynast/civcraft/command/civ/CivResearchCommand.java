
package com.dynast.civcraft.command.civ;

import java.util.ArrayList;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigTech;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.util.CivColor;

public class CivResearchCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ research";
		displayName = CivSettings.localize.localizedString("cmd_civ_research_name");
		
		commands.put("list", CivSettings.localize.localizedString("cmd_civ_research_listDesc"));
		commands.put("progress", CivSettings.localize.localizedString("cmd_civ_research_progressDesc"));
		commands.put("on", CivSettings.localize.localizedString("cmd_civ_research_onDesc"));
		commands.put("turn", CivSettings.localize.localizedString("cmd_civ_research_turnDesc"));
		commands.put("turnremove", CivSettings.localize.localizedString("cmd_civ_research_turnremoveDesc"));
		commands.put("onturn", CivSettings.localize.localizedString("cmd_civ_research_onturnDesc"));
		commands.put("change", CivSettings.localize.localizedString("cmd_civ_research_changeDesc"));
		commands.put("finished", CivSettings.localize.localizedString("cmd_civ_research_finishedDesc"));
		commands.put("era", CivSettings.localize.localizedString("cmd_civ_research_eraDesc"));
	}
	
	public void change_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		double cost = civ.getResearchTech().getAdjustedTechCost(civ);
		
		if (args.length < 2) {
			list_cmd();
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_changePrompt"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotFound",techname));
		}
		
		if (!civ.getTreasury().hasEnough(tech.getAdjustedTechCost(civ))) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotEnough1",CivSettings.CURRENCY_NAME,tech.name));
		}
		
		if(!tech.isAvailable(civ)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_NotAllowedNow"));
		}
		
		if (civ.getResearchTech() != null) {
			civ.setResearchProgress(0);
			CivMessage.send(sender, CivColor.Rose+CivSettings.localize.localizedString("var_cmd_civ_research_lostProgress1",civ.getResearchTech().name));
			civ.setResearchTech(null);
		}
				
		civ.getTreasury().deposit(cost/2);
		civ.startTechnologyResearch(tech);
		CivMessage.sendCiv(civ, CivSettings.localize.localizedString("var_cmd_civ_research_start",tech.name));
	}
	
	public void finished_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_finishedHeading"));
		String out = "";
		for (ConfigTech tech : civ.getTechs()) {
			out += tech.name+", ";
		}
		CivMessage.send(sender, out);
	}
	
	public void turnremove_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (civ.getTurnedResearchTech() != null); {
			civ.setTurnedResearchTech(null);
			CivMessage.send(sender, CivColor.LightGreen+CivSettings.localize.localizedString("var_cmd_civ_research_removeturned"));
		}
		throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_missingTurnedTech"));
     			
	}

	public void on_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_onPrompt"));
		}
		
		Town capitol = CivGlobal.getTown(civ.getCapitolName());
		if (capitol == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_missingCapitol",civ.getCapitolName())+" "+CivSettings.localize.localizedString("internalCommandException"));
		}
	
		TownHall townhall = capitol.getTownHall();
		if (townhall == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_missingTownHall"));
		}
		
		if (!townhall.isActive()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_incompleteTownHall"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotFound",techname));
		}
		
		civ.startTechnologyResearch(tech);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_research_start",tech.name));
	}
	
	public void turn_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_turnPrompt"));
		}
		
		Town capitol = CivGlobal.getTown(civ.getCapitolName());
		if (capitol == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_missingCapitol",civ.getCapitolName())+" "+CivSettings.localize.localizedString("internalCommandException"));
		}
		
		TownHall townhall = capitol.getTownHall();
		if (townhall == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_missingTownHall"));
		}
		
		if (!townhall.isActive()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_incompleteTownHall"));
		}
		
		String techname = combineArgs(stripArgs(args, 1));
		ConfigTech tech = CivSettings.getTechByName(techname);
		if (tech == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_research_NotFound",techname));
		}
		
		if (civ.hasTech(tech.id)) {
			throw new CivException(CivSettings.localize.localizedString("civ_research_alreadyDone"));
		}
		
		civ.setTurnedResearchTech(tech);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_research_turned",tech.name));
	}
	
	public void progress_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_current"));
		
		if (civ.getResearchTech() != null) {
			int percentageComplete = (int)((civ.getResearchProgress() / civ.getResearchTech().getAdjustedBeakerCost(civ))*100);
			CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_research_current",civ.getResearchTech().name,percentageComplete,(civ.getResearchProgress()+" / "+civ.getResearchTech().getAdjustedBeakerCost(civ))));
		} else {
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_research_NotAnything"));
		}
		
	}
	
	public void onturn_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_onturnHeading"));
		
		if (civ.getTurnedResearchTech() != null) {
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_research_onturn",civ.getTurnedResearchTech().name));
		} else {
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_civ_research_onturnanything"));
		}
	}
	
	public void list_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		ArrayList<ConfigTech> techs = ConfigTech.getAvailableTechs(civ);
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_Available"));
		for (ConfigTech tech : techs) {
			CivMessage.send(sender, tech.name+CivColor.LightGray+" "+CivSettings.localize.localizedString("Cost")+" "+
					CivColor.Yellow+tech.getAdjustedTechCost(civ)+CivColor.LightGray+" "+CivSettings.localize.localizedString("Beakers")+" "+
					CivColor.Yellow+tech.getAdjustedBeakerCost(civ));
		}
				
	}
	
	public void era_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_research_era"));
		CivMessage.send(sender, CivColor.White+CivSettings.localize.localizedString("var_cmd_civ_research_currentEra", CivColor.LightBlue+CivGlobal.localizedEraString(civ.getCurrentEra())));
		CivMessage.send(sender, CivColor.White+CivSettings.localize.localizedString("var_cmd_civ_research_highestEra", CivColor.LightBlue+CivGlobal.localizedEraString(CivGlobal.highestCivEra)));
		
		double eraRate = ConfigTech.eraRate(civ);
		if (eraRate == 0.0) {
			CivMessage.send(sender, CivColor.Yellow+CivSettings.localize.localizedString("cmd_civ_research_eraNoDiscount"));
		} else {
			CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("var_cmd_civ_research_eraDiscount",(eraRate*100),CivSettings.CURRENCY_NAME));
			
		}
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
		Resident resident = getResident();
		Civilization civ = getSenderCiv();
		
		if (!civ.getLeaderGroup().hasMember(resident) && !civ.getAdviserGroup().hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_research_notLeader"));
		}		
	}

}
