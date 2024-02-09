package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.structure.Pasture;
import com.dynast.civcraft.threading.CivAsyncTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class SpawnMobTask extends CivAsyncTask {
    Location loc;
    EntityType type;
    Pasture pasture;

    public SpawnMobTask(Location loc, EntityType type, Pasture pasture) {
        this.loc = loc;
        this.type = type;
        this.pasture = pasture;
    }

    @Override
    public void run() {
        Entity entity = Bukkit.getWorld("world").spawnEntity(this.loc, type);
        pasture.onBreed((LivingEntity)entity);
    }
}
