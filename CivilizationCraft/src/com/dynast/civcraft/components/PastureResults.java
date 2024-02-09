package com.dynast.civcraft.components;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.components.TradeLevelComponent.Result;

public class PastureResults {

    private Result result;
    private int consumed;

	private List<ItemStack> returnCargo = new LinkedList<>();
    
    public PastureResults() {
		this.consumed = 0;
		this.result = Result.UNKNOWN;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public int getConsumed() {
		return consumed;
	}

	public void setConsumed(int consumed) {
		this.consumed = consumed;
	}
	
	public void addReturnCargo(ItemStack cargo) {
		this.returnCargo.add(cargo);
	}

	public List<ItemStack> getReturnCargo() {
		return returnCargo;
	}

	public void setReturnCargo(List<ItemStack> returnCargo) {
		this.returnCargo = returnCargo;
	}

}
