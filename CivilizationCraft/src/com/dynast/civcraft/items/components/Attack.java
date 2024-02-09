package com.dynast.civcraft.items.components;

import com.dynast.civcraft.template.Template;
import gpl.AttributeUtil;
import gpl.AttributeUtil.Attribute;
import gpl.AttributeUtil.AttributeType;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.loreenhancements.LoreEnhancementAttack;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;


public class Attack extends ItemComponent {

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
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.Rose+CivSettings.localize.localizedString("itemLore_Warning")+" - "+CivColor.LightGray+CivSettings.localize.localizedString("itemLore_attackHalfDamage"));
		}
	}
	
	@Override
	public void onAttack(EntityDamageByEntityEvent event, ItemStack inHand) {
		AttributeUtil attrs = new AttributeUtil(inHand);
		double dmg = this.getDouble("value");
				
		double extraAtt = 0.0;
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementAttack) {
				extraAtt +=  ((LoreEnhancementAttack)enh).getExtraAttack(attrs);
			}
		}
		dmg += extraAtt;
		
		if (event.getDamager() instanceof Player) {
			Resident resident = CivGlobal.getResident(((Player)event.getDamager()));
			if (event.getEntity() instanceof Player) {
				if (resident.unit.equals("assassin")) {
					Player attacker = (Player) event.getDamager();
					Player entity = (Player) event.getEntity();
					if (resident.firstDam) {
						dmg += 4;
					}
					if (Template.getDirection(attacker.getLocation()).equals(Template.getDirection(entity.getLocation()))) {
						dmg += 5;
					}
				}
			}

			if (resident.unit.equals("spy") || resident.timedSpy > 0) {
				dmg -= 6;
			}

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
