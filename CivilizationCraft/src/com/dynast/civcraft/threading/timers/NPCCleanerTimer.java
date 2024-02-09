package com.dynast.civcraft.threading.timers;

import com.dynast.civcraft.npctraits.GeneralTrait;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.threading.CivAsyncTask;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class NPCCleanerTimer extends CivAsyncTask {

    //public static ReentrantLock lock = new ReentrantLock();

    @Override
    public void run() {
//        if (!lock.tryLock()) {
//            return;
//        }
        //try {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (npc.hasTrait(GeneralTrait.class)) {
                GeneralTrait trait = npc.getTrait(GeneralTrait.class);
                Buildable b = trait.buildable;
                if (b == null || b.isDestroyed()) {
                    CitizensAPI.getNPCRegistry().deregister(npc);
                    //npc.destroy();
                    //npc.despawn();
                }
            }
        }
        //} finally {
          //  lock.unlock();
        //}
    }
}
