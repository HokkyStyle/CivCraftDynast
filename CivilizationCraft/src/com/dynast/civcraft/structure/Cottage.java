
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.components.ConsumeLevelComponent;
import com.dynast.civcraft.components.ConsumeLevelComponent.Result;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigCottageLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.MultiInventory;

public class Cottage extends Structure {

	private ConsumeLevelComponent consumeComp = null;
	
	protected Cottage(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Cottage(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();
		
//		attrComp = new AttributeComponent();
//		attrComp.setType(AttributeType.DIRECT);
//		attrComp.setOwnerKey(this.getTown().getName());
//		attrComp.setAttrKey(Attribute.TypeKeys.COINS.name());
//		attrComp.setSource("Cottage("+this.getCorner().toString()+")");
//		attrComp.registerComponent();
	}
	
	@Override
	public String getDynmapDescription() {
		if (getConsumeComponent() == null) {
			return "";
		}
		
		String out = "";
		out += CivSettings.localize.localizedString("Level")+" "+getConsumeComponent().getLevel()+" "+getConsumeComponent().getCountString();
		return out;
	}
	
	@Override
	public String getMarkerIconName() {
		return "house";
	}
	
	public String getkey() {
		return this.getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}

	/*
	 * Returns true if the granary has been poisoned, false otherwise.
	 */
	public boolean processPoison(MultiInventory inv) {
		//Check to make sure the granary has not been poisoned!
		String key = "posiongranary:"+getTown().getName();
		ArrayList<SessionEntry> entries;
		entries = CivGlobal.getSessionDB().lookup(key);
		int max_poison_ticks = -1;
		for (SessionEntry entry : entries) {
			int next = Integer.valueOf(entry.value);
			
			if (next > max_poison_ticks) {
				max_poison_ticks = next;
			} 
		}
		
		if (max_poison_ticks > 0) {
			CivGlobal.getSessionDB().delete_all(key);
			max_poison_ticks--;
			
			if (max_poison_ticks > 0)
				CivGlobal.getSessionDB().add(key, ""+max_poison_ticks, this.getTown().getCiv().getId(), this.getTown().getId(), this.getId());
	
			// Add some rotten flesh to the chest lol
			CivMessage.sendTown(this.getTown(), CivColor.Rose+CivSettings.localize.localizedString("cottage_poisoned"));
			inv.addItemStack(ItemManager.createItemStack(CivData.ROTTEN_FLESH, 4));
			return true;
		}
		return false;
	}
	
	public void generateCoins(CivAsyncTask task) {
		
		if (!this.isActive()) {
			return;
		}
		
		MultiInventory multiInv = new MultiInventory();

		for (Structure struct : this.getTown().getStructures()) {
			if (struct instanceof Granary) {
				ArrayList<StructureChest> chests = struct.getAllChestsById(1);
				
				try {
					for (StructureChest c : chests) {
						task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
						Inventory tmp;
						try {
							tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
							multiInv.addInventory(tmp);
						} catch (CivTaskAbortException e) {
							e.printStackTrace();
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		getConsumeComponent().setSource(multiInv);
		
		double cottage_consume_mod = 1.0; //allows buildings and govs to change the totals for cottage consumption.

		if (this.getTown().getBuffManager().hasBuff(Buff.REDUCE_CONSUME)) {
			cottage_consume_mod += this.getTown().getBuffManager().getEffectiveDouble(Buff.REDUCE_CONSUME);
		}
		
		if (this.getTown().getBuffManager().hasBuff("buff_pyramid_cottage_consume")) {
			cottage_consume_mod += this.getTown().getBuffManager().getEffectiveDouble("buff_pyramid_cottage_consume");
		}
		
		if (this.getTown().getBuffManager().hasBuff(Buff.FISHING)) {
			int breadPerFish = this.getTown().getBuffManager().getEffectiveInt(Buff.FISHING);
			
			getConsumeComponent().addEquivExchange(CivData.BREAD, CivData.FISH_RAW, breadPerFish);
		}
		
		if (this.getTown().getCiv().hasInstitution("tradition_3")) {
			cottage_consume_mod += CivSettings.publicinsts.get("tradition_3").value;
		}
		
		getConsumeComponent().setConsumeRate(cottage_consume_mod);

		Result result = Result.STAGNATE;
		try {
			result = getConsumeComponent().processConsumption();
			getConsumeComponent().onSave();
			getConsumeComponent().clearEquivExchanges();
		} catch (IllegalStateException e) {
			CivLog.exception(this.getDisplayName()+" Process Error in town: "+this.getTown().getName()+" and Location: "+this.getCorner(), e);
		}	
		
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_cottage_starved_base",getConsumeComponent().getLevel(),CivSettings.localize.localizedString("var_cottage_status_starved",getConsumeComponent().getCountString()),CivSettings.CURRENCY_NAME));
			return;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_cottage_starved_base",(getConsumeComponent().getLevel()+1),CivSettings.localize.localizedString("var_cottage_status_lvlDown"),CivSettings.CURRENCY_NAME));
			return;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_cottage_starved_base",getConsumeComponent().getLevel(),CivSettings.localize.localizedString("var_cottage_status_stagnated",getConsumeComponent().getCountString()),CivSettings.CURRENCY_NAME));
			return;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.DarkPurple+CivSettings.localize.localizedString("var_cottage_starved_unknwon",CivSettings.CURRENCY_NAME));
			return;
		default:
			break;
		}
		
		if (processPoison(multiInv)) {
			return;
		}
		
		ConfigCottageLevel lvl;
		if (result == Result.LEVELUP) {
			lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel()-1);	
		} else {
			lvl = CivSettings.cottageLevels.get(getConsumeComponent().getLevel());
		}

		double cottage_rate = this.getTown().getCottageRate();
				
		//int total_coins = (int)Math.round(lvl.coins*this.getTown().getCottageRate());
		
		if (this.getCiv().hasTechnology("tech_taxation")) {
			double taxation_bonus;
			try {
				taxation_bonus = CivSettings.getDouble(CivSettings.techsConfig, "taxation_cottage_buff");
				cottage_rate += taxation_bonus;
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
			}
		}
		int total_coins = (int)Math.round(lvl.coins*cottage_rate);
		
		double taxesPaid = total_coins*this.getTown().getDepositCiv().getIncomeTaxRate();

		String stateMessage = "";
		switch (result) {
		case GROW:
			stateMessage = CivColor.Green+CivSettings.localize.localizedString("var_cottage_grew",getConsumeComponent().getCountString())+CivColor.LightGreen;
			break;
		case LEVELUP:
			stateMessage = CivColor.Green+CivSettings.localize.localizedString("var_cottage_grew_lvlUp")+CivColor.LightGreen;
			break;
		case MAXED:
			stateMessage = CivColor.Green+CivSettings.localize.localizedString("var_cottage_grew_isMaxed",getConsumeComponent().getCountString())+CivColor.LightGreen;
			break;
		default:
			break;
		}
		
		if (taxesPaid > 0) {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_cottage_grew_base",getConsumeComponent().getLevel(),stateMessage,CivColor.Yellow+total_coins,CivColor.LightGreen+CivSettings.CURRENCY_NAME,
					CivColor.Yellow+CivSettings.localize.localizedString("var_cottage_grew_taxes",Math.floor(taxesPaid),this.getTown().getDepositCiv().getName())));
		} else {
			CivMessage.sendTown(this.getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_cottage_grew_base",getConsumeComponent().getLevel(),stateMessage,CivColor.Yellow+total_coins,CivColor.LightGreen+CivSettings.CURRENCY_NAME,""));
		}
		
		this.getTown().getTreasury().deposit(total_coins - taxesPaid);
		this.getTown().getDepositCiv().taxPayment(this.getTown(), taxesPaid);
	}
	
	public int getLevel() {
		return getConsumeComponent().getLevel();
	}
	
	public Result getLastResult() {
		return getConsumeComponent().getLastResult();
	}

	public int getCount() {
		return getConsumeComponent().getCount();
	}

	public int getMaxCount() {
		int level = getLevel();
		
		ConfigCottageLevel lvl = CivSettings.cottageLevels.get(level);
		return lvl.count;
	}

	public double getCoinsGenerated() {
		int level = getLevel();
		
		ConfigCottageLevel lvl = CivSettings.cottageLevels.get(level);
		if (lvl == null) {
			return 0;
		}
		return lvl.coins;
	}

	public void delevel() {
		int currentLevel = getLevel();
		
		if (currentLevel > 1) {
			getConsumeComponent().setLevel(getLevel()-1);
			getConsumeComponent().setCount(0);
			getConsumeComponent().onSave();
		}
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		if (getConsumeComponent() != null) {
			getConsumeComponent().onDelete();
		}
	}
	
	public void onDestroy() {
		super.onDestroy();

		getConsumeComponent().setLevel((int)Math.round(this.getLevel()/1.8));
		getConsumeComponent().setCount(0);
		getConsumeComponent().onSave();
	}
}
