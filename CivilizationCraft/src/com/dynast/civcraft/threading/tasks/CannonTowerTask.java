
package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.main.CivGlobal;

public class CannonTowerTask implements Runnable {
	@Override
	public void run() {
		
		try {
			if (!CivGlobal.towersEnabled) {
				return;
			}
			
//			Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
//			while(iter.hasNext()) {
//				Structure struct = iter.next().getValue();
//				if (struct instanceof CannonTower) {
//					((CannonTower)struct).process();
//				}
//				
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
