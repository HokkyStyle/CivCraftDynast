
package com.dynast.civcraft.threading.tasks;


import com.dynast.civcraft.config.ConfigMission;
import com.dynast.civcraft.items.units.MissionBook;

public class PerformMissionTask implements Runnable {
	ConfigMission mission;
	String playerName;
	int online;
	
	public PerformMissionTask (ConfigMission mission, String playerName, int online) {
		this.mission = mission;
		this.playerName = playerName;
		this.online = online;
	}
	
	
	@Override
	public void run() {
		MissionBook.performMission(mission, playerName, online);
	}

}
