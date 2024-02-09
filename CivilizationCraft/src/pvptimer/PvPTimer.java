package pvptimer;

import java.util.Date;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.DateUtil;

public class PvPTimer implements Runnable {

	@Override
	public void run() {
		
		for (Resident resident : CivGlobal.getResidents()) {
			if (!resident.isProtected()) {
				continue;
			}
			
			int mins;
			try {
				mins = CivSettings.getInteger(CivSettings.civConfig, "global.pvp_timer");
				if (DateUtil.isAfterMins(new Date(resident.getRegistered()), mins)) {
				//if (DateUtil.isAfterSeconds(new Date(resident.getRegistered()), mins)) {
					resident.setisProtected(false);
					CivMessage.send(resident, CivColor.LightGray+CivSettings.localize.localizedString("pvpTimerEnded"));
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
