package pvptimer;

import com.dynast.civcraft.war.War;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class PvPListener implements Listener {
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPvP(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}

		if (event.getEntity() instanceof NPC) {
			return;
		}
		
		if (event.getDamager() instanceof Player) {
			Player damager = (Player) event.getDamager();
			Resident damagerResident = CivGlobal.getResident(damager);
			
			if (damagerResident.isProtected() && (event.getEntity() instanceof Player)) {
				CivMessage.sendError(damager, CivSettings.localize.localizedString("pvpListenerError"));
				event.setCancelled(true);					
			}
		}
		if (event.getDamager() instanceof Arrow && ((Arrow) event.getDamager()).getShooter() instanceof LivingEntity) {
			LivingEntity shooter = (LivingEntity) ((Arrow) event.getDamager()).getShooter();
			Arrow arrow = (Arrow)event.getDamager();
			
			if ((shooter instanceof Player) && (event.getEntity() instanceof Player)) {
				Player damager = (Player) shooter;
				Resident damagerResident = CivGlobal.getResident(damager);
				Player defendingPlayer = (Player) event.getEntity();
				Resident defendingResident = CivGlobal.getResident(defendingPlayer);

				if (War.isWarTime() && damagerResident.getCiv().getName().equals(defendingResident.getCiv().getName())) {
					event.setCancelled(true);
					arrow.remove();
					return;
				}

				if (damagerResident.isProtected()) {
					CivMessage.sendError(damager, CivSettings.localize.localizedString("pvpListenerError"));
					event.setCancelled(true);
					arrow.remove();
				} else if (defendingResident.isProtected()) {
					CivMessage.sendError(damager, CivSettings.localize.localizedString("pvpListenerError2"));
					event.setCancelled(true);
					arrow.remove();
				} else {
					if (damagerResident.timeInPvp == 0) {
						CivMessage.send(damager, CivColor.Red+CivSettings.localize.localizedString("pvpListenerInPvp1"));
					}

					if (defendingResident.timeInPvp == 0) {
						CivMessage.send(defendingPlayer, CivColor.Red+CivSettings.localize.localizedString("pvpListenerInPvp1"));
					}
					defendingResident.firstDam = false;
					damagerResident.firstDam = false;
					damagerResident.timeInPvp = 300;
					defendingResident.timeInPvp = 300;
				}
			}

		}
		if ((event.getEntity() instanceof Player) && !event.isCancelled() && (event.getDamager() instanceof Player)) {
			Player damager = (Player) event.getDamager();
			Player defendingPlayer = (Player) event.getEntity();
			Resident defendingResident = CivGlobal.getResident(defendingPlayer);
			Resident damagerResident = CivGlobal.getResident(damager);

			if (defendingResident == null) {
				event.setCancelled(true);
				return;
			}

			if (War.isWarTime() && damagerResident.getCiv().getName().equals(defendingResident.getCiv().getName())) {
				event.setCancelled(true);
				return;
			}

			if (event.getDamager() instanceof Player) {
				if (defendingResident.isProtected()) {
					event.setCancelled(true);
					CivMessage.sendError(damager, CivSettings.localize.localizedString("pvpListenerError2"));					
				} else {
					if (damagerResident.timeInPvp == 0) {
						CivMessage.send(damager, CivColor.Red+CivSettings.localize.localizedString("pvpListenerInPvp1"));
					}

					if (defendingResident.timeInPvp == 0) {
						CivMessage.send(defendingPlayer, CivColor.Red+CivSettings.localize.localizedString("pvpListenerInPvp1"));
					}

					defendingResident.firstDam = false;
					damagerResident.firstDam = false;
					damagerResident.timeInPvp = 300;
					defendingResident.timeInPvp = 300;
				}
			}
		}
	}
}
