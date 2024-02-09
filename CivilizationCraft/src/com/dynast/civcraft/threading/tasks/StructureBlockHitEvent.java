
package com.dynast.civcraft.threading.tasks;

import com.comphenix.protocol.ProtocolLib;
import com.connorlinfoot.titleapi.TitleAPI;
import gpl.AttributeUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.chat.BaseComponentSerializer;
import net.minecraft.server.v1_11_R1.Material;

import org.bukkit.World;
import org.bukkit.entity.Player;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.BuildableDamageBlock;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;

public class StructureBlockHitEvent implements Runnable {

	/*
	 * Called when a structure block is hit, this async task quickly determines
	 * if the block hit should take damage during war.
	 * 
	 */
	String playerName;
	BlockCoord coord;
	BuildableDamageBlock dmgBlock;
	World world;
	
	public StructureBlockHitEvent(String player, BlockCoord coord, BuildableDamageBlock dmgBlock, World world) {
		this.playerName = player;
		this.coord = coord;
		this.dmgBlock = dmgBlock;
		this.world = world;
	}
	
	@Override
	public void run() {
		
		if (playerName == null) {
			return;
		}
		Player player;
		Resident resident;
		try {
			player = CivGlobal.getPlayer(playerName);
			resident = CivGlobal.getResident(playerName);
		} catch (CivException e) {
			//Player offline now?
			return;
		}
		if (dmgBlock.allowDamageNow(player)) {
			/* Do our damage. */
			int damage = 1;
				
			LoreMaterial material = LoreMaterial.getMaterial(player.getInventory().getItemInMainHand());
			if (material != null) {
				damage = material.onStructureBlockBreak(dmgBlock, damage);
			}
			
			if (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
				AttributeUtil attrs = new AttributeUtil(player.getInventory().getItemInMainHand());
				for (LoreEnhancement enhance : attrs.getEnhancements()) {
					damage = enhance.onStructureBlockBreak(dmgBlock, damage);
				}
			}
			
			if (resident.getCiv().hasInstitution("chest_5")) {
				damage += 1;
			}

			/*if (damage > 1) {
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(CivColor.LightGray+CivSettings.localize.localizedString("var_StructureBlockHitEvent_punchoutDmg",(damage-1))).create());
				CivMessage.send(player, CivColor.LightGray+CivSettings.localize.localizedString("var_StructureBlockHitEvent_punchoutDmg",(damage-1)));
			}*/

			dmgBlock.getOwner().onDamage(damage, world, player, dmgBlock.getCoord(), dmgBlock);
		} else {
			CivMessage.sendErrorNoRepeat(player, 
					CivSettings.localize.localizedString("var_StructureBlockHitEvent_Invulnerable",dmgBlock.getOwner().getDisplayName()));
		}
	}
}
