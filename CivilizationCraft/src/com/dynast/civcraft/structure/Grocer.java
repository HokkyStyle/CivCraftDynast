
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.dynast.civcraft.components.NonMemberFeeComponent;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigGrocerLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;

public class Grocer extends Structure {

	public int level = 1;

	private NonMemberFeeComponent nonMemberFeeComponent; 
	
	protected Grocer(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
		setLevel(town.saved_grocer_levels);
	}

	public Grocer(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
	}

	@Override
	public String getDynmapDescription() {
		String out = "<u><b>"+this.getDisplayName()+"</u></b><br/>";

		for (int i = 0; i < level; i++) {
			ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(i+1);
			out += "<b>"+grocerlevel.itemName+"</b> "+CivSettings.localize.localizedString("Amount")+" "+grocerlevel.amount+ " "+CivSettings.localize.localizedString("Price")+" "+grocerlevel.price+" "+CivSettings.CURRENCY_NAME+".<br/>";
		}
		
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "cup";
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getNonResidentFee() {
		return nonMemberFeeComponent.getFeeRate();
	}

	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}
	
	public String getNonResidentFeeString() {
		return CivSettings.localize.localizedString("bank_sign_fee")+" "+(int)(getNonResidentFee()*100) + "%";
	}
	
	private StructureSign getSignFromSpecialId(int special_id) {
		for (StructureSign sign : getSigns()) {
			int id = Integer.valueOf(sign.getAction());
			if (id == special_id) {
				return sign;
			}
		}
		return null;
	}
	
	public void sign_buy_material(Player player, String itemName, int id, byte data, int amount, double price) {
		Resident resident;
		int payToTown = (int) Math.round(price*this.getNonResidentFee());
		try {
				
				resident = CivGlobal.getResident(player.getName());
				Civilization t = resident.getCiv();
			
				if (t == this.getCiv()) {
					// Pay no taxes! You're a member.
					resident.buyItem(itemName, id, data, price, amount);
					CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_grocer_msgBought",amount,itemName,price+" "+CivSettings.CURRENCY_NAME));
					return;
				} else {
					// Pay non-resident taxes
					resident.buyItem(itemName, id, data, price + payToTown, amount);
					getTown().depositDirect(payToTown);
					CivMessage.send(player, CivColor.LightGreen + CivSettings.localize.localizedString("var_grocer_msgBought",amount,itemName,price,CivSettings.CURRENCY_NAME));
					CivMessage.send(player, CivColor.Yellow + CivSettings.localize.localizedString("var_grocer_msgPaidTaxes",this.getTown().getName(),payToTown+" "+CivSettings.CURRENCY_NAME));
				}
			
			}
			catch (CivException e) {
				CivMessage.send(player, CivColor.Rose + e.getMessage());
			}
		return;
	}

	
	@Override
	public void updateSignText() {
		int count = 0;
	
		for (count = 0; count < level; count++) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("sign from special id was null, id:"+count);
				return;
			}
			ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(count+1);
			
			sign.setText(CivSettings.localize.localizedString("grocer_sign_buy")+"\n"+grocerlevel.itemName+"\n"+
						 CivSettings.localize.localizedString("grocer_sign_for")+" "+grocerlevel.price+" "+CivSettings.CURRENCY_NAME+"\n"+
					     getNonResidentFeeString());
			
			sign.update();
		}
		
		for (; count < getSigns().size(); count++) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("sign from special id was null, id:"+count);
				return;
			}
			sign.setText(CivSettings.localize.localizedString("grocer_sign_empty"));
			sign.update();
		}
		
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		int special_id = Integer.valueOf(sign.getAction());
		if (special_id < this.level) {
			ConfigGrocerLevel grocerlevel = CivSettings.grocerLevels.get(special_id+1);
			sign_buy_material(player, grocerlevel.itemName, grocerlevel.itemId, 
					(byte)grocerlevel.itemData, grocerlevel.amount, grocerlevel.price);
		} else {
			CivMessage.send(player, CivColor.Rose+CivSettings.localize.localizedString("grocer_sign_needUpgrade"));
		}
	}
	
	
}
