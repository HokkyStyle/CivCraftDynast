
package com.dynast.civcraft.threading.timers;

public class LagSimulationTimer implements Runnable {

	int targetTPS;
	
	public LagSimulationTimer(int targetTPS) {
		this.targetTPS = targetTPS;
	}
	
	
	@Override
	public void run() {
	
		/* Assume we're currently running at 20 tps. */
		int waitTime = 20 - targetTPS;
		
		if (waitTime < 0) {
			return;
		}
		
		double secondsPerTick = 0.05;
		long millis = (long)(waitTime*secondsPerTick*1000);
		synchronized (this) {
		try {
			this.wait(millis);
		} catch (InterruptedException e) {
		}
		}
	}

}
