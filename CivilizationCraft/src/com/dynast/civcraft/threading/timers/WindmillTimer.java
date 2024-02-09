package com.dynast.civcraft.threading.timers;

import java.util.Iterator;
import java.util.Map.Entry;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.structure.Windmill;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.war.War;

public class WindmillTimer implements Runnable {

	@Override
	public void run() {
		if (War.isWarTime()) {
			return;
		}
		
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			Structure struct = iter.next().getValue();
			if (struct instanceof Windmill) {
				((Windmill)struct).processWindmill();
			}
		}
	}

}
