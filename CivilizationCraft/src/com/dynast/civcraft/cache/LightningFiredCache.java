package com.dynast.civcraft.cache;

import com.dynast.civcraft.components.ProjectileLightningComponent;
import com.dynast.civcraft.util.CivColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;

import java.util.Calendar;
import java.util.UUID;

public class LightningFiredCache {
    private ProjectileLightningComponent fromTower;
    private Location target;
    private Entity targetEntity;
    private LightningStrike arrow;
    private UUID uuid;
    private Calendar expired;
    private boolean hit = false;

    public LightningFiredCache(ProjectileLightningComponent tower, Entity targetEntity, LightningStrike arrow) {
        this.setFromTower(tower);
        this.target = targetEntity.getLocation();
        this.targetEntity = targetEntity;
        this.setArrow(arrow);
        this.uuid = arrow.getUniqueId();
        expired = Calendar.getInstance();
        expired.add(Calendar.SECOND, 5);
    }

    public Location getTarget() {
        return target;
    }

    public void setTarget(Location target) {
        this.target = target;
    }

    public LightningStrike getArrow() {
        return arrow;
    }

    public void setArrow(LightningStrike arrow) {
        this.arrow = arrow;
    }

    public Object getUUID() {
        return uuid;
    }

    public void destroy(LightningStrike arrow) {
        //arrow.remove();
        this.arrow = null;
        CivCache.lightningFired.remove(this.uuid);
        this.uuid = null;
    }


    public void destroy(Entity damager) {
        if (damager instanceof LightningStrike) {
            this.destroy((LightningStrike) damager);
        }
    }

    public Calendar getExpired() {
        return expired;
    }

    public void setExpired(Calendar expired) {
        this.expired = expired;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public ProjectileLightningComponent getFromTower() {
        return fromTower;
    }

    public void setFromTower(ProjectileLightningComponent fromTower) {
        this.fromTower = fromTower;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(Entity targetEntity) {
        this.targetEntity = targetEntity;
    }
}
