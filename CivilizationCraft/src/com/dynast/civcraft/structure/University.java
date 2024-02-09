package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.components.ConsumeLevelComponent;
import com.dynast.civcraft.components.ConsumeLevelComponent.Result;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigUniverLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.MultiInventory;

public class University extends Structure {
	
	private ConsumeLevelComponent consumeComp = null;
	
	protected University(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public University(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	@Override
	public void loadSettings() {
		super.loadSettings();

	}
	
	@Override
	public String getMarkerIconName() {
		return "bronzestar";
	}
	
	public String getDynmapDescription() {
		if (getConsumeComponent() == null) {
			return "";
		}
		
		String out = "";
		out += CivSettings.localize.localizedString("Level")+" "+getConsumeComponent().getLevel()+" "+getConsumeComponent().getCountString();
		return out;
	}
	
	public String getkey() {
		return this.getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
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
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
    public Result consume(CivAsyncTask task) throws InterruptedException {
		
		//Look for the temple's chest.
		if (this.getChests().size() == 0)
			return Result.STAGNATE;	

		MultiInventory multiInv = new MultiInventory();
		
		ArrayList<StructureChest> chests = this.getAllChestsById(3);
		
		// Make sure the chest is loaded and add it to the multi inv.
		for (StructureChest c : chests) {
			task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
			Inventory tmp;
			try {
				tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
			} catch (CivTaskAbortException e) {
				return Result.STAGNATE;
			}
			multiInv.addInventory(tmp);
		}
		getConsumeComponent().setSource(multiInv);
		getConsumeComponent().setConsumeRate(1.0);
		try {
			Result result = getConsumeComponent().processConsumption();
			getConsumeComponent().onSave();		
			return result;
		} catch (IllegalStateException e) {
			CivLog.exception(this.getDisplayName()+" Process Error in town: "+this.getTown().getName()+" and Location: "+this.getCorner(), e);
			return Result.STAGNATE;
		}	
	}
    
    public void process_univer(CivAsyncTask task) throws InterruptedException {
		Result result = null;
		try {
			result = this.consume(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_university_productionFell",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_university_lostalvl",getConsumeComponent().getLevel()));
			return;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_university_stagnated",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_university_productionGrew",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_university_lvlUp",getConsumeComponent().getLevel()));
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_university_maxed",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.DarkPurple+CivSettings.localize.localizedString("university_unknown"));
			return;
		default:
			break;
		}		
	}
    
    public int getLevel() {
    	if (!this.isComplete()) {
			return 1;
		}
		return this.getConsumeComponent().getLevel();
	}

	public int getCount() {
		return this.getConsumeComponent().getCount();
	}

	public int getMaxCount() {
		int level = getLevel();
		
		ConfigUniverLevel lvl = CivSettings.univerLevels.get(level);
		return lvl.count;	
	}

	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}
	
	public double getBonusBeakers() {
		if (!this.isComplete() || this.isDestroyed() ||
				this.getLastResult() == Result.STAGNATE ||
				this.getLastResult() == Result.STARVE ||
				this.getLastResult() == Result.LEVELDOWN) {
			return 0;
		}
		int level = getLevel();
		
		ConfigUniverLevel lvl = CivSettings.univerLevels.get(level);
		int beakers = (int)lvl.beakers;
		double rate = 1.0;

		if (this.getCiv().hasInstitution("rational_4")) {
			rate += 0.5;
		}

		if (this.getTown().getBuffManager().hasBuff("buff_innovation")) {
			rate += this.getTown().getBuffManager().getEffectiveDouble("buff_innovation");
		}

		return beakers * rate;
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
	
	@Override
	public void updateSignText() {
		int count = 0;
		for (; count < getSigns().size(); count++) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("University sign was null");
				return;
			}
			
			sign.setText("\n"+CivSettings.localize.localizedString("university_sign")+"\n"+
					this.getTown().getName());
			
			sign.update();
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		CivMessage.send(player, CivColor.Green+CivSettings.localize.localizedString("university_sign")+" "+this.getTown().getName());
	}


}
