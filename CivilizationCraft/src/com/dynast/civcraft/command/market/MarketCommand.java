
package com.dynast.civcraft.command.market;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;

public class MarketCommand extends CommandBase {

	@Override
	public void init() {
		command = "/market";
		displayName = CivSettings.localize.localizedString("cmd_market_Name");	
				
		commands.put("buy", CivSettings.localize.localizedString("cmd_market_buyDesc"));

	}

	public void buy_cmd() {
		MarketBuyCommand cmd = new MarketBuyCommand();	
		cmd.onCommand(sender, null, "buy", this.stripArgs(args, 1));
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
