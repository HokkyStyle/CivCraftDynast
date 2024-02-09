
package com.dynast.civcraft.threading.sync.request;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.util.MultiInventory;

public class UpdateInventoryRequest extends AsyncRequest {

	public UpdateInventoryRequest(ReentrantLock lock) {
		super(lock);
	}

	public enum Action {
		ADD,
		REMOVE,
		SET
	}
	
	public MultiInventory multiInv;
	public ArrayList<StructureChest> schest;
	public Inventory inv;
	public ItemStack[] cont;
	public ItemStack stack;
	public Action action;
		
}
