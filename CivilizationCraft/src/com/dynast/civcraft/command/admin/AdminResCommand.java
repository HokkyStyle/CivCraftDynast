
package com.dynast.civcraft.command.admin;

import java.sql.SQLException;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.AlreadyRegisteredException;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidNameException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;

public class AdminResCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad res";
		displayName = CivSettings.localize.localizedString("adcmd_res_Name");
		
		commands.put("settown", CivSettings.localize.localizedString("adcmd_res_setTownDesc"));
		commands.put("setcamp", CivSettings.localize.localizedString("adcmd_res_setCampDesc"));
		commands.put("cleartown", CivSettings.localize.localizedString("adcmd_res_clearTownDesc"));
		commands.put("enchant", CivSettings.localize.localizedString("adcmd_res_enchantDesc"));
		commands.put("rename", CivSettings.localize.localizedString("adcmd_res_renameDesc"));
		commands.put("giveexpjob", CivSettings.localize.localizedString("adcmd_res_giveExpJobDesc"));
		commands.put("setexpjob", CivSettings.localize.localizedString("adcmd_res_setExpJobDesc"));
		commands.put("setleveljob", CivSettings.localize.localizedString("adcmd_res_setLevelJobDesc"));
	}
	
	public void rename_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		String newName = getNamedString(2, CivSettings.localize.localizedString("adcmd_res_renamePrompt"));

		
		
		Resident newResident = CivGlobal.getResident(newName);
		if (newResident != null) {
			throw new CivException(CivSettings.localize.localizedString("var_adcmd_res_renameExists",newResident.getName(),resident.getName()));
		}
		
		/* Create a dummy resident to make sure name is valid. */
		try {
			new Resident(null, newName);
		} catch (InvalidNameException e1) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_renameInvalid"));
		}
		
		/* Delete the old resident object. */
		try {
			resident.delete();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new CivException(e.getMessage());
		}
		
		/* Remove resident from CivGlobal tables. */
		CivGlobal.removeResident(resident);
		
		/* Change the resident's name. */
		try {
			resident.setName(newName);
		} catch (InvalidNameException e) {
			e.printStackTrace();
			throw new CivException(CivSettings.localize.localizedString("internalCommandException")+" "+e.getMessage());
		}
		
		/* Resave resident to DB and global tables. */
		CivGlobal.addResident(resident);
		resident.save();
		
		CivMessage.send(sender, CivSettings.localize.localizedString("adcmd_res_renameSuccess"));
	}
	
	public void enchant_cmd() throws CivException {
		Player player = getPlayer();
		String enchant = getNamedString(1, CivSettings.localize.localizedString("adcmd_res_enchantHeading"));
		int level = getNamedInteger(2);
		
		
		ItemStack stack = player.getInventory().getItemInMainHand();
		Enchantment ench = Enchantment.getByName(enchant);
		if (ench == null) {
			String out ="";
			for (Enchantment ench2 : Enchantment.values()) {
				out += ench2.getName()+",";
			}
			throw new CivException(CivSettings.localize.localizedString("var_adcmd_res_enchantInvalid1",enchant,out));
		}
		
		stack.addUnsafeEnchantment(ench, level);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("adcmd_res_enchantSuccess"));
	}
	
	public void cleartown_cmd() throws CivException {
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("EnterPlayerName"));
		}
				
		Resident resident = getNamedResident(1);
		
		if (resident.hasTown()) {
			resident.getTown().removeResident(resident);
		}
		
		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_cleartownSuccess",resident.getName()));

	}
	
	public void setcamp_cmd() throws CivException {		
		Resident resident = getNamedResident(1);
		Camp camp = getNamedCamp(2);

		if (resident.hasCamp()) {
			resident.getCamp().removeMember(resident);
		}		
		
		camp.addMember(resident);
		
		camp.save();
		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setcampSuccess",resident.getName(),camp.getName()));
	}
	
	
	public void settown_cmd() throws CivException {
		
		if (args.length < 3) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_settownPrompt"));
		}
		
		Resident resident = getNamedResident(1);

		Town town = getNamedTown(2);

		if (resident.hasTown()) {
			resident.getTown().removeResident(resident);
		}
		
		try {
			town.addResident(resident);
		} catch (AlreadyRegisteredException e) {
			e.printStackTrace();
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_settownErrorInTown"));
		}
		
		town.save();
		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setTownSuccess",resident.getName(),town.getName()));
	}

	public void giveexpjob_cmd() throws CivException {
		String type = getNamedString(2, CivSettings.localize.localizedString("adcmd_res_jobNamedPrompt"));

		if (args.length < 4) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveExpJobPrompt"));
		}

		Resident resident = getNamedResident(1);
		int amount = getNamedInteger(3);


		switch (type) {
			case "digger":
				resident.addExpJobDigger(amount);
				break;
			case "miner":
				resident.addExpJobMiner(amount);
				break;
			case "woodcutter":
				resident.addExpJobWoodcutter(amount);
				break;
			case "fisherman":
				resident.addExpJobFisherman(amount);
				break;
			case "farmer":
				resident.addExpJobFarmer(amount);
				break;
			case "hunter":
				resident.addExpJobHunter(amount);
				break;
			default:
				throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveJobInvalidJobName"));
		}

		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_giveExpJobSuccess", resident.getName(), amount, type));
	}

	public void setexpjob_cmd() throws CivException {
		String type = getNamedString(2, CivSettings.localize.localizedString("adcmd_res_jobNamedPrompt"));

		if (args.length < 4) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveExpJobPrompt"));
		}

		Resident resident = getNamedResident(1);
		int amount = getNamedInteger(3);

		switch (type) {
			case "digger":
				resident.setExpJobDigger(amount);
				break;
			case "miner":
				resident.setExpJobMiner(amount);
				break;
			case "woodcutter":
				resident.setExpJobWoodcutter(amount);
				break;
			case "fisherman":
				resident.setExpJobFisherman(amount);
				break;
			case "farmer":
				resident.setExpJobFarmer(amount);
				break;
			case "hunter":
				resident.setExpJobHunter(amount);
				break;
			default:
				throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveJobInvalidJobName"));
		}

		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setedExpJobSuccess", resident.getName(), amount, type));
	}

	public void setleveljob_cmd() throws CivException {
		String type = getNamedString(2, CivSettings.localize.localizedString("adcmd_res_jobNamedPrompt"));

		if (args.length < 4) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveExpJobPrompt"));
		}

		Resident resident = getNamedResident(1);
		int amount = getNamedInteger(3);

		if (amount < 0) {
			throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
		}

		switch (type) {
			case "digger":
				if (amount > CivSettings.getMaxJobDiggerLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobDigger(amount);
				break;
			case "miner":
				if (amount > CivSettings.getMaxJobMinerLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobMiner(amount);
				break;
			case "woodcutter":
				if (amount > CivSettings.getMaxJobWoodcutterLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobWoodcutter(amount);
				break;
			case "fisherman":
				if (amount > CivSettings.getMaxJobFishermanLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobFisherman(amount);
				break;
			case "farmer":
				if (amount > CivSettings.getMaxJobFarmerLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobFarmer(amount);
				break;
			case "hunter":
				if (amount > CivSettings.getMaxJobHunterLevel()) {
					throw new CivException(CivSettings.localize.localizedString("adcmd_res_setLevel_invalidNumber"));
				}
				resident.setLevelJobHunter(amount);
				break;
			default:
				throw new CivException(CivSettings.localize.localizedString("adcmd_res_giveJobInvalidJobName"));
		}

		resident.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_adcmd_res_setedExpJobSuccess", resident.getName(), amount, type));
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
