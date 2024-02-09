package com.dynast.global.perks.components;

import java.sql.SQLException;
import java.util.HashMap;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;
import com.dynast.global.perks.NotVerifiedException;
import com.dynast.global.perks.Perk;


public class PerkComponent {
	
	private HashMap<String, String> attributes = new HashMap<>();
	private String name;
	private Perk parent;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getString(String key) {
		return attributes.get(key);
	}
	
	public double getDouble(String key) {
		return Double.valueOf(attributes.get(key));
	}
	
	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}
	
	public Perk getParent() {
		return parent;
	}
	public void setParent(Perk parent) {
		this.parent = parent;
	}
	
	public void markAsUsed(Resident resident) {
		this.getParent().count--;
		if (this.getParent().count <= 0) {
			resident.perks.remove(this.getParent().getIdent());
		}
		
		try {
			CivGlobal.perkManager.markAsUsed(resident, this.getParent());
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NotVerifiedException e) {
			CivMessage.send(resident, CivColor.Rose+CivSettings.localize.localizedString("PerkComponent_notValidated"));
			e.printStackTrace();
		}
	}
	
	public void onActivate(Resident resident) {}
	public void createComponent() {}

}
