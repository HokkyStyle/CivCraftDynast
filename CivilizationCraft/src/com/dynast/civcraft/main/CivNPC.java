package com.dynast.civcraft.main;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.npctraits.Banker;
import com.dynast.civcraft.npctraits.TeleportMaster;
import com.dynast.civcraft.structure.Bank;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.threading.TaskMaster;
import de.hellfirepvp.CustomMobs;
import de.hellfirepvp.api.CustomMobsAPI;
import de.hellfirepvp.api.data.ICustomMob;
import de.hellfirepvp.api.exception.SpawnLimitException;
import de.hellfirepvp.data.mob.CustomMob;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.generator.BlockPopulator;


public class CivNPC implements Runnable {
    private String name;
    private Location loc;
    private Structure structure;

    public CivNPC(String name, Location loc, Structure structure) {
        this.name = name;
        this.loc = loc;
        this.structure = structure;
    }

    public static void newStructureNPC (String name, Location loc, Structure structure) {
        if (!isFreeLoc(loc)) {
            for (Entity en : loc.getWorld().getNearbyEntities(loc, 1, 2, 1)) {
                if (CitizensAPI.getNPCRegistry().isNPC(en)) {
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(en);
                    npc.despawn();
                    npc.destroy();
                    CitizensAPI.getNPCRegistry().deregister(npc);
                }
            }
        }

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.setProtected(true);

        //Set<EntityType> targets = new HashSet<>();
        //targets.add(EntityType.PLAYER);
        //npc.getDefaultGoalController().addGoal(TargetNearbyEntityGoal.builder(npc).aggressive(false).radius(10).targets(targets).build(), 1);

        switch (name) {
            case "Банкир":
                //npc.data().setPersistent(NPC.PLAYER_SKIN_UUID_METADATA, "BankerNPC");
                npc.addTrait(new Banker((Bank)structure));
                break;
            case "Мастер телепортации":
                npc.addTrait(new TeleportMaster());
                break;
        }

        npc.spawn(loc);
        structure.npc = npc;
    }

    private static boolean isFreeLoc(Location loc) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.isSpawned()) {
                if (npc.getEntity().getLocation().getBlockX() == loc.getBlockX() &&
                        npc.getEntity().getLocation().getBlockZ() == loc.getBlockZ()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void spawnBossDragon() throws CivException {
        CustomMob dragon = CustomMobs.instance.getMobDataHolder().getCustomMob("dragonBoss");
        if (dragon == null) {
            throw new CivException("Boss Dragon not found");
        }

        Location loc = CivGlobal.locBossDragon;
        if (loc == null) {
            throw new CivException("Location not found");
        }

        class SyncTask1 implements Runnable {

            public void run() {
                loc.getChunk().load();
                try {
                    dragon.spawnAt(CivGlobal.locBossDragon);
                } catch(SpawnLimitException e) {
                    CivLog.warning("Boss Dragon cannot be spawned, limited mob");
                }
            }
        }
        TaskMaster.syncTask(new SyncTask1());

    }

    public static void spawnBossWither() throws CivException {
        CustomMob wither = CustomMobs.instance.getMobDataHolder().getCustomMob("witherBoss");
        if (wither == null) {
            throw new CivException("Boss Wither not found");
        }

        Location loc = CivGlobal.locBossWither;
        if (loc == null) {
            throw new CivException("Location not found");
        }

        class SyncTask implements Runnable {

            public void run() {
                loc.getChunk().load();
                try {
                    wither.spawnAt(CivGlobal.locBossWither);
                } catch(SpawnLimitException e) {
                    CivLog.warning("Boss Wither cannot be spawned, limited mob");
                }
            }
        }
        TaskMaster.syncTask(new SyncTask());
    }

    @Override
    public void run() {
        newStructureNPC(name, loc, structure);
    }
}
