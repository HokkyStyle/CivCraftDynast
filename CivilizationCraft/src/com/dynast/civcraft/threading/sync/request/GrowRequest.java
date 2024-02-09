
package com.dynast.civcraft.threading.sync.request;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import com.dynast.civcraft.structure.farm.FarmChunk;
import com.dynast.civcraft.structure.farm.GrowBlock;

public class GrowRequest extends AsyncRequest {

	public GrowRequest(ReentrantLock lock) {
		super(lock);
	}
	
	public LinkedList<GrowBlock> growBlocks;
	public FarmChunk farmChunk;

}
