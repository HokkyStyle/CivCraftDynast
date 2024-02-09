package com.dynast.civcraft.fishing;

import java.util.*;

import com.dynast.civcraft.config.ConfigJobLevels;
import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigFishing;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import org.bukkit.scheduler.BukkitScheduler;

public class FishingListener implements Listener {
	
	private ArrayList<ConfigFishing> getRandomDrops() {
		Random rand = new Random();		
		ArrayList<ConfigFishing> dropped = new ArrayList<>();
		
		for (ConfigFishing d : CivSettings.fishingDrops) {
			int chance = rand.nextInt(10000);
			if (chance < (d.drop_chance*10000)) {
				dropped.add(d);
			}
			
		}
		return dropped;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	 public void onPlayerFish (PlayerFishEvent event) {
		 Player player = event.getPlayer();
		 Material mat = player.getTargetBlock((Set<Material>) null, 10).getType();
		 Random rand = new Random();
		 if (mat.equals(Material.IRON_DOOR) ||
				 mat.equals(Material.STRING) ||
				 mat.equals(Material.TRIPWIRE) ||
				 mat.equals(Material.NOTE_BLOCK) ||
				 mat.equals(Material.OAK_TRAPDOOR) ||
				 mat.equals(Material.IRON_TRAPDOOR) ||
				 mat.equals(Material.ACACIA_FENCE_GATE) ||
				 mat.equals(Material.BIRCH_FENCE_GATE) ||
				 mat.equals(Material.DARK_OAK_FENCE_GATE) ||
				 mat.equals(Material.OAK_FENCE) ||
			 	 mat.equals(Material.SPRUCE_FENCE) ||
				 mat.equals(Material.BIRCH_FENCE) ||
		 		 mat.equals(Material.JUNGLE_FENCE) ||
				 mat.equals(Material.ACACIA_FENCE) ||
				 mat.equals(Material.DARK_OAK_FENCE) ||
				 mat.equals(Material.JUNGLE_FENCE_GATE) ||
				 mat.equals(Material.SPRUCE_FENCE_GATE)) {
			 event.setCancelled(true);
			 event.getHook().remove();
			 return;
		 }

		 if (event.getState() == PlayerFishEvent.State.FISHING) {
			 BukkitScheduler sh = CivCraft.getPlugin().getServer().getScheduler();
			 World world = event.getPlayer().getWorld();
			 Location loc = event.getPlayer().getLocation();
			 //loc.setX(loc.getX()+2);
			 sh.scheduleSyncDelayedTask(CivCraft.getPlugin(), () -> world.playSound(loc, Sound.ENTITY_PLAYER_SWIM, 0.2f, 1), 30L);
			 //sh.scheduleSyncDelayedTask(CivCraft.getPlugin(), () -> world.playSound(loc, rand.nextInt(2) == 1 ? Sound.ENTITY_PIG_AMBIENT : Sound.ENTITY_COW_AMBIENT, 0.2f, 1), 30L);
			 //sh.scheduleSyncDelayedTask(CivCraft.getPlugin(), () -> world.playSound(loc, Sound.ENTITY_CHICKEN_AMBIENT, 0.2f, 1), 40L);
		 }

		 if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
			 //Player player = event.getPlayer();
			 Location loc = player.getLocation();
			 Resident res = CivGlobal.getResident(player);

			 ItemStack stack = null;
			 int amount = 0;
			 if (res.jobLevelFisherman >= 1) {
			 	amount = rand.nextInt(res.jobLevelFisherman*6)+res.jobLevelFisherman*6;
			 }

			 ArrayList<ConfigFishing> dropped = getRandomDrops();
			 event.setExpToDrop(amount);
			 event.getCaught().remove();

			 if (dropped.size() == 0) {
				 stack = ItemManager.createItemStack(ItemManager.getId(Material.RAW_FISH), 1);
				 HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
				 for (ItemStack is : leftovers.values()) {
					 player.getWorld().dropItem(player.getLocation(), is);
				 }
				 CivMessage.send(event.getPlayer(), CivColor.LightGreen+CivSettings.localize.localizedString("var_fishing_success",CivColor.LightPurple+CivSettings.localize.localizedString("fishing_rawFish")));

			 } else {
				 for (ConfigFishing d : dropped) {
					 if (d.craftMatId == null || d.craftMatId == "" || d.craftMatId.equals("")) {
					 	 String split[] = d.type_id.split(":");
						 stack = new ItemStack(Integer.valueOf(split[0]), 1, (short)0, Byte.valueOf(split[1]));
					 	 //stack = ItemManager.createItemStack(Integer.valueOf(split[0]), 1, Short.valueOf(split[1]));
					 	 CivMessage.send(event.getPlayer(), CivColor.LightGreen+CivSettings.localize.localizedString("var_fishing_success",CivColor.LightPurple+stack.getType().name().replace("_", " ").toLowerCase()));
					 } else {
						 LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.craftMatId);
						 if (craftMat != null) {
							 stack = LoreCraftableMaterial.spawn(craftMat);
							 CivMessage.send(event.getPlayer(), CivColor.LightGreen+CivSettings.localize.localizedString("var_fishing_success",CivColor.LightPurple+craftMat.getName()));
						 }
					 }
					 if (stack != null) {
						 HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
						 for (ItemStack is : leftovers.values()) {
							 player.getWorld().dropItem(player.getLocation(), is);
						 }
					 }
				 }
			 }
			 event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2f, 1);
			 player.updateInventory();

			 res.jobExpFisherman++;

			 ConfigJobLevels level = CivSettings.fishermanLevels.get(res.jobLevelFisherman);

			 if (res.jobLevelFisherman != CivSettings.getMaxJobFishermanLevel()) {
				 if (res.jobExpFisherman >= level.amount) {
					 res.jobLevelFisherman++;
					 CivMessage.send(res, CivColor.LightGreen+CivSettings.localize.localizedString("var_res_uplevelFisherman", res.jobLevelFisherman));
				 }
			 }
		 }
	 }
}
