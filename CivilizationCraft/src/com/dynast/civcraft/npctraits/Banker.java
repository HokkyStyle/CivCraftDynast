package com.dynast.civcraft.npctraits;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.interactive.InteractiveBuyItems;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.structure.Bank;
import com.dynast.civcraft.util.CivColor;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@TraitName("BankerTrait")
public class Banker extends Trait {
    protected Bank bank;
    private boolean SomeSetting = false;
    private CivCraft plugin;

    public Banker(Bank bank) {
        super("BankerTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
        this.bank = bank;
        bank.npc = this.getNPC();
    }

    public Banker() {
        super("BankerTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc != this.getNPC()) {
            return;
        }

        Inventory inventory = Bukkit.createInventory((Player)npc.getEntity(), 36, ChatColor.BLACK+"Банкир");

        Player player = event.getClicker();
        Bank bank = this.bank;

        ItemStack list_sell = new ItemStack(Material.PAPER);
        ItemStack list_buy = new ItemStack(Material.PAPER);
        List<String> info = new LinkedList<>();
        info.add(ChatColor.RESET+"" +ChatColor.WHITE+"" +ChatColor.BOLD+"Железо: " +(int)(bank.getBankExchangeRate()*CivSettings.iron_rate)+"/"+(int)(bank.getBankExchangeRate()*CivSettings.iron_rate*9));
        info.add(ChatColor.RESET+""+ChatColor.GOLD+""+ChatColor.BOLD+"Золото: "+(int)(bank.getBankExchangeRate()*CivSettings.gold_rate)+"/"+(int)(bank.getBankExchangeRate()*CivSettings.gold_rate*9));
        info.add(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Алмазы: "+(int)(bank.getBankExchangeRate()*CivSettings.diamond_rate)+"/"+(int)(bank.getBankExchangeRate()*CivSettings.diamond_rate*9));
        info.add(ChatColor.RESET+""+ChatColor.GREEN+""+ChatColor.BOLD+"Изумруды: "+(int)(bank.getBankExchangeRate()*CivSettings.emerald_rate)+"/"+(int)(bank.getBankExchangeRate()*CivSettings.emerald_rate*9));
        info.add(ChatColor.RESET+""+ChatColor.GOLD+""+ChatColor.BOLD+bank.getNonResidentFeeString());

        ItemMeta sell_meta = list_sell.getItemMeta();
        sell_meta.setDisplayName(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Цена за слиток/блок:");
        sell_meta.setLore(info);

        ItemMeta buy_meta = list_buy.getItemMeta();
        buy_meta.setDisplayName(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Купить блоки");

        list_sell.setItemMeta(sell_meta);
        list_buy.setItemMeta(buy_meta);

        inventory.addItem(list_buy);
        inventory.addItem(list_sell);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event == null || event.getClickedInventory() == null) {
            return;
        }

        if (!event.getClickedInventory().getName().equals(ChatColor.BLACK+"Банкир") &&
                !event.getClickedInventory().getName().equals(ChatColor.BLACK+"Банкир - покупка минералов")) {
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

        ItemStack clickedItem = event.getCurrentItem();
        String itemName = clickedItem.getItemMeta().getDisplayName();
        if (itemName.equals(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Цена за слиток/блок:")) {
            return;
        } else if (itemName.equals(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Купить блоки")) {
            Inventory inventory = Bukkit.createInventory((Player)this.getNPC().getEntity(), 9, ChatColor.BLACK+"Банкир - покупка минералов");
            ItemStack ironB = new ItemStack(Material.IRON_BLOCK);
            ItemMeta ironMeta = ironB.getItemMeta();
            List<String> info = new LinkedList<>();

            ironMeta.setDisplayName(ChatColor.RESET+""+ChatColor.WHITE+""+ChatColor.BOLD+"Купить железный блок");
            info.add(ChatColor.RESET+""+ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Цена: "+(int)(CivSettings.iron_rate*9*CivGlobal.getMaxBankRate()));

            ironMeta.setLore(info);
            ironB.setItemMeta(ironMeta);

            ItemStack goldB = new ItemStack(Material.GOLD_BLOCK);
            ItemMeta goldMeta = goldB.getItemMeta();
            List<String> infoG = new LinkedList<>();

            goldMeta.setDisplayName(ChatColor.RESET+""+ChatColor.GOLD+""+ChatColor.BOLD+"Купить золотой блок");
            infoG.add(ChatColor.RESET+""+ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Цена: "+(int)(CivSettings.gold_rate*9*CivGlobal.getMaxBankRate()));

            goldMeta.setLore(infoG);
            goldB.setItemMeta(goldMeta);

            ItemStack diamondB = new ItemStack(Material.DIAMOND_BLOCK);
            ItemMeta diamondMeta = diamondB.getItemMeta();
            List<String> infoD = new LinkedList<>();

            diamondMeta.setDisplayName(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Купить алмазный блок");
            infoD.add(ChatColor.RESET+""+ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Цена: "+(int)(CivSettings.diamond_rate*9*CivGlobal.getMaxBankRate()));

            diamondMeta.setLore(infoD);
            diamondB.setItemMeta(diamondMeta);

            ItemStack emeraldB = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta emeraldMeta = emeraldB.getItemMeta();
            List<String> infoE = new LinkedList<>();

            emeraldMeta.setDisplayName(ChatColor.RESET+""+ChatColor.GREEN+""+ChatColor.BOLD+"Купить изумрудный блок");
            infoE.add(ChatColor.RESET+""+ChatColor.LIGHT_PURPLE+""+ChatColor.BOLD+"Цена: "+(int)(CivSettings.emerald_rate*9*CivGlobal.getMaxBankRate()));

            emeraldMeta.setLore(infoE);
            emeraldB.setItemMeta(emeraldMeta);


            inventory.addItem(ironB);
            inventory.addItem(goldB);
            inventory.addItem(diamondB);
            inventory.addItem(emeraldB);

            player.openInventory(inventory);
            return;
        }

        if (itemName.equals(ChatColor.RESET+""+ChatColor.WHITE+""+ChatColor.BOLD+"Купить железный блок") ||
                itemName.equals(ChatColor.RESET+""+ChatColor.GOLD+""+ChatColor.BOLD+"Купить золотой блок") ||
                itemName.equals(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Купить алмазный блок") ||
                itemName.equals(ChatColor.RESET+""+ChatColor.GREEN+""+ChatColor.BOLD+"Купить изумрудный блок")) {
            event.setCancelled(true);
            player.closeInventory();
            if (!res.isInteractiveMode()) {
                res.setInteractiveMode(new InteractiveBuyItems(res, bank, clickedItem.getItemMeta().getDisplayName().replace("Купить ", ""), clickedItem.getType()));
            } else {
                CivMessage.sendError(res, "Нельзя сейчас купить минералы.");
            }
        }
    }

    private void buyItems(Player player, List<ItemStack> stacks) {
        Resident res = CivGlobal.getResident(player);
        int ironIngots = 0, goldIngots = 0, diamonds = 0, emeralds = 0;

        for (ItemStack is : stacks) {
            switch (is.getType()) {
                case IRON_INGOT:
                    ironIngots += is.getAmount();
                    break;
                case IRON_BLOCK:
                    ironIngots += is.getAmount()*9;
                    break;
                case GOLD_INGOT:
                    goldIngots += is.getAmount();
                    break;
                case GOLD_BLOCK:
                    goldIngots += is.getAmount()*9;
                    break;
                case DIAMOND:
                    diamonds += is.getAmount();
                    break;
                case DIAMOND_BLOCK:
                    diamonds += is.getAmount()*9;
                    break;
                case EMERALD:
                    emeralds += is.getAmount();
                    break;
                case EMERALD_BLOCK:
                    emeralds += is.getAmount()*9;
                    break;
                default:
                    break;
            }
        }

        double exRate = this.bank.getBankExchangeRate();

        if (ironIngots > 0) {
            int moneys = (int)Math.round(ironIngots*CivSettings.iron_rate*exRate);
            res.getTreasury().deposit(moneys);
            CivMessage.send(player, CivColor.LightGreen+"Продано "+ironIngots+" железа на сумму: "+moneys+" "+CivSettings.CURRENCY_NAME);
        }

        if (goldIngots > 0) {
            int moneys = (int)Math.round(goldIngots*CivSettings.gold_rate*exRate);
            res.getTreasury().deposit(moneys);
            CivMessage.send(player, CivColor.LightGreen+"Продано "+goldIngots+" золота на сумму: "+moneys+" "+CivSettings.CURRENCY_NAME);
        }

        if (diamonds > 0) {
            int moneys = (int)Math.round(diamonds*CivSettings.diamond_rate*exRate);
            res.getTreasury().deposit(moneys);
            CivMessage.send(player, CivColor.LightGreen+"Продано "+diamonds+" алмазов на сумму: "+moneys+" "+CivSettings.CURRENCY_NAME);
        }

        if (emeralds > 0) {
            int moneys = (int)Math.round(emeralds*CivSettings.emerald_rate*exRate);
            res.getTreasury().deposit(moneys);
            CivMessage.send(player, CivColor.LightGreen+"Продано "+emeralds+" изумрудов на сумму: "+moneys+" "+CivSettings.CURRENCY_NAME);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event == null) {
            return;
        }

        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        if (!(event.getInventory().getName().equals(ChatColor.BLACK+"Банкир"))) {
            return;
        }

        Inventory inv = event.getInventory();

        if (inv.getHolder() != this.getNPC().getEntity()) {
            return;
        }

        Player player = (Player)event.getPlayer();
        List<ItemStack> itemList = new LinkedList<>();
        for (ItemStack stack : inv.getContents()) {
            if (stack == null) {
                continue;
            }
            String name;
            if (stack.getItemMeta() != null && stack.getItemMeta().getDisplayName() != null) {
                name = stack.getItemMeta().getDisplayName();
                if (name.equals(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Цена за слиток/блок:") ||
                        name.equals(ChatColor.RESET+""+ChatColor.BLUE+""+ChatColor.BOLD+"Купить блоки")) {
                    continue;
                }
            }
            Material type = stack.getType();

            if (type == Material.IRON_INGOT || type == Material.IRON_BLOCK ||
                    type == Material.GOLD_INGOT || type == Material.GOLD_BLOCK ||
                    type == Material.DIAMOND || type == Material.DIAMOND_BLOCK ||
                    type == Material.EMERALD || type == Material.EMERALD_BLOCK) {
                LoreMaterial mat = LoreMaterial.getMaterial(stack);
                if (mat == null) {
                    itemList.add(stack);
                    continue;
                }
            }

            HashMap<Integer, ItemStack> stacks = player.getInventory().addItem(stack);
            if (stacks.size() > 0) {
                for (ItemStack is : stacks.values()) {
                    player.getLocation().getWorld().dropItem(player.getLocation(), is);
                }
            }
        }

        if (itemList.size() > 0) {
            this.buyItems(player, itemList);
        }
    }
}
