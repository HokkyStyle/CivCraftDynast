
package com.dynast.civcraft.command.resident;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class ResidentFriendCommand extends CommandBase {

	@Override
	public void init() {
		command = "/resident friend";
		displayName = CivSettings.localize.localizedString("cmd_res_friend_name");
		
		commands.put("add", CivSettings.localize.localizedString("cmd_res_friend_addDesc"));
		commands.put("remove", CivSettings.localize.localizedString("cmd_res_friend_removeDesc"));
		commands.put("list", CivSettings.localize.localizedString("cmd_res_friend_listDesc"));
	}
	
	public void add_cmd() throws CivException {
		Resident resident = getResident();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_res_friend_addPrompt"));
		}
		
		Resident friendToAdd = getNamedResident(1);
		
		resident.addFriend(friendToAdd);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_res_friend_addSuccess",args[1]));	
		resident.save();
	}
	
	public void remove_cmd() throws CivException {
	Resident resident = getResident();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_res_friend_removePrompt"));
		}
		
		Resident friendToRemove = getNamedResident(1);
		
		resident.removeFriend(friendToRemove);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_res_friend_removeSuccess",args[1]));	
		resident.save();
	}
	
	public void list_cmd() throws CivException {
		Resident resident = getResident();
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("var_cmd_res_friend_listHeading",resident.getName()));
		
		String out = "";
		for (String res : resident.getFriends()) {
			out += res+ ", ";
		}
		CivMessage.send(sender, out);
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
