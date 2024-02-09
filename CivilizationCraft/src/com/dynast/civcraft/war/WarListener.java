package com.dynast.civcraft.war;

import java.util.HashSet;
import java.util.Random;

import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.object.*;
import com.dynast.civcraft.structure.Wall;
import com.dynast.civcraft.threading.tasks.StructureBlockHitEvent;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.dynast.civcraft.camp.CampBlock;
import com.dynast.civcraft.camp.WarCamp;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.structure.TownHall;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.tasks.FireWorkTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.CivColor;

public class WarListener implements Listener {

	public static final String RESTORE_NAME = "special:TNT";
	private ChunkCoord coord = new ChunkCoord();
	public static BlockCoord bcoord = new BlockCoord("", 0,0,0);
	
	private PotionEffect slowDigging1 = new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 1, false, false, false);
	private PotionEffect slowDigging2 = new PotionEffect(PotionEffectType.SLOW_DIGGING, 900, 1, false, false,false);
	private PotionEffect slowDigging3 = new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 2, false, false,false);
	private PotionEffect slow1 = new PotionEffect(PotionEffectType.SLOW, 200, 1, false, false, false);
	private PotionEffect slow2 = new PotionEffect(PotionEffectType.SLOW, 300, 2, false, false, false);
	
	public static int yield;
	public static double playerDamage;
	public static int structureDamage;
	static {
		try {
			yield = CivSettings.getInteger(CivSettings.warConfig, "tnt.yield");
			playerDamage = CivSettings.getDouble(CivSettings.warConfig, "tnt.player_damage");
			structureDamage = CivSettings.getInteger(CivSettings.warConfig, "tnt.structure_damage");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (!War.isWarTime()) {
			return;
		}
		
		coord.setFromLocation(event.getBlock().getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		TownChunk tc = CivGlobal.getTownChunk(coord);
		bcoord.setFromLocation(event.getBlock().getLocation());
		StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
		
		if (cc == null) {
			return;
		}
		
		if (!cc.getCiv().getDiplomacyManager().isAtWar()) {
			return;
		}
		
		Player player = event.getPlayer();
		Resident resident = CivGlobal.getResident(player);
		
		int playerlocY = player.getLocation().getBlockY();
		
		if (event.getBlock().getType().equals(Material.DIRT) ||
				event.getBlock().getType().equals(Material.GRASS) ||
				event.getBlock().getType().equals(Material.SAND) ||
				event.getBlock().getType().equals(Material.GRAVEL) ||
				event.getBlock().getType().equals(Material.TORCH) ||
				event.getBlock().getType().equals(Material.LEGACY_REDSTONE_LAMP_OFF) ||
				event.getBlock().getType().equals(Material.LEGACY_REDSTONE_TORCH_ON) ||
				event.getBlock().getType().equals(Material.REDSTONE) ||
				event.getBlock().getType().equals(Material.TNT) ||
				event.getBlock().getType().equals(Material.LADDER) ||
				event.getBlock().getType().equals(Material.VINE) ||
				event.getBlock().getType().equals(Material.IRON_BLOCK) ||
				event.getBlock().getType().equals(Material.GOLD_BLOCK) ||
				event.getBlock().getType().equals(Material.DIAMOND_BLOCK) ||
				event.getBlock().getType().equals(Material.EMERALD_BLOCK) ||
				CivData.isShulker(event.getBlock().getType()) ||
				!event.getBlock().getType().isSolid()) {
		} else {
			if (sb == null) {
				if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
					if (player.getPotionEffect(PotionEffectType.SLOW_DIGGING).getAmplifier() == 1) {
						player.addPotionEffect(slowDigging2, true);
						player.addPotionEffect(slow2, true);
					}
				}

				if (playerlocY >= 54 && playerlocY < 62) {
					player.addPotionEffect(slowDigging1, true);
				}

				if (playerlocY >= 40 && playerlocY < 54) {
					player.addPotionEffect(slowDigging2, true);
					player.addPotionEffect(slow1, true);
				}

				if (playerlocY < 40) {
					player.addPotionEffect(slowDigging3, true);
					player.addPotionEffect(slow2, true);
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		if (!War.isWarTime()) {
			return;
		}
		
		coord.setFromLocation(event.getBlock().getLocation());
		CultureChunk cc = CivGlobal.getCultureChunk(coord);
		
		if (cc == null) {
			return;
		}
		
		if (!cc.getCiv().getDiplomacyManager().isAtWar()) {
			return;
		}

		if (CivData.isShulker(event.getBlock().getType())) {
			return;
		}

		if (event.getBlock().getType().equals(Material.DIRT) || 
			event.getBlock().getType().equals(Material.GRASS) ||
			event.getBlock().getType().equals(Material.SAND) ||
			event.getBlock().getType().equals(Material.GRAVEL) ||
			event.getBlock().getType().equals(Material.TORCH) ||
			event.getBlock().getType().equals(Material.LEGACY_REDSTONE_TORCH_ON) ||
			event.getBlock().getType().equals(Material.LEGACY_REDSTONE_TORCH_OFF) ||
			event.getBlock().getType().equals(Material.REDSTONE) ||
			event.getBlock().getType().equals(Material.LADDER) ||
			event.getBlock().getType().equals(Material.VINE) ||
			event.getBlock().getType().equals(Material.TNT)) {


			
			if (event.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
				return;
			}
			
			event.getBlock().getWorld().spawnFallingBlock(event.getBlock().getLocation(), event.getBlock().getType(), (byte) 0);
			event.getBlock().setType(Material.AIR);
			
			return;
		}
		
		if (event.getBlock().getType().equals(Material.IRON_BLOCK) || 
				event.getBlock().getType().equals(Material.GOLD_BLOCK) ||
				event.getBlock().getType().equals(Material.DIAMOND_BLOCK) ||
				event.getBlock().getType().equals(Material.EMERALD_BLOCK)) {
				
				if (event.getBlock().getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) {
					return;
				}
				
				return;
			}
		
		CivMessage.sendError(event.getPlayer(), CivSettings.localize.localizedString("war_onlyBuildCertainBlocks"));
		event.setCancelled(true);
	}
	
	private void explodeBlock(Block b) {
		WarRegen.explodeThisBlock(b, WarListener.RESTORE_NAME);
		launchExplodeFirework(b.getLocation());
	}
	
	private void launchExplodeFirework(Location loc) {
		Random rand = new Random();
		int rand1 = rand.nextInt(100);
		
		if (rand1 > 95) {
			FireworkEffect fe = FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.YELLOW).with(Type.BURST).withTrail().build();
			TaskMaster.syncTask(new FireWorkTask(fe, loc.getWorld(), loc, 2), 0);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityExplode(EntityExplodeEvent event) {

		if (War.isWarTime()) {
			event.setCancelled(false);
		} else {
			event.setCancelled(true);
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		if (event.getEntity() == null) {
			return;
		}
		
		if (event.getEntityType().equals(EntityType.UNKNOWN)) {
			return;
		}

		if (event.getEntityType().equals(EntityType.PRIMED_TNT) ||
				event.getEntityType().equals(EntityType.MINECART_TNT) || event.getEntityType().equals(EntityType.CREEPER)) {

			HashSet<Buildable> structuresHit = new HashSet<>();
		
			for (int y = -yield; y <= yield; y++) {
				for (int x = -yield; x <= yield; x++) {
					label:
					for (int z = -yield; z <= yield; z++) {
						Location loc = event.getLocation().clone().add(new Vector(x,y,z));
						Block b = loc.getBlock();
						if (loc.distance(event.getLocation()) < yield) {

							BlockCoord bcoord = new BlockCoord();
							bcoord.setFromLocation(loc);
							
							StructureBlock sb = CivGlobal.getStructureBlock(bcoord);
							CampBlock cb = CivGlobal.getCampBlock(bcoord);
							HashSet<Wall> walls = CivGlobal.getWallChunk(new ChunkCoord(bcoord));

							if (walls != null) {
								for (Wall wall : walls) {
									if (wall.isProtectedLocation(bcoord.getLocation())) {
										/*if (!structuresHit.contains(wall)) {
											structuresHit.add(wall);
											StructureBlock tmpStructureBlock = new StructureBlock(bcoord, wall);
											tmpStructureBlock.setAlwaysDamage(true);
											//TNTPrimed tnt = (TNTPrimed)event.getEntity();
											//Player pl = (Player)tnt.getSource();
											//TaskMaster.syncTask(new StructureBlockHitEvent(pl.getName(), bcoord, tmpStructureBlock, Bukkit.getWorld(bcoord.getWorldname())), 0);
										}*/
										continue label;
									}
								}
							}

							if (sb == null && cb == null && b.getType() != Material.BEDROCK) {
								explodeBlock(b);
								continue;
							}
							
							if (sb != null) {
								
								if (!sb.isDamageable()) {
									continue;
								}
								
								if (sb.getOwner() instanceof WarCamp) {
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
												explodeBlock(b);
											} else {
												th.onTNTDamage(structureDamage);
											}
										} else {
											sb.getOwner().onDamage(structureDamage, b.getWorld(), null, sb.getCoord(), sb);
											CivMessage.sendCiv(sb.getCiv(), CivColor.Yellow+CivSettings.localize.localizedString("var_war_tntMsg",sb.getOwner().getDisplayName(),(
													sb.getOwner().getCenterLocation().getX()+","+
													sb.getOwner().getCenterLocation().getY()+","+
													sb.getOwner().getCenterLocation().getZ()+")"),
													(sb.getOwner().getHitpoints()+"/"+sb.getOwner().getMaxHitPoints())));
										}
									}
								} else {
									explodeBlock(b);
								}
							}
						}
					}	
				}
			}
			event.setCancelled(true);
		}

	}

}

