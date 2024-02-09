
package com.dynast.civcraft.questions;

import com.dynast.civcraft.object.Resident;

public interface QuestionResponseInterface {	
	void processResponse(String param);
	void processResponse(String response, Resident responder);
}
