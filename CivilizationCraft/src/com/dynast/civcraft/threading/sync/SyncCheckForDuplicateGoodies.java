
package com.dynast.civcraft.threading.sync;

import com.dynast.civcraft.main.CivGlobal;

public class SyncCheckForDuplicateGoodies implements Runnable {

	@Override
	public void run() {
		CivGlobal.checkForDuplicateGoodies();
	}

}
