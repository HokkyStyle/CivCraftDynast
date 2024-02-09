package com.dynast.civcraft.event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import org.bukkit.Bukkit;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.util.CivColor;
import com.dynast.civcraft.war.War;

public class DisableTeleportEvent implements EventInterface {
	public int war;

	@Override
	public void process() {
		CivLog.info("TimerEvent: DisableTeleportEvent -------------------------------------");

		try {
			disableTeleport();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Calendar getNextDate() throws InvalidConfiguration {
		Calendar cal = EventTimer.getCalendarInServerTimeZone();

		int dayOfWeek = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_day");
		int hourBeforeWar = CivSettings.getInteger(CivSettings.warConfig, "war.disable_tp_time_hour");

		if (cal.get(Calendar.DAY_OF_WEEK) <= 4) {
			cal.set(Calendar.DAY_OF_WEEK, 4);
			cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			war = 1;
		} else {
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			war = 2;
		}

		Calendar now = Calendar.getInstance();
		if (now.after(cal)) {
			if (war == 1) {
				cal.add(Calendar.DAY_OF_MONTH, 3);
				cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
				cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.add(Calendar.DAY_OF_MONTH, 4);
				cal.set(Calendar.DAY_OF_WEEK, 4);
				cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
		}

		return cal;

		/*cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		Calendar now = Calendar.getInstance();
		if (now.after(cal)) {
			cal.add(Calendar.WEEK_OF_MONTH, 1);
			cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			cal.set(Calendar.HOUR_OF_DAY, hourBeforeWar);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
		}*/
	}
	

	public static void disableTeleport() throws IOException {
		if (War.hasWars()) {
			File file = new File(CivSettings.plugin.getDataFolder().getPath()+"/data/teleportsOff.txt");
			if (!file.exists()) {
				CivLog.warning("Configuration file: teleportsOff.txt was missing. Streaming to disk from Jar.");
				CivSettings.streamResourceToDisk("/data/teleportsOff.txt");
			}
		
			CivLog.info("Loading Configuration file: teleportsOff.txt");
		
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
			
				String line;
				try {
					CivMessage.globalHeading(CivColor.BOLD+CivSettings.localize.localizedString(CivSettings.localize.localizedString("warteleportDisable")));
					while ((line = br.readLine()) != null) {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
					}
		
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	

	
	public static void enableTeleport() throws IOException {
		
		File file = new File(CivSettings.plugin.getDataFolder().getPath()+"/data/teleportsOn.txt");
		if (!file.exists()) {
			CivLog.warning("Configuration file: teleportsOn.txt was missing. Streaming to disk from Jar.");
			CivSettings.streamResourceToDisk("/data/teleportsOn.txt");
		}
		
		CivLog.info("Loading Configuration file: teleportsOn.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			try {

				CivMessage.globalHeading(CivColor.BOLD+CivSettings.localize.localizedString("warteleportEnable"));
				while ((line = br.readLine()) != null) {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), line);
				}
		
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


}
