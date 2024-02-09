
package com.dynast.civcraft.threading.tasks;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigMission;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.items.units.Unit;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.CultureChunk;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.ScoutShip;
import com.dynast.civcraft.structure.ScoutTower;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.CivColor;

public class EspionageMissionTask implements Runnable {

	ConfigMission mission;
	String playerName;
	Town target;
	int secondsLeft, online;
	Location startLocation;
	
	
	public EspionageMissionTask (ConfigMission mission, String playerName, Location startLocation, Town target, int seconds) {
		this.mission = mission;
		this.playerName = playerName;
		this.target = target;
		this.startLocation = startLocation;
		this.secondsLeft = seconds;
		this.online = target.getCiv().getOnlineResidents().size();
	}
	
	@Override
	public void run() {
		double exposePerSecond;
		double exposePerPlayer;
		double exposePerScout;
		try {
			exposePerSecond = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_second");
			exposePerPlayer = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_player");
			exposePerScout = CivSettings.getInteger(CivSettings.espionageConfig, "espionage.exposure_per_scout");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}	
		
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		Resident resident = CivGlobal.getResident(player);	
		CivMessage.send(player, CivColor.LightGreen+CivColor.BOLD+CivSettings.localize.localizedString("espionage_missionStarted"));
			
		while (secondsLeft > 0) {
	
			if (secondsLeft > 0) {
				secondsLeft--;

				ChunkCoord coord = new ChunkCoord(player.getLocation());
				CultureChunk cc = CivGlobal.getCultureChunk(coord);

				if (cc == null || cc.getCiv() != target.getCiv()) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("espionage_missionAborted"));
					return;
				}

				if (player.getLocation().distance(startLocation) > 8) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("espionage_missionAbortedLoc"));
					return;
				}

				/* Add base exposure. */
				resident.setPerformingMission(true);
				resident.setSpyExposure(resident.getSpyExposure() + exposePerSecond);
				
				/* Add players nearby exposure */
				//PlayerLocationCache.lock.lock();
				int playerCount = PlayerLocationCache.getNearbyPlayers(new BlockCoord(player.getLocation()), 600).size();
				playerCount--;
				resident.setSpyExposure(resident.getSpyExposure() + (playerCount*exposePerPlayer));

				/* Add scout tower exposure */
				int amount = 0;
				double range;
				try {
					range = CivSettings.getDouble(CivSettings.warConfig, "scout_tower.range");
				} catch (InvalidConfiguration e) {
					e.printStackTrace();
					resident.setPerformingMission(false);
					return;
				}
				
				BlockCoord bcoord = new BlockCoord(player.getLocation());
								
				for (Structure struct : target.getStructures()) {
					if (!struct.isActive()) {
						continue;
					}
					
					if (struct instanceof ScoutTower || struct instanceof ScoutShip) {
						if (bcoord.distance(struct.getCenterLocation()) < range) {
							amount ++;
						}
					}
				}

				if (amount > 4) {
					amount = 4;
				}

				resident.setSpyExposure(resident.getSpyExposure() + amount*exposePerScout);
				
				/* Process exposure penalities */
				if (target.processSpyExposure(resident) || resident.timeInPvp > 0) {
					CivMessage.global(CivColor.Yellow+CivSettings.localize.localizedString("var_espionage_missionFailedAlert",(CivColor.White+player.getName()),mission.name,target.getName()));
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("espionage_missionFailed"));
					Unit.removeUnit(player);
					resident.setPerformingMission(false);
					return;
				}
				
				if ((secondsLeft % 15) == 0) {
					CivMessage.send(player, CivColor.Yellow+CivColor.BOLD+CivSettings.localize.localizedString("var_espionage_secondsRemain",secondsLeft));
				} else if (secondsLeft < 15) {
					CivMessage.send(player, CivColor.Yellow+CivColor.BOLD+CivSettings.localize.localizedString("var_espionage_secondsRemain",secondsLeft));
				}
				
			}
			
			ChunkCoord coord = new ChunkCoord(player.getLocation());
			CultureChunk cc = CivGlobal.getCultureChunk(coord);
			
			if (cc == null || cc.getCiv() != target.getCiv()) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("espionage_missionAborted"));
				return;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				return;
			}
		}
		
		resident.setPerformingMission(false);
		TaskMaster.syncTask(new PerformMissionTask(mission, playerName, online));
	}

}
