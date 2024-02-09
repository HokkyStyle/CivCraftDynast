
package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;

public class TheGreatPyramid extends Wonder {

	
	public TheGreatPyramid(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public TheGreatPyramid(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	private Civilization calculateNearestCivilization() {
		TreeMap<Double, Civilization> civMaps = CivGlobal.findNearestCivilizations(this.getTown());
		Civilization nearestCiv = null;
		if (civMaps.size() > 0) {
			nearestCiv = civMaps.firstEntry().getValue();
		}
		return nearestCiv;
	}
	
	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_pyramid_cottage_consume");
		addBuffToCiv(this.getCiv(), "buff_pyramid_cottage_bonus");
		addBuffToCiv(this.getCiv(), "buff_pyramid_culture");
		addBuffToTown(this.getTown(), "buff_pyramid_leech");
		Civilization nearest = calculateNearestCivilization();
		if (nearest != null) {
			addBuffToCiv(nearest, "debuff_pyramid_leech");
		}
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_pyramid_cottage_consume");
		removeBuffFromCiv(this.getCiv(), "buff_pyramid_cottage_bonus");
		removeBuffFromCiv(this.getCiv(), "buff_pyramid_culture");
		removeBuffFromTown(this.getTown(), "buff_pyramid_leech");
		Civilization nearest = calculateNearestCivilization();
		if (nearest != null) {
			removeBuffFromCiv(nearest, "debuff_pyramid_leech");
		}
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
