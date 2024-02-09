package com.dynast.civcraft.command.admin;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.loreenhancements.LoreEnhancementArenaItem;
import com.dynast.civcraft.loreenhancements.LoreEnhancementAttack;
import com.dynast.civcraft.loreenhancements.LoreEnhancementDefense;
import com.dynast.civcraft.loreenhancements.LoreEnhancementSoulBound;
import com.dynast.civcraft.loreenhancements.LoreEnhancementPunchout;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.ItemManager;

public class AdminItemCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad item";
		displayName = CivSettings.localize.localizedString("adcmd_item_cmdDesc");
		
		commands.put("enhance", CivSettings.localize.localizedString("adcmd_item_enhanceDesc"));
		commands.put("give", CivSettings.localize.localizedString("adcmd_item_giveDesc"));
	}

	public void give_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		String id = getNamedString(2, CivSettings.localize.localizedString("adcmd_item_givePrompt")+" materials.yml");
		int amount = getNamedInteger(3);
		
		Player player = CivGlobal.getPlayer(resident);
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(id);
		if (craftMat == null) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_item_giveInvalid")+id);
		}
		
		ItemStack stack = LoreCraftableMaterial.spawn(craftMat);
		
		stack.setAmount(amount);
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
		for (ItemStack is : leftovers.values()) {
			player.getWorld().dropItem(player.getLocation(), is);
		}
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("adcmd_item_giveSuccess"));
	}
	
	public void enhance_cmd() throws CivException {
		Player player = getPlayer();
		HashMap<String, LoreEnhancement> enhancements = new HashMap<>();
		ItemStack inHand = getPlayer().getInventory().getItemInMainHand();
		
		enhancements.put("soulbound", new LoreEnhancementSoulBound());
		enhancements.put("attack", new LoreEnhancementAttack());
		enhancements.put("defence", new LoreEnhancementDefense());
		enhancements.put("arena", new LoreEnhancementArenaItem());
		enhancements.put("punchout", new LoreEnhancementPunchout());

		if (inHand == null || ItemManager.getId(inHand) == CivData.AIR) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_item_enhanceNoItem"));
		}
		
		if (args.length < 2) {
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("adcmd_item_enhancementList"));
			String out = "";
			for (String str : enhancements.keySet()) {
				out += str + ", ";
			}
			CivMessage.send(sender, out);
			return;
		}
		
		String name = getNamedString(1, "enchantname");
		//int i = getNamedInteger(2);
		name.toLowerCase();
		for (String str : enhancements.keySet()) {
			if (name.equals(str)) {
				LoreEnhancement enh = enhancements.get(str);
				ItemStack stack = LoreMaterial.addEnhancement(inHand, enh);
				player.getInventory().setItemInMainHand(stack);
				CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_item_enhanceSuccess",name));
				return;
			}
		}
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		
	}

}
