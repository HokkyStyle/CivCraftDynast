package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.ControlPoint;
import com.dynast.civcraft.object.Town;


public class ChichenItza extends Wonder {

	public ChichenItza(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public ChichenItza(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_chichen_itza_tower_hp");
		removeBuffFromCiv(this.getCiv(), "buff_chichen_itza_regen_rate");
		removeBuffFromTown(this.getTown(), "buff_chichen_itza_cp_bonus_hp");
		//This is where the Itza's buff to CP is removed
		for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
			cp.removeMaxHitpoints((int)this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_cp_bonus_hp"));
		}
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_chichen_itza_tower_hp");
		addBuffToCiv(this.getCiv(), "buff_chichen_itza_regen_rate");
		addBuffToTown(this.getTown(), "buff_chichen_itza_cp_bonus_hp");
		//This is where the Itza's buff to CP applies
		for (ControlPoint cp : this.getTown().getTownHall().getControlPoints().values()) {
			cp.addMaxHitpoints((int)this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_cp_bonus_hp"));
		}		
	}
	
	@Override
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	@Override
	public void onComplete() {
		addBuffs();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}

}
