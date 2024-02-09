
package com.dynast.civcraft.items.components;

import gpl.AttributeUtil;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.util.CivColor;

public class Soulbound extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.Gold+CivSettings.localize.localizedString("Soulbound"));
	}

}
