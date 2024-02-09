
package com.dynast.civcraft.components;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.dynast.civcraft.cache.ArrowFiredCache;
import com.dynast.civcraft.cache.CivCache;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.util.BlockCoord;

public class ProjectileArrowComponent extends ProjectileComponent {
	
	public ProjectileArrowComponent(Buildable buildable, Location turretCenter) {
		super(buildable, turretCenter);
	}

	private double power;
	private boolean isActive = true;
	
	@Override
	public void loadSettings() {
		try {
			setDamage(CivSettings.getInteger(CivSettings.warConfig, "arrow_tower.damage"));
			power = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.power");
			range = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.range");
			if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range") && this.getBuildable().getConfigId().equals("s_arrowtower"))
			{
				range *= this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_tower_range");
			} else if (this.getTown().getBuffManager().hasBuff("buff_ingermanland_water_range") &&
					(this.getBuildable().getConfigId().equals("w_grand_ship_ingermanland") || this.getBuildable().getConfigId().equals("s_arrowship"))) {
				range *= this.getTown().getBuffManager().getEffectiveDouble("buff_ingermanland_water_range");
			}
			min_range = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.min_range");
			
			this.proximityComponent.setBuildable(buildable);
			this.proximityComponent.setCenter(new BlockCoord(getTurretCenter()));
			this.proximityComponent.setRadius(range);
			
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void fire(Location turretLoc, Entity targetEntity) {
		if (!buildable.isValid() || !isActive) {
			return;
		}
		
		Location playerLoc = targetEntity.getLocation();
		playerLoc.setY(playerLoc.getY()+1); //Target the head instead of feet.
					
		turretLoc = adjustTurretLocation(turretLoc, playerLoc);
		Vector dir = getVectorBetween(playerLoc, turretLoc).normalize();
		Arrow arrow = buildable.getCorner().getLocation().getWorld().spawnArrow(turretLoc, dir, (float)power, 0.0f);
		arrow.setVelocity(dir.multiply(power));
		
		if (buildable.getTown().getBuffManager().hasBuff(Buff.FIRE_BOMB) || buildable.getTown().getBuffManager().hasBuff("buff_fired_arrows")) {
			arrow.setFireTicks(1000);
		}
		
		CivCache.arrowsFired.put(arrow.getUniqueId(), new ArrowFiredCache(this, targetEntity, arrow));
	}

	public double getPower() {
		return power;
	}
	
	public void setPower(double power) {
		this.power = power;
	}
	
	public Town getTown() {
		return buildable.getTown();
	}
	
}
