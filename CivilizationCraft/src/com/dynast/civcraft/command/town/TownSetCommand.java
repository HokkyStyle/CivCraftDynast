
package com.dynast.civcraft.command.town;


import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import org.bukkit.entity.Player;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Bank;
import com.dynast.civcraft.structure.Blacksmith;
import com.dynast.civcraft.structure.Grocer;
import com.dynast.civcraft.structure.Library;
import com.dynast.civcraft.structure.ScoutShip;
import com.dynast.civcraft.structure.ScoutTower;
import com.dynast.civcraft.structure.Stable;
import com.dynast.civcraft.structure.Store;
import com.dynast.civcraft.structure.Structure;

public class TownSetCommand extends CommandBase {

	@Override
	public void init() {
		command = "/town set";
		displayName = CivSettings.localize.localizedString("cmd_town_set_name");
		
		commands.put("taxrate", CivSettings.localize.localizedString("cmd_town_set_taxrateDesc"));
		commands.put("flattax", CivSettings.localize.localizedString("cmd_town_set_flattaxDesc"));
		commands.put("bankfee", CivSettings.localize.localizedString("cmd_town_set_bankfeeDesc"));
		commands.put("storefee", CivSettings.localize.localizedString("cmd_town_set_storefeeDesc"));
		commands.put("grocerfee", CivSettings.localize.localizedString("cmd_town_set_grocerfeeDesc"));
		commands.put("libraryfee", CivSettings.localize.localizedString("cmd_town_set_libraryfeeDesc"));
		commands.put("blacksmithfee", CivSettings.localize.localizedString("cmd_town_set_blacksmithfeeDesc"));
		commands.put("stablefee", CivSettings.localize.localizedString("cmd_town_set_stablefeeDesc"));
		
		commands.put("scoutrate", CivSettings.localize.localizedString("cmd_town_set_scoutrateDesc"));
		
	}
	
	public void stablefee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		Structure struct = town.findStructureByConfigId("s_stable");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_stablefeeNone"));
		}
		
		Stable stable = (Stable)struct;
		
		if (feeInt < Stable.FEE_MIN || feeInt > Stable.FEE_MAX) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_stablefeeRates"));
		}
	
		stable.setNonResidentFee(((double)feeInt/100));
		stable.updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
	}
	
	public void scoutrate_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer rate = getNamedInteger(1);
		
		if (rate != 30 && rate != 60) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_scoutrateRates"));
		}
		
		for (Structure struct : town.getStructures()) {
			if (struct instanceof ScoutTower) {
				((ScoutTower)struct).setReportSeconds(rate);
			} else if (struct instanceof ScoutShip) {
				((ScoutShip)struct).setReportSeconds(rate);
			}
		}
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_scoutrateSuccess",rate));
	}
	
	public void blacksmithfee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		if (feeInt < 0 || feeInt > 15) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_rate5to15"));
		}
		
		Structure struct = town.findStructureByConfigId("s_blacksmith");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_blacksmithfeeNone"));
		}

		((Blacksmith)struct).setNonResidentFee(((double)feeInt/100));
		((Blacksmith)struct).updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
	}
	
	
	public void libraryfee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		if (feeInt < 0 || feeInt > 15) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_rate5to15"));
		}
		
		Structure struct = town.findStructureByConfigId("s_library");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_libraryfeeNone"));
		}

		((Library)struct).setNonResidentFee(((double)feeInt/100));
		((Library)struct).updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
	}
	
	public void grocerfee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		if (feeInt < 0 || feeInt > 15) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_rate5to15"));
		}
		
		Structure struct = town.findStructureByConfigId("s_grocer");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_grocerfeeNone"));
		}

		((Grocer)struct).setNonResidentFee(((double)feeInt/100));
		((Grocer)struct).updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
		
	}
	
	public void storefee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		if (feeInt < 0 || feeInt > 15) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_rate5to15"));
		}
		
		Structure struct = town.findStructureByConfigId("s_store");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_storefeeNone"));
		}
		
		((Store)struct).setNonResidentFee(((double)feeInt/100));
		((Store)struct).updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
		
	}
	
	public void bankfee_cmd() throws CivException {
		Town town = getSelectedTown();
		Integer feeInt = getNamedInteger(1);
		
		if (feeInt < 0 || feeInt > 15) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_rate5to15"));
		}
		
		Structure struct = town.findStructureByConfigId("s_bank");
		if (struct == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_bankfeeNone"));
		}
		
		((Bank)struct).setNonResidentFee(((double)feeInt/100));
		((Bank)struct).updateSignText();
		
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_town_set_feeSuccess",feeInt));
		
	}
	
	public void taxrate_cmd() throws CivException {
		Town town = getSelectedTown();
		
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_taxratePrompt"));
		}
		
		try { 
			town.setTaxRate(Double.valueOf(args[1])/100);
		} catch (NumberFormatException e) {
			throw new CivException(args[1]+" "+CivSettings.localize.localizedString("cmd_enterNumerError"));
		}
		
		town.quicksave();
		CivMessage.sendTown(town, CivSettings.localize.localizedString("var_cmd_town_set_taxrateSuccess",args[1]));
	}

	public void flattax_cmd() throws CivException {
		Town town = getSelectedTown();	
		if (args.length < 2) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_set_taxratePrompt"));
		}
				
		try { 
			town.setFlatTax(Integer.valueOf(args[1]));
		} catch (NumberFormatException e) {
			throw new CivException(args[1]+" "+CivSettings.localize.localizedString("cmd_enterNumerError"));
		}
		
		town.quicksave();
		CivMessage.send(town, CivSettings.localize.localizedString("var_cmd_town_set_flattaxSuccess",args[1]));
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
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		if (!town.playerIsInGroupName("mayors", player) && !town.playerIsInGroupName("assistants", player) && !town.getCiv().getLeaderGroup().hasMember(getResident())) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_claimNoPerm"));
		}		
	}

}
