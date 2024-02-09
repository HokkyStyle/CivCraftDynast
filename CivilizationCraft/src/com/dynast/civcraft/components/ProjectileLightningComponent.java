
package com.dynast.civcraft.components;


import com.dynast.civcraft.cache.ArrowFiredCache;
import com.dynast.civcraft.cache.CivCache;
import com.dynast.civcraft.cache.LightningFiredCache;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.util.BlockCoord;
import org.bukkit.entity.LightningStrike;

public class ProjectileLightningComponent extends ProjectileComponent {
	
//	private int speed;
//	private int splash;
	private int fireRate;
	private int halfSecondCount = 0;
	
	public ProjectileLightningComponent(Buildable buildable, Location turretCenter) {
		super(buildable, turretCenter);
	}

	@Override
	public void fire(Location turretLoc, Entity targetEntity) {
		if (halfSecondCount < fireRate) {
			halfSecondCount++;
			return;
		} else {
			halfSecondCount = 0;
		}

		World world = turretLoc.getWorld();
		Location location = targetEntity.getLocation();
		LightningStrike light = world.strikeLightning(location);

		CivCache.lightningFired.put(light.getUniqueId(), new LightningFiredCache(this, targetEntity, light));
	}

	@Override
	public void loadSettings() {
		try {
			setDamage(CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.damage"));
//			speed = CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.speed");
			range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.range");
			if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_tower_range") && this.getBuildable().getConfigId().equals("s_teslatower") ) {
				range *= this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_tower_range");
			}
			min_range = CivSettings.getDouble(CivSettings.warConfig, "tesla_tower.min_range");
//			splash = CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.splash");
			fireRate = CivSettings.getInteger(CivSettings.warConfig, "tesla_tower.fire_rate");
			
			
			this.proximityComponent.setBuildable(buildable);
			this.proximityComponent.setCenter(new BlockCoord(getTurretCenter()));
			this.proximityComponent.setRadius(range);
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}

	public int getHalfSecondCount() {
		return halfSecondCount;
	}

	public void setHalfSecondCount(int halfSecondCount) {
		this.halfSecondCount = halfSecondCount;
	}

	public Town getTown() {
		return buildable.getTown();
	}

	@Override
	public int getDamage() {
		double rate = 1;
		if (this.getTown().getStructureTypeCount("s_transtesla") >= 1) {
			rate += 0.5;
		}
		return (int)(damage*rate);
	}
	
	
}
