
package com.dynast.civcraft.structure.farm;

import java.util.LinkedList;

import com.dynast.civcraft.main.CivGlobal;


public class FarmCachePopulateTask implements Runnable {

	LinkedList<FarmChunk> farms;
	
	public FarmCachePopulateTask(LinkedList<FarmChunk> farms) {
		this.farms = farms;
	}
	
	@Override
	public void run() {
		if (!CivGlobal.growthEnabled) {
			return;
		}
		
		for (FarmChunk fc : farms) {
			try {
				fc.populateCropLocationCache();
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	}

}
