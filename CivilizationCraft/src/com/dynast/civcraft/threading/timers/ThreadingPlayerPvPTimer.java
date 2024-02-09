package com.dynast.civcraft.threading.timers;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.CivColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ThreadingPlayerPvPTimer extends CivAsyncTask {

    @Override
    public void run() {
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Resident res = CivGlobal.getResident(p);
                if (res.timeInPvp > 1) {
                    res.timeInPvp -= 20;
                    if (res.timeInPvp <= 1) {
                        res.timeInPvp = 0;
                        CivMessage.send(res, CivColor.Green+CivSettings.localize.localizedString("pvpListenerInPvp2"));
                        res.firstDam = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
