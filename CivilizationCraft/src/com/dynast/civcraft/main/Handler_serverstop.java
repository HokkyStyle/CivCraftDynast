package com.dynast.civcraft.main;

import java.util.Iterator;
import java.util.Map.Entry;

import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.util.BlockCoord;

public class Handler_serverstop extends Thread {

	public void run() {
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		while(iter.hasNext()) {
			Structure struct = iter.next().getValue();	
			struct.onUnload();
		}
	}
	
}
