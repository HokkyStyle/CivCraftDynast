
package com.dynast.civcraft.threading.tasks;


import com.dynast.civcraft.main.CivNPC;
import com.dynast.civcraft.structure.*;
import com.dynast.civcraft.threading.TaskMaster;
import javafx.concurrent.Task;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.material.MaterialData;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.StructureChest;
import com.dynast.civcraft.object.StructureSign;
import com.dynast.civcraft.structure.wonders.GrandShipIngermanland;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ItemManager;
import com.dynast.civcraft.util.SimpleBlock;

public class PostBuildSyncTask implements Runnable {

	/*
	 * Search the template for special command blocks and handle them *after* the structure 
	 * has finished building.
	 */
	
	Template tpl;
	Buildable buildable;
	
	public PostBuildSyncTask(Template tpl, Buildable buildable) {
		this.tpl = tpl;
		this.buildable = buildable;
	}
	
	public static void validate(Template tpl, Buildable buildable) {
		
		/*
		 * Use the location's of the command blocks in the template and the buildable's corner 
		 * to find their real positions. Then perform any special building we may want to do
		 * at those locations.
		 */
		/* These block coords do not point to a location in the world, just a location in the template. */
		for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			StructureSign structSign;
			Block block;
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			Location loc = relativeCoord.getLocation();
			//Location loc = absCoord.getCenteredLocation().setDirection(relativeCoord.get);
			/* Signs and chests should already be handled, look for more exotic things. */
			switch (sb.command) {
			case "/tradeoutpost":
				/* Builds the trade outpost tower at this location. */
				if (buildable instanceof TradeOutpost) {
					TradeOutpost outpost = (TradeOutpost)buildable;
					outpost.setTradeOutpostTower(absCoord);
					try {
						outpost.build_trade_outpost_tower();
					} catch (CivException e) {
						e.printStackTrace();
					}
					
				}
				break;
			case "/techbar":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					int index = Integer.valueOf(sb.keyvalues.get("id"));
					townhall.addTechBarBlock(absCoord, index);
					
				}*/
				break;
			case "/techname":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setTechnameSign(absCoord);
					townhall.setTechnameSignData((byte)sb.getData());
					
				}*/
				break;
			case "/techdata":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setTechdataSign(absCoord);
					townhall.setTechdataSignData((byte)sb.getData());
					
				}*/
				break;
			case "/itemframe":
				String strvalue = sb.keyvalues.get("id");
				if (strvalue != null) {
					int index = Integer.valueOf(strvalue);
					
					if (buildable instanceof TownHall) {
						TownHall townhall = (TownHall)buildable;
						townhall.createGoodieItemFrame(absCoord, index, sb.getData());
						townhall.addStructureBlock(absCoord, false);
					} 
				}
				break;
			case "/respawn":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setRespawnPoint(absCoord);
				}
				break;
			case "/revive":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setRevivePoint(absCoord);
				}
				break;
			case "/inwarroom":
				if (buildable instanceof Capitol) {
					Capitol capitol = (Capitol)buildable;

					capitol.setLocInWarroom(absCoord);
				}
				break;
			case "/outwarroom":
				if (buildable instanceof  Capitol) {
					Capitol capitol = (Capitol)buildable;

					capitol.setLocOutWarroom(absCoord);
				}
				break;
			case "/towerfire":
				if (buildable instanceof ArrowShip) {
					ArrowShip arrowship = (ArrowShip)buildable;
					arrowship.setTurretLocation(absCoord);
				} else if (buildable instanceof ArrowTower) {
					ArrowTower arrowtower = (ArrowTower)buildable;
					arrowtower.setTurretLocation(absCoord);
				} else if (buildable instanceof CannonShip) {
					CannonShip cannonship = (CannonShip)buildable;
					cannonship.setTurretLocation(absCoord);
				} else if (buildable instanceof CannonTower) {
					CannonTower cannontower = (CannonTower)buildable;
					cannontower.setTurretLocation(absCoord);
				} else if (buildable instanceof TeslaTower) {
					TeslaTower teslaTower = (TeslaTower)buildable;
					teslaTower.setTurretLocation(absCoord);
				}
				
				break;
			case "/arrowfire":
				if (buildable instanceof GrandShipIngermanland) {
					GrandShipIngermanland arrowtower = (GrandShipIngermanland)buildable;
					arrowtower.setArrowLocation(absCoord);
				}
				break;
			case "/cannonfire":
				if (buildable instanceof GrandShipIngermanland) {
					GrandShipIngermanland cannontower = (GrandShipIngermanland)buildable;
					cannontower.setCannonLocation(absCoord);
				}	
				break;
			case "/banker":
				if (buildable instanceof  Bank) {
					TaskMaster.syncTask(new CivNPC("Банкир", loc, (Bank)buildable));
				}
				break;
			case "/bakkal":
//				if (buildable instanceof Grocer) {
//					TaskMaster.syncTask(new CivNPC("Бакалейщик", loc, (Grocer) buildable));
//				}
				break;
			case "/master":
				if (buildable instanceof TownHall) {
					TaskMaster.syncTask(new CivNPC("Мастер телепортации", loc, (TownHall) buildable));
				}
				break;
			case "/mercenary":
				break;
			case "/sign":
				structSign = CivGlobal.getStructureSign(absCoord);
				if (structSign == null) {
					structSign = new StructureSign(absCoord, buildable);
				}
				block = absCoord.getBlock();
				ItemManager.setTypeId(block, sb.getType());
				ItemManager.setData(block, sb.getData());
				
				structSign.setDirection(ItemManager.getData(block.getState()));
				for (String key : sb.keyvalues.keySet()) {
					structSign.setType(key);
					structSign.setAction(sb.keyvalues.get(key));
					break;
				}
				
				structSign.setOwner(buildable);
				buildable.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);
				
				break;
			case "/chest":
				StructureChest structChest = CivGlobal.getStructureChest(absCoord);
				if (structChest == null) {
					structChest = new StructureChest(absCoord, buildable);
				}
				structChest.setChestId(Integer.valueOf(sb.keyvalues.get("id")));
				buildable.addStructureChest(structChest);
				CivGlobal.addStructureChest(structChest);
				
				/* Convert sign data to chest data.*/
				block = absCoord.getBlock();
				if (ItemManager.getId(block) != CivData.CHEST) {		
					byte chestData = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setTypeId(block, CivData.CHEST);
					ItemManager.setData(block, chestData, true);}
				
					Chest chest = (Chest)block.getState();
					MaterialData data = chest.getData();
//					ItemManager.setData(data, chestData);
					chest.setData(data);
					chest.update();
//				}
				
				break;
			}
			
			buildable.onPostBuild(absCoord, sb);
		}
		
	//	if (buildable instanceof Structure) {
		buildable.updateSignText();
	//}
	}
	
	public static void start(Template tpl, Buildable buildable) {
		for (BlockCoord relativeCoord : tpl.doorRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			Block block = absCoord.getBlock();
			if (ItemManager.getId(block) != sb.getType()) {
				if (buildable.getCiv().isAdminCiv()) {
					ItemManager.setTypeIdAndData(block, CivData.AIR, (byte)0, false);
				} else {
					ItemManager.setTypeIdAndData(block, sb.getType(), (byte)sb.getData(), false);
				}
			}
		}
			
		for (BlockCoord relativeCoord : tpl.attachableLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			Block block = absCoord.getBlock();
			if (ItemManager.getId(block) != sb.getType()) {
					ItemManager.setTypeIdAndData(block, sb.getType(), (byte)sb.getData(), false);
			}
		}
		
		/*
		 * Use the location's of the command blocks in the template and the buildable's corner 
		 * to find their real positions. Then perform any special building we may want to do
		 * at those locations.
		 */
		/* These block coords do not point to a location in the world, just a location in the template. */
		for (BlockCoord relativeCoord : tpl.commandBlockRelativeLocations) {
			SimpleBlock sb = tpl.blocks[relativeCoord.getX()][relativeCoord.getY()][relativeCoord.getZ()];
			StructureSign structSign;
			Block block;
			BlockCoord absCoord = new BlockCoord(buildable.getCorner().getBlock().getRelative(relativeCoord.getX(), relativeCoord.getY(), relativeCoord.getZ()));

			Location loc = absCoord.getCenteredLocation();
			
			/* Signs and chests should already be handled, look for more exotic things. */
			switch (sb.command) {
			case "/tradeoutpost":
				/* Builds the trade outpost tower at this location. */
				if (buildable instanceof TradeOutpost) {
					TradeOutpost outpost = (TradeOutpost)buildable;
					outpost.setTradeOutpostTower(absCoord);
					try {
						outpost.build_trade_outpost_tower();
					} catch (CivException e) {
						e.printStackTrace();
					}
					
				}
				break;
			
			case "/techbar":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					int index = Integer.valueOf(sb.keyvalues.get("id"));
					townhall.addTechBarBlock(absCoord, index);
					
				}*/
				break;
			case "/techname":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setTechnameSign(absCoord);
					townhall.setTechnameSignData((byte)sb.getData());
					
				}*/
				break;
			case "/techdata":
				/*if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setTechdataSign(absCoord);
					townhall.setTechdataSignData((byte)sb.getData());
					
				}*/
				break;
			case "/itemframe":
				String strvalue = sb.keyvalues.get("id");
				if (strvalue != null) {
					int index = Integer.valueOf(strvalue);
					
					if (buildable instanceof TownHall) {
						TownHall townhall = (TownHall)buildable;
						townhall.createGoodieItemFrame(absCoord, index, sb.getData());
						townhall.addStructureBlock(absCoord, false);
					} 
				}
				break;
			case "/respawn":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setRespawnPoint(absCoord);
				}
				break;
			case "/revive":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					
					townhall.setRevivePoint(absCoord);
				}
				break;
			case "/inwarroom":
				if (buildable instanceof Capitol) {
					Capitol capitol = (Capitol)buildable;

					capitol.setLocInWarroom(absCoord);
				}
				break;
			case "/outwarroom":
				if (buildable instanceof  Capitol) {
					Capitol capitol = (Capitol)buildable;

					capitol.setLocOutWarroom(absCoord);
				}
				break;
			case "/control":
				if (buildable instanceof TownHall) {
					TownHall townhall = (TownHall)buildable;
					townhall.createControlPoint(absCoord);
				}
				break;
			case "/towerfire":
				if (buildable instanceof ArrowShip) {
					ArrowShip arrowship = (ArrowShip)buildable;
					arrowship.setTurretLocation(absCoord);
				} else if (buildable instanceof ArrowTower) {
					ArrowTower arrowtower = (ArrowTower)buildable;
					arrowtower.setTurretLocation(absCoord);
				} else if (buildable instanceof CannonShip) {
					CannonShip cannonship = (CannonShip)buildable;
					cannonship.setTurretLocation(absCoord);
				} else if (buildable instanceof CannonTower) {
					CannonTower cannontower = (CannonTower)buildable;
					cannontower.setTurretLocation(absCoord);
				} else if (buildable instanceof TeslaTower) {
					TeslaTower teslaTower = (TeslaTower)buildable;
					teslaTower.setTurretLocation(absCoord);
				}
				break;
			case "/arrowfire":
				if (buildable instanceof GrandShipIngermanland) {
					GrandShipIngermanland arrowtower = (GrandShipIngermanland)buildable;
					arrowtower.setArrowLocation(absCoord);
				}
				break;
			case "/cannonfire":
				if (buildable instanceof GrandShipIngermanland) {
					GrandShipIngermanland cannontower = (GrandShipIngermanland)buildable;
					cannontower.setCannonLocation(absCoord);
				}	
				break;
			case "/banker":
				if (buildable instanceof  Bank) {
					TaskMaster.syncTask(new CivNPC("Банкир", loc, (Bank)buildable));
				}
				break;
			case "/bakkal":
				/*if (buildable instanceof Grocer) {
					TaskMaster.syncTask(new CivNPC("Бакалейщик", loc, (Grocer)buildable));
				}*/
				break;
			case "/master":
				if (buildable instanceof TownHall) {
					TaskMaster.syncTask(new CivNPC("Мастер телепортации", loc, (TownHall) buildable));
				}
				break;
			case "/mercenary":
				break;
			case "/sign":
				structSign = CivGlobal.getStructureSign(absCoord);
				if (structSign == null) {
					structSign = new StructureSign(absCoord, buildable);
				}
				block = absCoord.getBlock();
				ItemManager.setTypeId(block, sb.getType());
				ItemManager.setData(block, sb.getData());
				
				structSign.setDirection(ItemManager.getData(block.getState()));
				for (String key : sb.keyvalues.keySet()) {
					structSign.setType(key);
					structSign.setAction(sb.keyvalues.get(key));
					break;
				}
				
				structSign.setOwner(buildable);
				buildable.addStructureSign(structSign);
				CivGlobal.addStructureSign(structSign);
				
				break;
			case "/chest":
				StructureChest structChest = CivGlobal.getStructureChest(absCoord);
				if (structChest == null) {
					structChest = new StructureChest(absCoord, buildable);
				}
				structChest.setChestId(Integer.valueOf(sb.keyvalues.get("id")));
				buildable.addStructureChest(structChest);
				CivGlobal.addStructureChest(structChest);
				
				/* Convert sign data to chest data.*/
				block = absCoord.getBlock();
				if (ItemManager.getId(block) != CivData.CHEST) {		
					byte chestData = CivData.convertSignDataToChestData((byte)sb.getData());
					ItemManager.setTypeId(block, CivData.CHEST);
					ItemManager.setData(block, chestData, true); }
				
					Chest chest = (Chest)block.getState();
					MaterialData data = chest.getData();
//					ItemManager.setData(data, chestData);
					chest.setData(data);
					chest.update();
//				}
				break;
			}
			
			buildable.onPostBuild(absCoord, sb);
		}
		/* Run the tech bar task now in order to protect the blocks */
		if (buildable instanceof TownHall) {
			UpdateTechBar techbartask = new UpdateTechBar(buildable.getCiv());
			techbartask.run();
		}
		
	//	if (buildable instanceof Structure) {
		buildable.updateSignText();
	//}
	}
	
	@Override
	public void run() {
		PostBuildSyncTask.start(tpl, buildable);
	}
	
}
