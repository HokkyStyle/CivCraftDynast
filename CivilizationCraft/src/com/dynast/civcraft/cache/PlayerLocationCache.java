
package com.dynast.civcraft.cache;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.VanishNoPacketUtil;

public class PlayerLocationCache {

	private BlockCoord coord;
	private String name;
	private Resident resident;
	private boolean isDead;
	private boolean vanished;
	
	private static ConcurrentHashMap<String, PlayerLocationCache> cache = new ConcurrentHashMap<>();
	//public static ReentrantLock lock = new ReentrantLock();
	
	public static PlayerLocationCache get(String name) {
		return cache.get(name);
	}
		
	public static void add(Player player) {
		
		if (cache.containsKey(player.getName())) {
			return;
		}
		
		//Do not add creative of spectator players into the location cache
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		}
		
		Resident resident = CivGlobal.getResident(player);
		if (resident == null) {
			return;
		}
		
		PlayerLocationCache pc = new PlayerLocationCache();
		pc.setCoord(new BlockCoord(player.getLocation()));
		pc.setResident(resident);
		pc.setName(player.getName());
		pc.setDead(player.isDead());
		pc.setVanished(false);
		
		if (CivSettings.hasVanishNoPacket) {
			pc.setVanished(VanishNoPacketUtil.isVanished(player));
		}
		
		cache.put(pc.getName(), pc);
	}
	
	public static void remove(String playerName) {
		cache.remove(playerName);
	}
	
	public static void updateLocation(Player player) {
		
		PlayerLocationCache pc = get(player.getName());
		if (pc == null) {
			add(player);
			return;
		}
		
		pc.getCoord().setFromLocation(player.getLocation());
		pc.setDead(player.isDead());
		
		Resident resident = CivGlobal.getResident(player);
		if (resident != null) {
			resident.onRoadTest(pc.getCoord(), player);
//			resident.onWaterTest(pc.getCoord(), player);
		}
		
		if (CivSettings.hasVanishNoPacket) {
			pc.setVanished(VanishNoPacketUtil.isVanished(player));
		} else {
			pc.setVanished(false);
		}
	}
	
	public static Collection<PlayerLocationCache> getCache() {
		return cache.values();
	}
	
	public static List<PlayerLocationCache> getNearbyPlayers(BlockCoord bcoord, double radiusSquared) {
		LinkedList<PlayerLocationCache> list = new LinkedList<>();
		
		for (PlayerLocationCache pc : cache.values()) {
			if (pc.getCoord().distanceSquared(bcoord) < radiusSquared) {
				list.add(pc);
			}
		}
		
		return list;
	}
	
	public BlockCoord getCoord() {
		return coord;
	}
	
	public void setCoord(BlockCoord coord) {
		this.coord = coord;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Resident getResident() {
		return resident;
	}
	
	public void setResident(Resident resident) {
		this.resident = resident;
	}
	
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PlayerLocationCache) {
			PlayerLocationCache otherCache = (PlayerLocationCache)other;
			return otherCache.getName().equalsIgnoreCase(this.getName());
		}
		return false;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public boolean isVanished() {
		return vanished;
	}

	public void setVanished(boolean vanished) {
		this.vanished = vanished;
	}


}
