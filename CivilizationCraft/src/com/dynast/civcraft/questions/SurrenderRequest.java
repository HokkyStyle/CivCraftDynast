
package com.dynast.civcraft.questions;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class SurrenderRequest implements QuestionResponseInterface {

	public Civilization fromCiv;
	public Civilization toCiv;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			fromCiv.onDefeat(toCiv);
			CivMessage.global(CivSettings.localize.localizedString("var_surrender_accepted",fromCiv.getName(),toCiv.getName()));
		} else {
			CivMessage.sendCiv(fromCiv, CivColor.LightGray+CivSettings.localize.localizedString("var_RequestDecline",toCiv.getName()));
		}
	}

	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
