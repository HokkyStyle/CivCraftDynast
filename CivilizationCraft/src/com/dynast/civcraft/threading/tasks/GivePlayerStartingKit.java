
package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.ItemManager;

public class GivePlayerStartingKit implements Runnable {

	public String name;
	
	public GivePlayerStartingKit(String name) {
		this.name = name;
	}
	
	@Override
	public void run() {
		try {
			Player player = CivGlobal.getPlayer(name);
			
			for (String kitItems : CivSettings.kitItems) {
				String[] split = kitItems.split(":");
				
				ItemStack stack;
				try {
					Integer type = Integer.valueOf(split[0]);
					Integer amount = Integer.valueOf(split[1]);

					stack = ItemManager.createItemStack(type, amount);
				} catch (NumberFormatException e) {
					String customMatID = split[0];
					LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(customMatID);
					if (craftMat == null) {
						CivLog.warning("Couldn't find custom material:"+customMatID+" to give to player on first join.");
						continue;
					}
					
					stack = LoreCraftableMaterial.spawn(craftMat);
				}
//				if (split[0] == "mat_tutorial_book" || split[0] == "mat_found_camp")
//				{
					stack = LoreCraftableMaterial.addEnhancement(stack, LoreEnhancement.enhancements.get("LoreEnhancementSoulBound"));
//				}
				
				player.getInventory().addItem(stack);
			}
			
			Resident resident = CivGlobal.getResident(name);
			if (resident != null) {
				resident.getTreasury().deposit(CivSettings.startingCoins);
				resident.setGivenKit(true);
			}
			
		
		} catch (CivException e) {
		//	e.printStackTrace();
			CivLog.warning("Tried to give starting kit to offline player:"+name);
			return;
		}
		
	}

}
