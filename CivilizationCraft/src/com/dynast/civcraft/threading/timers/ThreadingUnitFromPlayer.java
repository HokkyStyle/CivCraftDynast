package com.dynast.civcraft.threading.timers;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.RegenDefender;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ThreadingUnitFromPlayer extends CivAsyncTask {
    private PotionEffect bersSpeed = new PotionEffect(PotionEffectType.FAST_DIGGING, 100, 1, true, true);
    private PotionEffect bersHaste = new PotionEffect(PotionEffectType.SPEED, 100, 1, true, true);
    private PotionEffect bersRegen = new PotionEffect(PotionEffectType.REGENERATION, 100, 2, true, true);
    private PotionEffect alchemistDef = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 0, true, true);
    private int time;

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (time == 2) {
                time = 0;
            }
            Resident res = CivGlobal.getResident(p);
            res.checkForUnits(p);
            res.calculateWalkingModifier(p);
            double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            if (res.unit.equals("berserker") && p.getHealth() < maxHealth/2 && res.timeInPvp > 0) {
                p.addPotionEffect(bersSpeed, true);
                p.addPotionEffect(bersHaste, true);
                p.addPotionEffect(bersRegen, true);
            }

            if (res.unit.equals("defender") && time == 1) {
                RegenDefender.run(p);
            }
            time++;

            if (res.unit.equals("alchemist") && !p.getActivePotionEffects().isEmpty() && p.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE) == null) {
                p.addPotionEffect(alchemistDef, true);
            }

            if (res.timedSpy > 0) {
                res.timedSpy--;
            }
        }
    }
}
