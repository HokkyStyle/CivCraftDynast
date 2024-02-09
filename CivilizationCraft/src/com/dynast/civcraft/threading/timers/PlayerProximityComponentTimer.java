
package com.dynast.civcraft.threading.timers;

import java.util.ArrayList;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.components.Component;
import com.dynast.civcraft.components.PlayerProximityComponent;

public class PlayerProximityComponentTimer implements Runnable {

	@Override
	public void run() {
		
		/* 
		 * Grab all of the player proximity components and update them, this task
		 * runs asynchronously once per tick and gathers all of the player locations
		 * into an async friendly data-structure.
		 */
		
		Component.componentsLock.lock();
		try {
			ArrayList<Component> proximityComponents = Component.componentsByType.get(PlayerProximityComponent.class.getName());
			
			if (proximityComponents == null) {
				return;
			}

			/* 
			 * Wait for the lock to free up before we continue; 
			 */
			for (Component comp : proximityComponents) {
				if (comp instanceof PlayerProximityComponent) {
					PlayerProximityComponent ppc = (PlayerProximityComponent)comp;
										
					if (ppc.lock.tryLock()) {
						try {
							ppc.buildNearbyPlayers(PlayerLocationCache.getCache());
						} finally {
							ppc.lock.unlock();
						}
					} 
				}
			}
		} finally {
			Component.componentsLock.unlock();
		}
	}

}
