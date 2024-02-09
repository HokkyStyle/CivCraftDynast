
package com.dynast.civcraft.structure.farm;

import java.util.LinkedList;
import java.util.Queue;

import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;

public class FarmGrowthRegrowTask extends CivAsyncTask {

	Queue<FarmChunk> farmsToGrow;

	
	public FarmGrowthRegrowTask(Queue<FarmChunk> farms) {
		this.farmsToGrow = farms;
	}
	
	@Override
	public void run() {
		
		Queue<FarmChunk> regrow = new LinkedList<>();
		CivLog.info("Regrowing "+farmsToGrow.size()+" farms due to locking failures.");
		
		FarmChunk fc;
		while((fc = farmsToGrow.poll()) != null) {
			if (fc.lock.tryLock()) {
				try {
					try {
						fc.processGrowth(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				} finally {
					fc.lock.unlock();
				}
			} else {
				regrow.add(fc);
			}
		}

		if (regrow.size() > 0) {
			TaskMaster.syncTask(new FarmGrowthRegrowTask(regrow));
		}
	}

}
