
package com.dynast.civcraft.threading.sync;

import java.util.Collection;

import net.md_5.itag.iTag;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;

public class SyncUpdateTags implements Runnable {

	Collection<Resident> residentsToSendUpdate;
	String playerToUpdate;
	
	public SyncUpdateTags(String playerToUpdate, Collection<Resident> residentsToSendUpdate) {
		this.residentsToSendUpdate = residentsToSendUpdate;
		this.playerToUpdate = playerToUpdate;
	}

	@Override
	public void run() {
		if (CivSettings.hasITag) {
			try {
				Player player = CivGlobal.getPlayer(playerToUpdate);		
				for (Resident resident : residentsToSendUpdate) {
					try {
						Player resPlayer = CivGlobal.getPlayer(resident);
						if (player == resPlayer) {
							continue;
						}
						iTag.getInstance().refreshPlayer(player, resPlayer);
						iTag.getInstance().refreshPlayer(resPlayer, player);
					} catch (CivException e) {
						// one of these players is not online.
					}
				}
				
				
			} catch (CivException e1) {
				return;
			}		
		}
	}
	
}
