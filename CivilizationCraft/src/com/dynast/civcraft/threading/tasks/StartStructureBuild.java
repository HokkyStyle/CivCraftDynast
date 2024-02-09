
package com.dynast.civcraft.threading.tasks;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.template.Template;

public class StartStructureBuild implements Runnable {

	public String playerName;
	public Structure struct;
	public Template tpl;
	public Location centerLoc;
	
	@Override
	public void run() {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e1) {
			e1.printStackTrace();
			return;
		}

		try {
			struct.doBuild(player, centerLoc, tpl);
			struct.save();
		} catch (CivException e) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("internalCommandException")+" "+e.getMessage());
		} catch (IOException e) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("internalIOException"));
			e.printStackTrace();
		} catch (SQLException e) {
			CivMessage.sendError(player, CivSettings.localize.localizedString("internalDatabaseException"));
			e.printStackTrace();
		}
	}

}
