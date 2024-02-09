
package com.dynast.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.camp.CampUpdateTick;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.structure.wonders.Wonder;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FisheryAsyncTask;
import com.dynast.civcraft.threading.tasks.MobGrinderAsyncTask;
import com.dynast.civcraft.threading.tasks.QuarryAsyncTask;
import com.dynast.civcraft.util.BlockCoord;

public class UpdateMinuteEventTimer extends CivAsyncTask {
		
	public static ReentrantLock lock = new ReentrantLock();
	
	public UpdateMinuteEventTimer() {
	}
	
	@Override
	public void run() {		

		if (!lock.tryLock()) {
			return;
		}
		
		try {
			// Loop through each structure, if it has an update function call it in another async process
			Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
	
			while(iter.hasNext()) {
				Structure struct = iter.next().getValue();
				
				if (!struct.isActive())
					continue;
				
				try {
					if (struct.getUpdateEvent() != null && !struct.getUpdateEvent().equals("")) {
						if (struct.getUpdateEvent().equals("mobGrinder_process")) {
							if (!CivGlobal.mobGrinderEnabled) {
								continue;
							}
							
							TaskMaster.asyncTask("mobGrinder-"+struct.getCorner().toString(), new MobGrinderAsyncTask(struct), 0);
						} else if (struct.getUpdateEvent().equals("fish_hatchery_process")) {
							if (!CivGlobal.fisheryEnabled) {
								continue;
							}
							
							TaskMaster.asyncTask("fishHatchery-"+struct.getCorner().toString(), new FisheryAsyncTask(struct), 0);
						}  else if (struct.getUpdateEvent().equals("quarry_process")) {
							if (!CivGlobal.quarriesEnabled) {
								continue;
							}

							TaskMaster.asyncTask("quarry-"+struct.getCorner().toString(), new QuarryAsyncTask(struct), 0);
						}
					}
					
					struct.onUpdate();
				} catch (Exception e) {
					e.printStackTrace();
					//We need to catch any exception so that an error in one town/structure/good does not
					//break things for everybody.
					//TODO log exception into a file or something...
	//				if (struct.getTown() == null) {
	//					RJ.logException("TownUnknown struct:"+struct.config.displayName, e);
	//				} else {
	//					RJ.logException(struct.town.getName()+":"+struct.config.displayName, e);
	//				}
				}
			}
		
		} finally {
			lock.unlock();
		}

	}

}
