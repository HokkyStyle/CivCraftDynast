package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.threading.CivAsyncTask;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wither;

public class SpawnBossTask extends CivAsyncTask {
    private NPC npc;
    private Location loc;

    public SpawnBossTask(NPC npc, Location loc) {
        this.npc = npc;
        this.loc = loc;
    }

    @Override
    public void run() {

        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.isSpawned()) {
                if (npc.getName().equals(this.npc.getName())) {
                    return;
                }
            }
        }
        npc.setProtected(false);

        npc.spawn(loc);
        if (npc.getEntity() instanceof Wither) {
            LivingEntity wither = ((LivingEntity) this.npc.getEntity());
            wither.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(40);
            wither.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(28);
            wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(720);
        }
    }
}
