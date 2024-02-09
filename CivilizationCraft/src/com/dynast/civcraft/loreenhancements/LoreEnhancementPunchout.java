package com.dynast.civcraft.loreenhancements;

import gpl.AttributeUtil;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.object.BuildableDamageBlock;
import com.dynast.civcraft.util.CivColor;

public class LoreEnhancementPunchout extends LoreEnhancement {
	
	public String getDisplayName() {
		return CivSettings.localize.localizedString("itemLore_Punchout");
	}
	
	public AttributeUtil add(AttributeUtil attrs) {
		attrs.addEnhancement("LoreEnhancementPunchout", null, null);
		attrs.addLore(CivColor.Gold+getDisplayName());
		return attrs;
	}
	
	@Override
	public int onStructureBlockBreak(BuildableDamageBlock sb, int damage) {
		Random rand = new Random();
		
		if (damage <= 1) {
			int r = rand.nextInt(100)+1;
			if (r <= 10) {
				damage += 3;
			} else if (r <= 25)	{
				damage += 2;
			} else if (r <= 50) {
				damage += 1;
			}
		}
		
		return damage; 
	}
	
	@Override
	public String serialize(ItemStack stack) {
		return "";
	}

	@Override
	public ItemStack deserialize(ItemStack stack, String data) {
		return stack;
	}

}