package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class GlobeTheatre extends Wonder {

	public GlobeTheatre(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public GlobeTheatre(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_globe_theatre_culture_from_towns");
	}

	@Override
	protected void addBuffs() {		
		addBuffToCiv(this.getCiv(), "buff_globe_theatre_culture_from_towns");
	}
	
	@Override
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}
	
	@Override
	public void onComplete() {
		addBuffs();
	}

}
