package com.dynast.civcraft.items.components;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import gpl.AttributeUtil;
import gpl.AttributeUtil.Attribute;
import gpl.AttributeUtil.AttributeType;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.items.units.Unit;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.loreenhancements.LoreEnhancementAttack;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;


public class AttackMetal extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		
		// Add generic attack damage of 0 to clear the default lore on item.
		attrs.add(Attribute.newBuilder().name("Attack").
				type(AttributeType.GENERIC_ATTACK_DAMAGE).
				amount(0).
				build());
		attrs.addLore(CivColor.Rose+""+this.getDouble("value")+" "+CivSettings.localize.localizedString("itemLore_Attack"));
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		
		Resident resident = CivGlobal.getResident(event.getPlayer());
		Player player = event.getPlayer();
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.Rose+CivSettings.localize.localizedString("itemLore_Warning")+" - "+CivColor.LightGray+CivSettings.localize.localizedString("itemLore_attackHalfDamage"));
		}


		if (Unit.isWearingFullLeather(player)) {
			CivMessage.sendErrorNoRepeat(player, CivSettings.localize.localizedString("itemLore_MetalAttack_errorLeather"));
			return;
		}

		if (resident.unit.equals("defender")) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("defender_dont_use_poleax"));
		}
	}

	private boolean offAxe(Player player) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(player.getInventory().getItemInOffHand());
		LoreCraftableMaterial craftMat2 = LoreCraftableMaterial.getCraftMaterial(player.getInventory().getItemInMainHand());

		return (craftMat != null && craftMat.hasComponent("AttackMetal") && craftMat.getConfigMaterial().tier == craftMat2.getConfigMaterial().tier);
	}
				
	@Override
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		AttributeUtil attrs = new AttributeUtil(inHand);
		double dmg = this.getDouble("value");

		Player player = (Player) event.getDamager();
		Resident res = CivGlobal.getResident(player);

		if (res.unit.equals("defender")) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("defender_dont_use_poleax"));
			dmg -= 2;
		}
				
		double extraAtt = 0.0;
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementAttack) {
				extraAtt +=  ((LoreEnhancementAttack)enh).getExtraAttack(attrs);
			}
		}
		dmg += extraAtt;

		if (res.unit.equals("berserker")) {
			dmg += 2.0;
			if (offAxe(player)) {
				dmg += 1.0;
			}
		}

		if (res.unit.equals("spy") || res.timedSpy > 0) {
			dmg -= 6;
		}

		if (Unit.isWearingFullLeather(player)) {
			event.setCancelled(true);
			CivMessage.sendError(player, CivSettings.localize.localizedString("itemLore_MetalAttack_errorLeather"));
			return;
		}
		
		if (event.getDamager() instanceof Player) {
			Resident resident = CivGlobal.getResident(((Player)event.getDamager()));
			if (!resident.hasTechForItem(inHand)) {
				dmg = dmg / 2;
			}
		}
		
		if (dmg < 0.5) {
			dmg = 0.5;
		}
		
		event.setDamage(dmg);
	}

}
