package com.dynast.civcraft.interactive;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.TimeTools;
import com.dynast.global.perks.components.ChangeWeather;

public class InteractiveConfirmWeatherChange implements InteractiveResponse {

	ChangeWeather perk;
	public InteractiveConfirmWeatherChange(ChangeWeather perk) {
		this.perk = perk;
	}
	
	@Override
	public void respond(String message, Resident resident) {
		resident.clearInteractiveMode();
		
		if (message.equalsIgnoreCase("yes")) {
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				player.getWorld().setStorm(false);
				player.getWorld().setThundering(false);
				player.getWorld().setWeatherDuration((int) TimeTools.toTicks(20*60));
				CivMessage.global(CivSettings.localize.localizedString("var_interactive_weather_success",resident.getName()));
				perk.markAsUsed(resident);
			} catch (CivException e) {
			}
		} else {
			CivMessage.send(resident, CivSettings.localize.localizedString("interactive_weather_cancel"));
		}
		
	}

}
