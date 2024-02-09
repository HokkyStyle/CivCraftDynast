package com.dynast.global.perks.components;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.interactive.InteractiveConfirmWeatherChange;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;

public class ChangeWeather extends PerkComponent {

	@Override
	public void onActivate(Resident resident) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}
		if (!player.getWorld().isThundering() && !player.getWorld().hasStorm()) {
			CivMessage.sendError(resident, CivSettings.localize.localizedString("weather_isSunny"));
			return;
		}
		
		CivMessage.sendHeading(resident, CivSettings.localize.localizedString("weather_heading"));
		CivMessage.send(resident, CivColor.Green+CivSettings.localize.localizedString("weather_confirmPrompt"));
		CivMessage.send(resident, CivColor.LightGray+CivSettings.localize.localizedString("weather_confirmPrompt2"));
		resident.setInteractiveMode(new InteractiveConfirmWeatherChange(this));
	}
}
