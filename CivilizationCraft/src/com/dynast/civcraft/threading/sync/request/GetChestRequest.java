
package com.dynast.civcraft.threading.sync.request;

import java.util.concurrent.locks.ReentrantLock;

public class GetChestRequest extends AsyncRequest {
	
	public GetChestRequest(ReentrantLock lock) {
		super(lock);
	}
	
	public int block_x;
	public int block_y;
	public int block_z;
	public String worldName;
	
}
