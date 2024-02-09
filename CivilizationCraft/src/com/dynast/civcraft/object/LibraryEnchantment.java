
package com.dynast.civcraft.object;

import org.bukkit.enchantments.Enchantment;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.structure.Library;

public class LibraryEnchantment {
	public Enchantment enchant;
	public LoreEnhancement enhancement;
	public int level;
	public double price;
	public String name;
	public String displayName;

	public LibraryEnchantment(String name, int lvl, double p) throws CivException {
		enchant = Library.getEnchantFromString(name);
		if (enchant == null)  {
			enhancement = LoreEnhancement.enhancements.get(name);
			if (enhancement == null) {
				throw new CivException(CivSettings.localize.localizedString("libraryEnchantError1",name));
			}
		}
		level = lvl;
		price = p;
		
		this.name = name;
		if (enchant != null) {
			displayName = name.replace("_", " ");
		} else {
			displayName = enhancement.getDisplayName();
		}
		
	}
}
