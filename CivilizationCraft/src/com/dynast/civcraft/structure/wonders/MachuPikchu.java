package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class MachuPikchu extends Wonder {

	
	public MachuPikchu(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public MachuPikchu(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_machu_trade");
		addBuffToCiv(this.getCiv(), "buff_machu_happiness");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_machu_trade");
		removeBuffFromCiv(this.getCiv(), "buff_machu_happiness");
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
