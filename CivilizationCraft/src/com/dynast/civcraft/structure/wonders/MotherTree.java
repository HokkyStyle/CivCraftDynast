package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;

public class MotherTree extends Wonder {

	public MotherTree(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public MotherTree(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	@Override
	protected void addBuffs() {
		addBuffToTown(this.getTown(), "buff_mother_tree_growth");
		addBuffToTown(this.getTown(), "buff_mother_tree_tile_improvement_cost");
		addBuffToTown(this.getTown(), "buff_mother_tree_tile_improvement_bonus");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromTown(this.getTown(), "buff_mother_tree_growth");
		removeBuffFromTown(this.getTown(), "buff_mother_tree_tile_improvement_cost");
		removeBuffFromTown(this.getTown(), "buff_mother_tree_tile_improvement_bonus");
	}
	
	@Override
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
	
}
