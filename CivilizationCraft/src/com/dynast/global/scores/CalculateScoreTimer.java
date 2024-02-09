
package com.dynast.global.scores;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.object.Civilization;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.threading.CivAsyncTask;

public class CalculateScoreTimer extends CivAsyncTask {
	
	@Override
	public void run() {
		
		if (!CivGlobal.scoringEnabled) {
			return;
		}
		
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup("endgame:winningCiv");
		if (entries.size() != 0) {
			/* we have a winner, do not accumulate scores anymore. */
			return;
		}
		
		TreeMap<Integer, Civilization> civScores = new TreeMap<>();
		for (Civilization civ : CivGlobal.getCivs()) {
			if (civ.isAdminCiv()) {
				continue;
			}
			civScores.put(civ.getScore(), civ);
			
			try {
				ScoreManager.UpdateScore(civ, civ.getScore());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		TreeMap<Integer, Town> townScores = new TreeMap<>();
		for (Town town : CivGlobal.getTowns()) {
			if (town.getCiv().isAdminCiv()) {
				continue;
			}
			try {
				townScores.put(town.getScore(), town);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				ScoreManager.UpdateScore(town, town.getScore());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		synchronized(CivGlobal.civilizationScores) {
			CivGlobal.civilizationScores = civScores;
		}
		
		synchronized(CivGlobal.townScores) {
			CivGlobal.townScores = townScores;
		}
		
		
		
//		//Save out to file.
//		try {
//			writeCivScores(civScores);
//			writeTownScores(townScores);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

}
