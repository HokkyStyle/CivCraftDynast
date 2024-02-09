package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class TransformerTesla extends Structure {

	protected TransformerTesla(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public TransformerTesla(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "tesla";
	}
	
	public void onDestroy() {
		super.onDestroy();
	}
}
