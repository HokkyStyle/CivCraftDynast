
package com.dynast.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map.Entry;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.Barracks;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.util.BlockCoord;

public class UnitTrainTimer implements Runnable {

	@Override
	public void run() {
		
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			Structure struct = iter.next().getValue();
			if (struct instanceof Barracks) {
				((Barracks)struct).updateTraining();
			}
		}
	}

}
