package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class TradePort extends WaterStructure {
	
	protected TradePort(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public TradePort(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
		
	@Override
	public void loadSettings() {
		super.loadSettings();
	}
	
	public String getkey() {
		return getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}
	
	@Override
	public String getMarkerIconName() {
		return "anchor";
	}

	@Override
	public String getDynmapDescription() {
		return null;
	}
	
/*	public double getHammersPerTile() {
		AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel)this.getComponent("AttributeBiomeBase");
		double base = attrBiome.getBaseValue();
	
		double rate = 1;
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		return (rate*base);
	}*/

}