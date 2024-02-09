package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class Lighthouse extends Structure {

	
	protected Lighthouse(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Lighthouse(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public void loadSettings() {
		super.loadSettings();

	}
	
	@Override
	public String getMarkerIconName() {
		return "lighthouse";
	}

/*	public double getHammersPerTile() {
		AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel)this.getComponent("AttributeBiomeBase");
		double base = attrBiome.getBaseValue();
	
		double rate = 1;
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		return (rate*base);
	}*/

}
