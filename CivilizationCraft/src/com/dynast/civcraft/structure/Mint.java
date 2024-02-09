package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class Mint extends Structure {

	public Mint(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public Mint(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public String getMarkerIconName() {
		return "coins";
	}
	
	public void loadSettings() {
		super.loadSettings();
	}
	
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
	
	protected void removeBuffs() {
		this.removeBuffFromTown(this.getTown(), "buff_barter_mint");
		this.removeBuffFromTown(this.getTown(), "buff_greed_mint");
	}

	protected void addBuffs() {
		this.addBuffToTown(this.getTown(), "buff_barter_mint");
		this.addBuffToTown(this.getTown(), "buff_greed_mint");

	}
	
	protected void addBuffToTown(Town town, String id) {
		try {
			town.getBuffManager().addBuff(id, id, this.getDisplayName()+" in "+this.getTown().getName());
		} catch (CivException e) {
			e.printStackTrace();
		}
	}
	
	protected void removeBuffFromTown(Town town, String id) {
		town.getBuffManager().removeBuff(id);
	}

}
