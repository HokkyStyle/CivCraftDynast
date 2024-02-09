package com.dynast.civcraft.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.CivColor;
import com.dthielke.herochat.ChannelChatEvent;
import com.dthielke.herochat.Chatter;
import com.dthielke.herochat.Chatter.Result;
import com.dthielke.herochat.Herochat;

public class HeroChatListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChannelChatEvent(ChannelChatEvent event) {
		Resident resident = CivGlobal.getResident(event.getSender().getName());
		if (resident == null) {
			event.setResult(Result.FAIL);
			return;
		}
		
		if (!resident.isInteractiveMode()) {
			if (resident.isMuted()) {
				event.setResult(Result.MUTED);
				return;
			}
		}
		
		if (event.getChannel().getDistance() > 0) {
			for (String name : Resident.allchatters) {
				Player player;
				try {
					player = CivGlobal.getPlayer(name);
				} catch (CivException e) {
					continue;
				}
				
				Chatter you = Herochat.getChatterManager().getChatter(player);
				if (!event.getSender().isInRange(you, event.getChannel().getDistance())) {
					player.sendMessage(CivColor.White+event.getSender().getName()+CivSettings.localize.localizedString("hc_prefix_far")+" "+event.getMessage());
				}
			}
		}
	}
}
