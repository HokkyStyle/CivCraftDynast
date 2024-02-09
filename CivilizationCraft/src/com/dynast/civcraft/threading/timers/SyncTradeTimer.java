
package com.dynast.civcraft.threading.timers;

import java.text.DecimalFormat;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.object.TradeGood;
import com.dynast.civcraft.util.CivColor;

public class SyncTradeTimer implements Runnable {

	public SyncTradeTimer() {
	}
	
	public void processTownsTradePayments(Town town) {
		
		//goodies = town.getEffectiveBonusGoodies();
		
		//double payment = TradeGood.getTownTradePayment(town, goodies);
		double payment = TradeGood.getTownTradePayment(town);
		DecimalFormat df = new DecimalFormat();
		
		if (payment > 0.0) {
			
			double taxesPaid = payment*town.getDepositCiv().getIncomeTaxRate();
			if (taxesPaid > 0) {
				CivMessage.sendTown(town, CivColor.LightGreen+
						CivSettings.localize.localizedString("var_syncTrade_payout",
								CivColor.Yellow+df.format(Math.round(payment))+
										CivColor.LightGreen+" "+CivSettings.CURRENCY_NAME)+
										CivColor.Yellow+CivSettings.localize.localizedString("var_cottage_grew_taxes",df.format(Math.round(taxesPaid)),town.getDepositCiv().getName()));
			} else {
				CivMessage.sendTown(town,
						CivColor.LightGreen+
								CivSettings.localize.localizedString("var_syncTrade_payout",
										CivColor.Yellow+df.format(Math.round(payment))+
												CivColor.LightGreen+" "+CivSettings.CURRENCY_NAME));
			}
			
			town.getTreasury().deposit(payment - taxesPaid);
			town.getDepositCiv().taxPayment(town, taxesPaid);
		}
	}
	
	@Override
	public void run() {
		if (!CivGlobal.tradeEnabled) {
			return;
		}

		CivGlobal.checkForDuplicateGoodies();
		
		for (Town town : CivGlobal.getTowns()) {
			try {
				processTownsTradePayments(town);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
