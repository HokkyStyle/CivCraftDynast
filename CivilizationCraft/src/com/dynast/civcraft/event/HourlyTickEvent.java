
package com.dynast.civcraft.event;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.dynast.civcraft.camp.CampHourlyTick;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.CultureProcessAsyncTask;
import com.dynast.civcraft.threading.timers.EffectEventTimer;
import com.dynast.civcraft.threading.timers.SyncTradeTimer;

public class HourlyTickEvent implements EventInterface {

	@Override
	public void process() {
		CivLog.info("TimerEvent: Hourly -------------------------------------");
		TaskMaster.asyncTask("cultureProcess", new CultureProcessAsyncTask(), 0);
		TaskMaster.asyncTask("EffectEventTimer", new EffectEventTimer(), 0);
		TaskMaster.syncTask(new SyncTradeTimer(), 0);
		TaskMaster.syncTask(new CampHourlyTick(), 0);
		CivLog.info("TimerEvent: Hourly Finished -----------------------------");
	}

	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
		Calendar cal = EventTimer.getCalendarInServerTimeZone();

		int hourly_peroid = CivSettings.getInteger(CivSettings.civConfig, "global.hourly_tick");
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.add(Calendar.SECOND, hourly_peroid);
		sdf.setTimeZone(cal.getTimeZone());
		return cal;
	}

}
