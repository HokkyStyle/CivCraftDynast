
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.WindmillStartSyncTask;

public class Windmill extends Structure {
	
	public enum CropType {
		WHEAT,
		CARROTS,
		POTATOES
	}
	
	public Windmill(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public Windmill(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}
	
	public String getMarkerIconName() {
		return "windmill";
	}

	@Override
	public void onEffectEvent() {

	
	}
	
	public void processWindmill() {
		/* Fire a sync task to perform this. */
		TaskMaster.syncTask(new WindmillStartSyncTask(this), 0);
	}
	
}
