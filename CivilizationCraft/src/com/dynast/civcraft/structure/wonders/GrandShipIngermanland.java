package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.components.ProjectileArrowComponent;
import com.dynast.civcraft.components.ProjectileCannonComponent;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BlockCoord;

public class GrandShipIngermanland extends Wonder {

	ProjectileArrowComponent arrowComponent;
	ProjectileCannonComponent cannonComponent;
	
	public GrandShipIngermanland(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public GrandShipIngermanland(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_ingermanland_trade_ship_income");
		addBuffToCiv(this.getCiv(), "buff_ingermanland_water_range");
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_ingermanland_trade_ship_income");
		removeBuffFromCiv(this.getCiv(), "buff_ingermanland_water_range");
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
	
	@Override
	public void loadSettings() {
		super.loadSettings();
		arrowComponent = new ProjectileArrowComponent(this, this.getCenterLocation().getLocation());
		arrowComponent.createComponent(this);

		cannonComponent = new ProjectileCannonComponent(this, this.getCenterLocation().getLocation()); 
		cannonComponent.createComponent(this);
	}
	
	/**
	 * @return the damage
	 */
	public int getArrowDamage() {
		double rate = 1;
		//rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
		rate += this.getTown().getBuffManager().getEffectiveDouble("buff_flydutch_ingerdamage");
		return (int)(arrowComponent.getDamage()*rate);
	}
	
	
	/**
	 * @param damage the damage to set
	 */
	public void setArrowDamage(int damage) {
		arrowComponent.setDamage(damage);
	}
	
	/**
	 * @return the power
	 */
	public double getArrowPower() {
		return arrowComponent.getPower();
	}

	/**
	 * @param power the power to set
	 */
	public void setArrorPower(double power) {
		arrowComponent.setPower(power);
	}

	public void setArrowLocation(BlockCoord absCoord) {
		arrowComponent.setTurretLocation(absCoord);
	}	
	
	public int getCannonDamage() {
		double rate = 1;
		//rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
		rate += this.getTown().getBuffManager().getEffectiveDouble("buff_flydutch_ingerdamage");
		return (int)(cannonComponent.getDamage()*rate);
	}
	
	public void setCannonLocation(BlockCoord absCoord) {
		cannonComponent.setTurretLocation(absCoord);
	}
	
}
