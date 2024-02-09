package com.dynast.global.perks.components;

import java.io.IOException;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigBuildableInfo;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.util.CivColor;

public class CustomPersonalTemplate extends PerkComponent {
	
	@Override
	public void onActivate(Resident resident) {
		CivMessage.send(resident, CivColor.LightGreen+CivSettings.localize.localizedString("customTemplate_personal"));
	}
	

	public Template getTemplate(Player player, ConfigBuildableInfo info) {
		Template tpl = new Template();
		try {
			tpl.initTemplate(player.getLocation(), info, this.getString("theme"));
		} catch (CivException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tpl;
	}
}
