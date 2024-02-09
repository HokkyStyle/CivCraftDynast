
package com.dynast.civcraft.main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.dynast.civcraft.war.War;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.dynast.civcraft.arena.Arena;
import com.dynast.civcraft.arena.ArenaTeam;
import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.util.Reflection;
import com.connorlinfoot.titleapi.TitleAPI;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class CivMessage {

	/* Stores the player name and the hash code of the last message sent to prevent error spamming the player. */
	private static HashMap<String, Integer> lastMessageHashCode = new HashMap<>();
	
	/* Indexed off of town names, contains a list of extra people who listen to town chats.(mostly for admins to listen to towns) */
	private static Map<String, ArrayList<String>> extraTownChatListeners = new ConcurrentHashMap<>();
	
	/* Indexed off of civ names, contains a list of extra people who listen to civ chats. (mostly for admins to list to civs) */
	private static Map<String, ArrayList<String>> extraCivChatListeners = new ConcurrentHashMap<>();
	
	public static void sendErrorNoRepeat(Object sender, String line) {
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			Integer hashcode = lastMessageHashCode.get(player.getName());
			if (hashcode != null && hashcode == line.hashCode()) {
				return;
			}
			
			lastMessageHashCode.put(player.getName(), line.hashCode());
		}
		
		send(sender, CivColor.Rose+line);
	}
	
	public static void sendError(Object sender, String line) {		
		send(sender, CivColor.Rose+line);
	}
	
	/*
	 * Sends message to playerName(if online) AND console. 
	 */
	public static void console(String playerName, String line) {
		try {
			Player player = CivGlobal.getPlayer(playerName);
			send(player, line);
		} catch (CivException e) {
		}
		CivLog.info(line);	
	}
	public static void sendTitle(Object sender, int fadeIn, int show, int fadeOut, String title, String subTitle) {
		if (CivSettings.hasTitleAPI) {
			Player player = null;
			Resident resident = null;
			if ((sender instanceof Player)) {
				player = (Player) sender;
				resident = CivGlobal.getResident(player);
			} else if (sender instanceof Resident) {
				try {
					resident = (Resident)sender;
					player = CivGlobal.getPlayer(resident);
				} catch (CivException e) {
					// No player online
				}
			}
			if (player != null && resident != null && resident.isTitleAPI())
			{
				TitleAPI.sendTitle(player, fadeIn, show, fadeOut, title, subTitle);
			}
		}
		send(sender, title);
		if (subTitle != "") {
			send(sender, subTitle);
		}
	}
	
	
	public static void sendTitle(Object sender, String title, String subTitle) {
		sendTitle(sender, 10, 40, 5, title, subTitle);
	}
	
	public static void send(Object sender, String line) {
		if ((sender instanceof Player)) {
			((Player) sender).sendMessage(line);
		} else if (sender instanceof CommandSender) {
			((CommandSender) sender).sendMessage(line);
		}
		else if (sender instanceof Resident) {
			try {
				CivGlobal.getPlayer(((Resident) sender)).sendMessage(line);
			} catch (CivException e) {
				// No player online
			}
		}
	}
	
	public static String itemTooltip(ItemStack itemStack)
	  {
	    try
	    {
	      Object nmsItem = Reflection.getMethod(Reflection.getOBCClass("inventory.CraftItemStack"), "asNMSCopy", new Class[] { ItemStack.class }).invoke(null, new Object[] { itemStack });
	      return (Reflection.getMethod(Reflection.getNMSClass("ItemStack"), "save", new Class[] { Reflection.getNMSClass("NBTTagCompound") }).invoke(nmsItem, new Object[] { Reflection.getNMSClass("NBTTagCompound").newInstance() }).toString());
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    return null;
	  }
	
	public static void send(Object sender, String line, ItemStack item) {
		if ((sender instanceof Player)) {
			Player p = (Player) sender;
			TextComponent msg = new TextComponent( line );
			msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemTooltip(item)).create() ) );

			p.spigot().sendMessage( msg );
		} else if (sender instanceof CommandSender) {
			
			((CommandSender) sender).sendMessage(line);
		}
		else if (sender instanceof Resident) {
			try {				
				Player p = CivGlobal.getPlayer(((Resident) sender));
				TextComponent msg = new TextComponent( line );
				msg.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_ITEM, new ComponentBuilder(itemTooltip(item)).create() ) );

				p.spigot().sendMessage( msg );
			} catch (CivException e) {
				// No player online
			}
		}
	}
	public static void send(Object sender, String[] lines) {
		boolean isPlayer = false;
		if (sender instanceof Player)
			isPlayer = true;

		for (String line : lines) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(line);
			}
		}
	}

	public static String buildTitle(String title) {
		String line =   "-------------------------------------------------";
		String titleBracket = "[ " + CivColor.Yellow + title + CivColor.LightBlue + " ]";
		
		if (titleBracket.length() > line.length()) {
			return CivColor.LightBlue+"-"+titleBracket+"-";
		}
		
		int min = (line.length() / 2) - titleBracket.length() / 2;
		int max = (line.length() / 2) + titleBracket.length() / 2;
		
		String out = CivColor.LightBlue + line.substring(0, Math.max(0, min));
		out += titleBracket + line.substring(max);
		
		return out;
	}
	
	public static String buildSmallTitle(String title) {
		String line =   CivColor.LightBlue+"------------------------------";
	
		String titleBracket = "[ "+title+" ]";
		
		int min = (line.length() / 2) - titleBracket.length() / 2;
		int max = (line.length() / 2) + titleBracket.length() / 2;
		
		String out = CivColor.LightBlue + line.substring(0, Math.max(0, min));
		out += titleBracket + line.substring(max);
		
		return out;
	}
	
	public static void sendSubHeading(CommandSender sender, String title) {
		send(sender, buildSmallTitle(title));
	}
	
	public static void sendHeading(Resident resident, String title) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
			sendHeading(player, title);
		} catch (CivException e) {
		}
	}
	
	public static void sendHeading(CommandSender sender, String title) {	
		send(sender, buildTitle(title));
	}

	public static void sendSuccess(CommandSender sender, String message) {
		send(sender, CivColor.LightGreen+message);
	}

	public static void global(String string) {
		CivLog.info("[Global] "+string);
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(CivColor.LightBlue+CivSettings.localize.localizedString("civMsg_Globalprefix")+" "+CivColor.White+string);
		}
	}
	
	public static void globalTitle(String title, String subTitle) {
		CivLog.info("[GlobalTitle] "+title+" - "+subTitle);
		for (Player player : Bukkit.getOnlinePlayers()) {
			Resident resident = CivGlobal.getResident(player);
			if (CivSettings.hasTitleAPI && resident.isTitleAPI()) {
				CivMessage.sendTitle(player, 10, 60, 10, title, subTitle);
			} else {
				send(player, buildTitle(title));
				if (!subTitle.equals("")) {
					send(player, subTitle);
				}
			}
		}
	}
	
	public static void globalHeading(String string) {
		CivLog.info("[GlobalHeading] "+string);
		for (Player player : Bukkit.getOnlinePlayers()) {
			send(player, buildTitle(string));
		}
	}
	
	public static void sendScout(Civilization civ, String string) {
		CivLog.info("[Scout:"+civ.getName()+"] "+string);
		for (Town t : civ.getTowns()) {
			for (Resident resident : t.getResidents()) {
				if (!resident.isShowScout()) {
					continue;
				}
				
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					if (player != null) {
						CivMessage.send(player, CivColor.Purple + CivSettings.localize.localizedString("civMsg_ScoutPrefix") + " " + CivColor.White + string);
					}
				} catch (CivException ignored) {
				}
			}
			
		}
	}

	public static void sendScout(Civilization civ, Player scouted, Town scoutTown) {
		for (Town t : civ.getTowns()) {
			for (Resident res : t.getResidents()) {
				Player player;
				try {
					player = CivGlobal.getPlayer(res);
					player.setScoreboard(civ.getScoutScoreboard(scouted, scoutTown));
				} catch (CivException ignored) {
				}
			}
		}
	}
	
	public static void sendTown(Town town, String string) {
		CivLog.info("[Town:"+town.getName()+"] "+string);
		
		for (Resident resident : town.getResidents()) {
			if (!resident.isShowTown()) {
				continue;
			}
			
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				if (player != null) {
					CivMessage.send(player, CivColor.Gold+CivSettings.localize.localizedString("civMsg_Townprefix")+" "+CivColor.White+string);
				}
			} catch (CivException e) {
			}
		}
	}

	public static void sendCiv(Civilization civ, String string) {
		CivLog.info("[Civ:"+civ.getName()+"] "+string);
		for (Town t : civ.getTowns()) {
			for (Resident resident : t.getResidents()) {
				if (!resident.isShowCiv()) {
					continue;
				}
				
				Player player;
				try {
					player = CivGlobal.getPlayer(resident);
					if (player != null) {
						CivMessage.send(player, CivColor.LightPurple+CivSettings.localize.localizedString("civMsg_Civprefix")+" "+CivColor.White+string);
					}
				} catch (CivException e) {
				}
			}
			
		}
	}


	public static void send(CommandSender sender, List<String> outs) {
		for (String str : outs) {
			send(sender, str);
		}
	}


	public static void sendTownChat(Town town, Resident resident, String format, String message) {
		if (town == null) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Rose+CivSettings.localize.localizedString("civMsg_tcNotInTown"));

			} catch (CivException e) {
			}
			return;
		}
		
		CivLog.info("[TC:"+town.getName()+"] "+resident.getName()+": "+message);
		
		for (Resident r : town.getResidents()) {
			try {
				Player player = CivGlobal.getPlayer(r);
				String msg = CivColor.LightBlue+CivSettings.localize.localizedString("civMsg_tcPrefix")+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) {
				continue; /* player not online. */
			}
		}
		
		for (String name : getExtraTownChatListeners(town)) {
			try {
				Player player = CivGlobal.getPlayer(name);
				String msg = CivColor.LightBlue+CivSettings.localize.localizedString("civMsg_tcPrefix2")+town.getName()+"]"+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) {
				/* player not online. */
			}
		}
	}


	public static void sendCivChat(Civilization civ, Resident resident, String format, String message) {
		if (civ == null) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Rose+CivSettings.localize.localizedString("civMsg_ccNotInCiv"));

			} catch (CivException e) {
			}
			return;
		}
			
		String townName = "";
		if (resident.getTown() != null) {
			townName = resident.getTown().getName();
		}
		
		for (Town t : civ.getTowns()) {
			for (Resident r : t.getResidents()) {
				try {
					Player player = CivGlobal.getPlayer(r);
					
					
					String msg = CivColor.Gold+CivSettings.localize.localizedString("civMsg_ccPrefix1")+" "+townName+"]"+CivColor.White+String.format(format, resident.getName(), message);
					player.sendMessage(msg);
				} catch (CivException e) {
					continue; /* player not online. */
				}
			}
		}
		
		for (String name : getExtraCivChatListeners(civ)) {
			try {
				Player player = CivGlobal.getPlayer(name);
				String msg = CivColor.Gold+CivSettings.localize.localizedString("civMsg_ccPrefix2")+civ.getName()+" "+townName+"]"+CivColor.White+String.format(format, resident.getName(), message);
				player.sendMessage(msg);
			} catch (CivException e) {
				/* player not online. */
			}
		}
		
		return;
	}
	
	public static void sendChat(Resident resident, String format, String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			String msg = String.format(format, resident.getName(), message);
			player.sendMessage(msg);
		}
	}
	
	public static void addExtraTownChatListener(Town town, String name) {
		
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) {
			names = new ArrayList<>();
		}
		
		for (String str : names) {
			if (str.equals(name)) {
				return;
			}
		}
		
		names.add(name);		
		extraTownChatListeners.put(town.getName().toLowerCase(), names);
	}
	
	public static void removeExtraTownChatListener(Town town, String name) {
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) {
			return;
		}
		
		for (String str : names) {
			if (str.equals(name)) {
				names.remove(str);
				break;
			}
		}
		
		extraTownChatListeners.put(town.getName().toLowerCase(), names);
	}
	
	public static ArrayList<String> getExtraTownChatListeners(Town town) {
		ArrayList<String> names = extraTownChatListeners.get(town.getName().toLowerCase());
		if (names == null) {
			return new ArrayList<>();
		}
		return names;
	}
	
	public static void addExtraCivChatListener(Civilization civ, String name) {
		
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) {
			names = new ArrayList<>();
		}
		
		for (String str : names) {
			if (str.equals(name)) {
				return;
			}
		}
		
		names.add(name);
		
		extraCivChatListeners.put(civ.getName().toLowerCase(), names);
	}
	
	public static void removeExtraCivChatListener(Civilization civ, String name) {
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) {
			return;
		}
		
		for (String str : names) {
			if (str.equals(name)) {
				names.remove(str);
				break;
			}
		}
		
		extraCivChatListeners.put(civ.getName().toLowerCase(), names);
	}
	
	public static ArrayList<String> getExtraCivChatListeners(Civilization civ) {
		ArrayList<String> names = extraCivChatListeners.get(civ.getName().toLowerCase());
		if (names == null) {
			return new ArrayList<>();
		}
		return names;
	}

	public static void sendTownSound(Town town, Sound sound, float f, float g) {
		for (Resident resident : town.getResidents()) {
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				
				player.playSound(player.getLocation(), sound, f, g);
			} catch (CivException e) {
				//player not online.
			}
		}
		
	}

	public static void sendAll(String str) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(str);
		}
	}

	public static void sendCamp(Camp camp, String message) {
		for (Resident resident : camp.getMembers()) {
			try {
				Player player = CivGlobal.getPlayer(resident);
				player.sendMessage(CivColor.Yellow+"[Camp] "+CivColor.Yellow+message);		
				CivLog.info("[Camp:"+camp.getName()+"] "+message);

			} catch (CivException e) {
				//player not online.
			}
		}
	}

	public static void sendTownHeading(Town town, String string) {
		CivLog.info("[Town:"+town.getName()+"] "+string);
		for (Resident resident : town.getResidents()) {
			if (!resident.isShowTown()) {
				continue;
			}
			
			Player player;
			try {
				player = CivGlobal.getPlayer(resident);
				if (player != null) {
					CivMessage.sendHeading(player, string);
				}
			} catch (CivException ignored) {
			}
		}
	}

	public static void sendSuccess(Resident resident, String message) {
		try {
			Player player = CivGlobal.getPlayer(resident);
			sendSuccess(player, message);
		} catch (CivException ignored) {
		}
	}

	public static void sendTeam(ArenaTeam team, String message) {
		for (Resident resident : team.teamMembers) {
			CivMessage.send(resident, CivColor.Blue+CivSettings.localize.localizedString("civMsg_teamchatPrefix")+" ("+team.getName()+")] "+CivColor.RESET+message);
		}
	}
	
	public static void sendTeamHeading(ArenaTeam team, String message) {
		for (Resident resident : team.teamMembers) {
			CivMessage.sendHeading(resident, message);
		}
	}
	
	public static void sendArena(Arena arena, String message) {
		CivLog.info("[Arena] "+message);
		for (ArenaTeam team : arena.getTeams()) {
			for (Resident resident : team.teamMembers) {
				CivMessage.send(resident, CivColor.LightBlue+CivSettings.localize.localizedString("civMsg_arenaPrefix")+" "+CivColor.RESET+message);
			}
		}
	}

	private static String switchSymbols(String str) {
		switch (str) {
			case "а":
				str = "{a";
				break;
			case "А":
				str = "{A";
				break;
			case "б":
				str = "{b";
				break;
			case "Б":
				str = "{B";
				break;
			case "в":
				str = "{v";
				break;
			case "В":
				str = "{V";
				break;
			case "г":
				str = "{g";
				break;
			case "Г":
				str = "{G";
				break;
			case "д":
				str = "{d";
				break;
			case "Д":
				str = "{D";
				break;
			case "е":
				str = "{e";
				break;
			case "Е":
				str = "{E";
				break;
			case "ё":
				str = "{yo";
				break;
			case "Ё":
				str = "{YO";
				break;
			case "ж":
				str = "{zh";
				break;
			case "Ж":
				str = "{ZH";
				break;
			case "з":
				str = "{z";
				break;
			case "З":
				str = "{Z";
				break;
			case "и":
				str = "{i";
				break;
			case "И":
				str = "{I";
				break;
			case "й":
				str = "{y";
				break;
			case "Й":
				str = "{Y";
				break;
			case "к":
				str = "{k";
				break;
			case "К":
				str = "{K";
				break;
			case "л":
				str = "{l";
				break;
			case "Л":
				str = "{L";
				break;
			case "м":
				str = "{m";
				break;
			case "М":
				str = "{M";
				break;
			case "н":
				str = "{n";
				break;
			case "Н":
				str = "{N";
				break;
			case "о":
				str = "{o";
				break;
			case "О":
				str = "{O";
				break;
			case "п":
				str = "{p";
				break;
			case "П":
				str = "{P";
				break;
			case "р":
				str = "{r";
				break;
			case "Р":
				str = "{R";
				break;
			case "с":
				str = "{s";
				break;
			case "С":
				str = "{S";
				break;
			case "т":
				str = "{t";
				break;
			case "Т":
				str = "{T";
				break;
			case "у":
				str = "{u";
				break;
			case "У":
				str = "{U";
				break;
			case "ф":
				str = "{f";
				break;
			case "Ф":
				str = "{F";
				break;
			case "х":
				str = "{h";
				break;
			case "Х":
				str = "{H";
				break;
			case "ц":
				str = "{c";
				break;
			case "Ц":
				str = "{C";
				break;
			case "ч":
				str = "{ch";
				break;
			case "Ч":
				str = "{CH";
				break;
			case "ш":
				str = "{sh";
				break;
			case "Ш":
				str = "{SH";
				break;
			case "щ":
				str = "{sch";
				break;
			case "Щ":
				str = "{SCH";
				break;
			case "ъ":
				str = "{q";
				break;
			case "Ъ":
				str = "{Q";
				break;
			case "ы":
				str = "{w";
				break;
			case "Ы":
				str = "{W";
				break;
			case "ь":
				str = "{j";
				break;
			case "Ь":
				str = "{J";
				break;
			case "э":
				str = "{x";
				break;
			case "Э":
				str = "{X";
				break;
			case "ю":
				str = "{yu";
				break;
			case "Ю":
				str = "{YU";
				break;
			case "я":
				str = "{ya";
				break;
			case "Я":
				str = "{YA";
				break;
			default:
				break;
		}
		return str;
	}

	private static String reswitchSymbols(String str) {
		switch (str) {
			case "a":
				str = "а";
				break;
			case "A":
				str = "А";
				break;
			case "b":
				str = "б";
				break;
			case "B":
				str = "Б";
				break;
			case "v":
				str = "в";
				break;
			case "V":
				str = "В";
				break;
			case "g":
				str = "г";
				break;
			case "G":
				str = "Г";
				break;
			case "d":
				str = "д";
				break;
			case "D":
				str = "Д";
				break;
			case "e":
				str = "е";
				break;
			case "E":
				str = "Е";
				break;
			case "yo":
				str = "ё";
				break;
			case "YO":
				str = "Ё";
				break;
			case "zh":
				str = "ж";
				break;
			case "ZH":
				str = "Ж";
				break;
			case "z":
				str = "з";
				break;
			case "Z":
				str = "З";
				break;
			case "i":
				str = "и";
				break;
			case "I":
				str = "И";
				break;
			case "y":
				str = "й";
				break;
			case "Y":
				str = "Й";
				break;
			case "k":
				str= "к";
				break;
			case "K":
				str = "К";
				break;
			case "l":
				str = "л";
				break;
			case "L":
				str = "Л";
				break;
			case "m":
				str = "м";
				break;
			case "M":
				str = "М";
				break;
			case "n":
				str = "н";
				break;
			case "N":
				str = "Н";
				break;
			case "o":
				str = "о";
				break;
			case "O":
				str = "О";
				break;
			case "p":
				str = "п";
				break;
			case "P":
				str = "П";
				break;
			case "r":
				str = "р";
				break;
			case "R":
				str = "Р";
				break;
			case "s":
				str = "с";
				break;
			case "S":
				str = "С";
				break;
			case "t":
				str = "т";
				break;
			case "T":
				str = "Т";
				break;
			case "u":
				str = "у";
				break;
			case "U":
				str = "У";
				break;
			case "f":
				str = "ф";
				break;
			case "F":
				str = "Ф";
				break;
			case "h":
				str = "х";
				break;
			case "H":
				str = "Х";
				break;
			case "c":
				str = "ц";
				break;
			case "C":
				str = "Ц";
				break;
			case "ch":
				str = "ч";
				break;
			case "CH":
				str = "Ч";
				break;
			case "sh":
				str = "ш";
				break;
			case "SH":
				str = "Ш";
				break;
			case "sch":
				str = "щ";
				break;
			case "SCH":
				str = "Щ";
				break;
			case "q":
				str = "ъ";
				break;
			case "Q":
				str = "Ъ";
				break;
			case "w":
				str = "ы";
				break;
			case "W":
				str = "Ы";
				break;
			case "j":
				str = "ь";
				break;
			case "J":
				str = "Ь";
				break;
			case "x":
				str = "э";
				break;
			case "X":
				str = "Э";
				break;
			case "yu":
				str = "ю";
				break;
			case "YU":
				str = "Ю";
				break;
			case "ya":
				str = "я";
				break;
			case "YA":
				str = "Я";
				break;
			default:
				break;
		}
		return str;
	}

	public static String transliteString(String str) {
		if (str == null || str.equals("")) {
			return null;
		} else {
			str = str.replace("}", "");
			str = str.replace("{", "");
		}

		if (str == null || str.equals("")) {
			return null;
		}

		StringBuilder out = new StringBuilder();
		for (Character symbol : str.toCharArray()) {
			String sym = symbol.toString();
			sym = switchSymbols(sym) + "}";
			out.append(sym);
		}

		return out.toString();
	}

	public static String retransliteString(String str) {
		if (str == null) {
			return null;
		}

		StringBuilder out = new StringBuilder();
		String[] split = str.split("}");
		if (split.length != 0) {
			for (String s : split) {
				if (s.regionMatches(0, "{", 0, 1)) {
					s = s.replace("{", "");
					s = reswitchSymbols(s);
				}
				out.append(s);
			}
		}
		return out.toString();
	}
	
}
