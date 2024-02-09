
package com.dynast.civcraft.command.admin;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;

public class AdminCampCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad camp";
		displayName = CivSettings.localize.localizedString("adcmd_camp_name");	
		
		commands.put("destroy", CivSettings.localize.localizedString("adcmd_camp_destroyDesc"));
		commands.put("setraidtime", CivSettings.localize.localizedString("adcmd_camp_setRaidTimeDesck"));
		commands.put("rebuild", CivSettings.localize.localizedString("adcmd_camp_rebuildDesc"));
	}
	
	public void rebuild_cmd() throws CivException {
		Camp camp = this.getNamedCamp(1);
		
		try {
			camp.repairFromTemplate();
		} catch (IOException e) {
		} catch (CivException e) {
			e.printStackTrace();
		}
		camp.reprocessCommandSigns();
		CivMessage.send(sender, CivSettings.localize.localizedString("Repaired"));
	}
	
	public void setraidtime_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		
		if (!resident.hasCamp()) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_camp_setRaidTimeNoCamp"));
		}
		
		if (args.length < 3) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_camp_setRaidTimeInvlidInput"));
		}
		
		Camp camp = resident.getCamp();
		
		String dateStr = args[2];
		SimpleDateFormat parser = new SimpleDateFormat("d:M:y:H:m");
		
		Date next;
		try {
			next = parser.parse(dateStr);
			camp.setNextRaidDate(next);
			CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_camp_setRaidTimeSuccess"));
		} catch (ParseException e) {
			throw new CivException(CivSettings.localize.localizedString("var_adcmd_camp_setRaidTimeFailedFormat",args[2]));
		}
		
	}
	
	public void destroy_cmd() throws CivException {
		Camp camp = getNamedCamp(1);		
		camp.destroy();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_camp_destroyedSuccess"));
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
