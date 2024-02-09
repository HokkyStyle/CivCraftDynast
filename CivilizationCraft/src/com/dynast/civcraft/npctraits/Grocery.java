package com.dynast.civcraft.npctraits;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigGrocerLevel;
import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.structure.Grocer;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;


@TraitName("GroceryTrait")
public class Grocery extends Trait {
    protected Grocer grocer;
    private boolean SomeSetting = false;
    private CivCraft plugin;

    public Grocery(Grocer grocer) {
        super("GroceryTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
        this.grocer = grocer;
        grocer.npc = this.getNPC();
    }

    public Grocery() {
        super("GroceryTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc != this.getNPC()) {
            return;
        }

        Inventory inventory = Bukkit.createInventory((Player)npc.getEntity(), 9, ChatColor.BLACK+"Бакалейщик");
        String buy = "Цена: ";
        String none = "НЕДОСТУПНО";

        Player player = event.getClicker();
        Grocer grocer = this.grocer;

        ItemStack fish = new ItemStack(Material.COOKED_FISH);
        ItemStack fishF = new ItemStack(Material.COOKED_FISH, 64);

        ItemStack rabbit = new ItemStack(Material.COOKED_RABBIT);
        ItemStack rabbitF = new ItemStack(Material.COOKED_RABBIT, 64);

        ItemStack pork = new ItemStack(Material.GRILLED_PORK);
        ItemStack porkF = new ItemStack(Material.GRILLED_PORK, 64);

        ItemStack carr = new ItemStack(Material.GOLDEN_CARROT);
        ItemStack carrF = new ItemStack(Material.GOLDEN_CARROT, 64);

        ItemStack feeList = new ItemStack(Material.PAPER);

        ItemMeta fishMeta = fish.getItemMeta();
        ItemMeta fishFMeta = fishF.getItemMeta();

        ItemMeta rabbitMeta = rabbit.getItemMeta();
        ItemMeta rabbitFMeta = rabbitF.getItemMeta();

        ItemMeta porkMeta = pork.getItemMeta();
        ItemMeta porkFMeta = porkF.getItemMeta();

        ItemMeta carrMeta = carr.getItemMeta();
        ItemMeta carrFMeta = carrF.getItemMeta();

        ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(1);

        int fishPrice = (int)(grocerlevel.price + grocerlevel.price * grocer.getNonResidentFee());

        grocerlevel = CivSettings.grocerLevels.get(2);
        int rabbitPrice = (int)(grocerlevel.price + grocerlevel.price * grocer.getNonResidentFee());

        grocerlevel = CivSettings.grocerLevels.get(3);
        int porkPrice = (int)(grocerlevel.price + grocerlevel.price * grocer.getNonResidentFee());

        grocerlevel = CivSettings.grocerLevels.get(4);
        int carrPrice = (int)(grocerlevel.price + grocerlevel.price * grocer.getNonResidentFee());

        fishMeta.setDisplayName(buy+fishPrice);
        fish.setItemMeta(fishMeta);

        fishFMeta.setDisplayName(buy+fishPrice*64);
        fishF.setItemMeta(fishFMeta);

        if (grocer.level >= 2) {
            rabbitMeta.setDisplayName(buy+rabbitPrice);
            rabbit.setItemMeta(rabbitMeta);

            rabbitFMeta.setDisplayName(buy+rabbitPrice*64);
            rabbitF.setItemMeta(rabbitFMeta);
        } else {
            rabbitMeta.setDisplayName(none);
            rabbit.setItemMeta(rabbitMeta);

            rabbitFMeta.setDisplayName(none);
            rabbitF.setItemMeta(rabbitFMeta);
        }

        if (grocer.level >= 3) {
            porkMeta.setDisplayName(buy+porkPrice);
            pork.setItemMeta(porkMeta);

            porkFMeta.setDisplayName(buy+porkPrice*64);
            porkF.setItemMeta(porkFMeta);
        } else {
            porkMeta.setDisplayName(none);
            pork.setItemMeta(porkMeta);

            porkFMeta.setDisplayName(none);
            porkF.setItemMeta(porkFMeta);
        }

        if (grocer.level >= 4) {
            carrMeta.setDisplayName(buy+carrPrice);
            carr.setItemMeta(carrMeta);

            carrFMeta.setDisplayName(buy+carrPrice*64);
            carrF.setItemMeta(carrFMeta);
        } else {
            carrMeta.setDisplayName(none);
            carr.setItemMeta(carrMeta);

            carrFMeta.setDisplayName(none);
            carr.setItemMeta(carrFMeta);
        }


        ItemMeta feeMeta = feeList.getItemMeta();
        feeMeta.setDisplayName(ChatColor.RESET+""+ChatColor.GOLD+""+ChatColor.BOLD+grocer.getNonResidentFeeString());
        feeList.setItemMeta(feeMeta);

        inventory.addItem(fish);
        inventory.addItem(fishF);

        inventory.addItem(rabbit);
        inventory.addItem(rabbitF);

        inventory.addItem(pork);
        inventory.addItem(porkF);

        inventory.addItem(carr);
        inventory.addItem(carrF);

        player.openInventory(inventory);
    }

}
