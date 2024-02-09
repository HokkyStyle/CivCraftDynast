package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Location;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.SimpleBlock;

public class Quarry extends Structure {
	public static final int MAX_CHANCE = CivSettings.getIntegerStructure("quarry.max");
	private static final double OTHER_RATE = CivSettings.getDoubleStructure("quarry.other_rate"); //10%
	private static final double COAL_RATE = CivSettings.getDoubleStructure("quarry.coal_rate"); //10%
	private static final double OBSIDIAN_RATE = CivSettings.getDoubleStructure("quarry.obsidian_rate");
	
	private int level = 1;
	public int skippedCounter = 0;
	public ReentrantLock lock = new ReentrantLock();
	
	public enum Mineral {
		COAL,
		OTHER,
		OBSIDIAN
	}
	
	protected Quarry(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
		setLevel(town.saved_quarry_level);
	}
	
	public Quarry(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		String out = "<u><b>"+this.getDisplayName()+"</u></b><br/>";
		out += CivSettings.localize.localizedString("Level")+" "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "gear";
	}

	public double getChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case COAL:
			chance = COAL_RATE;
			break;
		case OTHER:
			chance = OTHER_RATE;
			break;
		case OBSIDIAN:
			chance = OBSIDIAN_RATE;
			break;
		}
		return this.modifyChance(chance);
	}
	
	private double modifyChance(Double chance) {
		double increase = chance*this.getTown().getBuffManager().getEffectiveDouble(Buff.EXTRACTION);
		chance += increase;
		
		try {
			if (this.getTown().getGovernment().id.equals("gov_despotism")) {
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "quarry.despotism_rate");
			} else if (this.getTown().getGovernment().id.equals("gov_theocracy") || this.getTown().getGovernment().id.equals("gov_monarchy") || this.getTown().getGovernment().id.equals("gov_oligarchy")){
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "quarry.penalty_rate");
			}
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		return chance;
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_quarry_level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
