
package com.dynast.civcraft.threading.tasks;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;

import com.dynast.civcraft.util.FireworkEffectPlayer;

public class FireWorkTask implements Runnable {
	
	FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
	FireworkEffect fe;
	int repeats;
	World world;
	Location loc;
	
	public FireWorkTask(FireworkEffect fe, World world, Location loc, int repeats) {
		this.fe = fe;
		this.repeats = repeats;
		this.world = world;
		this.loc = loc;
	}
	
	@Override
	public void run() {
		for (int i = 0; i < repeats; i++) {
			try {
				fplayer.playFirework(world, loc, fe);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
