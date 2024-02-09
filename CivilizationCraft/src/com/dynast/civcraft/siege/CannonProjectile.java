package com.dynast.civcraft.siege;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.server.v1_11_R1.EntityPlayer;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.dynast.civcraft.camp.CampBlock;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureBlock;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FireWorkTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.EntityProximity;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.war.WarRegen;

public class CannonProjectile {
	public Cannon cannon;
	public Location loc;
	private Location startLoc;
	public Resident whoFired;
	public double speed = 1.0f;
	
	public static double yield;
	public static double playerDamage;
	public static double maxRange;
	public static int controlBlockHP;
	static {
		try {
			yield = CivSettings.getDouble(CivSettings.warConfig, "cannon.yield");
			playerDamage = CivSettings.getDouble(CivSettings.warConfig, "cannon.player_damage");
			maxRange = CivSettings.getDouble(CivSettings.warConfig, "cannon.max_range");
			controlBlockHP = CivSettings.getInteger(CivSettings.warConfig, "cannon.control_block_hp");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}
	
	public CannonProjectile(Cannon cannon, Location loc, Resident whoFired) {
		this.cannon = cannon;
		this.loc = loc;
		this.startLoc = loc.clone();
		this.whoFired = whoFired;
	}
	
	private void explodeBlock(Block b) {
		WarRegen.explodeThisBlock(b, Cannon.RESTORE_NAME);
		launchExplodeFirework(b.getLocation());
	}
	
	public static BlockCoord bcoord = new BlockCoord();
	public void onHit() {
		//launchExplodeFirework(loc);
		
		int radius = (int)yield;
		HashSet<Buildable> structuresHit = new HashSet<>();
		
		for (int x =  -radius; x < radius; x++) {
			for (int z = -radius; z < radius;  z++) {
				for (int y = -radius; y < radius; y++) {
					
					Block b = loc.getBlock().getRelative(x, y, z);
					if (ItemManager.getId(b) == CivData.BEDROCK) {
						continue;
					}
			
					if (loc.distance(b.getLocation()) <= yield) {
						bcoord.setFromLocation(b.getLocation());
						StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
						CampBlock cb = CivGlobal.getCampBlock(bcoord);
						
						if (sb == null && cb == null) {
							explodeBlock(b);
							continue;
						}
						
						if (sb != null) {
							
							if (!sb.isDamageable()) {
								continue;
							}
							
							if (sb.getOwner() instanceof TownHall) {
								TownHall th = (TownHall)sb.getOwner();
								if (th.getControlPoints().containsKey(bcoord)) {
									continue;
								}
							}
							
							if (!sb.getOwner().isDestroyed()) {
								if (!structuresHit.contains(sb.getOwner())) {
									
									structuresHit.add(sb.getOwner());

									if (sb.getOwner() instanceof TownHall) {
										TownHall th = (TownHall)sb.getOwner();

										if (th.getHitpoints() == 0) {
											//explodeBlock(b);
										} else {
											try {
												th.onCannonDamage(cannon.getDamage(), this);
											} catch (CivException e1) {
												e1.printStackTrace();
											}
										}
									} else {
										Player player = null;
										try {
											player = CivGlobal.getPlayer(whoFired);	
										} catch (CivException e) {
										}
										
										if (!sb.getCiv().getDiplomacyManager().atWarWith(whoFired.getCiv())) {
											if (player != null) {
												CivMessage.sendError(player, CivSettings.localize.localizedString("cannonProjectile_ErrorNotAtWar"));
												return;
											}
										}
										
										sb.getOwner().onDamage(cannon.getDamage(), b.getWorld(), player, sb.getCoord(), sb);
										CivMessage.sendCiv(sb.getCiv(), CivColor.Yellow+CivSettings.localize.localizedString("cannonProjectile_hitAnnounce",sb.getOwner().getDisplayName(),
												sb.getOwner().getCenterLocation().getX()+","+
												sb.getOwner().getCenterLocation().getY()+","+
												sb.getOwner().getCenterLocation().getZ())
												+" ("+sb.getOwner().getHitpoints()+"/"+sb.getOwner().getMaxHitPoints()+")");
									}
									
									CivMessage.sendCiv(whoFired.getCiv(), CivColor.LightGreen+CivSettings.localize.localizedString("var_cannonProjectile_hitSuccess",sb.getOwner().getTown().getName(),sb.getOwner().getDisplayName())+
											" ("+sb.getOwner().getHitpoints()+"/"+sb.getOwner().getMaxHitPoints()+")");
								}
							} else {
								
								if (!Cannon.cannonBlocks.containsKey(bcoord)) {
									explodeBlock(b);
								}
							}
							continue;
						}
					}
				}
			}
		}
		
		/* Instantly kill any players caught in the blast. */
		LinkedList<Entity> players = EntityProximity.getNearbyEntities(null, loc, yield, EntityPlayer.class);
		for (Entity e : players) {
			Player player = (Player)e;
			player.damage(playerDamage);
			if (player.isDead()) {
				CivMessage.global(CivColor.LightGray+CivSettings.localize.localizedString("var_cannonProjectile_userKilled",player.getName(),whoFired.getName()));
			}
		}
	}
	
	private void launchExplodeFirework(Location loc) {
		Random rand = new Random();
		int rand1 = rand.nextInt(100);
		
		if (rand1 > 95) {
			FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).build();
			TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 1), 0);
		}
	}
	
	public boolean advance() {
		Vector dir = loc.getDirection();
		dir.add(new Vector(0.0f, -0.008, 0.0f)); //Apply 'gravity'		
		loc.setDirection(dir);

		loc.add(dir.multiply(speed));
		loc.getWorld().createExplosion(loc, 0.0f, false);
		
		if (ItemManager.getId(loc.getBlock()) != CivData.AIR) {
			return true;
		}
		
		if (loc.distance(startLoc) > maxRange) {
			return true;
		}
		
		return false;
	}
	
	public void fire() {
		class SyncTask implements Runnable {
			CannonProjectile proj;
			
			public SyncTask(CannonProjectile proj) {
				this.proj = proj;
			}
			
			@Override
			public void run() {
				if (proj.advance()) {
					onHit();
					return;
				}
				TaskMaster.syncTask(this, 1);				
			}
		}
		
		TaskMaster.syncTask(new SyncTask(this));
	}
	
}
