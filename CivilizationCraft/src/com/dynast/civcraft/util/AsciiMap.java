
package com.dynast.civcraft.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.TownChunk;

public class AsciiMap {

	private static final int width = 9;
	private static final int height = 40;	
	
	public static List<String> getMapAsString(Location center) {
		ArrayList<String> out = new ArrayList<>();
		
	//	ChunkCoord[][] chunkmap = new ChunkCoord[width][height]; 
		ChunkCoord centerChunk = new ChunkCoord(center);
		
		/* Use the center to build a starting point. */
		ChunkCoord currentChunk = new ChunkCoord(center.getWorld().getName(),
											(centerChunk.getX() - (width/2)),
											(centerChunk.getZ() - (height/2)));
		
		int startX = currentChunk.getX();
		int startZ = currentChunk.getZ();
	
		out.add(CivMessage.buildTitle(CivSettings.localize.localizedString("Map")));
		
		//ChunkCoord currentChunk = new ChunkCoord(center);
		for (int x = 0; x < width; x++) {
			String outRow = new String("         ");
			for (int z = 0; z < height; z++) {
				String color = CivColor.White;
								
				currentChunk = new ChunkCoord(center.getWorld().getName(), 
						startX+x, startZ+z);
				
				if (currentChunk.equals(centerChunk)) {
					color = CivColor.Yellow;
				}
				
				/* Try to see if there is a town chunk here.. */
				TownChunk tc = CivGlobal.getTownChunk(currentChunk);
				if (tc != null) {
					
					if (color.equals(CivColor.White)) {
						if (tc.perms.getOwner() != null) {
							color = CivColor.LightGreen;
						} else {
							color = CivColor.Rose;
						}
					}
					
					if (tc.isForSale()) {
						outRow += CivColor.Yellow+"$";
					} else if (tc.isOutpost()) { 
						outRow += CivColor.Yellow+"O";
					} else {
						outRow += color+"T";
					}
				} else {
					outRow += color+"-";
				}
			}
			out.add(outRow);
		}
		
		
		return out;
	}
	
}
