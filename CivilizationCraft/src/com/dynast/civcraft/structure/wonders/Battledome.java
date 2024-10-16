package com.dynast.civcraft.structure.wonders;

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

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.LoadBattledomeEntityTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;

public class Battledome extends Wonder {

	/* Global Battle-dome chunks */
	public static Map<ChunkCoord, Battledome> battledomeChunks = new ConcurrentHashMap<>();
	public static Map<UUID, Battledome> battledomeEntities = new ConcurrentHashMap<>();

	/* Chunks bound to this Battle-dome. */
	public HashSet<ChunkCoord> chunks = new HashSet<>();
	public HashSet<UUID> entities = new HashSet<>();
	public ReentrantLock lock = new ReentrantLock(); 

	public Battledome(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	public Battledome(Location center, String id, Town town) throws CivException {
		super(center, id, town);
	}
	
	public int getMobCount() {
		return entities.size();
	}

	public int getMobMax() {
		int max;
		try {
			max = CivSettings.getInteger(CivSettings.structureConfig, "battledome.max_mobs");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return 0;
		}
		return max;
	}
	
	public void bindBattledomeChunks() {
		for (BlockCoord bcoord : this.structureBlocks.keySet()) {
			ChunkCoord coord = new ChunkCoord(bcoord);
			this.chunks.add(coord);
			battledomeChunks.put(coord, this);
		}
	}
	
	public void unbindBattledomeChunks() {
		for (ChunkCoord coord : this.chunks) {
			battledomeChunks.remove(coord);
		}
		
		this.entities.clear();
		this.chunks.clear();
		
		LinkedList<UUID> removeUs = new LinkedList<>();
		for (UUID id : battledomeEntities.keySet()) {
			Battledome battledome = battledomeEntities.get(id);
			if (battledome == this) {
				removeUs.add(id);
			}
		}
		
		for (UUID id : removeUs) {
			battledomeEntities.remove(id);
		}
		
	}
	
	@Override
	public void onComplete() {
		bindBattledomeChunks();
	}
	
	@Override
	public void onLoad() {
		bindBattledomeChunks();
		loadEntities();
	}
	
	public String getEntityKey() {
		return "battledome:"+this.getId();
	}
	
	public String getValue(String worldName, UUID id) {
		return worldName+":"+id;
	}
	
	public void saveEntity(String worldName, UUID id) {
		class AsyncTask implements Runnable {
			Battledome battledome;
			UUID id;
			String worldName;
			
			public AsyncTask(Battledome battledome, UUID id, String worldName) {
				this.battledome = battledome;
				this.id = id;
				this.worldName = worldName;
			}

			@Override
			public void run() {
				battledome.sessionAdd(getEntityKey(), getValue(worldName, id));
				lock.lock();
				try {
					entities.add(id);
					battledomeEntities.put(id, battledome);
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
		TaskMaster.syncTask(new LoadBattledomeEntityTask(entriesToLoad, this));
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
					battledomeEntities.remove(entity.getUniqueId());
				} finally {
					lock.unlock();
				}
			}
			
		}
		
		TaskMaster.asyncTask(new AsyncTask(entity), 0);
	}
	
	@Override
	public void delete() throws SQLException {
		super.delete();
		unbindBattledomeChunks();
		clearEntities();
	}
	
	public void clearEntities() {
		// TODO Clear entities bound to us?
	}

	@Override
	protected void removeBuffs() {
	}

	@Override
	protected void addBuffs() {		
	}

}
