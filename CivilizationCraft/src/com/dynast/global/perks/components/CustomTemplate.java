package com.dynast.global.perks.components;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.interactive.InteractiveCustomTemplateConfirm;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.util.CivColor;
import com.dynast.global.perks.Perk;

public class CustomTemplate extends PerkComponent {
	
	@Override
	public void onActivate(Resident resident) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}
		
		Town town = resident.getTown();
		if (town == null) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("customTemplate_noTown"));
			return;
		}
		
		if (hasTownTemplate(town)) {
			CivMessage.sendError(player, CivColor.Rose+CivSettings.localize.localizedString("customTemplatE_alreadyBound"));
			return;
		}
		
		/*
		 * Send resident into interactive mode to confirm that they want
		 * to bind the template to this town. 
		 */
		resident.setInteractiveMode(new InteractiveCustomTemplateConfirm(resident.getName(), this));
		
	}
	
	private String getTemplateSessionKey(Town town) {
		return "customtemplate:"+town.getName()+":"+this.getString("template");
	}
	private static String getTemplateSessionKey(Town town, String buildableBaseName) {
		return "customtemplate:"+town.getName()+":"+buildableBaseName;
	}
	
	private static String getTemplateSessionValue(Perk perk, Resident resident) {
		return perk.getIdent()+":"+resident.getName();
	}
	
	public void bindTemplateToTown(Town town, Resident resident) {
		CivGlobal.getSessionDB().add(getTemplateSessionKey(town), getTemplateSessionValue(this.getParent(), resident), 
				town.getCiv().getId(), town.getId(), 0);		
	}
	
	public boolean hasTownTemplate(Town town) {
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getTemplateSessionKey(town));
		
		for (SessionEntry entry : entries) {
			String[] split = entry.value.split(":");
			
			if (this.getParent().getIdent().equals(split[0])) {
				return true;
			}
		}
		
		return false;
	}
	
	public static ArrayList<Perk> getTemplatePerksForBuildable(Town town, String buildableBaseName) {
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getTemplateSessionKey(town, buildableBaseName));
		ArrayList<Perk> perks = new ArrayList<>();
		
		for (SessionEntry entry : entries) {
			String[] split = entry.value.split(":");
			
			Perk perk = Perk.staticPerks.get(split[0]);
			if (perk != null) {
				Perk tmpPerk = new Perk(perk.configPerk);
				tmpPerk.provider = split[1];
				perks.add(tmpPerk);
			} else {
				CivLog.warning("Unknown perk in session db:"+split[0]);
				continue;
			}
		}
		
		return perks;
	}
	

	public Template getTemplate(Player player, Buildable buildable) {
		Template tpl = new Template();
		try {
			tpl.initTemplate(player.getLocation(), buildable, this.getString("theme"));
		} catch (CivException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tpl;
	}
	
}
