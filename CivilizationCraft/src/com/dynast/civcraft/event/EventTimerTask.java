
package com.dynast.civcraft.event;

import java.util.Calendar;

import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivLog;


public class EventTimerTask implements Runnable {

	@Override
	public void run() {
		
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		
		for (EventTimer timer : EventTimer.timers.values()) {
			
			if (cal.after(timer.getNext())) {
				timer.setLast(cal);

				Calendar next;
				try {
					next = timer.getEventFunction().getNextDate();
				} catch (InvalidConfiguration e) {
					e.printStackTrace();
					continue;
				}
				
				if (next == null) {
					CivLog.warning("WARNING timer:"+timer.getName()+" did not return a next time.");
					continue;
				}

				timer.setNext(next);
				timer.save();

				timer.getEventFunction().process();
			}
			
		}
		
	}
}
