package com.dynast.civcraft.interactive;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigGrocerLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.structure.Bank;
import com.dynast.civcraft.structure.Grocer;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.DropItemsTask;
import com.dynast.civcraft.util.CivColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class InteractiveBuyItems implements InteractiveResponse {
    Resident resident;
    Structure structure;
    String itemName;
    Material material;

    public InteractiveBuyItems(Resident resident, Structure structure, String itemName, Material material) {
        this.resident = resident;
        this.structure = structure;
        this.itemName = itemName;
        this.material = material;
        this.displayQuestion();
    }

    private void displayQuestion() {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        CivMessage.sendHeading(player, "Покупка предмета "+itemName);

        CivMessage.send(player, CivColor.Green+"Введите количество покупаемого предмета");
    }

    private void sellItems(int amount) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        ConfigGrocerLevel cgl;

        double price = 0;
        switch (material) {
            case IRON_BLOCK:
                price = CivSettings.iron_rate*9*CivGlobal.getMaxBankRate();
                break;
            case GOLD_BLOCK:
                price = CivSettings.gold_rate*9*CivGlobal.getMaxBankRate();
                break;
            case DIAMOND_BLOCK:
                price = CivSettings.diamond_rate*9*CivGlobal.getMaxBankRate();
                break;
            case EMERALD_BLOCK:
                price = CivSettings.emerald_rate*9*CivGlobal.getMaxBankRate();
                break;
            case COOKED_FISH:
                cgl = CivSettings.grocerLevels.get(1);
                price = cgl.price;
                break;
            case COOKED_RABBIT:
                cgl = CivSettings.grocerLevels.get(2);
                price = cgl.price;
                break;
            case GRILLED_PORK:
                cgl = CivSettings.grocerLevels.get(3);
                price = cgl.price;
                break;
            case GOLDEN_CARROT:
                cgl = CivSettings.grocerLevels.get(4);
                price = cgl.price;
                break;
            default:
                break;
        }

        if (!(resident.getTreasury().hasEnough(amount*price))) {
            CivMessage.sendError(resident, CivSettings.localize.localizedString("resident_notEnoughMoney")+" "+CivSettings.CURRENCY_NAME);
            return;
        }
        int am = amount;
        int maxSize = material.getMaxStackSize();
        HashMap<Integer, ItemStack> dropped;
        ItemStack item;
        if (am/maxSize > 1) {
            while ((am - maxSize) > maxSize) {
                am -= maxSize;
                item = new ItemStack(material, maxSize);
                dropped = player.getInventory().addItem(item);
                if (dropped.size() > 0) {
                    TaskMaster.syncTask(new DropItemsTask(dropped, player));
                }
            }
            item = new ItemStack(material, am);
            dropped = player.getInventory().addItem(item);
            if (dropped.size() > 0) {
                TaskMaster.syncTask(new DropItemsTask(dropped, player));
            }
        } else {
            item = new ItemStack(material, am);
            player.getInventory().addItem(item);
        }

        resident.getTreasury().withdraw(amount*price);
        CivMessage.send(resident, CivColor.LightGreen+"Куплен "+itemName+ ChatColor.RESET+""+CivColor.LightGreen+" в количестве "+amount+" за "+(int)(amount*price)+" "+CivSettings.CURRENCY_NAME);
    }

    @Override
    public void respond(String message, Resident resident) {
        Player player;
        try {
            player = CivGlobal.getPlayer(resident);
        } catch (CivException e) {
            return;
        }

        resident.clearInteractiveMode();
        int amount;

        try {
            amount = Integer.valueOf(message);
        } catch (NumberFormatException e) {
            CivMessage.sendError(player,"Введите целое число.");
            return;
        }

        if (amount == 0) {
            CivMessage.sendError(player,"Введите целое число.");
            return;
        }

        this.sellItems(amount);
    }
}
