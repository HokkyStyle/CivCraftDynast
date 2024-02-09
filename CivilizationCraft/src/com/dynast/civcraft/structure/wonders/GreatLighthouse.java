package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class GreatLighthouse extends Wonder {

	public GreatLighthouse(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public GreatLighthouse(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_great_lighthouse_tower_range");
		addBuffToCiv(this.getCiv(), "buff_great_lighthouse_trade_ship_income");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_great_lighthouse_tower_range");
		removeBuffFromCiv(this.getCiv(), "buff_great_lighthouse_trade_ship_income");
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
