
package com.dynast.civcraft.interactive;

import com.dynast.civcraft.object.Resident;

public interface InteractiveResponse {
	void respond(String message, Resident resident);
}
