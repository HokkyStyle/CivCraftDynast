
package com.dynast.civcraft.util;

import org.bukkit.inventory.Inventory;

public class ItemInvPair {
	public Inventory inv;
	public String mid;
	public int type;
	public short data;
	public int amount;
	
	public ItemInvPair (Inventory inv, String mid, int type, short data, int amount) {
		this.inv = inv;
		this.mid = mid;
		this.type = type;
		this.data = data;
		this.amount = amount;
	}
}