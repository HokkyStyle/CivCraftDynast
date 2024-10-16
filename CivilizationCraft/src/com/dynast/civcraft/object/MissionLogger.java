
package com.dynast.civcraft.object;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import com.dynast.civcraft.database.SQL;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;

public class MissionLogger {

	
	public static String TABLE_NAME = "MISSION_LOGS";
	public static void init() throws SQLException {
		if (!SQL.hasTable(TABLE_NAME)) {
			String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" + 
					"`id` int(11) unsigned NOT NULL auto_increment," +
					"`town_id` int(11) unsigned DEFAULT 0," +
					"`target_id` int(11) unsigned DEFAULT 0," +
					"`time` long," +
					"`playerName` mediumtext," +
					"`missionName` mediumtext," +
					"`result` mediumtext," +
				"PRIMARY KEY (`id`)" + ")";
			
			SQL.makeTable(table_create);
			CivLog.info("Created "+TABLE_NAME+" table");
		} else {
			CivLog.info(TABLE_NAME+" table OK!");
		}
	}
	
	
	public static void logMission(Town town, Town target, Resident resident, String missionName, String result) {
		HashMap<String, Object> hashmap = new HashMap<>();
		
		hashmap.put("town_id", town.getId());
		hashmap.put("target_id", target.getId());
		hashmap.put("time", new Date());
			hashmap.put("playerName", resident.getUUIDString());
		
		
		hashmap.put("missionName", missionName);
		hashmap.put("result", result);
		
		try {
			SQL.insertNow(hashmap, TABLE_NAME);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getMissionLogs(Town town) {
		Connection context = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		
		try {
			ArrayList<String> out = new ArrayList<>();
			try {
				context = SQL.getGameConnection();		
				ps = context.prepareStatement("SELECT * FROM "+SQL.tb_prefix+TABLE_NAME+" WHERE `town_id` = ?");
				ps.setInt(1, town.getId());
				rs = ps.executeQuery();
		
				SimpleDateFormat sdf = new SimpleDateFormat("M/dd h:mm:ss a z");
				while(rs.next()) {
					Date date = new Date(rs.getLong("time"));
					Town target = CivGlobal.getTownFromId(rs.getInt("target_id"));
					if (target == null) {
						continue;
					}
					
					String playerName = rs.getString("playerName");
						 playerName = CivGlobal.getResidentViaUUID(UUID.fromString(playerName)).getName();
					
					String str = sdf.format(date)+" - "+rs.getString("playerName")+":"+target.getName()+":"+rs.getString("missionName")+" -- "+rs.getString("result");
					out.add(str);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return out;
		} finally {
			SQL.close(rs, ps, context);
		}
	}
	
}
