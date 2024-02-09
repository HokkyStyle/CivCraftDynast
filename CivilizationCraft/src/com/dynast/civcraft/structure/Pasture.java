package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.dynast.civcraft.threading.tasks.SpawnMobTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;

import com.dynast.civcraft.components.ConsumeLevelComponent;
import com.dynast.civcraft.components.ConsumeLevelComponent.Result;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigPastureLevel;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.CivTaskAbortException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.LoadPastureEntityTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.MultiInventory;

public class Pasture extends Structure {
	
	private ConsumeLevelComponent consumeComp = null;
	
	private int skippedCounter = 0;
	
	public static HashSet<String> debugTowns = new HashSet<>();

	/* Global pasture chunks */
	public static Map<ChunkCoord, Pasture> pastureChunks = new ConcurrentHashMap<>();
	public static Map<UUID, Pasture> pastureEntities = new ConcurrentHashMap<>();
	
	/* Chunks bound to this pasture. */
	public HashSet<ChunkCoord> chunks = new HashSet<>();
	public HashSet<UUID> entities = new HashSet<>();
	public ReentrantLock lock = new ReentrantLock(); 
	
	private int pendingBreeds = 0;
	
	protected Pasture(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}

	public Pasture(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	
	public String getMarkerIconName(){
		return "pasture";
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
	
	public ConsumeLevelComponent getConsumeComponent() {
		if (consumeComp == null) {
			consumeComp = (ConsumeLevelComponent) this.getComponent(ConsumeLevelComponent.class.getSimpleName());
		}
		return consumeComp;
	}
	
	public String getkey() {
		return this.getTown().getName()+"_"+this.getConfigId()+"_"+this.getCorner().toString();
	}

	public static void debug(Pasture pasture, String msg) {
		if (debugTowns.contains(pasture.getTown().getName())) {
			CivLog.warning("TrommelDebug:"+pasture.getTown().getName()+":"+msg);
		}
	}

    public Result consume(CivAsyncTask task) throws InterruptedException {

		if (this.getChests().size() == 0)
			return Result.STAGNATE;

		MultiInventory multiInv = new MultiInventory();

		ArrayList<StructureChest> chests = this.getAllChestsById(0);

		//ArrayList<StructureChest> schests = this.getAllChestsById(5);

		for (StructureChest c : chests) {
			task.syncLoadChunk(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getZ());
			Inventory tmp;
				try {
					tmp = task.getChestInventory(c.getCoord().getWorldname(), c.getCoord().getX(), c.getCoord().getY(), c.getCoord().getZ(), true);
					multiInv.addInventory(tmp);
				} catch (CivTaskAbortException e) {
					return Result.STAGNATE;
			}
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

    public void pastureConsume(CivAsyncTask task) throws InterruptedException {
		Result result = this.consume(task);
		switch (result) {
		case STARVE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_pasture_productionFell",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case LEVELDOWN:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_pasture_lostalvl",getConsumeComponent().getLevel()));
			return;
		case STAGNATE:
			CivMessage.sendTown(getTown(), CivColor.Rose+CivSettings.localize.localizedString("var_pasture_stagnated",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			return;
		case GROW:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_pasture_productionGrew",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case LEVELUP:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_pasture_lvlUp",getConsumeComponent().getLevel()));
			break;
		case MAXED:
			CivMessage.sendTown(getTown(), CivColor.LightGreen+CivSettings.localize.localizedString("var_pasture_maxed",getConsumeComponent().getLevel(),getConsumeComponent().getCountString()));
			break;
		case UNKNOWN:
			CivMessage.sendTown(getTown(), CivColor.DarkPurple+CivSettings.localize.localizedString("pasture_unknown"));
			return;
		default:
			break;
		}

		ConfigPastureLevel lvl = CivSettings.pastureLevels.get(getConsumeComponent().getLevel());

		for (int cow = 0; cow < lvl.cows; cow++) {
			if (this.processMobBreed(this.getTown())) {
				task.syncLoadChunk("world", this.getCenterLocation().getX(), this.getCenterLocation().getZ());
				TaskMaster.syncTask(new SpawnMobTask(this.getCenterLocation().getLocation(), EntityType.COW, this));
				//LivingEntity entity = (LivingEntity) Bukkit.getWorld("world").spawnEntity(this.getCenterLocation().getLocation(), EntityType.COW);
				//this.onBreed(entity);
			}
		}

		for (int chicken = 0; chicken < lvl.chickens; chicken++) {
			if (this.processMobBreed(this.getTown())) {
				task.syncLoadChunk("world", this.getCenterLocation().getX(), this.getCenterLocation().getZ());
				TaskMaster.syncTask(new SpawnMobTask(this.getCenterLocation().getLocation(), EntityType.CHICKEN, this));
				//LivingEntity entity = (LivingEntity) Bukkit.getWorld("world").spawnEntity(this.getCenterLocation().getLocation(), EntityType.CHICKEN);
				//this.onBreed(entity);
			}
		}

		for (int sheep = 0; sheep < lvl.sheeps; sheep++) {
			if (this.processMobBreed(this.getTown())) {
				task.syncLoadChunk("world", this.getCenterLocation().getX(), this.getCenterLocation().getZ());
				TaskMaster.syncTask(new SpawnMobTask(this.getCenterLocation().getLocation(), EntityType.SHEEP, this));
				//LivingEntity entity = (LivingEntity) Bukkit.getWorld("world").spawnEntity(this.getCenterLocation().getLocation(), EntityType.SHEEP);
				//this.onBreed(entity);
			}
		}

		for (int pig = 0; pig < lvl.pigs; pig++) {
			if (this.processMobBreed(this.getTown())) {
				task.syncLoadChunk("world", this.getCenterLocation().getX(), this.getCenterLocation().getZ());
				TaskMaster.syncTask(new SpawnMobTask(this.getCenterLocation().getLocation(), EntityType.PIG, this));
				//LivingEntity entity = (LivingEntity) Bukkit.getWorld("world").spawnEntity(this.getCenterLocation().getLocation(), EntityType.PIG);
				//this.onBreed(entity);
			}
		}

	}

	public int getLevel() {
		return this.getConsumeComponent().getLevel();
	}

	public int getCount() {
		return this.getConsumeComponent().getCount();
	}

	public int getMaxCount() {
		int level = getLevel();
		ConfigPastureLevel lvl = CivSettings.pastureLevels.get(level);

		return lvl.amount;
	}

	public Result getLastResult() {
		return this.getConsumeComponent().getLastResult();
	}

	public void delevel() {
		int currentLevel = getLevel();

		if (currentLevel > 1) {
			getConsumeComponent().setLevel(getLevel()-1);
			getConsumeComponent().setCount(0);
			getConsumeComponent().onSave();
		}
	}

	public void onDestroy() {
		super.onDestroy();

		getConsumeComponent().setLevel((int)Math.round(this.getLevel()/1.8));
		getConsumeComponent().setCount(0);
		getConsumeComponent().onSave();
	}
	

	public int getMobCount() {
		return entities.size();
	}

	public int getMobMax() {
		int max;
		try {
			max = CivSettings.getInteger(CivSettings.structureConfig, "pasture.max_mobs");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return 0;
		}
		return max;
	}

	public boolean processMobBreed(Player player) {
				
		if (!this.isActive()) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("pasture_destroyed"));
			return false;
		}
		
		if (this.getMobCount() >= this.getMobMax()) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("pasture_isFull"));
			return false;
		}
		
		if ((getPendingBreeds() + this.getMobCount()) >= this.getMobMax()) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("pasture_TooMuchorIsFull",CivSettings.localize.localizedString("pasture_isFull")));
			return false;
		}
		
		return true;
	}

	public boolean processMobBreed(Town player) {

		if (!this.isActive()) {
			CivMessage.sendTown(player, CivSettings.localize.localizedString("town_pasture_destroyed"));
			return false;
		}

		if (this.getMobCount() >= this.getMobMax()) {
			CivMessage.sendTown(player, CivSettings.localize.localizedString("town_pasture_isFull"));
			return false;
		}

		return true;
	}
	
	public void bindPastureChunks() {
		for (BlockCoord bcoord : this.structureBlocks.keySet()) {
			ChunkCoord coord = new ChunkCoord(bcoord);
			this.chunks.add(coord);
			pastureChunks.put(coord, this);
		}
	}
	
	public void unbindPastureChunks() {
		for (ChunkCoord coord : this.chunks) {
			pastureChunks.remove(coord);
		}
		
		this.entities.clear();
		this.chunks.clear();
		
		LinkedList<UUID> removeUs = new LinkedList<>();
		for (UUID id : pastureEntities.keySet()) {
			Pasture pasture = pastureEntities.get(id);
			if (pasture == this) {
				removeUs.add(id);
			}
		}
		
		for (UUID id : removeUs) {
			pastureEntities.remove(id);
		}
		
	}
	
	@Override
	public void onComplete() {
		bindPastureChunks();
	}
	
	@Override
	public void onLoad() throws CivException {
		bindPastureChunks();
		loadEntities();
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		unbindPastureChunks();
		if (getConsumeComponent() != null) {
			getConsumeComponent().onDelete();
		}
	}

	public void onBreed(LivingEntity entity) {
		saveEntity(entity.getWorld().getName(), entity.getUniqueId());
		setPendingBreeds(getPendingBreeds() - 1);
	}
	
	public String getEntityKey() {
		return "pasture:"+this.getId();
	}
	
	public String getValue(String worldName, UUID id) {
		return worldName+":"+id;
	}
	
	public void saveEntity(String worldName, UUID id) {
		class AsyncTask implements Runnable {
			Pasture pasture;
			UUID id;
			String worldName;
		
			public AsyncTask(Pasture pasture, UUID id, String worldName) {				
				this.pasture = pasture;
				this.id = id;
				this.worldName = worldName;
			}
			
			@Override
			public void run() {
				pasture.sessionAdd(getEntityKey(), getValue(worldName, id));
				lock.lock();
				try {
					entities.add(id);
					pastureEntities.put(id, pasture);
				} finally {
					lock.unlock();
				}
			}
		}
		
		TaskMaster.asyncTask(new AsyncTask(this, id, worldName), 0);
	}
	
	public void loadEntities() {
		Queue<SessionEntry> entriesToLoad = new LinkedList<>();
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getEntityKey());
		entriesToLoad.addAll(entries);
		TaskMaster.syncTask(new LoadPastureEntityTask(entriesToLoad, this));
	}
	
	public void onEntityDeath(LivingEntity entity) {
		class AsyncTask implements Runnable {
			LivingEntity entity;
			
			public AsyncTask(LivingEntity entity) {
				this.entity = entity;
			}
			
			
			@Override
			public void run() {
				lock.lock();
				try {
					entities.remove(entity.getUniqueId());
					pastureEntities.remove(entity.getUniqueId());
				} finally {
					lock.unlock();
				}
			}
			
		}
		
		TaskMaster.asyncTask(new AsyncTask(entity), 0);
	}

	public int getPendingBreeds() {
		return pendingBreeds;
	}

	public void setPendingBreeds(int pendingBreeds) {
		this.pendingBreeds = pendingBreeds;
	}
	
}
