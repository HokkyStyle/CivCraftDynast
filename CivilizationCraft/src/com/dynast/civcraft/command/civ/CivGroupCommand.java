
package com.dynast.civcraft.command.civ;

import org.bukkit.entity.Player;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.permission.PermissionGroup;
import com.dynast.civcraft.util.CivColor;

public class CivGroupCommand extends CommandBase {

	@Override
	public void init() {
		command = "/civ group";
		displayName = CivSettings.localize.localizedString("cmd_civ_group_name");
		
		commands.put("add", CivSettings.localize.localizedString("cmd_civ_group_addDesc"));
		commands.put("remove", CivSettings.localize.localizedString("cmd_civ_group_removeDesc"));
		commands.put("info", CivSettings.localize.localizedString("cmd_civ_group_infoDesc"));
		
	}

	public void remove_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		Resident resident = getResident();
		Resident oldMember = getNamedResident(1);
		String groupName = getNamedString(2, CivSettings.localize.localizedString("cmd_civ_group_removePrompt"));
	
		PermissionGroup grp = null;
		if (groupName.equalsIgnoreCase("leaders")) {
			grp = civ.getLeaderGroup();
		} else if (groupName.equalsIgnoreCase("advisers")) {
			grp = civ.getAdviserGroup();
		} else {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_group_removeInvalid",groupName));
		}
		
		if (grp == civ.getLeaderGroup() && !grp.hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_group_removeOnlyLeader"));
		}
		
		if (!grp.hasMember(oldMember)) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_group_removeNotInGroup",oldMember.getName()));
		}
		
		if (grp == civ.getLeaderGroup() && resident == oldMember) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_group_removeYourself"));
		}
		
		grp.removeMember(oldMember);	
		grp.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_group_removeSuccess",oldMember.getName(),groupName));	
		try {
			Player newPlayer = CivGlobal.getPlayer(oldMember);
			CivMessage.send(newPlayer, CivColor.Rose+CivSettings.localize.localizedString("var_cmd_civ_group_removeNotify1",groupName,civ.getName()));
		} catch (CivException e) {
			/* player not online. forget the exception*/
		}
	}
	
	public void add_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		Resident resident = getResident();
		Resident newMember = getNamedResident(1);
		String groupName = getNamedString(2, CivSettings.localize.localizedString("cmd_civ_group_removePrompt"));
		
		PermissionGroup grp = null;
		if (groupName.equalsIgnoreCase("leaders")) {
			grp = civ.getLeaderGroup();
		} else if (groupName.equalsIgnoreCase("advisers")) {
			grp = civ.getAdviserGroup();
		} else {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_group_removeInvalid",groupName));
		}
		
		if (grp == civ.getLeaderGroup() && !grp.hasMember(resident)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_group_addOnlyLeader"));
		}
		
		if (newMember.getCiv() != civ) {
			throw new CivException(CivSettings.localize.localizedString("cmd_civ_group_addNotInCiv"));
		}
		
		grp.addMember(newMember);
		grp.save();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_civ_group_addSuccess",newMember.getName(),groupName));

		try {
			Player newPlayer = CivGlobal.getPlayer(newMember);
			CivMessage.sendSuccess(newPlayer, CivSettings.localize.localizedString("var_cmd_civ_group_addNotify",groupName,civ.getName()));
		} catch (CivException e) {
			/* player not online. forget the exception*/
		}
	}
	
	
	public void info_cmd() throws CivException {
		Civilization civ = getSenderCiv();
		
		if (args.length > 1) {
			PermissionGroup grp = null;
			if (args[1].equalsIgnoreCase("leaders")) {
				grp = civ.getLeaderGroup();
			} else if (args[1].equalsIgnoreCase("advisers")) {
				grp = civ.getAdviserGroup();
			} else {
				throw new CivException(CivSettings.localize.localizedString("var_cmd_civ_group_removeInvalid",args[1]));
			}
			
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_group_listGroup")+" "+args[1]);
			
			String residents = "";
			for (Resident res : grp.getMemberList()) {
				residents += res.getName() + " ";
			}
			CivMessage.send(sender, residents);
			
		} else {
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_civ_group_listHeading"));

			PermissionGroup grp = civ.getLeaderGroup();
			CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_group_listGroup",grp.getName()+CivColor.LightGray,grp.getMemberCount()));
					
			grp = civ.getAdviserGroup();
			CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_civ_group_listGroup",grp.getName()+CivColor.LightGray,grp.getMemberCount()));
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
		this.validLeaderAdvisor();
	}

}
