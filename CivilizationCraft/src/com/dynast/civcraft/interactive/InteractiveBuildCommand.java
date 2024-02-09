
package com.dynast.civcraft.interactive;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Buildable;
import com.dynast.civcraft.structure.wonders.Wonder;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.threading.TaskMaster;

public class InteractiveBuildCommand implements InteractiveResponse {

	Town town;
	Buildable buildable;
	Location center;
	Template tpl;
	
	public InteractiveBuildCommand(Town town, Buildable buildable, Location center, Template tpl) {
		this.town = town;
		this.buildable = buildable;
		this.center = center.clone();
		this.tpl = tpl;
	}
	
	@Override
	public void respond(String message, Resident resident) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
		} catch (CivException e) {
			return;
		}
		
		if (!message.equalsIgnoreCase("yes")) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_cancel"));
			resident.clearInteractiveMode();
			resident.undoPreview();
			return;
		}
		
		
		if (!buildable.validated) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_invalid"));
			return;
		}
		
		if (!buildable.isValid() && !player.isOp()) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("interactive_build_invalidNotOP"));
			return;
		}
		
		class SyncTask implements Runnable {
			Resident resident;
			
			public SyncTask(Resident resident) {
				this.resident = resident;
			}
			
			@Override
			public void run() {
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
				} catch (CivException e) {
					return;
				}
				
				try {
					if (buildable instanceof Wonder) {
						town.buildWonder(player, buildable.getConfigId(), center, tpl);
					} else {
						town.buildStructure(player, buildable.getConfigId(), center, tpl);
					}
					resident.clearInteractiveMode();
				} catch (CivException e) {
					CivMessage.sendError(player, e.getMessage());
				}
			}
		}
		
		TaskMaster.syncTask(new SyncTask(resident));

	}

}
