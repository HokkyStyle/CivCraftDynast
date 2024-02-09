
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;

import com.dynast.civcraft.components.ProjectileArrowComponent;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BlockCoord;

public class ArrowTower extends Structure {

	ProjectileArrowComponent arrowComponent;
	
	protected ArrowTower(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
		this.hitpoints = this.getMaxHitPoints();
	}
	
	protected ArrowTower(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();
		arrowComponent = new ProjectileArrowComponent(this, this.getCenterLocation().getLocation());
		arrowComponent.createComponent(this);
	}
	
	public String getMarkerIconName() {
		return "shield";
	}

	/**
	 * @return the damage
	 */
	public int getDamage() {
		//double rate = 1;
		//rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.FIRE_BOMB);
		return (arrowComponent.getDamage());
	}
	
	@Override
	public int getMaxHitPoints() {
		double rate = 1;
		rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARRICADE);
		return (int) (info.max_hitpoints * rate);
	}

//	/**
//	 * @param damage the damage to set
//	 */
//	public void setDamage(int damage) {
//		arrowComponent.setDamage(damage);
//	}
	
//	/**
//	 * @return the power
//	 */
//	public double getPower() {
//		return arrowComponent.getPower();
//	}

	/**
	 * @param power the power to set
	 */
	public void setPower(double power) {
		arrowComponent.setPower(power);
	}

	public void setTurretLocation(BlockCoord absCoord) {
		arrowComponent.setTurretLocation(absCoord);
	}	

}
