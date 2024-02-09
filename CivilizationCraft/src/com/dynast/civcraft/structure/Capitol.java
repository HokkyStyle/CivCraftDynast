package com.dynast.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import com.dynast.civcraft.components.ProjectileArrowComponent;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.ControlPoint;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.StructureBlock;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.SimpleBlock;
import com.dynast.civcraft.war.War;

public class Capitol extends TownHall {
	
	private HashMap<Integer, ProjectileArrowComponent> arrowTowers = new HashMap<>();
	private StructureSign respawnSign;
	private int index = 0;
	private BlockCoord inWarroom = new BlockCoord();
	private BlockCoord outWarroom = new BlockCoord();

	public Capitol(ResultSet rs) throws SQLException, CivException {
		super(rs);
	}
	

	protected Capitol(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
	}
	
	public String getMarkerIconName() {
		return "king";
	}
	
	private RespawnLocationHolder getSelectedHolder() {
		ArrayList<RespawnLocationHolder> respawnables =  this.getTown().getCiv().getAvailableRespawnables();	
		return respawnables.get(index);
	}

	private void changeIndex(int newIndex) {
		ArrayList<RespawnLocationHolder> respawnables =  this.getTown().getCiv().getAvailableRespawnables();
			
		if (this.respawnSign != null) {
			try {
				this.respawnSign.setText(CivSettings.localize.localizedString("capitol_sign_respawnAt")+"\n"+CivColor.Green+CivColor.BOLD+respawnables.get(newIndex).getRespawnName());
				index = newIndex;
			} catch (IndexOutOfBoundsException e) {
				if (respawnables.size() > 0) {
					this.respawnSign.setText(CivSettings.localize.localizedString("capitol_sign_respawnAt")+"\n"+CivColor.Green+CivColor.BOLD+respawnables.get(0).getRespawnName());
					index = 0;
				}
				//this.unitNameSign.setText(getUnitSignText(index));
			}
			this.respawnSign.update();
		} else {
			CivLog.warning("Could not find civ spawn sign:"+this.getId()+" at "+this.getCorner());
		}
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		//int special_id = Integer.valueOf(sign.getAction());
		Resident resident = CivGlobal.getResident(player);
		
		if (resident == null) {
			return;
		}

		Boolean hasPermission = false;
		if (this.getTown().getAssistantGroup().hasMember(resident) ||
				this.getTown().getMayorGroup().hasMember(resident) ||
				this.getCiv().getLeaderGroup().hasMember(resident) ||
				this.getCiv().getAdviserGroup().hasMember(resident)){
			hasPermission = true;
		}

		if (!War.isWarTime()) {
			switch (sign.getAction()) {
				case "inwarroom":
					if (hasPermission) {
						Location loc = outWarroom.getLocation();
						player.teleport(loc);
					} else {
						CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_Sign_warRoom_noPermission"));
					}
					break;
				case "outwarroom":
					Location loc = inWarroom.getLocation();
					player.teleport(loc);
			}
			return;
		}

		switch (sign.getAction()) {
		case "prev":
			//todo comment it
			if(hasPermission){
				changeIndex((index-1));
			} else {
				CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_Sign_noPermission"));
			}
			break;
		case "next":
			//todo comment it
			if(hasPermission){
				changeIndex((index+1));
			} else {
				CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_Sign_noPermission"));
			}
			break;
		case "respawn":

			ArrayList<RespawnLocationHolder> respawnables =  this.getTown().getCiv().getAvailableRespawnables();
			if (index >= respawnables.size()) {
				index = 0;
				changeIndex(index);
				CivMessage.sendError(resident, CivSettings.localize.localizedString("capitol_cannotRespawn"));
				return;
			}
			
			RespawnLocationHolder holder = getSelectedHolder();
			int respawnTimeSeconds = this.getRespawnTime();
			Date now = new Date();
			
			if (resident.getLastKilledTime() != null) {
				long secondsLeft = (resident.getLastKilledTime().getTime() + (respawnTimeSeconds*1000)) - now.getTime();
				if (secondsLeft > 0) {
					secondsLeft /= 1000; 
					CivMessage.sendError(resident, CivColor.Rose+CivSettings.localize.localizedString("var_capitol_secondsLeftTillRespawn",secondsLeft));
					return;
				}
			}
			
			BlockCoord revive = holder.getRandomRevivePoint();
			Location loc;
			if (revive == null) {
				loc = player.getBedSpawnLocation();
			} else {
				loc = revive.getLocation();
			}
			
			CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("capitol_respawningAlert"));
			player.teleport(loc);		
			break;
		}
	}

	public void setLocInWarroom(BlockCoord absCoord) {
		this.inWarroom = absCoord;
	}

	public void setLocOutWarroom(BlockCoord absCoord) {
		this.outWarroom = absCoord;
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock commandBlock) {
		StructureSign structSign;

		switch (commandBlock.command) {
			case "/towerfire":
				String id = commandBlock.keyvalues.get("id");
				Integer towerID = Integer.valueOf(id);

				if (!arrowTowers.containsKey(towerID)) {

					ProjectileArrowComponent arrowTower = new ProjectileArrowComponent(this, absCoord.getLocation());
					arrowTower.createComponent(this);
					arrowTower.setTurretLocation(absCoord);

					arrowTowers.put(towerID, arrowTower);
				}
				break;
			case "/next":
				//todo comment it
				ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
				ItemManager.setData(absCoord.getBlock(), commandBlock.getData());

				structSign = new StructureSign(absCoord, this);
				structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("capitol_sign_nextLocation"));
				structSign.setDirection(commandBlock.getData());
				structSign.setAction("next");
				structSign.update();
				this.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);

				break;
			case "/prev":
				//todo comment it
				ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
				ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
				structSign = new StructureSign(absCoord, this);
				structSign.setText("\n" + ChatColor.BOLD + ChatColor.UNDERLINE + CivSettings.localize.localizedString("capitol_sign_previousLocation"));
				structSign.setDirection(commandBlock.getData());
				structSign.setAction("prev");
				structSign.update();
				this.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);

				break;
			case "/respawndata":
				ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
				ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
				structSign = new StructureSign(absCoord, this);
				structSign.setText(CivSettings.localize.localizedString("capitol_sign_Capitol"));
				structSign.setDirection(commandBlock.getData());
				structSign.setAction("respawn");
				structSign.update();
				this.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);

				this.respawnSign = structSign;
				changeIndex(index);

				break;
			case "/inwarroom":
				ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
				ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
				structSign = new StructureSign(absCoord, this);
				structSign.setText(CivSettings.localize.localizedString("capitol_sign_warroom_teleport"));
				structSign.setDirection(commandBlock.getData());
				structSign.setAction("inwarroom");
				structSign.update();
				this.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);

				break;
			case "/outwarroom":
				ItemManager.setTypeId(absCoord.getBlock(), commandBlock.getType());
				ItemManager.setData(absCoord.getBlock(), commandBlock.getData());
				structSign = new StructureSign(absCoord, this);
				structSign.setText(CivSettings.localize.localizedString("capitol_sign_outwarroom_teleport"));
				structSign.setDirection(commandBlock.getData());
				structSign.setAction("outwarroom");
				structSign.update();
				this.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);

				break;
		}
	}
	
	@Override
	public void createControlPoint(BlockCoord absCoord) {
		
		Location centerLoc = absCoord.getLocation();
		
		/* Build the bedrock tower. */
		//for (int i = 0; i < 1; i++) {
		Block b = centerLoc.getBlock();
		ItemManager.setTypeId(b, ItemManager.getId(Material.SANDSTONE)); ItemManager.setData(b, 0);
		
		StructureBlock sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		//}
		
		/* Build the control block. */
		b = centerLoc.getBlock().getRelative(0, 1, 0);
		ItemManager.setTypeId(b, CivData.OBSIDIAN);
		sb = new StructureBlock(new BlockCoord(b), this);
		this.addStructureBlock(sb.getCoord(), true);
		
		int capitolControlHitpoints;
		try {
			capitolControlHitpoints = CivSettings.getInteger(CivSettings.warConfig, "war.control_block_hitpoints_capitol");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			capitolControlHitpoints = 100;
		}
		
		BlockCoord coord = new BlockCoord(b);
		this.controlPoints.put(coord, new ControlPoint(coord, this, capitolControlHitpoints));
	}
	
	@Override
	public void onInvalidPunish() {
		int invalid_respawn_penalty;
		try {
			invalid_respawn_penalty = CivSettings.getInteger(CivSettings.warConfig, "war.invalid_respawn_penalty");
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		
		CivMessage.sendTown(this.getTown(), CivColor.Rose+CivColor.BOLD+CivSettings.localize.localizedString("capitol_cannotSupport1")+
				" "+CivSettings.localize.localizedString("var_capitol_cannotSupport2",invalid_respawn_penalty));
	}
	
	@Override
	public boolean isValid() {
		if (this.getCiv().isAdminCiv()) {
			return true;
		}
		
		/* 
		 * Validate that all of the towns in our civ have town halls. If not, then 
		 * we need to punish by increasing respawn times.
		 */
		for (Town town : this.getCiv().getTowns()) {
			TownHall townhall = town.getTownHall();
			if (townhall == null) {
				return false;
			}
		}
		
		return super.isValid();
	}
	
	@Override
	public String getRespawnName() {
		return "Капитолий\n"+this.getTown().getName();
	}
}
