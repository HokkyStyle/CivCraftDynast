
package com.dynast.civcraft.command.town;

import java.util.ArrayList;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigTownUpgrade;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Library;
import com.dynast.civcraft.structure.Store;

public class TownResetCommand extends CommandBase {

	@Override
	public void init() {
		command = "/town reset";
		displayName = CivSettings.localize.localizedString("cmd_town_reset_name");
		
		commands.put("library", CivSettings.localize.localizedString("cmd_town_reset_libraryDesc"));
		commands.put("store", CivSettings.localize.localizedString("cmd_town_reset_storeDesc"));
	}

	public void library_cmd() throws CivException {
		Town town = getSelectedTown();
		
		Library library = (Library) town.findStructureByConfigId("s_library");
		if (library == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_reset_libraryNone"));
		}
		
		ArrayList<ConfigTownUpgrade> removeUs = new ArrayList<>();
		for(ConfigTownUpgrade upgrade : town.getUpgrades().values()) {
			if (upgrade.action.contains("enable_library_enchantment")) {
				removeUs.add(upgrade);
			}
		}
		
		for (ConfigTownUpgrade upgrade : removeUs) {
			town.removeUpgrade(upgrade);
		}
		
		library.reset();
		
		town.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_reset_librarySuccess"));
	}
	
	public void store_cmd() throws CivException {
		Town town = getSelectedTown();
		
		Store store = (Store) town.findStructureByConfigId("s_store");
		if (store == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_town_reset_storeNone"));
		}
		
		ArrayList<ConfigTownUpgrade> removeUs = new ArrayList<>();
		for(ConfigTownUpgrade upgrade : town.getUpgrades().values()) {
			if (upgrade.action.contains("set_store_material")) {
				removeUs.add(upgrade);
			}
		}
		
		for (ConfigTownUpgrade upgrade : removeUs) {
			town.removeUpgrade(upgrade);
		}
		
		store.reset();
		
		town.save();
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_town_reset_storeSuccess"));
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		this.showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		this.validMayorAssistantLeader();
	}

}
