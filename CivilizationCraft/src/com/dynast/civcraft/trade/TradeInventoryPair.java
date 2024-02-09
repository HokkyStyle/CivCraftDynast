package com.dynast.civcraft.trade;

import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.object.Resident;

public class TradeInventoryPair {
	public Inventory inv;
	public Inventory otherInv;
	public Resident resident;
	public Resident otherResident;
	public double coins;
	public double otherCoins;
	public boolean valid;
}
