
package com.dynast.civcraft.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.CultureChunk;
import com.dynast.civcraft.object.TownChunk;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.CivColor;

public class HereCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			ChunkCoord coord = new ChunkCoord(player.getLocation());
			
			CultureChunk cc = CivGlobal.getCultureChunk(coord);
			if (cc != null) {
				CivMessage.send(sender, CivColor.LightPurple+CivSettings.localize.localizedString("var_cmd_here_inCivAndTown",
						CivColor.Yellow+cc.getCiv().getName()+CivColor.LightPurple,CivColor.Yellow+cc.getTown().getName()));
			}
			
			TownChunk tc = CivGlobal.getTownChunk(coord);
			if (tc != null) {
				CivMessage.send(sender, CivColor.Green+CivSettings.localize.localizedString("var_cmd_here_inTown",CivColor.LightGreen+tc.getTown().getName()));
				if (tc.isOutpost()) {
					CivMessage.send(sender, CivColor.Yellow+CivSettings.localize.localizedString("cmd_here_outPost"));
				}
			}
			
			if (cc == null && tc == null) {
				CivMessage.send(sender, CivColor.Yellow+CivSettings.localize.localizedString("cmd_here_wilderness"));
			}
			
		}
		
		
		return false;
	}

}
