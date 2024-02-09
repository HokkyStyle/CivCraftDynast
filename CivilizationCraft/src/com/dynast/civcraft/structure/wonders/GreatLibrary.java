
package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigEnchant;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;

public class GreatLibrary extends Wonder {

	public GreatLibrary(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public GreatLibrary(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	@Override
	public void onComplete() {
		addBuffs();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}
	
	@Override
	protected void removeBuffs() {
		this.removeBuffFromCiv(this.getCiv(), "buff_greatlibrary_extra_beakers");
		this.removeBuffFromTown(this.getTown(), "buff_greatlibrary_double_tax_beakers");
	}

	@Override
	protected void addBuffs() {
		this.addBuffToCiv(this.getCiv(), "buff_greatlibrary_extra_beakers");
		this.addBuffToTown(this.getTown(), "buff_greatlibrary_double_tax_beakers");
	}
	
	
	@Override
	public void updateSignText() {
		
		for (StructureSign sign : getSigns()) {
			ConfigEnchant enchant;
			switch (sign.getAction().toLowerCase()) {
			case "0":
				enchant = CivSettings.enchants.get("ench_fortune");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
				break;
			case "1":
				enchant = CivSettings.enchants.get("ench_unbreaking");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
				break;
			case "2":
				enchant = CivSettings.enchants.get("ench_feather_falling");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);				
				break;			
			case "3":
				enchant = CivSettings.enchants.get("ench_punchout");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
				break;
			}
			
			sign.update();
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		//int special_id = Integer.valueOf(sign.getAction());
		Resident resident = CivGlobal.getResident(player);
		
		if (resident == null) {
			return;
		}
		
		if (!resident.hasTown() || resident.getCiv() != this.getCiv()) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("var_greatLibrary_nonMember",this.getCiv().getName()));
			return;
		}
		
		ItemStack hand = player.getInventory().getItemInMainHand();
		ConfigEnchant configEnchant;
		
		switch (sign.getAction()) {
		case "0": /* fire aspect */
			if (!Enchantment.LOOT_BONUS_BLOCKS.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;
			}
			
			configEnchant = CivSettings.enchants.get("ench_fortune");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 2);			
			break;
		case "1": /* fire protection */
			if (!Enchantment.DURABILITY.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			
			configEnchant = CivSettings.enchants.get("ench_unbreaking");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.DURABILITY, 1);			
			break;
		case "2": /* flame */
			if (!Enchantment.PROTECTION_FALL.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			
			configEnchant = CivSettings.enchants.get("ench_feather_falling");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.PROTECTION_FALL, 4);
			break;
		case "3":
			switch (ItemManager.getId(hand)) {
			case CivData.STONE_PICKAXE:
			case CivData.IRON_PICKAXE:
			case CivData.DIAMOND_PICKAXE:
			case CivData.GOLD_PICKAXE:
				configEnchant = CivSettings.enchants.get("ench_punchout");
				
				if (!LoreMaterial.isCustom(hand)) {					
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_nonEnchantable"));
					return;
				}
				
				if (LoreMaterial.hasEnhancement(hand, configEnchant.enchant_id)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_hasEnchantment"));
					return;
				}
				
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}
				
				resident.getTreasury().withdraw(configEnchant.cost);
				ItemStack newItem = LoreMaterial.addEnhancement(hand, LoreEnhancement.enhancements.get(configEnchant.enchant_id));				
				player.getInventory().setItemInMainHand(newItem);
				break;
			default:
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			break;
		default:
			return;
		}
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("library_enchantment_success"));
	}

}
