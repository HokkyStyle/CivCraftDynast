package com.dynast.civcraft.event;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.main.CivNPC;

import java.util.Calendar;

public class SpawnBossEvent implements EventInterface {

    @Override
    public void process() {
        CivLog.info("TimerEvent: SpawnBossEvent -------------------------------------");

        try {
            CivNPC.spawnBossWither();
            CivNPC.spawnBossDragon();
        } catch (CivException e) {
           CivLog.warning("SpawnBossEvent canceled.");
        } finally {
            CivMessage.globalTitle("Боссы возвращаются!", "");
        }
    }

    @Override
    public Calendar getNextDate() {
        Calendar cal = EventTimer.getCalendarInServerTimeZone();
        if (cal.get(Calendar.HOUR_OF_DAY) < 4) {
            cal.set(Calendar.HOUR_OF_DAY, 4);
        } else if (cal.get(Calendar.HOUR_OF_DAY) < 12) {
            cal.set(Calendar.HOUR_OF_DAY, 12);
        } else if (cal.get(Calendar.HOUR_OF_DAY) < 20) {
            cal.set(Calendar.HOUR_OF_DAY, 20);
        } else if (cal.get(Calendar.HOUR_OF_DAY) >= 20){
            cal.add(Calendar.DAY_OF_WEEK, 1);
            cal.set(Calendar.HOUR_OF_DAY, 4);
        }

        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        Calendar now = Calendar.getInstance();
        if (now.after(cal)) {
            cal.add(Calendar.HOUR_OF_DAY, 8);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
        }

        return cal;
    }
}
