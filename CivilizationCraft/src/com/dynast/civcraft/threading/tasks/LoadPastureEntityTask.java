package com.dynast.civcraft.threading.tasks;

import java.util.Queue;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.structure.Pasture;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.EntityUtil;
import com.dynast.civcraft.util.TimeTools;

public class LoadPastureEntityTask implements Runnable {

	public Queue<SessionEntry> entriesToLoad;
	public Pasture pasture;
		
	public LoadPastureEntityTask(Queue<SessionEntry> entriesToLoad, Pasture pasture) {
		this.entriesToLoad = entriesToLoad;
		this.pasture = pasture;
	}
	
	@Override
	public void run() {
		int max = pasture.getMobMax();
		
		if (pasture.lock.tryLock()) {
			CivLog.info("Started Pasture Entity Load Task...");
			try {
				for (int i = 0; i < max; i++) {
					SessionEntry entry = entriesToLoad.poll();
					if (entry == null) {
						break;
					}
					
					String[] split = entry.value.split(":");
					Entity entity = EntityUtil.getEntity(Bukkit.getWorld(split[0]), UUID.fromString(split[1]));
					
					if (entity != null) {			
						pasture.entities.add(entity.getUniqueId());
						Pasture.pastureEntities.put(entity.getUniqueId(), pasture);
					} else {
						CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
					}
				}
			} finally {
				pasture.lock.unlock();
			}
		} else {
			/* try again in 5 seconds. */
			CivLog.warning("Couldn't obtain pasture lock, trying again in 5 seconds.");
			TaskMaster.syncTask(this, TimeTools.toTicks(5));
		}
		
		/* Everything else is beyond our max, lets just forget about them. */
		SessionEntry entry = entriesToLoad.poll();
		while (entry != null) {
			CivGlobal.getSessionDB().delete(entry.request_id, entry.key);
			entry = entriesToLoad.poll();
		}
		CivLog.info("...Finished Pasture Entity Load Task");
	}
}
