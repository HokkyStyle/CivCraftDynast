package com.dynast.civcraft.command.civ;

import java.util.ArrayList;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigPublicInstitution;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.util.CivColor;

public class CivInstituteCommand extends CommandBase {
	
	public void init() {
		command = "/civ institute";
		displayName = CivSettings.localize.localizedString("cmd_civ_institute_name");
		
		commands.put("list", CivSettings.localize.localizedString("cmd_civ_institute_listDesc"));
		commands.put("select", CivSettings.localize.localizedString("cmd_civ_institute_selectDesc"));
		commands.put("info", CivSettings.localize.localizedString("cmd_civ_institute_infoDesc"));
		commands.put("points", CivSettings.localize.localizedString("cmd_civ_institute_pointsDesc"));
	}
	
	public void list_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		ArrayList<ConfigPublicInstitution> insts = ConfigPublicInstitution.getAvailableInstitutes(civ);
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_institute_Available"));
		for (ConfigPublicInstitution inst : insts) {
			CivMessage.send(sender, inst.displayName+CivColor.LightGray+" Описание:"+inst.displayInfo);
		}		
	}
	
	public void points_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		CivMessage.send(sender, "" + civ.points);
	}
	
	public void select_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_institute_selectPrompt"));
		}
		
		Town capitol = CivGlobal.getTown(civ.getCapitolName());
		TownHall townhall = capitol.getTownHall();
		if (!townhall.isActive()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_institute_incompleteTownHall"));
		}
		
		String instname = combineArgs(stripArgs(args, 1));
		ConfigPublicInstitution inst = CivSettings.getInstByName(instname);
		if (inst == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_institute_NotFound",instname));
		}
		
		civ.giveInstitution(inst);
	}
	
	public void info_cmd() throws CivException {
        Civilization civ = getSenderCiv();
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_institute_givedHeading"));
		//String out = "";
		for (ConfigPublicInstitution inst : civ.getInstitutions()) {
			CivMessage.send(sender, CivColor.White+inst.displayName+CivColor.LightGray+" ��������:"+inst.displayInfo);
		}
		//CivMessage.send(sender, out);
	}
	
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
		
		if (!civ.getLeaderGroup().hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_institute_notLeader"));
		}		
	}

}
