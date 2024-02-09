package com.dynast.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.camp.CampUpdateTick;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.structure.wonders.Wonder;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.*;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.war.War;

public class UpdateEventTimer extends CivAsyncTask {
		
	public static ReentrantLock lock = new ReentrantLock();
	
	public UpdateEventTimer() {
	}
	
	@Override
	public void run() {
		if (!lock.tryLock()) {
			return;
		}
		
		try {
			Iterator<Map.Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();

			while(iter.hasNext()) {
				Structure struct = iter.next().getValue();
				
				if (!struct.isActive())
					continue;
				
				try {
					if (struct.getUpdateEvent() != null && !struct.getUpdateEvent().equals("")) {
						if (struct.getUpdateEvent().equals("trommel_process")) {
							if (!CivGlobal.trommelsEnabled) {
								continue;
							}
							
							TaskMaster.asyncTask("trommel-"+struct.getCorner().toString(), new TrommelAsyncTask(struct), 0);
						}
					}
					struct.onUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for (Wonder wonder : CivGlobal.getWonders()) {
				wonder.onUpdate();
			}

			for (Camp camp : CivGlobal.getCamps()) {
				if (!camp.sifterLock.isLocked()) {
					TaskMaster.asyncTask(new CampUpdateTick(camp), 0);
				}
			}

			if (War.isWarTime()) {
				TaskMaster.syncTask(new ScoreboardTask());
			}

		} finally {
			lock.unlock();
		}

	}

}
