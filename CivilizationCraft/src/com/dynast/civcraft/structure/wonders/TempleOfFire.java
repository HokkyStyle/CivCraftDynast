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
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class TempleOfFire extends Wonder {

	public TempleOfFire(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public TempleOfFire(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	@Override
	protected void removeBuffs() {
		removeBuffFromCiv(this.getCiv(), "buff_fired_arrows");
	}

	@Override
	protected void addBuffs() {
		addBuffToCiv(this.getCiv(), "buff_fired_arrows");
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
	public void updateSignText() {
		
		for (StructureSign sign : getSigns()) {
			ConfigEnchant enchant;
			switch (sign.getAction().toLowerCase()) {
			case "0":
				enchant = CivSettings.enchants.get("ench_fire_protection");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
				break;
			case "1":
				enchant = CivSettings.enchants.get("ench_fire_aspect");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);
				break;
			case "2":
				enchant = CivSettings.enchants.get("ench_flame");
				sign.setText(enchant.name+"\n\n"+CivColor.LightGreen+enchant.cost+" "+CivSettings.CURRENCY_NAME);				
				break;			
			case "3":
				enchant = CivSettings.enchants.get("ench_infinity");
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
			CivMessage.sendError(player, CivSettings.localize.localizedString("var_templeoffire_nonMember",this.getCiv().getName()));
			return;
		}
		
		ItemStack hand = player.getInventory().getItemInMainHand();
		ConfigEnchant configEnchant;
		
		switch (sign.getAction()) {
		case "0": 
			if (!Enchantment.PROTECTION_FIRE.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;
			}
			
			configEnchant = CivSettings.enchants.get("ench_fire_protection");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.PROTECTION_FIRE, 4);
			break;
		case "1":
			if (!Enchantment.FIRE_ASPECT.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			
			configEnchant = CivSettings.enchants.get("ench_fire_aspect");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.FIRE_ASPECT, 2);			
			break;
		case "2": /* flame */
			if (!Enchantment.ARROW_FIRE.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			
			configEnchant = CivSettings.enchants.get("ench_flame");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.ARROW_FIRE, 1);	
			break;
		case "3":
			if (!Enchantment.ARROW_INFINITE.canEnchantItem(hand)) {
				CivMessage.sendError(player, CivSettings.localize.localizedString("library_enchant_cannotEnchant"));
				return;	
			}
			
			configEnchant = CivSettings.enchants.get("ench_infinity");
			if (!resident.getTreasury().hasEnough(configEnchant.cost)) {
				CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("var_library_enchant_cannotAfford",configEnchant.cost,CivSettings.CURRENCY_NAME));
				return;
			}
			
			resident.getTreasury().withdraw(configEnchant.cost);
			hand.addEnchantment(Enchantment.ARROW_INFINITE, 1);	
			break;
		}
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("library_enchantment_success"));
	}

}