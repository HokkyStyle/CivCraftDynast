
package com.dynast.civcraft.command.market;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.war.War;

public class MarketBuyCommand extends CommandBase {

	@Override
	public void init() {
		command = "/market buy";
		displayName = CivSettings.localize.localizedString("cmd_market_buy_Name");
		
		commands.put("towns", CivSettings.localize.localizedString("cmd_market_buy_townsDesc"));
		commands.put("civs", CivSettings.localize.localizedString("cmd_market_buy_civsDesc"));
		
	}
	
	private void list_towns_for_sale(Civilization ourCiv) {
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_market_buy_townsHeading"));
		for (Town town : CivGlobal.getTowns()) {
			if (!town.isCapitol()) {
				if (town.isForSale()) {
					CivMessage.send(sender, town.getName()+" - "+CivColor.Yellow+
							df.format(town.getForSalePrice())+" "+CivSettings.CURRENCY_NAME);
				}
			}
		}
		
	}
	
	private void list_civs_for_sale(Civilization ourCiv) {
		
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_market_buy_civsHeading"));
		for (Civilization civ : CivGlobal.getCivs()) {
				if (civ.isForSale()) {
					CivMessage.send(sender, civ.getName()+" - "+CivColor.Yellow+
							df.format(civ.getTotalSalePrice())+" "+CivSettings.CURRENCY_NAME);
				}
		}
	}
	
	public void towns_cmd() throws CivException {
		this.validLeaderAdvisor();
		Civilization senderCiv = this.getSenderCiv();
		
		if (args.length < 2) {
			list_towns_for_sale(senderCiv);
			CivMessage.send(sender, CivSettings.localize.localizedString("cmd_market_buy_townsPrompt"));
			return;
		}
		
		Town town = getNamedTown(1);
		
		if (senderCiv.isForSale()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_ErrorCivForSale"));
		}
		
		if (town.getCiv() == senderCiv) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_townsOwned"));
		}
		
		if (town.isCapitol()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_townCapitol"));
		}
		
		if (!town.isForSale()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_NotForSale"));
		}
		
		if (War.isWarTime() || War.isWithinWarDeclareDays()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_warOrClose"));
		}
		
		senderCiv.buyTown(town);
		CivMessage.global(CivSettings.localize.localizedString("var_cmd_market_buy_townsBroadcast",town.getName(),senderCiv.getName()));
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_market_buy_townsSuccess",args[1]));
	}
	
	
	public void civs_cmd() throws CivException {
		this.validLeaderAdvisor();
		Civilization senderCiv = this.getSenderCiv();
		
		if (args.length < 2) {
			list_civs_for_sale(senderCiv);
			CivMessage.send(sender, "Use /market buy civs [civ-name] to buy this civ.");
			return;
		}
		
		Civilization civBought = getNamedCiv(1);
		
		if (senderCiv.isForSale()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_ErrorCivForSale"));
		}
		
		if (civBought == senderCiv) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_civsOwned"));
		}
		
		if (!civBought.isForSale()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_NotForSale"));
		}
		
		if (War.isWarTime() || War.isWithinWarDeclareDays()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_market_buy_warOrClose"));
		}
		
		senderCiv.buyCiv(civBought);
		CivMessage.global(CivSettings.localize.localizedString("var_cmd_market_buy_civsSuccess",civBought.getName(),senderCiv.getName()));
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("var_cmd_market_buy_civsSuccess2",args[1]));
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
