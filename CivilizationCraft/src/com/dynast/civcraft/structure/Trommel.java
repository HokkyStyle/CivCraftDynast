
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

public class Trommel extends Structure {
	public static final int GRAVEL_MAX_CHANCE = CivSettings.getIntegerStructure("trommel_gravel.max"); //100%
	private static final double GRAVEL_REDSTONE_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.redstone_chance"); //3%
	private static final double GRAVEL_IRON_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.iron_chance"); //4%
	private static final double GRAVEL_IRON_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.iron_ore_chance");
	private static final double GRAVEL_GOLD_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.gold_chance"); //2%
	private static final double GRAVEL_DIAMOND_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.diamond_chance"); //0.50%
	private static final double GRAVEL_EMERALD_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.emerald_chance"); //0.20%
	private static final double GRAVEL_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.chromium_chance");
	private static final double GRAVEL_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_gravel.tungsten_chance");
	
	public static final int GRANITE_MAX_CHANCE = CivSettings.getIntegerStructure("trommel_granite.max");
	private static final double GRANITE_REDSTONE_CHANCE = CivSettings.getDoubleStructure("trommel_granite.redstone_chance");
	private static final double GRANITE_IRON_CHANCE = CivSettings.getDoubleStructure("trommel_granite.iron_chance");
	private static final double GRANITE_IRON_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_granite.iron_ore_chance");
	private static final double GRANITE_GOLD_CHANCE = CivSettings.getDoubleStructure("trommel_granite.gold_chance");
	private static final double GRANITE_GOLD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_granite.gold_ore_chance");
	private static final double GRANITE_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_granite.tungsten_chance");
	private static final double GRANITE_DIAMOND_CHANCE = CivSettings.getDoubleStructure("trommel_granite.diamond_chance");
	private static final double GRANITE_EMERALD_CHANCE = CivSettings.getDoubleStructure("trommel_granite.emerald_chance");
	private static final double GRANITE_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_granite.chromium_chance");
	private static final double GRANITE_REFINED_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_granite.refined_chromium_chance");
	private static final double GRANITE_REFINED_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_granite.refined_tungsten_chance");
	
	public static final int DIORITE_MAX_CHANCE = CivSettings.getIntegerStructure("trommel_diorite.max");
	private static final double DIORITE_REDSTONE_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.redstone_chance");
	private static final double DIORITE_IRON_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.iron_chance");
	private static final double DIORITE_IRON_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.iron_ore_chance");
	private static final double DIORITE_GOLD_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.gold_chance");
	private static final double DIORITE_GOLD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.gold_ore_chance");
	private static final double DIORITE_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.tungsten_chance");
	private static final double DIORITE_DIAMOND_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.diamond_chance");
	private static final double DIORITE_DIAMOND_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.diamond_ore_chance");
	private static final double DIORITE_EMERALD_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.emerald_chance");
	private static final double DIORITE_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.chromium_chance");
	private static final double DIORITE_REFINED_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.refined_chromium_chance");
	private static final double DIORITE_REFINED_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_diorite.refined_tungsten_chance");
	
	public static final int ANDESITE_MAX_CHANCE = CivSettings.getIntegerStructure("trommel_andesite.max");
	private static final double ANDESITE_REDSTONE_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.redstone_chance");
	private static final double ANDESITE_IRON_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.iron_chance");
	private static final double ANDESITE_IRON_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.iron_ore_chance");
	private static final double ANDESITE_GOLD_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.gold_chance");
	private static final double ANDESITE_GOLD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.gold_ore_chance");
	private static final double ANDESITE_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.tungsten_chance");
	private static final double ANDESITE_DIAMOND_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.diamond_chance");
	private static final double ANDESITE_DIAMOND_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.diamond_ore_chance");
	private static final double ANDESITE_EMERALD_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.emerald_chance");
	private static final double ANDESITE_EMERALD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.emerald_ore_chance");
	private static final double ANDESITE_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.chromium_chance");
	private static final double ANDESITE_REFINED_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.refined_chromium_chance");
	private static final double ANDESITE_REFINED_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_andesite.refined_tungsten_chance");
	
	public static final int OBSIDIAN_MAX_CHANCE = CivSettings.getIntegerStructure("trommel_obsidian.max");
	private static final double OBSIDIAN_REDSTONE_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.redstone_chance");
	private static final double OBSIDIAN_IRON_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.iron_chance");
	private static final double OBSIDIAN_IRON_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.iron_ore_chance");
	private static final double OBSIDIAN_GOLD_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.gold_chance");
	private static final double OBSIDIAN_GOLD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.gold_ore_chance");
	private static final double OBSIDIAN_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.tungsten_chance");
	private static final double OBSIDIAN_DIAMOND_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.diamond_chance");
	private static final double OBSIDIAN_DIAMOND_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.diamond_ore_chance");
	private static final double OBSIDIAN_EMERALD_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.emerald_chance");
	private static final double OBSIDIAN_EMERALD_ORE_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.emerald_ore_chance");
	private static final double OBSIDIAN_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.chromium_chance");
	private static final double OBSIDIAN_REFINED_CHROMIUM_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.refined_chromium_chance");
	private static final double OBSIDIAN_REFINED_TUNGSTEN_CHANCE = CivSettings.getDoubleStructure("trommel_obsidian.refined_tungsten_chance");
	
	private int level = 1;
	public int skippedCounter = 0;
	public ReentrantLock lock = new ReentrantLock();
	
	public enum Mineral {
		REFINED_TUNGSTEN,
		REFINED_CHROMIUM,
		CHROMIUM,
		EMERALD,
		EMERALD_ORE,
		DIAMOND,
		DIAMOND_ORE,
		TUNGSTEN,
		GOLD,
		GOLD_ORE,
		REDSTONE,
		IRON,
		IRON_ORE		
	}
	
	protected Trommel(Location center, String id, Town town) throws CivException {
		super(center, id, town);	
		setLevel(town.saved_trommel_level);
	}
	
	public Trommel(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public String getDynmapDescription() {
		String out = "<u><b>"+this.getDisplayName()+"</u></b><br/>";
		out += "Level: "+this.level;
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "minecart";
	}
	
	public double getGravelChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case EMERALD:
			chance = GRAVEL_EMERALD_CHANCE;
			break;
		case DIAMOND:
			chance = GRAVEL_DIAMOND_CHANCE;
			break;
		case GOLD:
			chance = GRAVEL_GOLD_CHANCE;
			break;
		case IRON:
			chance = GRAVEL_IRON_CHANCE;
			break;
		case IRON_ORE:
			chance = GRAVEL_IRON_ORE_CHANCE;
			break;
		case REDSTONE:
			chance = GRAVEL_REDSTONE_CHANCE;
			break;
		case CHROMIUM:
			chance = GRAVEL_CHROMIUM_CHANCE;
			break;
		case TUNGSTEN:
			chance = GRAVEL_TUNGSTEN_CHANCE;
		default:
			break;	
		}
		return this.modifyChance(chance);
	}
	
	public double getGraniteChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case REFINED_CHROMIUM:
			chance = GRANITE_REFINED_CHROMIUM_CHANCE;
			break;
		case REFINED_TUNGSTEN:
			chance = GRANITE_REFINED_TUNGSTEN_CHANCE;
			break;
		case EMERALD:
			chance = GRANITE_EMERALD_CHANCE;
			break;
		case DIAMOND:
			chance = GRANITE_DIAMOND_CHANCE;
			break;
		case TUNGSTEN:
			chance = GRANITE_TUNGSTEN_CHANCE;
			break;
		case GOLD:
			chance = GRANITE_GOLD_CHANCE;
			break;
		case GOLD_ORE:
			chance = GRANITE_GOLD_ORE_CHANCE;
			break;
		case IRON:
			chance = GRANITE_IRON_CHANCE;
			break;
		case IRON_ORE:
			chance = GRANITE_IRON_ORE_CHANCE;
			break;
		case REDSTONE:
			chance = GRANITE_REDSTONE_CHANCE;
			break;
		case CHROMIUM:
			chance = GRANITE_CHROMIUM_CHANCE;
		default:
			break;
		}
		return this.modifyChance(chance);
	}
	
	public double getDioriteChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case REFINED_CHROMIUM:
			chance = DIORITE_REFINED_CHROMIUM_CHANCE;
			break;
		case REFINED_TUNGSTEN:
			chance = DIORITE_REFINED_TUNGSTEN_CHANCE;
			break;
		case EMERALD:
			chance = DIORITE_EMERALD_CHANCE;
			break;
		case DIAMOND:
			chance = DIORITE_DIAMOND_CHANCE;
			break;
		case DIAMOND_ORE:
			chance = DIORITE_DIAMOND_ORE_CHANCE;
			break;
		case TUNGSTEN:
			chance = DIORITE_TUNGSTEN_CHANCE;
			break;
		case GOLD:
			chance = DIORITE_GOLD_CHANCE;
			break;
		case GOLD_ORE:
			chance = DIORITE_GOLD_ORE_CHANCE;
			break;
		case IRON:
			chance = DIORITE_IRON_CHANCE;
			break;
		case IRON_ORE:
			chance = DIORITE_IRON_ORE_CHANCE;
			break;
		case REDSTONE:
			chance = DIORITE_REDSTONE_CHANCE;
			break;
		case CHROMIUM:
			chance = DIORITE_CHROMIUM_CHANCE;
		default:
			break;
		}
		return this.modifyChance(chance);
	}

	
	public double getAndesiteChance(Mineral mineral) {
		double chance = 0;
		switch (mineral) {
		case REFINED_CHROMIUM:
			chance = ANDESITE_REFINED_CHROMIUM_CHANCE;
			break;
		case REFINED_TUNGSTEN:
			chance = ANDESITE_REFINED_TUNGSTEN_CHANCE;
			break;
		case EMERALD:
			chance = ANDESITE_EMERALD_CHANCE;
			break;
		case EMERALD_ORE:
			chance = ANDESITE_EMERALD_ORE_CHANCE;
			break;
		case DIAMOND:
			chance = ANDESITE_DIAMOND_CHANCE;
			break;
		case DIAMOND_ORE:
			chance = ANDESITE_DIAMOND_ORE_CHANCE;
			break;
		case TUNGSTEN:
			chance = ANDESITE_TUNGSTEN_CHANCE;
			break;
		case GOLD:
			chance = ANDESITE_GOLD_CHANCE;
			break;
		case GOLD_ORE:
			chance = ANDESITE_GOLD_ORE_CHANCE;
			break;
		case IRON:
			chance = ANDESITE_IRON_CHANCE;
			break;
		case IRON_ORE:
			chance = ANDESITE_IRON_ORE_CHANCE;
			break;
		case REDSTONE:
			chance = ANDESITE_REDSTONE_CHANCE;
			break;
		case CHROMIUM:
			chance = ANDESITE_CHROMIUM_CHANCE;
		}
		return this.modifyChance(chance);
	}	
	
		public double getObsidianChance(Mineral mineral) {
			double chance = 0;
			switch (mineral) {
			case REFINED_CHROMIUM:
				chance = OBSIDIAN_REFINED_CHROMIUM_CHANCE;
				break;
			case REFINED_TUNGSTEN:
				chance = OBSIDIAN_REFINED_TUNGSTEN_CHANCE;
				break;
			case EMERALD:
				chance = OBSIDIAN_EMERALD_CHANCE;
				break;
			case EMERALD_ORE:
				chance = OBSIDIAN_EMERALD_ORE_CHANCE;
				break;
			case DIAMOND:
				chance = OBSIDIAN_DIAMOND_CHANCE;
				break;
			case DIAMOND_ORE:
				chance = OBSIDIAN_DIAMOND_ORE_CHANCE;
				break;
			case TUNGSTEN:
				chance = OBSIDIAN_TUNGSTEN_CHANCE;
				break;
			case GOLD:
				chance = OBSIDIAN_GOLD_CHANCE;
				break;
			case GOLD_ORE:
				chance = OBSIDIAN_GOLD_ORE_CHANCE;
				break;
			case IRON:
				chance = OBSIDIAN_IRON_CHANCE;
				break;
			case IRON_ORE:
				chance = OBSIDIAN_IRON_ORE_CHANCE;
				break;
			case REDSTONE:
				chance = OBSIDIAN_REDSTONE_CHANCE;
				break;
			case CHROMIUM:
				chance = OBSIDIAN_CHROMIUM_CHANCE;
			}
			return this.modifyChance(chance);	
	}
	
	private double modifyChance(Double chance) {
		double increase = chance*this.getTown().getBuffManager().getEffectiveDouble(Buff.EXTRACTION);
		chance += increase;
		
		try {
			if (this.getTown().getGovernment().id.equals("gov_despotism")) {
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "trommel.despotism_rate");
			} else if (this.getTown().getGovernment().id.equals("gov_theocracy") || this.getTown().getGovernment().id.equals("gov_monarchy") || this.getTown().getGovernment().id.equals("gov_oligarchy")){
				chance *= CivSettings.getDouble(CivSettings.structureConfig, "trommel.penalty_rate");
			}
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		return chance;
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.level = getTown().saved_trommel_level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
