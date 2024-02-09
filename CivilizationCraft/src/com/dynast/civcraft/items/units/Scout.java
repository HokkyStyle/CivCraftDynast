package com.dynast.civcraft.items.units;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigUnit;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;
import gpl.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Scout extends UnitMaterial {

    Scout(String id, ConfigUnit unit) {
        super(id, unit);
    }

    public static void spawn(Inventory inv, Town town) throws CivException {
        spawn(inv);
    }

    public static void spawn(Inventory inv) throws  CivException {
        ItemStack stack = LoreMaterial.spawn(Unit.SCOUT_UNIT);

        AttributeUtil attr = new AttributeUtil(stack);
        attr.addLore(CivColor.LightGray+ CivSettings.localize.localizedString("scout_lore1"));
        attr.addLore(CivColor.LightGreen+CivSettings.localize.localizedString("scout_lore2"));
        attr.addLore(CivColor.LightGray+CivSettings.localize.localizedString("scout_lore3"));
        attr.addLore(CivColor.LightBlue+CivSettings.localize.localizedString("scout_lore4"));
        attr.addLore(CivColor.LightGray+CivSettings.localize.localizedString("scout_lore5"));
        attr.addLore(CivColor.Red+CivSettings.localize.localizedString("scout_lore6"));
        attr.addLore(CivColor.LightBlue+CivSettings.localize.localizedString("scout_lore7"));
        attr.addEnhancement("LoreEnhancementSoulBound", null, null);

        attr.setCivCraftProperty("unit", "scout");
        stack = attr.getStack();

        if (!Unit.addItemNoStack(inv, stack)) {
            throw new CivException(CivSettings.localize.localizedString("var_settler_errorBarracksFull",Unit.SCOUT_UNIT.getUnit().name));
        }
    }

    public static ItemStack spawn() {
        ItemStack stack = LoreMaterial.spawn(Unit.SCOUT_UNIT);

        AttributeUtil attr = new AttributeUtil(stack);
        attr.addLore(CivColor.LightGray+ CivSettings.localize.localizedString("scout_lore1"));
        attr.addLore(CivColor.LightGreen+CivSettings.localize.localizedString("scout_lore2"));
        attr.addLore(CivColor.LightGray+CivSettings.localize.localizedString("scout_lore3"));
        attr.addLore(CivColor.LightBlue+CivSettings.localize.localizedString("scout_lore4"));
        attr.addLore(CivColor.LightGray+CivSettings.localize.localizedString("scout_lore5"));
        attr.addLore(CivColor.Red+CivSettings.localize.localizedString("scout_lore6"));
        attr.addLore(CivColor.LightBlue+CivSettings.localize.localizedString("scout_lore7"));
        attr.addEnhancement("LoreEnhancementSoulBound", null, null);

        attr.setCivCraftProperty("unit", "scout");
        stack = attr.getStack();

        return stack;
    }

    public void onItemToPlayer(Player player, ItemStack stack) {
        Resident res = CivGlobal.getResident(player);
        if (res != null) {
            res.checkForUnits(player);
        }
    }

    public void onItemFromPlayer(Player player, ItemStack stack) {
        Resident res = CivGlobal.getResident(player);
        res.unit = "";
    }
}
