
package com.dynast.civcraft.util;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.dynast.civcraft.main.CivCraft;

public class BukkitObjects {

	private static CivCraft plugin = null;
	private static Server server = null;
	
	public static void initialize(CivCraft plugin) {

		BukkitObjects.plugin = plugin;
		BukkitObjects.server = plugin.getServer();
	}

	public static List<World> getWorlds() {
		return  getServer().getWorlds();
	}
	
	public static World getWorld(String name) {
		return  getServer().getWorld(name);
	}
	
	public static Server getServer() {
		synchronized(server) {
			return server;
		}
	}
	
	public static BukkitScheduler getScheduler() {
		return getServer().getScheduler();
	}
	
	public static int scheduleSyncDelayedTask(Runnable task, long delay) {
		return getScheduler().scheduleSyncDelayedTask(plugin, task, delay);
	}
	
	public static BukkitTask scheduleAsyncDelayedTask(Runnable task, long delay) {
		return getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
	}
	
	public static int scheduleSyncRepeatingTask(Runnable task, long delay, long repeat) {
		return getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, repeat);
	}
	
	public static BukkitTask scheduleAsyncRepeatingTask(Runnable task, long delay, long repeat) {
		return getScheduler().runTaskTimerAsynchronously(plugin, task, delay, repeat);
	}

}
