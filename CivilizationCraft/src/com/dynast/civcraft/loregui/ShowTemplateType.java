package com.dynast.civcraft.loregui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.lorestorage.LoreGuiItem;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Resident;
import com.dynast.global.perks.Perk;

public class ShowTemplateType implements GuiAction {

	@Override
	public void performAction(InventoryClickEvent event, ItemStack stack) {
		// TODO Auto-generated method stub
		Resident resident = CivGlobal.getResident((Player)event.getWhoClicked());
		String perk_id = LoreGuiItem.getActionData(stack, "perk");
		Perk perk = resident.perks.get(perk_id);
		if (perk != null) {
			if (perk.getIdent().startsWith("template_arctic"))
			{
				resident.showTemplatePerks("arctic");
			}
			else if (perk.getIdent().startsWith("template_atlantean"))
			{
				resident.showTemplatePerks("atlantean");
			}
			else if (perk.getIdent().startsWith("template_aztec"))
			{
				resident.showTemplatePerks("aztec");
			}
			else if (perk.getIdent().startsWith("template_cultist"))
			{
				resident.showTemplatePerks("cultist");
			}
			else if (perk.getIdent().startsWith("template_egyptian"))
			{
				resident.showTemplatePerks("egyptian");
			}
			else if (perk.getIdent().startsWith("template_elven"))
			{
				resident.showTemplatePerks("elven");
			}
			else if (perk.getIdent().startsWith("template_roman"))
			{
				resident.showTemplatePerks("roman");
			}
			else if (perk.getIdent().startsWith("template_hell"))
			{
				resident.showTemplatePerks("hell");
			}
			else if (perk.getIdent().startsWith("template_medieval"))
			{
				resident.showTemplatePerks("medieval");
			}
		} else {
			CivLog.error(perk_id+" "+CivSettings.localize.localizedString("loreGui_perkActivationFailed"));
		}
	}

}
