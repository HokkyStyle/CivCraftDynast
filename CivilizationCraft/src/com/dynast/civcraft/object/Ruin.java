package com.dynast.civcraft.object;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.config.ConfigRuin;
import com.dynast.civcraft.config.ConfigTradeGood;
import com.dynast.civcraft.database.SQL;
import com.dynast.civcraft.database.SQLUpdate;
import com.dynast.civcraft.exception.InvalidNameException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.structure.TradeOutpost;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.util.ChunkCoord;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Ruin extends SQLObject {

    public ConfigRuin info;
    public ChunkCoord chunkCoord;
    //public Map<BlockCoord, Chest> chests = new HashMap<>();

    public Ruin(ConfigRuin ruin, ChunkCoord coord) {
        this.info = ruin;
        this.chunkCoord = coord;
        try {
            this.setName(ruin.id);
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
    }

    public Ruin(ResultSet rs) throws SQLException, InvalidNameException {
        this.load(rs);
    }

    public static final String TABLE_NAME = "RUINS";
    public static void init() throws SQLException {
        if (!SQL.hasTable(TABLE_NAME)) {
            String table_create = "CREATE TABLE " + SQL.tb_prefix + TABLE_NAME+" (" +
                    "`id` int(11) unsigned NOT NULL auto_increment," +
                    "`name` VARCHAR(64) NOT NULL," +
                    "`coord` mediumtext DEFAULT NULL,"+
                    //"`chests` mediumtext DEFAULT NULL,"+
                    "PRIMARY KEY (`id`)" + ")";

            SQL.makeTable(table_create);
            CivLog.info("Created "+TABLE_NAME+" table");
        } else {
            CivLog.info(TABLE_NAME+" table OK!");
        }
    }

    @Override
    public void load(ResultSet rs) throws SQLException, InvalidNameException {
        this.setId(rs.getInt("id"));
        this.setName(rs.getString("name"));
        this.setInfo(CivSettings.ruins.get(this.getName()));

        String[] split = rs.getString("coord").split(",");
        this.chunkCoord = new ChunkCoord(split[0], Integer.valueOf(split[1]), Integer.valueOf(split[2]));

        /*String[] split = rs.getString("chests").split(":");
        for (String s : split){
            BlockCoord bcoord = new BlockCoord(s);
            Chest chest = (Chest)bcoord.getBlock().getState();

            this.chests.put(bcoord, chest);
        }*/

    }

    @Override
    public void save() {
        SQLUpdate.add(this);
    }

    @Override
    public void saveNow() throws SQLException {
        HashMap<String, Object> hashmap = new HashMap<>();

        hashmap.put("name", this.getName());
        hashmap.put("coord", this.chunkCoord.toString());

        /*StringBuilder sb = new StringBuilder();
        for (BlockCoord coord: this.chests.keySet()) {
            sb.append(coord.toString()).append(":");
        }
        hashmap.put("chests", sb.toString());*/

        SQL.updateNamedObject(this, hashmap, TABLE_NAME);
    }

    @Override
    public void delete() throws SQLException {
    }

    public ConfigRuin getInfo() {
        return this.info;
    }


    public void setInfo(ConfigRuin info) {
        this.info = info;
    }

    public void setCoord(BlockCoord coord) {
        this.chunkCoord = new ChunkCoord(coord);
    }

    public BlockCoord getCoord() {
        return new BlockCoord(this.chunkCoord);
    }

}
