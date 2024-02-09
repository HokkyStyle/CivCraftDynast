package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.ControlPoint;
import com.dynast.civcraft.object.Town;

public class MilitaryBase extends Structure {

	protected MilitaryBase(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public MilitaryBase(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "warning";
	}
	
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	public void onComplete() {
		addBuffs();
	}
	
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}
	
	protected void removeBuffs() {
		for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
			cp.removeMaxHitpoints(10);
		}
	}

	protected void addBuffs() {
		for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
			cp.addMaxHitpoints(10);
		}
	}
}
