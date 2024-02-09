
package com.dynast.civcraft.util;

import java.util.Calendar;
import java.util.Date;

public class TimeTools {

	public static long toTicks(long seconds) {
		return 20*seconds;
	}

	public static long getTicksUnitl(Date next) {
		Calendar c = Calendar.getInstance();
		Date now = c.getTime();
		
		long seconds = Math.abs((now.getTime() - next.getTime())/1000);
		
		return seconds*20;
	}
	
}
