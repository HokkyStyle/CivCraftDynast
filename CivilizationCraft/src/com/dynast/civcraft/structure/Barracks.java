package com.dynast.civcraft.structure;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import com.dynast.civcraft.config.ConfigPublicInstitution;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigUnit;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.interactive.InteractiveRepairItem;
import com.dynast.civcraft.items.components.RepairCost;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.UnitSaveAsyncTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.SimpleBlock;

public class Barracks extends Structure {

	private static final long SAVE_INTERVAL = 60*1000;

	private int index = 0;
	private StructureSign unitNameSign;
	
	private ConfigUnit trainingUnit = null;
	private double currentHammers = 0.0;
	
	private TreeMap<Integer, StructureSign> progresBar = new TreeMap<>();
	private Date lastSave = null;
	
	protected Barracks(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}

	public Barracks(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public String getMarkerIconName() {
		return "barracks";
	}

	private String getUnitSignText(int index) throws IndexOutOfBoundsException {
		ArrayList<ConfigUnit> unitList = getTown().getAvailableUnits();
		
		if (unitList.size() == 0) {
			return "\n"+CivColor.LightGray+CivSettings.localize.localizedString("Nothing")+"\n"+CivColor.LightGray+CivSettings.localize.localizedString("Available");			
		}
		
		ConfigUnit unit = unitList.get(index);
		String out = "\n";
		int previousSettlers = 1;
		double coinCost = unit.cost;
		if (unit.id.equals("u_settler")) {
			
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("settlers:"+this.getCiv().getName());
			if (entries != null) {
				for (SessionEntry entry : entries) {
					previousSettlers += Integer.parseInt(entry.value);
				}
			}

			coinCost *= previousSettlers;
		}
		
		out += CivColor.LightPurple+unit.name+"\n";
		out += CivColor.Yellow+coinCost+"\n";
		out += CivColor.Yellow+CivSettings.CURRENCY_NAME;
		
		return out;
	}
	
	private void changeIndex(int newIndex) {
		if (this.unitNameSign != null) {
			try {
				this.unitNameSign.setText(getUnitSignText(newIndex));
				index = newIndex;
			} catch (IndexOutOfBoundsException e) {
				//index = 0;
				//this.unitNameSign.setText(getUnitSignText(index));
			}
			this.unitNameSign.update();
		} else {
			CivLog.warning("Could not find unit name sign for barracks:"+this.getId()+" at "+this.getCorner());
		}
	}
	
	
	private void train(Resident whoClicked) throws CivException {
		ArrayList<ConfigUnit> unitList = getTown().getAvailableUnits();

		ConfigUnit unit = unitList.get(index);
		if (unit == null) {
			throw new CivException(CivSettings.localize.localizedString("barracks_unknownUnit"));
		}
		
		if (unit.limit != 0 && unit.limit < getTown().getUnitTypeCount(unit.id)) {
			throw new CivException(CivSettings.localize.localizedString("var_barracks_atLimit",unit.name));
		}
		
		if (!unit.isAvailable(getTown())) {
			throw new CivException(CivSettings.localize.localizedString("barracks_unavailable"));
		}
		
		if (this.trainingUnit != null) {
			throw new CivException(CivSettings.localize.localizedString("var_barracks_inProgress",this.trainingUnit.name));
		}

		if (this.getTown().getCurrentStructureInProgress() != null) {
			throw new CivException(CivSettings.localize.localizedString("var_barracks_strInProgress", getTown().getCurrentStructureInProgress().getDisplayName()));
		}

		int previousSettlers = 1;
		double coinCost = unit.cost;
		if (unit.id.equals("u_settler")) {
			if (!this.getCiv().getLeaderGroup().hasMember(whoClicked) && !this.getCiv().getAdviserGroup().hasMember(whoClicked)) {
				throw new CivException(CivSettings.localize.localizedString("barracks_trainSettler_NoPerms"));
			}
			
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("settlers:"+this.getCiv().getName());
			if (entries != null) {
				CivLog.debug("entries: "+entries.size());
				for (SessionEntry entry : entries) {
					CivLog.debug("value: "+entry.value);
					previousSettlers += Integer.parseInt(entry.value);
				}
			}

			CivLog.debug("previousSettlers: "+previousSettlers);
			coinCost *= previousSettlers;
			CivLog.debug("unit.cost: "+coinCost);
		}
		
		if (!getTown().getTreasury().hasEnough(coinCost)) {
			throw new CivException(CivSettings.localize.localizedString("var_barracks_tooPoor",unit.name,coinCost,CivSettings.CURRENCY_NAME));
		}
		
		
		getTown().getTreasury().withdraw(coinCost);
		
		
		this.setCurrentHammers(0.0);
		this.setTrainingUnit(unit);
		CivMessage.sendTown(getTown(), CivSettings.localize.localizedString("var_barracks_begin",unit.name));
		this.updateTraining();
		if (unit.id.equals("u_settler")) {
			CivGlobal.getSessionDB().add("settlers:"+this.getCiv().getName(), "1" , this.getCiv().getId(), this.getCiv().getId(), this.getId());
		}
		this.onTechUpdate();
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		//int special_id = Integer.valueOf(sign.getAction());
		Resident resident = CivGlobal.getResident(player);
		
		if (resident == null) {
			return;
		}
		
		switch (sign.getAction()) {
		case "prev":
			changeIndex((index-1));
			break;
		case "next":
			changeIndex((index+1));
			break;
		case "train":
			if (resident.hasTown()) {
				try {
				if (getTown().getAssistantGroup().hasMember(resident) || getTown().getMayorGroup().hasMember(resident) || getCiv().getLeaderGroup().hasMember(resident)) {
					train(resident);
				} else {
					throw new CivException(CivSettings.localize.localizedString("barracks_actionNoPerms"));
				}
				} catch (CivException e) {
					CivMessage.send(player, CivColor.Rose+e.getMessage());
				}
			}
			break;
		case "repair_item":
			repairItem(player, resident, event);			
			break;
		}
	}
	
	private void repairItem(Player player, Resident resident, PlayerInteractEvent event) {
		try {
			ItemStack inHand = player.getInventory().getItemInMainHand();
			if (inHand == null || inHand.getType().equals(Material.AIR)) {
				throw new CivException(CivSettings.localize.localizedString("barracks_repair_noItem"));
			}
			
			if (inHand.getType().getMaxDurability() == 0) {
				throw new CivException(CivSettings.localize.localizedString("barracks_repair_invalidItem"));
			}
			
			if (inHand.getDurability() == 0) {
				throw new CivException(CivSettings.localize.localizedString("barracks_repair_atFull"));
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(inHand);
			if (craftMat == null) {
				throw new CivException(CivSettings.localize.localizedString("barracks_repair_irreperable"));
			}
			
			try {
				double totalCost;
				if (craftMat.hasComponent("RepairCost")) {
					RepairCost repairCost = (RepairCost)craftMat.getComponent("RepairCost");
					totalCost = repairCost.getDouble("value");
				} else {
					double baseTierRepair = CivSettings.getDouble(CivSettings.structureConfig, "barracks.base_tier_repair");
					double tierDamp = CivSettings.getDouble(CivSettings.structureConfig, "barracks.tier_damp");
					double tierCost = Math.pow((craftMat.getConfigMaterial().tier), tierDamp);				
					double fromTier = Math.pow(baseTierRepair, tierCost);				
					totalCost = Math.round(fromTier+0);
				}
				
				InteractiveRepairItem repairItem = new InteractiveRepairItem(totalCost, player.getName(), craftMat);
				repairItem.displayMessage();
				resident.setInteractiveMode(repairItem);
				return;
				
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalException"));
			}
			
			
			
		} catch (CivException e) {
			CivMessage.sendError(player, e.getMessage());
			event.setCancelled(true);
		}
	}

	public static void repairItemInHand(double cost, String playerName, LoreCraftableMaterial craftMat) {
		Player player;
		
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		
		Resident resident = CivGlobal.getResident(player);
		
		if (!resident.getTreasury().hasEnough(cost)) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("var_barracks_repair_TooPoor",cost,CivSettings.CURRENCY_NAME));
			return;
		}
		
		LoreCraftableMaterial craftMatInHand = LoreCraftableMaterial.getCraftMaterial(player.getInventory().getItemInMainHand());
		
		if (!craftMatInHand.getConfigId().equals(craftMat.getConfigId())) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("barracks_repair_DifferentItem"));
			return;
		}
		
		resident.getTreasury().withdraw(cost);
		player.getInventory().getItemInMainHand().setDurability((short)0);
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_barracks_repair_Success",craftMat.getName(),cost,CivSettings.CURRENCY_NAME));
		
	}
	
	@Override
	public void onTechUpdate() {
		
		class BarracksSyncUpdate implements Runnable {

			StructureSign unitNameSign;
			
			public BarracksSyncUpdate(StructureSign unitNameSign) {
				this.unitNameSign = unitNameSign;
			}
			
			@Override
			public void run() {

				this.unitNameSign.setText(getUnitSignText(index));
				this.unitNameSign.update();
			}
		}
		
		TaskMaster.syncTask(new BarracksSyncUpdate(this.unitNameSign));
		
	}
		
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		StructureSign structSign;

		switch (sb.command) {
		case "/prev":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());
			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+CivSettings.localize.localizedString("barracks_sign_previousUnit"));
			structSign.setDirection(sb.getData());
			structSign.setAction("prev");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			
			break;
		case "/unitname":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());

			structSign = new StructureSign(absCoord, this);
			structSign.setText(getUnitSignText(0));
			structSign.setDirection(sb.getData());
			structSign.setAction("info");
			structSign.update();
			
			this.unitNameSign = structSign;
			
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			
			break;
		case "/next":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());

			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+CivSettings.localize.localizedString("barracks_sign_nextUnit"));
			structSign.setDirection(sb.getData());
			structSign.setAction("next");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
						
			break;
		case "/train":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());

			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+CivSettings.localize.localizedString("barracks_sign_train"));
			structSign.setDirection(sb.getData());
			structSign.setAction("train");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			
			break;
		case "/progress":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());

			structSign = new StructureSign(absCoord, this);
			structSign.setText("");
			structSign.setDirection(sb.getData());
			structSign.setAction("");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			
			this.progresBar.put(Integer.valueOf(sb.keyvalues.get("id")), structSign);
			
			break;
		case "/repair":
			ItemManager.setTypeId(absCoord.getBlock(), sb.getType());
			ItemManager.setData(absCoord.getBlock(), sb.getData());

			structSign = new StructureSign(absCoord, this);
			structSign.setText("\n"+ChatColor.BOLD+ChatColor.UNDERLINE+CivSettings.localize.localizedString("barracks_sign_repairItem"));
			structSign.setDirection(sb.getData());
			structSign.setAction("repair_item");
			structSign.update();
			this.addStructureSign(structSign);
			CivGlobal.addStructureSign(structSign);
			
			break;

		}
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public ConfigUnit getTrainingUnit() {
		return trainingUnit;
	}

	public void setTrainingUnit(ConfigUnit trainingUnit) {
		this.trainingUnit = trainingUnit;
	}

	public double getCurrentHammers() {
		return currentHammers;
	}

	public void setCurrentHammers(double currentHammers) {
		this.currentHammers = currentHammers;
	}

	public double getPercentageProgress() {
		return (this.currentHammers / this.getHammerCostUnit());
	}

	public int getHammerCostUnit() {
		double hammerrate = 1.0;

		ConfigPublicInstitution pi = getCiv().getInstitute("freedom_2");
		if (pi != null) {
			hammerrate += pi.value;
		}
		double cost = this.getTrainingUnit().hammer_cost * hammerrate;

		if (this.getTrainingUnit() == null) {
			return 0;
		} else {
			return (int) cost;
		}
	}

	public void createUnit(ConfigUnit unit) {
		
		// Find the chest inventory
		ArrayList<StructureChest> chests = this.getAllChestsById(0);
		if (chests.size() == 0) {
			return;
		}
		
		Chest chest = (Chest)chests.get(0).getCoord().getBlock().getState();
		
		try {
			Class<?> c = Class.forName(unit.class_name);
			Method m = c.getMethod("spawn", Inventory.class, Town.class);
			m.invoke(null, chest.getInventory(), this.getTown());
			
			CivMessage.sendTown(this.getTown(), CivSettings.localize.localizedString("var_barracks_completedTraining",unit.name));
			this.trainingUnit = null;
			this.currentHammers = 0.0;
			
			CivGlobal.getSessionDB().delete_all(getSessionKey());
			
		} catch (ClassNotFoundException | SecurityException | 
				IllegalAccessException | IllegalArgumentException | NoSuchMethodException e) {
			this.trainingUnit = null;
			this.currentHammers = 0.0;
			CivMessage.sendTown(getTown(), CivColor.Red+CivSettings.localize.localizedString("barracks_errorUnknown")+e.getMessage());
		} catch (InvocationTargetException e) {
			CivMessage.sendTown(getTown(), CivColor.Rose+e.getCause().getMessage());
			this.currentHammers -= 20.0;
			if (this.currentHammers < 0.0) {
				this.currentHammers = 0.0;
			}
		//	e.getCause().getMessage()
			//e.printStackTrace();
		//	CivMessage.sendTown(getTown(), CivColor.Rose+e.getMessage());
		}
		
	}
	
	public void updateProgressBar() {
		/*double percentageDone = 0.0;
		
		if (this.getTown().getCiv().hasInstitution("freedom_2")) {		
			percentageDone = this.currentHammers / (this.trainingUnit.hammer_cost * 0.75);
		} else {
			percentageDone = this.currentHammers / this.trainingUnit.hammer_cost;
		}*/

		int size = this.progresBar.size();
		int textCount = (int) (size*16*getPercentageProgress());
		int textIndex = 0;
		
		for (int i = 0; i < size; i++) {
			StructureSign structSign = this.progresBar.get(i);
			String[] text = new String[4];
			text[0] = "";
			text[1] = "";
			text[2] = "";
			text[3] = "";
			for (int j = 0; j < 16; j++) {
				if (textIndex == 0) {
					text[2] += "[";
				} else if (textIndex == ((size*15)+3)) {
					text[2] += "]";
				} else if (textIndex < textCount) {
					text[2] += "=";
				} else {
					text[2] += "_";
				}
	
				textIndex++;
			}
	
			if (i == (size/2)) {
				text[1] = CivColor.LightGreen+this.trainingUnit.name;
			}
			
			structSign.setText(text);
			structSign.update();
		}
				
	}
	
	public String getSessionKey() {
		return this.getTown().getName()+":"+"barracks"+":"+this.getId();
	}

	public void saveProgress() {
		if (this.getTrainingUnit() != null) {
			String key = getSessionKey();
			String value = this.getTrainingUnit().id+":"+this.currentHammers; 
			ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);

			if (entries.size() > 0) {
				SessionEntry entry = entries.get(0);
				CivGlobal.getSessionDB().update(entry.request_id, key, value);
				
				/* delete any bad extra entries. */
				for (int i = 1; i < entries.size(); i++) {
					SessionEntry bad_entry = entries.get(i);
					CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
				}
			} else {
				this.sessionAdd(key, value);
			}
			
			lastSave = new Date();
		}	
	}
	
	@Override
	public void onUnload() {
		saveProgress();
	}
	
	@Override
	public void onLoad() {
		String key = getSessionKey();
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(key);
	
		if (entries.size() > 0) {
			SessionEntry entry = entries.get(0);
			String[] values = entry.value.split(":");
			
			this.trainingUnit = CivSettings.units.get(values[0]);
			
			if (trainingUnit == null) {
				CivLog.error("Couldn't find in-progress unit id:"+values[0]+" for town "+this.getTown().getName());
				return;
			}
			
			this.currentHammers = Double.valueOf(values[1]);
			
			/* delete any bad extra entries. */
			for (int i = 1; i < entries.size(); i++) {
				SessionEntry bad_entry = entries.get(i);
				CivGlobal.getSessionDB().delete(bad_entry.request_id, key);
			}
		} 
	}
	
	
	public void updateTraining() {
		if (this.trainingUnit != null) {
			// Hammers are per hour, this runs per min. We need to adjust the hammers we add.
			double addedHammers = (getTown().getHammers().total / 60) / 60;
			this.currentHammers += addedHammers;
			
			
			this.updateProgressBar();
			Date now = new Date();
			
			if (lastSave == null || ((lastSave.getTime() + SAVE_INTERVAL) < now.getTime())) {
				TaskMaster.asyncTask(new UnitSaveAsyncTask(this), 0);
			}

			if (getPercentageProgress() >= 1) {
				createUnit(getTrainingUnit());
			}
			
			/*if (this.getTown().getCiv().hasInstitution("freedom_2")) {
				if (this.currentHammers >= this.trainingUnit.hammer_cost*0.75) {
					this.currentHammers = this.trainingUnit.hammer_cost*0.75;
					this.createUnit(this.trainingUnit);
				}
			} else {			
			    if (this.currentHammers >= this.trainingUnit.hammer_cost) {
					this.currentHammers = this.trainingUnit.hammer_cost;
					this.createUnit(this.trainingUnit);
			    }
			}*/
			
		}
	}
	
}
