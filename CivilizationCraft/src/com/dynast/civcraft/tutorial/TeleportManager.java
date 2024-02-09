package com.dynast.civcraft.tutorial;

import com.dynast.civcraft.camp.WarCamp;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.lorestorage.LoreGuiItem;
import com.dynast.civcraft.lorestorage.LoreGuiItemListener;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.structure.Capitol;
import com.dynast.civcraft.structure.RespawnLocationHolder;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.util.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TeleportManager {

    public static Inventory locationList = null;

    public void showLocationList(Player player, Civilization civ) {
        if (locationList == null) {
            locationList = Bukkit.getServer().createInventory(player, 9*6, CivSettings.localize.localizedString("teleport_manager_heading"));
            int i = 0;
            for (RespawnLocationHolder holder : civ.getAvailableRespawnables()) {
                if (civ.getAvailableRespawnables().size() < 54) {
                    locationList.setItem(i, getLocItem(holder));
                    i++;
                } else {

                }
            }

            LoreGuiItemListener.guiInventories.put(locationList.getName(), locationList);
        }

        if (player != null && player.isOnline() && player.isValid()) {
            player.openInventory(locationList);
        }
    }

    private ItemStack getLocItem(RespawnLocationHolder holder) {
        ItemStack item;
        String[] split = holder.getRespawnName().split("\n");
        String title = "", message = "";
        int id = 0;
        int data = 0;

        if (holder instanceof WarCamp) {
            title = split[0];
            message = split[1];
            id = ItemManager.getId(Material.BEDROCK);
        } else if (holder instanceof Capitol) {
            title = split[1];
            id = ItemManager.getId(Material.BRICK);
        } else if (holder instanceof TownHall) {
            title = split[1];
            id = ItemManager.getId(Material.COBBLESTONE);
        }
        item = LoreGuiItem.build(title, id, data, message);
        item = LoreGuiItem.setAction(item,"OpenInventory");
        item = LoreGuiItem.setActionData(item, "invType", "teleportWar");

        return item;

    }
}
