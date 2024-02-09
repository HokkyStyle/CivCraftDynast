package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class Emporium extends Structure {

	public Emporium(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Emporium(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public String getMarkerIconName() {
		return "cart";
	}
}
