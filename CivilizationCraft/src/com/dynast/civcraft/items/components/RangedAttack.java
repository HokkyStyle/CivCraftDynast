package com.dynast.civcraft.items.components;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.util.BlockCoord;
import gpl.AttributeUtil;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.items.units.Unit;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.loreenhancements.LoreEnhancementAttack;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class RangedAttack extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.Rose+this.getDouble("value")+" "+CivSettings.localize.localizedString("itemLore_RangedAttack"));	
	}
	
	private static double ARROW_MAX_VEL = 6.0; 
	
	@Override
	public void onInteract(PlayerInteractEvent event) {
		if (Unit.isWearingAnyMetal(event.getPlayer())) {
			event.setCancelled(true);
			CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("itemLore_RangedAttack_errorMetal"));
			return;
		}
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.Rose+CivSettings.localize.localizedString("itemLore_Warning")+" - "+CivColor.LightGray+
					CivSettings.localize.localizedString("itemLore_attackHalfDamage"));
		}
	}
	
	public void onRangedAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		AttributeUtil attrs = new AttributeUtil(inHand);
		double dmg = this.getDouble("value");
		
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Player attacker = (Player)arrow.getShooter();
				if (Unit.isWearingAnyMetal(attacker)) {
					event.setCancelled(true);
					CivMessage.sendError(attacker, CivSettings.localize.localizedString("itemLore_RangedAttack_errorMetal"));
					return;
				}
			}
		}
		
		double extraAtt = 0.0;
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementAttack) {
				extraAtt +=  ((LoreEnhancementAttack)enh).getExtraAttack(attrs);
			}
		}
		dmg += extraAtt;

		Player player = (Player)((Arrow)event.getDamager()).getShooter();
		Resident attacker = CivGlobal.getResident(player);
		if (attacker.unit.equals("bowman")) {
			dmg += 2;
			if (player != null) {
				int countEnemy = 0;
				for (PlayerLocationCache pc : PlayerLocationCache.getNearbyPlayers(new BlockCoord(player.getLocation()), 400)) {
					Resident nearbyResident = pc.getResident();
					if (attacker.getCiv().getDiplomacyManager().atWarWith(nearbyResident.getCiv())) {
						countEnemy++;
					}
				}
				if (countEnemy == 0) {
					dmg += 2;
				}
			}
		}

		if (attacker.unit.equals("spy") || attacker.timedSpy > 0) {
			dmg -= 6;
		}
		
		Vector vel = event.getDamager().getVelocity();
		double magnitudeSquared = Math.pow(vel.getX(), 2) + Math.pow(vel.getY(), 2) + Math.pow(vel.getZ(), 2);
		
		double percentage = magnitudeSquared / ARROW_MAX_VEL;
		double totalDmg = percentage * dmg;
		
		if (totalDmg > dmg) {
			totalDmg = dmg;
		}
		
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				Resident resident = CivGlobal.getResident(((Player)arrow.getShooter()));
				if (inHand.getType() != Material.BOW) {
					event.setDamage(0);
					return;
				}
				if (!resident.hasTechForItem(inHand)) {
					totalDmg = totalDmg / 2;
				}
			}
		}
		
		if (totalDmg < 0.5) {
			totalDmg = 0.5;
		}
		
		event.setDamage(totalDmg);
	}


}
