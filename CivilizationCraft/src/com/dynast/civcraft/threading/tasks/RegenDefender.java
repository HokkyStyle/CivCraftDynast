package com.dynast.civcraft.threading.tasks;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Relation;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.BlockCoord;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class RegenDefender {

    public static void run(Player player) {
        Resident defender = CivGlobal.getResident(player);
        if (defender == null || defender.getCiv() == null) {
            return;
        }
        for (PlayerLocationCache pc : PlayerLocationCache.getNearbyPlayers(new BlockCoord(player.getLocation()), 400)) {
            Resident nearbyResident = pc.getResident();
            if (nearbyResident == null || nearbyResident.getCiv() == null) {
                return;
            }
            if (defender.getCiv().getName().equals(nearbyResident.getCiv().getName()) ||
                    defender.getCiv().getDiplomacyManager().getRelationStatus(nearbyResident.getCiv()) == Relation.Status.ALLY) {
                if (player.getHealth() < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()-1 && !player.isDead() && player.isValid()) {
                    player.setHealth(player.getHealth() + 1.0);
                    break;
                }
            }
        }

    }
}
