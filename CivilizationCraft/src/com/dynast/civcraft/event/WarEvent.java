package com.dynast.civcraft.event;

import java.util.Calendar;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.timers.WarEndCheckTask;
import com.dynast.civcraft.util.TimeTools;
import com.dynast.civcraft.war.War;

public class WarEvent implements EventInterface {

	@Override
	public void process() {
		CivLog.info("TimerEvent: WarEvent -------------------------------------");

		try {
			War.setWarTime(true);
		} catch (Exception e) {
			CivLog.error("WarStartException:"+e.getMessage());
			e.printStackTrace();
		}
		
		// Start repeating task waiting for war time to end.
		TaskMaster.syncTask(new WarEndCheckTask(), TimeTools.toTicks(1));
	}

	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		int war;
		
		int dayOfWeek = CivSettings.getInteger(CivSettings.warConfig, "war.time_day");
		int hourOfWar = CivSettings.getInteger(CivSettings.warConfig, "war.time_hour");

		if (cal.get(Calendar.DAY_OF_WEEK) <= 4) {
			cal.set(Calendar.DAY_OF_WEEK, 4);
			cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			war = 1;
		} else {
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			war = 2;
		}
		
		Calendar now = Calendar.getInstance();
		if (now.after(cal)) {
			if (war == 1) {
				cal.add(Calendar.DAY_OF_MONTH, 3);
				cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
				cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.add(Calendar.DAY_OF_MONTH, 4);
				cal.set(Calendar.DAY_OF_WEEK, 4);
				cal.set(Calendar.HOUR_OF_DAY, hourOfWar);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
		}
		
		return cal;
	}

}
