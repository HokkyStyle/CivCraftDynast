
package com.dynast.civcraft.event;

import java.util.Calendar;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.items.BonusGoodie;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.threading.TaskMaster;

public class GoodieRepoEvent implements EventInterface {

	public static void repoProcess() {
		class SyncTask implements Runnable {
			@Override
			public void run() {
				
				for (Town town : CivGlobal.getTowns()) {
					for (BonusGoodie goodie : town.getBonusGoodies()) {
						town.removeGoodie(goodie);
					}
				}
				
				for (BonusGoodie goodie : CivGlobal.getBonusGoodies()) {
					try {
						goodie.replenish();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
		TaskMaster.syncTask(new SyncTask());
	}
	
	@Override
	public void process() {
		CivLog.info("TimerEvent: GoodieRepo -------------------------------------");
		repoProcess();
		CivMessage.globalTitle(CivSettings.localize.localizedString("goodieRepoBroadcastTitle"),"");
		CivMessage.global(CivSettings.localize.localizedString("goodieRepoBroadcast"));
	}

	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		Calendar cal = EventTimer.getCalendarInServerTimeZone();
		int repo_day = CivSettings.getInteger(CivSettings.goodsConfig, "trade_goodie_repo_day");
		int repo_hour = CivSettings.getInteger(CivSettings.goodsConfig, "trade_goodie_repo_hour");
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, repo_hour);
		cal.add(Calendar.DATE, repo_day);
		return cal;
	}

}
