
package com.dynast.civcraft.threading.timers;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;

public class PlayerLocationCacheUpdate implements Runnable {

	public static int UPDATE_LIMIT = 20;
	public static Queue<String> playerQueue = new LinkedList<>();
	
	@Override
	public void run() {

	//	if (PlayerLocationCache.lock.tryLock()) {		
			try {
				for (int i = 0; i < UPDATE_LIMIT; i++) {
					String playerName = playerQueue.poll();
					if (playerName == null) {
						return;
					}
					
					try {
						Player player = CivGlobal.getPlayer(playerName);
						if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
							//Don't leave creative or Spectator players in the cache.
							PlayerLocationCache.remove(playerName);
							continue;
						}
						PlayerLocationCache.updateLocation(player);
						playerQueue.add(playerName);
						
					} catch (CivException e) {
						// player not online. remove from queue by not re-adding.
						PlayerLocationCache.remove(playerName);
						continue;
					}
				}
			} finally {
			//	PlayerLocationCache.lock.unlock();
			}
		//}
	}

}
