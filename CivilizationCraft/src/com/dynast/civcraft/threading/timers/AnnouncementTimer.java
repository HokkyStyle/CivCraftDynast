
package com.dynast.civcraft.threading.timers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.util.CivColor;

public class AnnouncementTimer implements Runnable {

	List<String> announcements;
	int minutes = 5;
	
	
	public AnnouncementTimer(String filename, int interval) {
		minutes = interval;
		
		File file = new File(CivSettings.plugin.getDataFolder().getPath()+"/data/" +filename);
		if (!file.exists()) {
			CivLog.warning("Configuration file: "+filename+" was missing. Streaming to disk from Jar.");
			try {
				CivSettings.streamResourceToDisk("/data/"+filename);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		CivLog.info("Loading Configuration file: "+filename);
		
		
		announcements = new ArrayList<>();
		
		if (!file.exists()) {
			CivLog.warning("No "+filename+" to run announcements on.");
			return;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String line;
			try {
				while ((line = br.readLine()) != null) {
					announcements.add(line);
				}
		
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	
	@Override
	public void run() {		
		
		for (String str : announcements) {
			CivMessage.sendAll(CivColor.Gold+CivSettings.localize.localizedString("TipHeading")+" "+CivColor.White+str);
			
			try {
				Thread.sleep(60*minutes*1000); //sleep for x mins
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} 
		}
	}

}
