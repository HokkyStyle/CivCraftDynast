
package com.dynast.civcraft.threading.timers;

import java.util.Date;

import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.TimeTools;
import com.dynast.civcraft.war.War;

public class WarEndCheckTask implements Runnable {

	@Override
	public void run() {

		Date now = new Date();
		if (War.isWarTime()) {
			if (War.getEnd() == null || now.after(War.getEnd())) {
				War.setWarTime(false);
			} else {
				TaskMaster.syncTask(this, TimeTools.toTicks(1));
			}
		}		
	}

}
