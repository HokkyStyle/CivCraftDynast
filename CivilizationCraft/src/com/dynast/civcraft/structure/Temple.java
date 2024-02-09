
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.components.ConsumeLevelComponent;
import com.dynast.civcraft.components.ConsumeLevelComponent.Result;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigTempleLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.MultiInventory;

public class Temple extends Structure {

	private ConsumeLevelComponent consumeComp = null;
	
	protected Temple(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Temple(ResultSet rs) throws SQLException, CivException {
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
		return "church";
	}
	
	public String getkey() {
		return this.getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}

	public Result consume(CivAsyncTask task) throws InterruptedException {
		
		//Look for the temple's chest.
		if (this.getChests().size() == 0)
			return Result.STAGNATE;	

		MultiInventory multiInv = new MultiInventory();
		
		ArrayList<StructureChest> chests = this.getAllChestsById(1);
		
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
	
	public void templeCulture(CivAsyncTask task) throws InterruptedException {
		Result result = this.consume(task);
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_temple_productionFell",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_temple_lostalvl",getConsumeComponent().getLevel()));
			return;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_temple_stagnated",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_temple_productionGrew",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_temple_lvlUp",getConsumeComponent().getLevel()));
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_temple_maxed",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.DarkPurple+CivSettings.localize.localizedString("temple_unknown"));
			return;
		default:
			break;
		}
		
		this.getTown().addAccumulatedCulture(getCultureGenerated());
		this.getTown().save();
		
		CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_temple_cultureGenerated",(CivColor.LightPurple+getCultureGenerated()+CivColor.LightGreen)));

	}

	public int getLevel() {
		return this.getConsumeComponent().getLevel();
	}

	public int getCount() {
		return this.getConsumeComponent().getCount();
	}

	public int getMaxCount() {
		int level = getLevel();
		
		ConfigTempleLevel lvl = CivSettings.templeLevels.get(level);
		return lvl.count;	
	}

	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}
	
	public double getCultureGenerated() {
		if (!this.isComplete() || this.isDestroyed() ||
				this.getLastResult() == Result.STAGNATE ||
				this.getLastResult() == Result.STARVE ||
				this.getLastResult() == Result.LEVELDOWN) {
			return 0;
		}
		int level = getLevel();
		
		ConfigTempleLevel lvl = CivSettings.templeLevels.get(level);
		
		if (lvl == null) {
			return 0;
		}
		
		int total_culture = (int)Math.round(lvl.culture * this.getTown().getCultureRate().total);
		
		if (this.getTown().getCiv().hasInstitution("estetic_4")) {
			total_culture *= 1.5;
		}
		
		return Math.round(total_culture);
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
