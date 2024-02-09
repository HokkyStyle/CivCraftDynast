package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.loreenhancements.LoreEnhancementDefense;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class Defense extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.Blue+""+this.getDouble("value")+" "+CivSettings.localize.localizedString("newItemLore_Defense"));
	}
	
	@Override
	public void onHold(PlayerItemHeldEvent event) {	
		
		Resident resident = CivGlobal.getResident(event.getPlayer());
		if (!resident.hasTechForItem(event.getPlayer().getInventory().getItem(event.getNewSlot()))) {		
			CivMessage.send(resident, CivColor.Rose+CivSettings.localize.localizedString("itemLore_Warning")+" - "+CivColor.LightGray+CivSettings.localize.localizedString("itemLore_defenseHalfPower"));
		}
	}
	
	@Override
	public void onDefense(EntityDamageByEntityEvent event, ItemStack stack) {
		double defValue = this.getDouble("value");
		
		/* Try to get any extra defense enhancements from this item. */
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) {
			return;
		}
				
		double extraDef = 0;
		AttributeUtil attrs = new AttributeUtil(stack);
		
		for (LoreEnhancement enh : attrs.getEnhancements()) {
			if (enh instanceof LoreEnhancementDefense) {
				extraDef +=  ((LoreEnhancementDefense)enh).getExtraDefense(attrs);
			}
		}
		
		defValue += extraDef;

		double damage = event.getDamage();
		
		if (event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			Resident resident = CivGlobal.getResident(player);
			if (resident.unit.equals("defender")) {
				defValue += 0.5;
			}
			if (!resident.hasTechForItem(stack)) {
				defValue = defValue / 2;
			}
		}
		
		damage -= defValue;
		if (damage < 0.5) {
			/* Always do at least 0.5 damage. */
			damage = 0.5;
		}
		
		event.setDamage(damage);
	}

}
