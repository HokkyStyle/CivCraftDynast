package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.dynast.civcraft.object.*;
import com.dynast.civcraft.war.War;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.dynast.civcraft.cache.PlayerLocationCache;
import com.dynast.civcraft.components.PlayerProximityComponent;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.util.CivColor;

public class ScoutTower extends Structure {

	double range;
	private PlayerProximityComponent proximityComponent;
	
	private int reportSeconds = 60;
	private int count = 0;
	
	public ScoutTower(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}

	protected ScoutTower(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
		this.hitpoints = this.getMaxHitPoints();
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();

		try {
			range = CivSettings.getDouble(CivSettings.warConfig, "scout_tower.range");
			proximityComponent = new PlayerProximityComponent();
			proximityComponent.createComponent(this);
			
			proximityComponent.setBuildable(this);
			proximityComponent.setCenter(this.getCenterLocation());
			proximityComponent.setRadius(range);
			
			reportSeconds = (int)CivSettings.getDouble(CivSettings.warConfig, "scout_tower.update");
			
			
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
		}
	}
	
	private void scoutDebug(String str) {
		if (this.getCiv().scoutDebug && this.getCiv().scoutDebugPlayer != null) {
			Player player;
			try {
				player = CivGlobal.getPlayer(this.getCiv().scoutDebugPlayer);
			} catch (CivException e) {
				return;
			}
			CivMessage.send(player, CivColor.Yellow+"[ScoutDebug] "+str);
		}
	}
	
	@Override
	public int getMaxHitPoints() {
		double rate = 1;
		if (this.getTown().getBuffManager().hasBuff("buff_chichen_itza_tower_hp")) {
			rate += this.getTown().getBuffManager().getEffectiveDouble("buff_chichen_itza_tower_hp");
			rate += this.getTown().getBuffManager().getEffectiveDouble(Buff.BARRICADE);
		}
		return (int) (info.max_hitpoints * rate);
	}
	
	/*
	 * Asynchronously sweeps for players within the scout tower's radius. If
	 * it finds a player that is not in the civ, then it informs the town.
	 * If the town is the capitol, it informs the civ.
	 */
	public void process(HashSet<String> alreadyAnnounced) {
		count++;
		if (count < reportSeconds) {
			return;
		}
		
		count = 0;
		boolean empty = true;
		
		for (PlayerLocationCache pc : proximityComponent.tryGetNearbyPlayers(true)) {
			empty = false;
			scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_inspectingPlayer")+pc.getName());
			Player player;
			try {
				player = CivGlobal.getPlayer(pc.getName());
			} catch (CivException e) {
				scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_notOnline"));
				return;
			}
			
			if (player.isOp() || player.getGameMode() != GameMode.SURVIVAL) {
				scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_isOP"));
				continue;
			}
			
			Location center = this.getCenterLocation().getLocation();
			
			/* Do not re-announce players announced by other scout towers */
			if (alreadyAnnounced.contains(this.getCiv().getName()+":"+player.getName())) {
				scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_alreadyAnnounced")+pc.getName());
				continue;
			}
			
			/* Always announce outlaws, so skip down to bottom. */
			String relationName = "";
			String relationColor = "";
			if (!this.getTown().isOutlaw(player.getName())) {
				/* do not announce residents in this civ */
				Resident resident = CivGlobal.getResident(player);
				if (resident != null && resident.hasTown() && resident.getCiv() == this.getCiv()) {
					scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_sameCiv"));
					continue;
				}
				
				/* Only announce hostile, war, and neutral players */
				Relation.Status relation = this.getCiv().getDiplomacyManager().getRelationStatus(player);
				switch (relation) {
				case PEACE:
				case ALLY:
//				case VASSAL:
//				case MASTER:
					scoutDebug(CivSettings.localize.localizedString("acoutTower_debug_ally"));
					continue;
				default:
					break;
				}
				
				relationName = relation.name();
				relationColor = Relation.getRelationColor(relation);
			} else {
				relationName = CivSettings.localize.localizedString("scoutTower_isOutlaw");
				relationColor = CivColor.Yellow;
			}
			
			
			if (center.getWorld() != this.getCorner().getLocation().getWorld()) {
				scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_wrongWorld"));
				continue;
			}

			CultureChunk cc = CivGlobal.getCultureChunk(player.getLocation());
			if (cc == null) {
				continue;
			}
			double range1 = range;
			Resident res = CivGlobal.getResident(player);
			if (res.unit.equals("spy")) {
				range1 /= 5;
			}
			
			if (center.distance(player.getLocation()) < range1 &&
					!this.isDestroyed() &&
					this.isEnabled() &&
					this.isComplete() &&
					cc.getCiv() == this.getCiv()) {
				/* Notify the town or civ. */
				if (War.isWarTime()) {
					//players.put(player, this.getTown());
					CivMessage.sendScout(this.getCiv(), player, this.getTown());
				} else {
					CivMessage.sendScout(this.getCiv(), CivSettings.localize.localizedString("var_scoutTower_detection",
							(relationColor + player.getName() + "(" + relationName + ")" + CivColor.White),
							(player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ()),
							this.getTown().getName()));
				}
				alreadyAnnounced.add(this.getCiv().getName()+":"+player.getName());
			}
		}
		//CivMessage.sendScout(this.getCiv(), players, this.getTown());
		
		if (empty) {
			scoutDebug(CivSettings.localize.localizedString("scoutTower_debug_emptyCache"));
		}
	}
	
	@Override
	public String getMarkerIconName() {
		return "tower";
	}

	public int getReportSeconds() {
		return reportSeconds;
	}

	public void setReportSeconds(int reportSeconds) {
		this.reportSeconds = reportSeconds;
	}
}
