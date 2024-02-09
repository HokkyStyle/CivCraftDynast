package com.dynast.civcraft.npctraits;

import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;

public class TeleportMaster extends Trait {
    private boolean SomeSetting = false;
    CivCraft plugin;

    public TeleportMaster() {
        super("TeleportMater");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc != this.getNPC()) {
            return;
        }
        Inventory inv = Bukkit.createInventory((Player)npc.getEntity(), 9, ChatColor.BLACK+"Выбор локации");

        Player pl = event.getClicker();

        ItemStack witherTP = new ItemStack(Material.PAPER);
        ItemStack dragonTP = new ItemStack(Material.PAPER);

        List<String> infoWither = new LinkedList<>();
        infoWither.add(ChatColor.RESET+""+ChatColor.GOLD+"Телепорт на локацию с "+ChatColor.GRAY+"ЗАСУШЕННЫМ");
        infoWither.add(ChatColor.RESET+""+ChatColor.BLUE+"Стоимость телепорта: "+ChatColor.GOLD+"2,500"+ChatColor.BLUE+" монет");

        List<String> infoDragon = new LinkedList<>();
        infoDragon.add(ChatColor.RESET+""+ChatColor.DARK_GRAY+"Телепорт на локацию ужасов, в логово "+ChatColor.DARK_RED+"АЛДУИНА");
        infoDragon.add(ChatColor.RESET+""+ChatColor.BLUE+"Стоимость телепорта: "+ChatColor.GOLD+"5,000"+ChatColor.BLUE+" монет");

        ItemMeta witherMeta = witherTP.getItemMeta();
        ItemMeta dragonMeta = dragonTP.getItemMeta();

        witherMeta.setDisplayName(ChatColor.RESET+""+ChatColor.WHITE+"За ЗАСУШЕННЫМ!");
        witherMeta.setLore(infoWither);

        dragonMeta.setDisplayName(ChatColor.RESET+""+ChatColor.WHITE+"На АЛДУИНА!");
        dragonMeta.setLore(infoDragon);

        witherTP.setItemMeta(witherMeta);
        dragonTP.setItemMeta(dragonMeta);

        inv.addItem(witherTP);
        inv.addItem(dragonTP);

        pl.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event == null || event.getClickedInventory() == null) {
            return;
        }

        if (!event.getClickedInventory().getName().equals(ChatColor.BLACK+"Выбор локации")) {
            return;
        }

        Player player = null;
        Resident res = null;

        if (event.getWhoClicked() instanceof Player) {
            event.setCancelled(true);
            player = (Player)event.getWhoClicked();
            res = CivGlobal.getResident(player);
        }

        if (player == null || res == null) {
            return;
        }

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getItemMeta().getDisplayName() == null) {
            return;
        }

        if (event.getClickedInventory().getHolder() != this.getNPC().getEntity()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        String itemName = clicked.getItemMeta().getDisplayName();
        player.closeInventory();

        if (itemName.equals(ChatColor.RESET+""+ChatColor.WHITE+"За ЗАСУШЕННЫМ!")) {
            if (!res.getTreasury().hasEnough(2500)) {
                CivMessage.sendError(res, "Не могу перевести Вас в другое измерение без звонкого мешочка.");
            } else {
                if (CivGlobal.locForTpToWither == null) {
                    CivMessage.sendError(res,"Не могу перевести Вас в другое измерение, портал не готов.");
                } else {
                    res.getTreasury().withdraw(2500);
                    player.teleport(CivGlobal.locForTpToWither);
                }
            }
        } else if (itemName.equals(ChatColor.RESET+""+ChatColor.WHITE+"На АЛДУИНА!")) {
            if (!res.getTreasury().hasEnough(5000)) {
                CivMessage.sendError(res, "Не могу перевести Вас в другое измерение без звонкого мешочка.");
            } else {
                if (CivGlobal.locForTpToDragon == null) {
                    CivMessage.sendError(res, "Не могу перевести Вас в другое измерение, портал не готов.");
                } else {
                    res.getTreasury().withdraw(5000);
                    player.teleport(CivGlobal.locForTpToDragon);
                }
            }
        }
    }
}
