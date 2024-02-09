package com.dynast.civcraft.threading.timers;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.AttrSource;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.object.TownChunk;
import com.dynast.civcraft.structure.*;
import com.dynast.civcraft.threading.CivAsyncTask;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.CivColor;

public class EffectEventTimer extends CivAsyncTask {
	
	//public static Boolean running = false;
	
	public static ReentrantLock runningLock = new ReentrantLock();
	
	public EffectEventTimer() {
	}

	private void processTick() {
		/* Clear the last taxes so they don't accumulate. */
		for (Civilization civ : CivGlobal.getCivs()) {
			civ.lastTaxesPaidMap.clear();
		}
		
		//HashMap<Town, Integer> cultureGenerated = new HashMap<Town, Integer>();
		
		// Loop through each structure, if it has an update function call it in another async process
		Iterator<Entry<BlockCoord, Structure>> iter = CivGlobal.getStructureIterator();
		
		while(iter.hasNext()) {
			Structure struct = iter.next().getValue();
			if (struct == null) {
				continue;
			}
			TownHall townhall = struct.getTown().getTownHall();

			if (townhall == null) {
				continue;
			}

			if (!struct.isActive())
				continue;

			struct.onEffectEvent();

			if (struct.getEffectEvent() == null || struct.getEffectEvent().equals(""))
				continue;
			
			String[] split = struct.getEffectEvent().toLowerCase().split(":"); 
			switch (split[0]) {
			case "generate_coins":
				if (struct instanceof Cottage) {
					Cottage cottage = (Cottage)struct;
					//cottage.generate_coins(this);
					cottage.generateCoins(this);
				}
				break;
			case "process_pasture":
				if (struct instanceof Pasture) {
					Pasture pasture = (Pasture)struct;
					try {
						pasture.pastureConsume(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "process_mine":
				if (struct instanceof Mine) {
					Mine mine = (Mine)struct;
					try {
						mine.process_mine(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "process_sawmill":
				if (struct instanceof Sawmill) {
					Sawmill sawmill = (Sawmill)struct;
					try {
						sawmill.process_sawmill(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "process_factory":
				if (struct instanceof Factory) {
					Factory factory = (Factory)struct;
					try {
						factory.process_factory(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "process_univer":
				if (struct instanceof University) {
					University univer = (University)struct;
					try {
						univer.process_univer(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "temple_culture":
				if (struct instanceof Temple) {
					Temple temple = (Temple)struct;
					try {
						temple.templeCulture(this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				break;
			case "process_trade_ship":
				if (struct instanceof TradeShip) {
					TradeShip tradeShip = (TradeShip)struct;
					try {
						tradeShip.process_trade_ship(this);
					} catch (InterruptedException | InvalidConfiguration e) {
						e.printStackTrace();
					}
				}
				break;
			}
			
		}
		
		/*
		 * Process any hourly attributes for this town.
		 *  - Culture
		 *  
		 */
		for (Town town : CivGlobal.getTowns()) {
			double cultureGenerated;
			
//			if (town.riotsTime != 0 && town.riotsTime > 0) {
//				town.riotsTime--;
//				CivMessage.sendTown(town, CivColor.Yellow+CivSettings.localize.localizedString("effectEvent_riots_notEnded",town.riotsTime));
//				
//				if (town.riotsTime == 0 || town.riotsTime < 0) {
//					town.riotsTime = 0;
//					town.riots = false;
//					CivMessage.sendTown(town, CivColor.Green+CivSettings.localize.localizedString("effectEvent_riots_ended"));
//				}
//			}
			
			town.processRiots();

			
			// highjack this loop to display town hall warning.
			TownHall townhall = town.getTownHall();
			if (townhall == null) {
				CivMessage.sendTown(town, CivColor.Yellow+CivSettings.localize.localizedString("effectEvent_noTownHall"));
				continue;
			}
							
			AttrSource cultureSources = town.getCulture();
			
			// Get amount generated after culture rate/bonus.
			cultureGenerated = cultureSources.total;
			cultureGenerated = Math.round(cultureGenerated);
			town.addAccumulatedCulture(cultureGenerated);
			
			// Get from unused beakers.
			DecimalFormat df = new DecimalFormat();
			double unusedBeakers = town.getUnusedBeakers();
	
			try {
				double cultureToBeakerConversion = CivSettings.getDouble(CivSettings.cultureConfig, "beakers_per_culture");
				if (unusedBeakers > 0) {
					double cultureFromBeakers = unusedBeakers*cultureToBeakerConversion;
					cultureFromBeakers = Math.round(cultureFromBeakers);
					unusedBeakers = Math.round(unusedBeakers);
					
					if (cultureFromBeakers > 0) {
						CivMessage.sendTown(town, CivColor.LightGreen+CivSettings.localize.localizedString("var_effectEvent_convertBeakers",(CivColor.LightPurple+
								df.format(unusedBeakers)+CivColor.LightGreen),(CivColor.LightPurple+
								df.format(cultureFromBeakers)+CivColor.LightGreen)));
						cultureGenerated += cultureFromBeakers;
						town.addAccumulatedCulture(cultureFromBeakers);
						town.setUnusedBeakers(0);
					}
				}
			} catch (InvalidConfiguration e) {
				e.printStackTrace();
				return;
			}
			
			cultureGenerated = Math.round(cultureGenerated);
			CivMessage.sendTown(town, CivColor.LightGreen+CivSettings.localize.localizedString("var_effectEvent_generatedCulture",(CivColor.LightPurple+cultureGenerated+CivColor.LightGreen)));

			if (town.getDefeatDate() != null) {
				Calendar cal = Calendar.getInstance();
				Calendar revcal = Calendar.getInstance();
				revcal.setTime(town.getDefeatDate());
				revcal.add(Calendar.DAY_OF_MONTH, 2);

				if (cal.after(revcal)) {
					CivGlobal.removeConqueredTown(town);
					town.setMotherCiv(null);
					town.setDefeateDate(null);
					town.save();
				}
			}

			for (TownChunk tc : town.getTownChunks()) {
				if (CivGlobal.getCultureChunk(tc.getChunkCoord()) == null) {
					try {
						TownChunk.unclaim(tc);
					} catch (CivException ignored) {}
				}
			}
		}
		/* Checking for expired vassal states. */
		CivGlobal.checkForExpiredRelations();
	}
	
	@Override
	public void run() {
		if (runningLock.tryLock()) {
			try {
				processTick();
			} finally {
				runningLock.unlock();
			}
		} else {
			CivLog.error("COULDN'T GET LOCK FOR HOURLY TICK. LAST TICK STILL IN PROGRESS?");
		}
		
				
	}
}
