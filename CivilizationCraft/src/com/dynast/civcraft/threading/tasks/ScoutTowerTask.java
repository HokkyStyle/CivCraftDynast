
package com.dynast.civcraft.threading.tasks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.ScoutShip;
import com.dynast.civcraft.structure.ScoutTower;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.util.BlockCoord;

public class ScoutTowerTask implements Runnable {
	@Override
	public void run() {		
		HashSet<String> announced = new HashSet<>();
		
		try {
			if (!CivGlobal.towersEnabled) {
				return;
			}
			
			Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
			while(iter.hasNext()) {
				Structure struct = iter.next().getValue();
				if (struct instanceof ScoutTower) {
					((ScoutTower)struct).process(announced);
				} else if (struct instanceof ScoutShip) {
					((ScoutShip)struct).process(announced);
				}

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
