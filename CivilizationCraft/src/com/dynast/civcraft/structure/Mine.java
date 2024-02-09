
package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.components.AttributeBiomeRadiusPerLevel;
import com.dynast.civcraft.components.ConsumeLevelComponent;
import com.dynast.civcraft.components.ConsumeLevelComponent.Result;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigMineLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.MultiInventory;

public class Mine extends Structure {

	private ConsumeLevelComponent consumeComp = null;
	
	protected Mine(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Mine(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
		
	@Override
	public void loadSettings() {
		super.loadSettings();
	}
	
	public String getkey() {
		return getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString(); 
	}
		
	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "hammer";
	}
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	public Result consume(CivAsyncTask task) throws InterruptedException {
		
		//Look for the mine's chest.
		if (this.getChests().size() == 0)
			return Result.STAGNATE;	

		MultiInventory multiInv = new MultiInventory();
		
		ArrayList<StructureChest> chests = this.getAllChestsById(0);
		
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
	
	public void process_mine(CivAsyncTask task) throws InterruptedException {
		Result result = null;
		try {
			result = this.consume(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_mine_productionFell",getConsumeComponent().getLevel(),CivColor.LightGreen+getConsumeComponent().getCountString()));
			break;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_mine_lostalvl",getConsumeComponent().getLevel()));
			break;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_mine_stagnated",getConsumeComponent().getLevel(),CivColor.LightGreen+getConsumeComponent().getCountString()));
			break;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_mine_productionGrew",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_mine_lvlUp",getConsumeComponent().getLevel()));
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_mine_maxed",getConsumeComponent().getLevel(),CivColor.LightGreen+getConsumeComponent().getCountString()));
			break;
		default:
			break;
		}
	}
	
	public double getBonusHammers() {
		if (!this.isComplete() || this.isDestroyed() ||
				this.getLastResult() == Result.STAGNATE ||
				this.getLastResult() == Result.STARVE ||
				this.getLastResult() == Result.LEVELDOWN) {
			return 0.0;
		}
		int level = getLevel();
		double add = 0.0;

		if (level != 0) {
			ConfigMineLevel lvl = CivSettings.mineLevels.get(level);
			add = lvl.hammers;
		}
		if (this.getTown().getBuffManager().hasBuff("buff_advanced_tooling")) {
			add *= this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		}
		return add;	
	}

	public int getLevel() {
		if (!this.isComplete()) {
			return 1;
		}
		return this.getConsumeComponent().getLevel();
	}
	
	public double getHammersPerTile() {
		AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel)this.getComponent("AttributeBiomeRadiusPerLevel");
		double base = 1.0;
		
		if (attrBiome != null) {
			base = attrBiome.getBaseValue();
		}
	
		double rate = 1;
		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
		return (rate*base);
	}

	public int getCount() {
		return this.getConsumeComponent().getCount();
	}

	public int getMaxCount() {
		int level = getLevel();
		
		ConfigMineLevel lvl = CivSettings.mineLevels.get(level);
		return lvl.count;	
	}

	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}
	
	public void onDestroy() {
		super.onDestroy();

		getConsumeComponent().setLevel((int)Math.round(this.getLevel()/1.8));
		getConsumeComponent().setCount(0);
		getConsumeComponent().onSave();
	}

}
