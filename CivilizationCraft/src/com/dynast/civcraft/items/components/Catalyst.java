package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.util.CivColor;

public class Catalyst extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
		attrUtil.addLore(ChatColor.RESET+CivColor.Gold+CivSettings.localize.localizedString("itemLore_Catalyst"));
	}

	public ItemStack getEnchantedItem(ItemStack stack) {
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
		if (craftMat == null) {
			return null;
		}
		
		String materials[] = this.getString("allowed_materials").split(",");
		boolean found = false;
		for (String mat : materials) {
			mat = mat.replaceAll(" ", "");
			if (mat.equals(LoreMaterial.getMID(stack))) {
				found = true;
				break;
			}
		}
		
		if (!found) {
			return null;
		}
		
		String enhStr = this.getString("enhancement");

		LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
		if (enhance == null) {
			CivLog.error("Couldn't find enhancement titled:"+enhStr);
			return null;
		}
		
		if (enhance != null) {
			if (enhance.canEnchantItem(stack)) {
				AttributeUtil attrs = new AttributeUtil(stack);
				enhance.variables.put("amount", getString("amount"));
				attrs = enhance.add(attrs);	
				return attrs.getStack();
			}
		}
		
		return null;
	}
	
	public int getEnhancedLevel(ItemStack stack) {
		String enhStr = this.getString("enhancement");

		LoreEnhancement enhance = LoreEnhancement.enhancements.get(enhStr);
		if (enhance == null) {
			CivLog.error("Couldn't find enhancement titled:"+enhStr);
			return 0;
		}
		
		return (int)enhance.getLevel(new AttributeUtil(stack));
	}

	public boolean enchantSuccess(ItemStack stack, byte amount) {

			//int free_catalyst_amount = CivSettings.getInteger(CivSettings.civConfig, "global.free_catalyst_amount");
			//int extra_catalyst_amount = CivSettings.getInteger(CivSettings.civConfig, "global.extra_catalyst_amount");
			//double extra_catalyst_percent = CivSettings.getDouble(CivSettings.civConfig, "global.extra_catalyst_percent");
			
			//int level = getEnhancedLevel(stack);
			
			/*if (level <= free_catalyst_amount) {
				return true;
			}*/
			
			int chance = Integer.valueOf(getString("chance"));

			if (amount > 1) {
				chance =  (100 - chance) / (6 - amount) + chance;
				//chance = Math.round(chance + (amount * (chance/10)));
			}

			if (chance >= 95 && chance < 100) {
				chance = 92;
			} else if (chance >= 100) {
				chance = 95;
			}

			Random rand = new Random();
			//int extra = 0;
			int n = rand.nextInt(100)+1;
			
			/*if (level <= extra_catalyst_amount) {
				n -= (int)(extra_catalyst_percent*100);
			}*/
			
			//n += extra;
			
			/*if (n <= chance) {
				return true;
			}
			return false;*/

			return (n <= chance);
	}
}
