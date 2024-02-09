package com.dynast.civcraft.items.units;

import gpl.AttributeUtil;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigUnit;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.lorestorage.LoreMaterial;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.MissionLogger;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.BookUtil;

public class Spy extends UnitMaterial {
	
	public static final int BOOK_ID = 403;

	public ArrayList<UnitItemMaterial> missionBooks = new ArrayList<>();
	
	public Spy(String id, ConfigUnit configUnit) {
		super(id, configUnit);
	}

	public static void spawn(Inventory inv, Town town) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.SPY_UNIT);

		UnitMaterial.setOwningTown(town, is);		
		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException(CivSettings.localize.localizedString("var_settler_errorBarracksFull",Unit.SPY_UNIT.getUnit().name));
		}
	}
	
	public static void spawn(Inventory inv) throws CivException {
		ItemStack is = LoreMaterial.spawn(Unit.SPY_UNIT);

		if (!Unit.addItemNoStack(inv, is)) {
			throw new CivException(CivSettings.localize.localizedString("var_settler_errorBarracksFull",Unit.SPY_UNIT.getUnit().name));
		}
	}
	
	public void addMissionBook(UnitItemMaterial umat) {
		this.missionBooks.add(umat);
		this.allowedSubslots.add(umat.getSocketSlot());
	}
	
	public void giveMissionBooks(Player player) {
		
		Inventory inv = player.getInventory();
		
		for (UnitItemMaterial book : missionBooks) {
			
			
			ItemStack stack = inv.getItem(book.getSocketSlot());
			
			ItemStack is = LoreMaterial.spawn(book);
			AttributeUtil attrs = new AttributeUtil(is);
			attrs.setLore(book.getLore());
			is = attrs.getStack();
			
			if (!player.getInventory().contains(is)) {
				inv.setItem(book.getSocketSlot(), is);
			
				// Try to re-add any removed items.
				if (stack != null) {
					HashMap<Integer, ItemStack> leftovers = inv.addItem(stack);
					
					for (ItemStack s : leftovers.values()) {
						player.getWorld().dropItem(player.getLocation(), s);
					}
				}	
			}
		}
		
	}
	
	@Override
	public void onItemToPlayer(Player player, ItemStack stack) {
		//Add Books to inventory.
		giveMissionBooks(player);
	}
	
	
	@Override
	public void onItemFromPlayer(Player player, ItemStack stack) {
		removeChildren(player.getInventory());
		Resident res = CivGlobal.getResident(player);
		res.timedSpy = 30;
	}
	
	@Override
	public void onPlayerDeath(EntityDeathEvent event, ItemStack stack) {
		
		Player player = (Player)event.getEntity();
		Resident resident = CivGlobal.getResident(player);
		if (resident == null || !resident.hasTown()) {
			return;
		}
		
		
		ArrayList<String> bookout = MissionLogger.getMissionLogs(resident.getTown());
		
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta meta = (BookMeta) book.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		lore.add("Mission Report");
		meta.setAuthor("Mission Reports");
		meta.setTitle("Missions From"+" "+resident.getTown().getName());
		
		String out = "";
		for (String str : bookout) {
			out += str+"\n";
		}
		BookUtil.paginate(meta, out);			

		
		meta.setLore(lore);
		book.setItemMeta(meta);
		player.getWorld().dropItem(player.getLocation(), book);
		
	}
	
	
}
