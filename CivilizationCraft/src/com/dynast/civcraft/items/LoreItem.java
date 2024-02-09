
package com.dynast.civcraft.items;

import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.dynast.civcraft.object.SQLObject;

public abstract class LoreItem extends SQLObject {
	/* 
	 * A lore item represents a custom item inside of civcraft which overloads the lore data.
	 */
	public enum Type {
		NORMAL,
		BONUSGOODIE,
	}
	
		
	private Type type;
	
	public abstract void load();	
	
	public LoreItem() {}

	public void setLore(ItemStack stack, List<String> lore) {
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}
	
	public List<String> getLore(ItemStack stack) {
		return stack.getItemMeta().getLore();
	}
	
	public void setDisplayName(ItemStack stack, String name) {
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(name);
		stack.setItemMeta(meta);
	}
	
	public String getDisplayName(ItemStack stack) {
		return stack.getItemMeta().getDisplayName();
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
}
