
package com.dynast.civcraft.questions;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Relation;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class ChangeRelationResponse implements QuestionResponseInterface {

	public Civilization fromCiv;
	public Civilization toCiv;
	public Relation.Status status;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			CivGlobal.setRelation(fromCiv, toCiv, status);
		} else {
			CivMessage.sendCiv(fromCiv, CivColor.LightGray+CivSettings.localize.localizedString("var_RequestDecline",toCiv.getName()));
		}
	}
	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
