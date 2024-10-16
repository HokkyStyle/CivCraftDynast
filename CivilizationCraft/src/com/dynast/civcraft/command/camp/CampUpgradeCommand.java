
package com.dynast.civcraft.command.camp;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigCampUpgrade;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.util.CivColor;

public class CampUpgradeCommand extends CommandBase {
	@Override
	public void init() {
		command = "/camp upgrade";
		displayName = CivSettings.localize.localizedString("cmd_camp_upgrade_name");
		
		
		commands.put("list", CivSettings.localize.localizedString("cmd_camp_upgrade_listDesc"));
		commands.put("purchased", CivSettings.localize.localizedString("cmd_camp_upgrade_purchasedDesc"));
		commands.put("buy", CivSettings.localize.localizedString("cmd_camp_upgrade_buyDesc"));
		
	}

	public void purchased_cmd() throws CivException {
		Camp camp = this.getCurrentCamp();
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_purchasedSuccess"));

		String out = "";
		for (ConfigCampUpgrade upgrade : camp.getUpgrades()) {
			out += upgrade.name+", ";
		}
		
		CivMessage.send(sender, out);
	}
	
	private void list_upgrades(Camp camp) throws CivException {				
		for (ConfigCampUpgrade upgrade : CivSettings.campUpgrades.values()) {
			if (upgrade.isAvailable(camp)) {
				CivMessage.send(sender, upgrade.name+" "+CivColor.LightGray+CivSettings.localize.localizedString("Cost")+" "+CivColor.Yellow+upgrade.cost);
			}
		}
	}
	
	public void list_cmd() throws CivException {
		Camp camp = this.getCurrentCamp();

		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_list"));	
		list_upgrades(camp);		
	}
	
	public void buy_cmd() throws CivException {
		Camp camp = this.getCurrentCamp();

		if (args.length < 2) {
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_list"));
			list_upgrades(camp);		
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_camp_upgrade_buyHeading"));
			return;
		}
				
		String combinedArgs = "";
		args = this.stripArgs(args, 1);
		for (String arg : args) {
			combinedArgs += arg + " ";
		}
		combinedArgs = combinedArgs.trim();
		
		ConfigCampUpgrade upgrade = CivSettings.getCampUpgradeByNameRegex(camp, combinedArgs);
		if (upgrade == null) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_camp_upgrade_buyInvalid",combinedArgs));
		}
		
		if (camp.hasUpgrade(upgrade.id)) {
			throw new CivException(CivSettings.localize.localizedString("cmd_camp_upgrade_buyOwned"));
		}
		
		camp.purchaseUpgrade(upgrade);
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_camp_upgrade_buySuccess",upgrade.name));
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
		this.validCampOwner();
	}
}
