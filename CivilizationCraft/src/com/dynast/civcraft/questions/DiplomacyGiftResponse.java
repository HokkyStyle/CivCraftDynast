
package com.dynast.civcraft.questions;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class DiplomacyGiftResponse implements QuestionResponseInterface {

	public Object giftedObject;
	public Civilization fromCiv;
	public Civilization toCiv;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			
			if (giftedObject instanceof Town) {
				Town town = (Town)giftedObject;
				
				if (!toCiv.getTreasury().hasEnough(town.getGiftCost())) {
					CivMessage.sendCiv(toCiv, CivColor.Rose+CivSettings.localize.localizedString("var_diplomacy_gift_ErrorTooPoor",town.getName(),town.getGiftCost(),CivSettings.CURRENCY_NAME));
					CivMessage.sendCiv(fromCiv, CivColor.Rose+CivSettings.localize.localizedString("var_diplomacy_gift_ErrorTooPoor2",toCiv.getName(),town.getName(),town.getGiftCost(),CivSettings.CURRENCY_NAME));
					return;
				}
				
				toCiv.getTreasury().withdraw(town.getGiftCost());
				town.changeCiv(toCiv);
				CivMessage.sendCiv(fromCiv, CivColor.LightGray+CivSettings.localize.localizedString("var_diplomacy_gift_accept",toCiv.getName(),town.getName()));
				return;
			} else if (giftedObject instanceof Civilization) {
				int coins = fromCiv.getMergeCost();
				
				if (!toCiv.getTreasury().hasEnough(coins)) {
					CivMessage.sendCiv(toCiv, CivColor.Rose+CivSettings.localize.localizedString("var_diplomacy_merge_ErrorTooPoor",fromCiv.getName(),coins,CivSettings.CURRENCY_NAME));
					CivMessage.sendCiv(fromCiv, CivColor.Rose+CivSettings.localize.localizedString("var_diplomacy_merge_ErrorTooPoor2",toCiv.getName(),fromCiv.getName(),coins,CivSettings.CURRENCY_NAME));
					return;
				}
				
				toCiv.getTreasury().withdraw(coins);
				CivMessage.sendCiv(fromCiv, CivColor.Yellow+CivSettings.localize.localizedString("var_diplomacy_merge_offerAccepted",toCiv.getName()));
				toCiv.mergeInCiv(fromCiv);
				CivMessage.global(CivSettings.localize.localizedString("var_diplomacy_merge_SuccessAlert1",fromCiv.getName(),toCiv.getName()));
				return;
			} else {
				CivLog.error(CivSettings.localize.localizedString("diplomacy_merge_UnexpectedError")+" "+giftedObject);
				return;
			}
		} else {
			CivMessage.sendCiv(fromCiv, CivColor.LightGray+CivSettings.localize.localizedString("var_RequestDecline",toCiv.getName()));
		}
		
	}
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
