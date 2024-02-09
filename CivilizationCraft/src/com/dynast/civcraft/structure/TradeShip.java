package com.dynast.civcraft.structure;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

//import com.dynast.civcraft.components.AttributeBiomeRadiusPerLevel;
import com.dynast.civcraft.components.TradeLevelComponent;
import com.dynast.civcraft.components.TradeLevelComponent.Result;
import com.dynast.civcraft.components.TradeShipResults;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
//import com.dynast.civcraft.object.Buff;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.MultiInventory;
import com.dynast.civcraft.util.SimpleBlock;
import com.dynast.civcraft.util.TimeTools;

public class TradeShip extends WaterStructure {
	
	private int upgradeLevel = 1;
	private int tickLevel = 1;

	public HashSet<BlockCoord> goodsDepositPoints = new HashSet<>();
	public HashSet<BlockCoord> goodsWithdrawPoints = new HashSet<>();
	
	private TradeLevelComponent consumeComp = null;
	
	protected TradeShip(Location center, String id, Town town) throws CivException {
		super(center, id, town);
		setUpgradeLvl(town.saved_tradeship_upgrade_levels);
	}

	public TradeShip(ResultSet rs) throws SQLException, CivException {
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
		return "tradeship";
	}
	
	public TradeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (TradeLevelComponent) this.getComponent(TradeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	@Override 
	public void updateSignText() {
		reprocessCommandSigns();
	}
	
	public void reprocessCommandSigns() {
		/* Load in the template. */
		//Template tpl = new Template();
		Template tpl;
		try {
			//tpl.load_template(this.getSavedTemplatePath());
			tpl = Template.getTemplate(this.getSavedTemplatePath(), null);
		} catch (IOException | CivException e) {
			e.printStackTrace();
			return;
		}
		class SyncTask implements Runnable {
			Template template;
			BlockCoord structCorner;
			
			public SyncTask(Template template, BlockCoord structCorner) {
				this.template = template;
				this.structCorner = structCorner;
			}
			
			@Override
			public void run() {
				
				processCommandSigns(template, structCorner);
			}
		}
		
		TaskMaster.syncTask(new SyncTask(tpl, corner), TimeTools.toTicks(1));

	}
	
	private void processCommandSigns(Template tpl, BlockCoord corner) {
		for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(corner.getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			switch (sb.command) {
			case "/incoming":{
				Integer ID = Integer.valueOf(sb.keyvalues.get("id"));
				if (this.getUpgradeLvl() >= ID+1) {
					this.goodsWithdrawPoints.add(absCoord);
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.CHEST));
					byte data3 = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setData(absCoord.getBlock(), data3);
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.AIR));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
				}
				this.addStructureBlock(absCoord, false);
				break;}
			case "/inSign":{
				Integer ID = Integer.valueOf(sb.keyvalues.get("id"));
				if (this.getUpgradeLvl() >= ID+1) {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_input_line0"));
					sign.setLine(1, ""+(ID+1));
					sign.setLine(2, "");
					sign.setLine(3, "");
					sign.update();
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_input_line0"));
					sign.setLine(1, CivSettings.localize.localizedString("tradeship_sign_input_notupgraded_line1"));
					sign.setLine(2, (CivSettings.localize.localizedString("tradeship_sign_input_notupgraded_line2")));
					sign.setLine(3, CivSettings.localize.localizedString("tradeship_sign_input_notupgraded_line3"));
					sign.update();
				}
				this.addStructureBlock(absCoord, false);
				break;}
			case "/outgoing":{
				Integer ID = Integer.valueOf(sb.keyvalues.get("id"));
				
				if (this.getLevel() >= (ID*2)+1) {
					this.goodsDepositPoints.add(absCoord);
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.CHEST));
					byte data3 = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setData(absCoord.getBlock(), data3);
					this.addStructureBlock(absCoord, false);
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.AIR));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
				}
				break;}
			case "/outSign":{
				Integer ID = Integer.valueOf(sb.keyvalues.get("id"));
				if (this.getLevel() >= (ID*2)+1) {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_output_line0"));
					sign.setLine(1, ""+(ID+1));
					sign.setLine(2, "");
					sign.setLine(3, "");
					sign.update();
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_output_line0"));
					sign.setLine(1, CivSettings.localize.localizedString("tradeship_sign_output_notupgraded_line1"));
					sign.setLine(2, (CivSettings.localize.localizedString("var_tradeship_sign_output_notupgraded_line2",((ID*2)+1))));
					sign.setLine(3, CivSettings.localize.localizedString("tradeship_sign_output_notupgraded_line3"));
					sign.update();
				}
				this.addStructureBlock(absCoord, false);
				break;}
			case "/in":{
				Integer ID = Integer.valueOf(sb.keyvalues.get("id"));
				if (ID == 0) {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_input_line0"));
					sign.setLine(1, "1");
					sign.setLine(2, "2");
					sign.setLine(3, "");
					sign.update();
				} else {
					ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
					ItemManager.setData(absCoord.getBlock(), sb.getData());
					
					Sign sign = (Sign)absCoord.getBlock().getState();
					sign.setLine(0, CivSettings.localize.localizedString("tradeship_sign_input_line0"));
					sign.setLine(1, "3");
					sign.setLine(2, "4");
					sign.setLine(3, "");
					sign.update();
				}
				this.addStructureBlock(absCoord, false);
				break;}
			default:{
				/* Unrecognized command... treat as a literal sign. */
				ItemManager.setTypeId(absCoord.getBlock(), ItemManager.getId(Material.WALL_SIGN));
				ItemManager.setData(absCoord.getBlock(), sb.getData());
				
				Sign sign = (Sign)absCoord.getBlock().getState();
				sign.setLine(0, sb.message[0]);
				sign.setLine(1, sb.message[1]);
				sign.setLine(2, sb.message[2]);
				sign.setLine(3, sb.message[3]);
				sign.update();

				this.addStructureBlock(absCoord, false);
				break;}
			}
		}
	}
		
	
	public TradeShipResults consume(CivAsyncTask task) throws InterruptedException {
		TradeShipResults tradeResult;
		//Look for the TradeShip chests.
		if (this.goodsDepositPoints.size() == 0 || this.goodsWithdrawPoints.size() == 0)
		{
			tradeResult = new TradeShipResults();
			tradeResult.setResult(Result.STAGNATE);
			return tradeResult;
		}
		MultiInventory mInv = new MultiInventory();
		
		for (BlockCoord bcoord : this.goodsDepositPoints) {
			task.syncLoadChunk(bcoord.getWorldname(), bcoord.getX(), bcoord.getZ());
			Inventory tmp;
			try {
				tmp = task.getChestInventory(bcoord.getWorldname(), bcoord.getX(), bcoord.getY(), bcoord.getZ(), true);
			} catch (CivTaskAbortException e) {
				tradeResult = new TradeShipResults();
				tradeResult.setResult(Result.STAGNATE);
				return tradeResult;
			}
			mInv.addInventory(tmp);
		}
		
		if (mInv.getInventoryCount() == 0) {
			tradeResult = new TradeShipResults();
			tradeResult.setResult(Result.STAGNATE);
			return tradeResult;
		}
		getConsumeComponent().setSource(mInv);
		getConsumeComponent().setConsumeRate(1.0);
		
		try {
			tradeResult = getConsumeComponent().processConsumption(this.getUpgradeLvl()-1);
			getConsumeComponent().onSave();	
		} catch (IllegalStateException e) {
			tradeResult = new TradeShipResults();
			tradeResult.setResult(Result.STAGNATE);
			CivLog.exception(this.getDisplayName()+" Process Error in town: "+this.getTown().getName()+" and Location: "+this.getCorner(), e);
			return tradeResult;
		}	
		return tradeResult;
	}
	
	public void process_trade_ship(CivAsyncTask task) throws InterruptedException, InvalidConfiguration {	
		TradeShipResults tradeResult = this.consume(task);
		
		Result result = tradeResult.getResult();
		switch (result) {
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_tradeship_stagnated",getConsumeComponent().getLevel(),CivColor.LightGreen+getConsumeComponent().getCountString()));
			break;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_tradeship_productionGrew",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_tradeship_lvlUp",getConsumeComponent().getLevel()));
			this.reprocessCommandSigns();
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_tradeship_maxed",getConsumeComponent().getLevel(),CivColor.LightGreen+getConsumeComponent().getCountString()));
			break;
		default:
			break;
		}
		
		int total_culture = Math.round(tradeResult.getCulture());
		total_culture *= this.getTown().getCultureRate().total/2;
		
		if (tradeResult.getCulture() >= 1) {			
			this.getTown().addAccumulatedCulture(total_culture);
			this.getTown().save();
		}
		
		if (tradeResult.getMoney() >= 1) {
			double total_coins = tradeResult.getMoney();
			double shipRate = this.getTown().getTradeRate()/2;
			if (this.getTown().getBuffManager().hasBuff("buff_ingermanland_trade_ship_income")) {
				shipRate += this.getTown().getBuffManager().getEffectiveDouble("buff_ingermanland_trade_ship_income");
			}
			
			if (this.getTown().getBuffManager().hasBuff("buff_great_lighthouse_trade_ship_income")) {
				shipRate += this.getTown().getBuffManager().getEffectiveDouble("buff_great_lighthouse_trade_ship_income");
			}

			if (this.getTown().getStructureTypeCount("s_lighthouse") >= 1) {
				shipRate += CivSettings.getDouble(CivSettings.townConfig, "town.lighthouse_trade_ship_boost");
			}
			if (this.getTown().getCiv().hasInstitution("commerce_4")) {
				shipRate += CivSettings.publicinsts.get("commerce_4").value;
			}
			
			total_coins *= shipRate;
			
			if (total_coins >= 1) {
				CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_tradeship_success",CivColor.Yellow+Math.round(total_coins),CivColor.LightGreen+CivSettings.CURRENCY_NAME,CivColor.LightPurple+total_culture+CivColor.LightGreen,tradeResult.getConsumed()));
			}
						
			this.getTown().getTreasury().deposit(total_coins);
		}
		
		if (tradeResult.getReturnCargo().size() >= 1) {
			MultiInventory multiInv = new MultiInventory();
			
			for (BlockCoord bcoord : this.goodsWithdrawPoints) {
				task.syncLoadChunk(bcoord.getWorldname(), bcoord.getX(), bcoord.getZ());
				Inventory tmp;
				try {
					tmp = task.getChestInventory(bcoord.getWorldname(), bcoord.getX(), bcoord.getY(), bcoord.getZ(), true);
					multiInv.addInventory(tmp);
				} catch (CivTaskAbortException e) {

					e.printStackTrace();
				}
			}
			
			for (ItemStack item : tradeResult.getReturnCargo()) {
				multiInv.addItemStack(item);
			}
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("tradeship_successSpecail"));
		}
	}
	
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		this.upgradeLevel = getTown().saved_tradeship_upgrade_levels;
		this.reprocessCommandSigns();
	}

	public int getUpgradeLvl() {
		return upgradeLevel;
	}

	public void setUpgradeLvl(int level) {
		this.upgradeLevel = level;

		if (this.isComplete()) {
			this.reprocessCommandSigns();
		}
	}

	public int getLevel() {
		try {
			return this.getConsumeComponent().getLevel();
		} catch (Exception e) {
			return tickLevel;
		}
	}
	
//	public double getHammersPerTile() {
//		AttributeBiomeRadiusPerLevel attrBiome = (AttributeBiomeRadiusPerLevel)this.getComponent("AttributeBiomeBase");
//		double base = attrBiome.getBaseValue();
//	
//		double rate = 1;
//		rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.ADVANCED_TOOLING);
//		return (rate*base);
//	}

	public int getCount() {
		return this.getConsumeComponent().getCount();
	}

//	public int getMaxCount() {
//		int level = getLevel();
//		
//		ConfigMineLevel lvl = CivSettings.mineLevels.get(level);
//		return lvl.count;	
//	}

	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
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
