package com.dynast.civcraft.command;

import java.io.IOException;
import java.text.DecimalFormat;

import com.dynast.civcraft.config.ConfigUnit;
import com.dynast.civcraft.structure.Barracks;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigBuildableInfo;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.structure.wonders.Wonder;
import com.dynast.civcraft.threading.tasks.BuildAsyncTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.war.War;

public class BuildCommand extends CommandBase {

	@Override
	public void init() {
		command = "/build";
		displayName = CivSettings.localize.localizedString("cmd_build_Desc");
		sendUnknownToDefault = true;
		
		commands.put("list", CivSettings.localize.localizedString("cmd_build_listDesc"));
		commands.put("progress", CivSettings.localize.localizedString("cmd_build_progressDesc"));
		commands.put("repairnearest", CivSettings.localize.localizedString("cmd_build_repairnearestDesc"));
		commands.put("undo", CivSettings.localize.localizedString("cmd_build_undoDesc"));
		commands.put("demolish", CivSettings.localize.localizedString("cmd_build_demolishDesc"));
		commands.put("demolishnearest", CivSettings.localize.localizedString("cmd_build_demolishnearestDesc"));
		commands.put("refreshnearest", CivSettings.localize.localizedString("cmd_build_refreshnearestDesc"));
		commands.put("validatenearest", CivSettings.localize.localizedString("cmd_build_validateNearestDesc"));
		//commands.put("preview", "shows a preview of this structure at this location.");
	}
	
	public void validatenearest_cmd() throws CivException {
		Player player = getPlayer();
		Resident resident = getResident();
		Buildable buildable = CivGlobal.getNearestBuildable(player.getLocation());
		
		if (buildable.getTown() != resident.getTown()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_validateNearestYourTownOnly"));
		}
		
		if (War.isWarTime()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_validatenearestNotDuringWar"));
		}
		
		if (buildable.isIgnoreFloating()) {
			throw new CivException(CivSettings.localize.localizedString("var_cmd_build_validateNearestExempt",buildable.getDisplayName()));
		}
		
		CivMessage.sendSuccess(player, CivSettings.localize.localizedString("var_cmd_build_validateNearestSuccess",buildable.getDisplayName(),buildable.getCenterLocation()));
		buildable.validate(player);
	}
	
	public void refreshnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Resident resident = getResident();
		town.refreshNearestBuildable(resident);
	}
	
	public void repairnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		if (War.isWarTime()) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_repairNotDuringWar"));
		}
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
			
		if (nearest == null) {
			throw new CivException (CivSettings.localize.localizedString("cmd_build_Invalid"));
		}
		
		if (!nearest.isDestroyed()) {
			throw new CivException (CivSettings.localize.localizedString("var_cmd_build_repairNotDestroyed",nearest.getDisplayName(),nearest.getCorner()));
		}
		
		if (!town.getCiv().hasTechnology(nearest.getRequiredTechnology())) {
			throw new CivException (CivSettings.localize.localizedString("var_cmd_build_repairMissingTech",nearest.getDisplayName(),nearest.getCorner()));
		}
	
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_cmd_build_repairConfirmPrompt",
					CivColor.Yellow+nearest.getDisplayName()+CivColor.LightGreen,CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen,CivColor.Yellow+nearest.getRepairCost()+CivColor.LightGreen,CivColor.Yellow+CivSettings.CURRENCY_NAME+CivColor.LightGreen));
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_repairConfirmPrompt2"));
			return;
		}
		
		town.repairStructure(nearest);		
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" "+CivSettings.localize.localizedString("Repaired"));
	}
	
	public void demolishnearest_cmd() throws CivException {
		Town town = getSelectedTown();
		Player player = getPlayer();
		
		Structure nearest = town.getNearestStrucutre(player.getLocation());
		
		if (nearest == null) {
			throw new CivException (CivSettings.localize.localizedString("cmd_build_Invalid"));
		}
		
		if (args.length < 2 || !args[1].equalsIgnoreCase("yes")) {
			CivMessage.send(player, CivColor.LightGreen+CivSettings.localize.localizedString("var_cmd_build_demolishNearestConfirmPrompt",CivColor.Yellow+nearest.getDisplayName()+CivColor.LightGreen,
					CivColor.Yellow+nearest.getCorner()+CivColor.LightGreen));
			CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_demolishNearestConfirmPrompt2"));
						
			nearest.flashStructureBlocks();
			return;
		}
		
		town.demolish(nearest, false);
		CivMessage.sendSuccess(player, nearest.getDisplayName()+" at "+nearest.getCorner()+" "+CivSettings.localize.localizedString("adcmd_build_demolishComplete"));
	}
	
	
	public void demolish_cmd() throws CivException {
		Town town = getSelectedTown();
		
		
		if (args.length < 2) {
			CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_demolishHeader"));
			for (Structure struct : town.getStructures()) {
				CivMessage.send(sender, CivSettings.localize.localizedString("var_cmd_build_demolish",struct.getDisplayName(),CivColor.Yellow+struct.getCorner().toString()+CivColor.White));
			}
			return;
		}
		
		try {
			BlockCoord coord = new BlockCoord(args[1]);
			Structure struct = town.getStructure(coord);
			if (struct == null) {
				CivMessage.send(sender, CivColor.Rose+" "+CivSettings.localize.localizedString("NoStructureAt")+" "+args[1]);
				return;
			}
			struct.getTown().demolish(struct, false);
			CivMessage.sendTown(struct.getTown(), struct.getDisplayName()+" "+CivSettings.localize.localizedString("adcmd_build_demolishComplete"));
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			CivMessage.sendError(sender, CivSettings.localize.localizedString("cmd_build_demolishFormatError"));
		}
	}
	
	public void undo_cmd() throws CivException {
		Town town = getSelectedTown();
		town.processUndo();
	}
	
	public void progress_cmd() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_undoHeader"));
		Town town = getSelectedTown();
		Barracks barracks = (Barracks)town.getStructureByType("s_barracks");
		DecimalFormat df = new DecimalFormat("#");
		if (barracks != null) {
			if (barracks.getTrainingUnit() != null) {
				ConfigUnit unit = barracks.getTrainingUnit();
				CivMessage.send(sender, CivColor.LightPurple+unit.name+": "+
						CivColor.Yellow+df.format(barracks.getPercentageProgress()*100)+
						"% ("+df.format(barracks.getCurrentHammers())+"/"+barracks.getHammerCostUnit()+")");
			}
		}

		for (BuildAsyncTask task : town.build_tasks) {
			Buildable b = task.buildable;
			double total = b.getHammerCost();
			double current = b.getBuiltHammers();
			double builtPercentage = current/total;
			builtPercentage = Math.round(builtPercentage *100);
			
			CivMessage.send(sender, CivColor.LightPurple+b.getDisplayName()+": "+CivColor.Yellow+builtPercentage+"% ("+df.format(current) + "/"+total+")"+
					CivColor.LightPurple+" Блоков "+CivColor.Yellow+"("+b.builtBlockCount+"/"+b.getTotalBlockCount()+")");
			
			//CivMessage.send(sender, CivColor.LightPurple+b.getDisplayName()+" "+CivColor.Yellow+"("+
				//	b.builtBlockCount+" / "+b.getTotalBlockCount()+")");
		}
		
	}

	public void list_available_structures() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_listHeader"));
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.structures.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = CivSettings.localize.localizedString("Unlimited");
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				if ((sinfo.id.equals("ti_trade_outpost") || sinfo.id.equals("ti_fishing_boat")) && town.getCiv().hasInstitution("commerce_2")) {
					CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+" "+
							CivColor.Yellow+
							CivSettings.localize.localizedString("Cost")+" "+sinfo.cost/2+" "+						
							CivSettings.localize.localizedString("Upkeep")+" "+sinfo.upkeep+" "+CivSettings.localize.localizedString("Hammers")+" "+sinfo.hammer_cost+" "+ 
							CivSettings.localize.localizedString("Remaining")+" "+leftString);
				} else {
					CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+" "+
							CivColor.Yellow+
							CivSettings.localize.localizedString("Cost")+" "+sinfo.cost+" "+
							CivSettings.localize.localizedString("Upkeep")+" "+sinfo.upkeep+" "+CivSettings.localize.localizedString("Hammers")+" "+sinfo.hammer_cost+" "+ 
							CivSettings.localize.localizedString("Remaining")+" "+leftString);
				}
			}
		}
	}
	
	public void list_available_wonders() throws CivException {
		CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_build_listWondersHeader"));
		Town town = getSelectedTown();
		for (ConfigBuildableInfo sinfo : CivSettings.wonders.values()) {
			if (sinfo.isAvailable(town)) {
				String leftString = "";
				if (sinfo.limit == 0) {
					leftString = CivSettings.localize.localizedString("Unlimited");
				} else {
					leftString = ""+(sinfo.limit - town.getStructureTypeCount(sinfo.id));
				}
				
				if (Wonder.isWonderAvailable(sinfo.id)) {				
					CivMessage.send(sender, CivColor.LightPurple+sinfo.displayName+" "+
							CivColor.Yellow+
							CivSettings.localize.localizedString("Cost")+" "+sinfo.cost+" "+
							CivSettings.localize.localizedString("Upkeep")+" "+sinfo.upkeep+" "+CivSettings.localize.localizedString("Hammers")+" "+sinfo.hammer_cost+" "+
							CivSettings.localize.localizedString("Remaining")+" "+leftString);
				} else {
					Wonder wonder = CivGlobal.getWonderByConfigId(sinfo.id);
					CivMessage.send(sender, CivColor.LightGray+sinfo.displayName+" Cost: "+sinfo.cost+" - "+CivSettings.localize.localizedString("var_cmd_build_listWonderAlreadyBuild",wonder.getTown().getName(),wonder.getTown().getCiv().getName()));
				}
			}
		}
	}
	
	public void list_cmd() throws CivException {
		this.list_available_structures();
		this.list_available_wonders();
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		if (args.length == 0) {		
			showHelp();
			return;
		}
		
		String fullArgs = "";
		for (String arg : args) {
			fullArgs += arg + " ";
		}
		fullArgs = fullArgs.trim();
		
		buildByName(fullArgs);
	}

	public void preview_cmd() throws CivException {
		String fullArgs = this.combineArgs(this.stripArgs(args, 1));
		
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);
		if (sinfo == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_defaultUnknownStruct")+" "+fullArgs);
		}
		
		Town town = getSelectedTown();
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		} else {
		Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		}
		CivMessage.sendSuccess(sender, CivSettings.localize.localizedString("cmd_build_showPreviewSuccess"));
	}
	
	
	private void buildByName(String fullArgs) throws CivException {
		ConfigBuildableInfo sinfo = CivSettings.getBuildableInfoByName(fullArgs);

		if (sinfo == null) {
			throw new CivException(CivSettings.localize.localizedString("cmd_build_defaultUnknownStruct")+" "+fullArgs);
		}
		
		Town town = getSelectedTown();
		
		if (sinfo.isWonder) {
			Wonder wonder = Wonder.newWonder(getPlayer().getLocation(), sinfo.id, town);
			try {
				wonder.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		} else {
			Structure struct = Structure.newStructure(getPlayer().getLocation(), sinfo.id, town);
			try {
				struct.buildPlayerPreview(getPlayer(), getPlayer().getLocation());
			} catch (IOException e) {
				e.printStackTrace();
				throw new CivException(CivSettings.localize.localizedString("internalIOException"));
			}
		}
		
//		if (sinfo.isWonder) {
//			town.buildWonder(getPlayer(), sinfo.id, getPlayer().getLocation());
//		} else {
//			town.buildStructure(getPlayer(), sinfo.id, getPlayer().getLocation());
//		}
//		CivMessage.sendSuccess(sender, "Started building "+sinfo.displayName);
	}

	@Override
	public void showHelp() {
		showBasicHelp();		
		CivMessage.send(sender, CivColor.LightPurple+command+" "+CivColor.Yellow+CivSettings.localize.localizedString("cmd_build_help1")+" "+
				CivColor.LightGray+CivSettings.localize.localizedString("cmd_build_help2"));
	}

	@Override
	public void permissionCheck() throws CivException {
		validMayorAssistantLeader();
	}

}
