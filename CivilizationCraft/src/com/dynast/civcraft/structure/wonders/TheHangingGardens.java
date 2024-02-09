package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.object.TownChunk;

public class TheHangingGardens extends Wonder {

	public TheHangingGardens(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public TheHangingGardens(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_hanging_gardens_growth");
		//addBuffToCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
		addBuffToTown(this.getTown(), "buff_hanging_gardens_regen");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_growth");
		//removeBuffFromCiv(this.getCiv(), "buff_hanging_gardens_additional_growth");
		removeBuffFromTown(this.getTown(), "buff_hanging_gardens_regen");
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
	
	@Override
	public void onUpdate() {
		super.onUpdate();
		
		for (Town t : this.getTown().getCiv().getTowns()) {
			for (Resident res : t.getResidents()) {
				try {
					Player player = CivGlobal.getPlayer(res);
					
					if (player.isDead() || !player.isValid()) {
						continue;
					}
					
					if (player.getHealth() >= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
						continue;
					}
					
					TownChunk tc = CivGlobal.getTownChunk(player.getLocation());
					if (tc == null || tc.getTown().getCiv() != this.getTown().getCiv()) {
						continue;
					}
					
					if (player.getHealth() >= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()-1) {
						player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
					} else {
						player.setHealth(player.getHealth() + 1);
					}
				} catch (CivException e) {
					//Player not online;
				}
				
			}
		}
	}
	
}
