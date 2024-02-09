package com.dynast.civcraft.structure.wonders;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigEnchant;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import org.bukkit.Location;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Town;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FlyingDutchman extends Wonder {
	
	public FlyingDutchman(Location center, String id, Town town)
	        throws CivException {
		super(center, id, town);
	}
	
	public FlyingDutchman(ResultSet rs) 
			throws SQLException, CivException {
		super(rs);
	}

	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_ingermanland_fishing_boat_immunity");
		removeBuffFromCiv(this.getCiv(), "buff_flydutch_ingerdamage");
		removeBuffFromCiv(this.getCiv(), "buff_flydutch_shipdamage");
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_ingermanland_fishing_boat_immunity");
		addBuffToCiv(this.getCiv(), "buff_flydutch_ingerdamage");
		addBuffToCiv(this.getCiv(), "buff_flydutch_shipdamage");
	}
	
	public void onLoad() {
		if (this.isActive()) {
			addBuffs();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeBuffs();
	}
	
	@Override
	public void onComplete() {
		addBuffs();
	}

	@Override
	public void updateSignText() {
		for (StructureSign sign : getSigns()) {
			ConfigEnchant enchant;
			switch (sign.getAction().toLowerCase()) {
				case "0":
					enchant = CivSettings.enchants.get("ench_frost_walker");
					sign.setText(enchant.name+"\n\n"+ CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
					break;
				case "1":
					enchant = CivSettings.enchants.get("ench_depth_strider");
					sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
					break;
				case "2":
					enchant = CivSettings.enchants.get("ench_respiration");
					sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
					break;
				case "3":
					enchant = CivSettings.enchants.get("ench_aqua_affinity");
					sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
					break;
				case "4":
					enchant = CivSettings.enchants.get("ench_lure");
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
				if (!Enchantment.FROST_WALKER.canEnchantItem(hand)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
					return;
				}

				configEnchant = CivSettings.enchants.get("ench_frost_walker");
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}

				resident.getTreasury().withdraw(configEnchant.cost);
				hand.addEnchantment(Enchantment.FROST_WALKER, 2);
				break;
			case "1": /* fire protection */
				if (!Enchantment.DEPTH_STRIDER.canEnchantItem(hand)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
					return;
				}

				configEnchant = CivSettings.enchants.get("ench_depth_strider");
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}

				resident.getTreasury().withdraw(configEnchant.cost);
				hand.addEnchantment(Enchantment.DEPTH_STRIDER, 3);
				break;
			case "2": /* flame */
				if (!Enchantment.OXYGEN.canEnchantItem(hand)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
					return;
				}

				configEnchant = CivSettings.enchants.get("ench_respiration");
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}

				resident.getTreasury().withdraw(configEnchant.cost);
				hand.addEnchantment(Enchantment.OXYGEN, 3);
				break;
			case "3":
				if (!Enchantment.WATER_WORKER.canEnchantItem(hand)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
					return;
				}

				configEnchant = CivSettings.enchants.get("ench_aqua_affinity");
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}

				resident.getTreasury().withdraw(configEnchant.cost);
				hand.addEnchantment(Enchantment.WATER_WORKER, 1);
				break;
			case "4":
				if (!Enchantment.LURE.canEnchantItem(hand)) {
					CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
					return;
				}

				configEnchant = CivSettings.enchants.get("ench_lure");
				if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
					CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
					return;
				}

				resident.getTreasury().withdraw(configEnchant.cost);
				hand.addEnchantment(Enchantment.LURE, 3);
				break;
			default:
				return;
		}

		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("library_enchantment_success"));
	}

}
