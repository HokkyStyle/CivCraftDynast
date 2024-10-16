package com.dynast.civcraft.arena;


import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigArena;
import com.dynast.civcraft.config.ConfigArenaTeam;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;

public class Arena {
	public ConfigArena config;
	public int instanceID;
	
	private HashMap<Integer, ArenaTeam> teams = new HashMap<>();
	private HashMap<Integer, Integer> teamIDmap = new HashMap<>();
	private HashMap<Integer, Integer> teamHP = new HashMap<>();
	private HashMap<String, Inventory> playerInvs = new HashMap<>();
	public  HashMap<String, Scoreboard> scoreboards = new HashMap<>();
	public  HashMap<String, Objective> objectives = new HashMap<>();
	public int timeleft;
	public boolean ended = false;
		
	int teamCount = 0;
	
	public static int nextInstanceID = 0;
	
	public Arena(ConfigArena a) throws CivException {
		this.config = a;
		
		/* Search for a free instance id. */
		boolean found = false;
		int id = 0;
		for (int i = 0; i < ArenaManager.MAX_INSTANCES; i++) {
			String possibleName = getInstanceName(id, config);
			if (ArenaManager.activeArenas.containsKey(possibleName)) {
				id++;
			} else {
				found = true;
				break;
			}
		}
		
		if (!found) {
			throw new CivException("Couldn't find a free instance ID!");
		}
		
		try {
			this.timeleft = CivSettings.getInteger(CivSettings.arenaConfig, "timeout");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
		this.instanceID = id;
				
	}

	public static String getInstanceName(int id, ConfigArena config) {
		String instanceWorldName = config.world_source+"_"+"instance_"+id;
		return instanceWorldName;
	}
	
	public String getInstanceName() {
		return getInstanceName(this.instanceID, config);
	}
	
	@SuppressWarnings("deprecation")
	public void addTeam(ArenaTeam team) throws CivException {
		this.scoreboards.put(team.getName(), ArenaManager.scoreboardManager.getNewScoreboard());
		
		teams.put(teamCount, team);
		teamIDmap.put(team.getId(), teamCount);
		teamHP.put(teamCount, config.teams.get(teamCount).controlPoints.size());
		team.setScoreboardTeam(getScoreboard(team.getName()).registerNewTeam(team.getName()));
		team.getScoreboardTeam().setAllowFriendlyFire(false);
		if (teamCount == 0) {
			team.setTeamColor(CivColor.Blue);
		} else {
			team.setTeamColor(CivColor.Gold);
		}
		
		for (Resident resident : team.teamMembers) {
			try {
			CivGlobal.getPlayer(resident);
			} catch (CivException e) {
				continue;
			}
			
			try {
				teleportToRandomRevivePoint(resident, teamCount);
				createInventory(resident);
				team.getScoreboardTeam().addPlayer(Bukkit.getOfflinePlayer(resident.getUUID()));
			} catch (CivException e) {
				e.printStackTrace();
			}
		}
		
		
		teamCount++;
	}
	
	private void createInventory(Resident resident) {
		Player player;
			try {
				player = CivGlobal.getPlayer(resident);
			
			Inventory inv = Bukkit.createInventory(player, 9*6, resident.getName()+"'s Gear");

			for (int i = 0; i < 3; i++) {
				addCivCraftItemToInventory("mat_iron_sword", inv);
				addCivCraftItemToInventory("mat_iron_boots", inv);
				addCivCraftItemToInventory("mat_iron_chestplate", inv);
				addCivCraftItemToInventory("mat_iron_leggings", inv);
				addCivCraftItemToInventory("mat_iron_helmet", inv);
		
				addCivCraftItemToInventory("mat_hunting_bow", inv);
				addCivCraftItemToInventory("mat_leather_boots", inv);
				addCivCraftItemToInventory("mat_leather_chestplate", inv);
				addCivCraftItemToInventory("mat_leather_leggings", inv);
				addCivCraftItemToInventory("mat_leather_helmet", inv);
			}

			
			playerInvs.put(resident.getName(), inv);
			} catch (CivException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
        
    private void addCivCraftItemToInventory(String id, Inventory inv) {
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(id);
		ItemStack stack = LoreCraftableMaterial.spawn(craftMat);
		stack = LoreCraftableMaterial.addEnhancement(stack, LoreEnhancement.enhancements.get("LoreEnhancementArenaItem"));
        stack = LoreCraftableMaterial.addEnhancement(stack, LoreEnhancement.enhancements.get("LoreEnhancementSoulBound"));
		inv.addItem(stack);
	}
	
	private ConfigArenaTeam getConfigTeam(int id) throws CivException {
		for (ConfigArenaTeam ct : config.teams) {
			if (ct.number == id) {
				return ct;
			}
		}
		throw new CivException("Couldn't find configuration for team id:"+id);
	}
	
	public void teleportToRandomRevivePoint(Resident r, int teamID) throws CivException {
		ConfigArenaTeam ct = getConfigTeam(teamID);
		Random rand = new Random();
		int index = rand.nextInt(ct.revivePoints.size());
		
		int i = 0;
		for (BlockCoord coord : ct.revivePoints) {
			
			if (index == i) {
				try {
					Player player = CivGlobal.getPlayer(r);
					coord.setWorldname(this.getInstanceName());
					player.teleport(coord.getLocation());
				} catch (CivException e) {
					// Player offline..
					e.printStackTrace();
				}
			}
			i++;
		}

	}

	public void returnPlayers() {
		for (ArenaTeam team : teams.values()) {
			for (Resident r : team.teamMembers) {
				try {
					try {
						/* Only set inside arena to false if the player is online. */
						Player player = CivGlobal.getPlayer(r);
						player.setScoreboard(ArenaManager.scoreboardManager.getNewScoreboard());
						
						r.setInsideArena(false);
						r.restoreInventory();
						r.teleportHome();
						r.save();
						CivMessage.send(r, CivColor.LightGray+CivSettings.localize.localizedString("arena_endedTeleport"));
					} catch (CivException e) {
						/* player not online, inside arena is set true */
					}
				} catch (Exception e) {
					// Continue if there was an error restoring one player.
					r.teleportHome();
					e.printStackTrace();
				}
			}
		}
	}

	public Collection<ArenaTeam> getTeams() {
		return teams.values();
	}
	
	public ArenaTeam getTeamFromID(int id) {
		return teams.get(id);
	}
	
	public void onControlBlockDestroy(int teamID, ArenaTeam attackingTeam) {
		Integer hp = teamHP.get(teamID);
		hp--;
		teamHP.put(teamID, hp);
		
		ArenaTeam team = teams.get(teamID);
		
		if (hp <= 0) {
			ArenaManager.declareVictor(this, team, attackingTeam);
		}
		
	}

	public void clearTeams() {
		for (ArenaTeam team : teams.values()) {
			team.setCurrentArena(null);
		}
		
		teams.clear();
	}

	public Location getRespawnLocation(Resident resident) {
		int teamID = teamIDmap.get(resident.getTeam().getId());
		for (int i = 0; i < config.teams.size(); i++) {
			ConfigArenaTeam configTeam = config.teams.get(i);
			if (configTeam.number == teamID) {
				Random rand = new Random();
				int index = rand.nextInt(configTeam.respawnPoints.size());
				return configTeam.respawnPoints.get(index).getCenteredLocation();
			}
		}
		
		return null;
	}

	public BlockCoord getRandomReviveLocation(Resident resident) {
		int teamID = teamIDmap.get(resident.getTeam().getId());
		for (int i = 0; i < config.teams.size(); i++) {
			ConfigArenaTeam configTeam = config.teams.get(i);
			if (configTeam.number == teamID) {
				Random rand = new Random();
				int index = rand.nextInt(configTeam.respawnPoints.size());
				return configTeam.revivePoints.get(index);
			}
		}
		
		return null;
	}

	public Inventory getInventory(Resident resident) {
		return playerInvs.get(resident.getName());
	}
	
	public Scoreboard getScoreboard(String name) {
		return this.scoreboards.get(name);
	}

	public void decrementScoreForTeamID(int teamID) {
		ArenaTeam team = getTeamFromID(teamID);
		
		for (ArenaTeam t : this.teams.values()) {
			Objective obj = this.objectives.get(t.getName()+";score");
			
			for (ArenaTeam t2 : this.teams.values()) {
				Score score = obj.getScore(t2.getTeamScoreboardName());
				if (t2.getName().equals(team.getName())) {
					score.setScore(score.getScore() - 1);
				}
			}
		}
	}

	public void decrementTimer() {
		if (timeleft <= 0) {
			if (!ended) {
				CivMessage.sendArena(this, CivSettings.localize.localizedString("arena_timeUp"));
				ArenaManager.declareDraw(this);
				ended = true;
			}
		} else {
			this.timeleft--;

			for (ArenaTeam team : this.teams.values()) {	
				Objective obj = objectives.get(team.getName()+";score");
				Score score = obj.getScore("Time Left");
				score.setScore(timeleft);
			}
		}
	}
	
}
