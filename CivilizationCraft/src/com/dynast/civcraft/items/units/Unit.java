package com.dynast.civcraft.items.units;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigMission;
import com.dynast.civcraft.config.ConfigUnit;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.util.ItemManager;

public abstract class Unit {
	
	static Spy SPY_UNIT;
	static Settler SETTLER_UNIT;
	public static Berserker BERSERKER_UNIT;
	public static Scout SCOUT_UNIT;
	public static Bowman BOWMAN_UNIT;
	public static Defender DEFENDER_UNIT;
	public static Alchemist ALCHEMIST_UNIT;
	public static Angel ANGEL_UNIT;
	public static Assassin ASSASSIN_UNIT;
	public static Wizard WIZARD_UNIT;
	
	public static void init() {
		
		SPY_UNIT = new Spy("u_spy", CivSettings.units.get("u_spy"));
		
		for (ConfigMission mission : CivSettings.missions.values()) {
			if (mission.slot > 0) {
				MissionBook book = new MissionBook(mission.id, Spy.BOOK_ID, (short)0);
				book.setName(mission.name);
				book.setupLore(book.getId());
				book.setParent(SPY_UNIT);
				book.setSocketSlot(mission.slot);
				SPY_UNIT.addMissionBook(book);
			}
		}
		
		SETTLER_UNIT = new Settler("u_settler", CivSettings.units.get("u_settler"));
		BERSERKER_UNIT = new Berserker("u_berserker", CivSettings.units.get("u_berserker"));
		SCOUT_UNIT = new Scout("u_scout", CivSettings.units.get("u_scout"));
		BOWMAN_UNIT = new Bowman("u_bowman", CivSettings.units.get("u_bowman"));
		DEFENDER_UNIT = new Defender("u_defender", CivSettings.units.get("u_defender"));
		ALCHEMIST_UNIT = new Alchemist("u_alchemist", CivSettings.units.get("u_alchemist"));
		//ANGEL_UNIT = new Angel("u_angel", CivSettings.units.get("u_angel"));
		ASSASSIN_UNIT = new Assassin("u_assassin", CivSettings.units.get("u_assassin"));
		//WIZARD_UNIT = new Wizard("u_wizard", CivSettings.units.get("u_wizard"));
	}
	
	public Unit() {
	}
	
	
	public Unit(Inventory inv) throws CivException {
				
	}
	
	
	static boolean addItemNoStack(Inventory inv, ItemStack stack) {
						
			ItemStack[] contents = inv.getContents();
			for (int i = 0; i < contents.length; i++) {
				if (contents[i] == null) {
					contents[i] = stack;
					inv.setContents(contents);
					return true;
				}
			}
			
			return false;
	}

	public static ConfigUnit getPlayerUnit(Player player) {
		
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack == null) {
				continue;
			}
			
			LoreMaterial material = LoreMaterial.getMaterial(stack);
			if (material != null && (material instanceof UnitMaterial)) {
				
				if(!UnitMaterial.validateUnitUse(player, stack)) {
					return null;
				}
				
				
				return ((UnitMaterial)material).getUnit();
			}
		}
		
		return null;
	}

	public static void removeUnit(Player player) {
		
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack != null) {
				LoreMaterial material = LoreMaterial.getMaterial(stack);
				if (material != null) {
					if (material instanceof UnitMaterial) {
						player.getInventory().remove(stack);
						continue;
					}
					
					if (material instanceof UnitItemMaterial) {
						player.getInventory().remove(stack);
					}
					
				}
			}
		}
		player.updateInventory();
	}

	public static boolean isWearingFullLeather(Player player) {
		
		try {
			if (ItemManager.getId(player.getEquipment().getBoots()) != CivData.LEATHER_BOOTS) {
				return false;
			}
			
			if (ItemManager.getId(player.getEquipment().getChestplate()) != CivData.LEATHER_CHESTPLATE) {
				return false;
			}
			
			if (ItemManager.getId(player.getEquipment().getHelmet()) != CivData.LEATHER_HELMET) {
				return false;
			}
			
			if (ItemManager.getId(player.getEquipment().getLeggings()) != CivData.LEATHER_LEGGINGS) {
				return false;
			}
		
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}
	
	public static boolean isWearingFullComposite(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().contains("mat_composite_leather"))) {
				return false;
			}
		}
		return true;	
	}
	
	public static boolean isWearingFullHardened(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().contains("mat_hardened_leather"))) {
				return false;
			}
		}
		return true;	
	}
	
	public static boolean isWearingFullRefined(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().contains("mat_refined_leather"))) {
				return false;
			}
			
		}
		return true;	
	}
	
	public static boolean isWearingFullBasicLeather(Player player) {
		
		for (ItemStack stack : player.getInventory().getArmorContents()) {
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
			if (craftMat == null) {
				return false;
			}
			
			if ((!craftMat.getConfigId().contains("mat_carved_leather"))) {
				return false;
			}
			
			
		}
		return true;	
	}
	
	public static boolean isWearingAnyMetal(Player player) {
		return isWearingAnyChain(player) || isWearingAnyGold(player) || isWearingAnyIron(player) || isWearingAnyDiamond(player);
	}
	
	public static boolean isWearingAnyChain(Player player) {
		
		if (player.getEquipment().getBoots() != null) {
			if (player.getEquipment().getBoots().getType().equals(Material.CHAINMAIL_BOOTS)) {
				return true;
			}
		}
		
		if (player.getEquipment().getChestplate() != null) {
			if (player.getEquipment().getChestplate().getType().equals(Material.CHAINMAIL_CHESTPLATE)) {
				return true;
			}
		}
		
		if (player.getEquipment().getHelmet() != null) {
			if (player.getEquipment().getHelmet().getType().equals(Material.CHAINMAIL_HELMET)) {
				return true;
			}
		}
		
		if (player.getEquipment().getLeggings() != null) {
			if (player.getEquipment().getLeggings().getType().equals(Material.CHAINMAIL_LEGGINGS)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean isWearingAnyGold(Player player) {
		
		if (player.getEquipment().getBoots() != null) {
			if (player.getEquipment().getBoots().getType().equals(Material.GOLD_BOOTS)) {
				return true;
			}
		}
		
		if (player.getEquipment().getChestplate() != null) {
			if (player.getEquipment().getChestplate().getType().equals(Material.GOLD_CHESTPLATE)) {
				return true;
			}
		}
		
		if (player.getEquipment().getHelmet() != null) {
			if (player.getEquipment().getHelmet().getType().equals(Material.GOLD_HELMET)) {
				return true;
			}
		}
		
		if (player.getEquipment().getLeggings() != null) {
			if (player.getEquipment().getLeggings().getType().equals(Material.GOLD_LEGGINGS)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static boolean isWearingAnyIron(Player player) {
		
		if (player.getEquipment().getBoots() != null) {
			if (ItemManager.getId(player.getEquipment().getBoots()) == CivData.IRON_BOOTS) {
				return true;
			}
		}
		
		if (player.getEquipment().getChestplate() != null) {
			if (ItemManager.getId(player.getEquipment().getChestplate()) == CivData.IRON_CHESTPLATE) {
				return true;
			}
		}
		
		if (player.getEquipment().getHelmet() != null) {
			if (ItemManager.getId(player.getEquipment().getHelmet()) == CivData.IRON_HELMET) {
				return true;
			}
		}
		
		if (player.getEquipment().getLeggings() != null) {
			if (ItemManager.getId(player.getEquipment().getLeggings()) == CivData.IRON_LEGGINGS) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isWearingAnyDiamond(Player player) {
		
		if (player.getEquipment().getBoots() != null) {
			if (ItemManager.getId(player.getEquipment().getBoots()) == CivData.DIAMOND_BOOTS) {
				return true;
			}
		}
		
		if (player.getEquipment().getChestplate() != null) {
			if (ItemManager.getId(player.getEquipment().getChestplate()) == CivData.DIAMOND_CHESTPLATE) {
				return true;
			}
		}
		
		if (player.getEquipment().getHelmet() != null) {
			if (ItemManager.getId(player.getEquipment().getHelmet()) == CivData.DIAMOND_HELMET) {
				return true;
			}
		}
		
		if (player.getEquipment().getLeggings() != null) {
			if (ItemManager.getId(player.getEquipment().getLeggings()) == CivData.DIAMOND_LEGGINGS) {
				return true;
			}
		}
		
		return false;
	}

	public static boolean isWearingFullHeavy(Player player) {
		ItemStack armor[] = {player.getEquipment().getBoots(),
							player.getEquipment().getLeggings(),
							player.getEquipment().getChestplate(),
							player.getEquipment().getHelmet()};

		return (armor[0] != null && bootsIsMetal(armor[0])) &&
				(armor[1] != null && leggingsIsMetal(armor[1])) &&
				(armor[2] != null && chestplateIsMetal(armor[2])) &&
				(armor[3] != null && helmetIsMetal(armor[3]));
	}

	public static boolean bootsIsLeather(ItemStack boots) {
		return ItemManager.getId(boots) == CivData.LEATHER_BOOTS;
	}

	public static boolean bootsIsMetal(ItemStack boots) {
		return ItemManager.getId(boots) == CivData.IRON_BOOTS ||
				ItemManager.getId(boots) == CivData.CHAIN_BOOTS ||
				ItemManager.getId(boots) == CivData.GOLD_BOOTS ||
				ItemManager.getId(boots) == CivData.DIAMOND_BOOTS;
	}

	public static boolean leggingsIsLeather(ItemStack leggings) {
		return ItemManager.getId(leggings) == CivData.LEATHER_LEGGINGS;
	}

	public static boolean leggingsIsMetal(ItemStack leggings) {
		return ItemManager.getId(leggings) == CivData.IRON_LEGGINGS ||
				ItemManager.getId(leggings) == CivData.CHAIN_LEGGINGS ||
				ItemManager.getId(leggings) == CivData.GOLD_LEGGINGS ||
				ItemManager.getId(leggings) == CivData.DIAMOND_LEGGINGS;
	}

	public static boolean chestplateIsLeather(ItemStack chestplate) {
		return ItemManager.getId(chestplate) == CivData.LEATHER_CHESTPLATE;
	}

	public static boolean chestplateIsMetal(ItemStack chestplate) {
		return ItemManager.getId(chestplate) == CivData.IRON_CHESTPLATE ||
				ItemManager.getId(chestplate) == CivData.CHAIN_CHESTPLATE ||
				ItemManager.getId(chestplate) == CivData.GOLD_CHESTPLATE ||
				ItemManager.getId(chestplate) == CivData.DIAMOND_CHESTPLATE;
	}

	public static boolean helmetIsLeather(ItemStack helmet) {
		return ItemManager.getId(helmet) == CivData.LEATHER_HELMET;
	}

	public static boolean helmetIsMetal(ItemStack helmet) {
		return ItemManager.getId(helmet) == CivData.IRON_HELMET ||
				ItemManager.getId(helmet) == CivData.CHAIN_HELMET ||
				ItemManager.getId(helmet) == CivData.GOLD_HELMET ||
				ItemManager.getId(helmet) == CivData.DIAMOND_HELMET;
	}

	public static boolean isSuitableBerserker(Player player) {
		ItemStack armor[] = {player.getEquipment().getBoots(),
							player.getEquipment().getLeggings(),
							player.getEquipment().getChestplate(),
							player.getEquipment().getHelmet()};

		return (armor[0] != null && bootsIsMetal(armor[0])) &&
				(armor[1] != null && leggingsIsLeather(armor[1])) &&
				(armor[2] != null && chestplateIsLeather(armor[2])) &&
				(armor[3] != null && helmetIsMetal(armor[3]));

	}

	public static boolean isSuitableScout(Player player) {
		ItemStack armor[] = {player.getEquipment().getBoots(),
							player.getEquipment().getLeggings(),
							player.getEquipment().getChestplate(),
							player.getEquipment().getHelmet()};

		return (armor[0] != null && bootsIsLeather(armor[0])) &&
				(armor[1] != null && leggingsIsLeather(armor[1])) &&
				(armor[2] != null && chestplateIsLeather(armor[2])) &&
				(armor[3] == null || ItemManager.getId(armor[3]) == CivData.AIR);
	}

	public static boolean isSuitableBowman(Player player) {
		return isWearingFullLeather(player);
	}

	public static boolean isSuitableDefender(Player player) {
		return isWearingFullHeavy(player) &&
				(player.getInventory().getItemInOffHand().getType() == Material.SHIELD ||
				player.getInventory().getItemInMainHand().getType() == Material.SHIELD);
	}

	public static boolean isSuitableAssassin(Player player) {
		ItemStack armor[] = {player.getEquipment().getBoots(),
				player.getEquipment().getLeggings(),
				player.getEquipment().getChestplate(),
				player.getEquipment().getHelmet()};

		return (armor[0] != null && bootsIsLeather(armor[0])) &&
				(armor[1] != null && leggingsIsLeather(armor[1])) &&
				(armor[2] != null && chestplateIsLeather(armor[2])) &&
				(armor[3] != null && helmetIsMetal(armor[3]));
	}
}
