
package com.dynast.civcraft.threading.sync.request;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class AsyncRequest {
	
	public AsyncRequest(ReentrantLock lock) {
		condition = lock.newCondition();
	}
	
	public Condition condition;
	public Boolean finished = false;
	public Object result = null;
	
}
