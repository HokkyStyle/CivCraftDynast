
package com.dynast.civcraft.threading.sync;

import java.util.HashSet;
import java.util.Set;

import net.md_5.itag.iTag;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;


public class SyncUpdateTagsBetweenCivs implements Runnable {
	Set<Player> civList = new HashSet<>();
	Set<Player> otherCivList = new HashSet<>();
	
	public SyncUpdateTagsBetweenCivs(Set<Player> civList, Set<Player> otherCivList) {
		this.civList = civList;
		this.otherCivList = otherCivList;
	}

	@Override
	public void run() {
		if (CivSettings.hasITag) {
			for (Player player : civList) {
				if (!otherCivList.isEmpty()) {
					iTag.getInstance().refreshPlayer(player, otherCivList);
				}
			}
			
			for (Player player : otherCivList) {
				if (!civList.isEmpty()) {
					iTag.getInstance().refreshPlayer(player, civList);
				}
			}
		}
	}
	
}
