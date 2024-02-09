package com.dynast.civcraft.structurevalidation;

import java.util.Iterator;
import java.util.Map.Entry;

import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.structure.Bank;
import com.dynast.civcraft.structure.Structure;
import com.dynast.civcraft.util.BlockCoord;
import com.dynast.civcraft.war.War;

public class StructureValidationChecker implements Runnable {

	@Override
	public void run() {
		Iterator<Entry<BlockCoord, Structure>> structIter = CivGlobal.getStructureIterator();
		while (structIter.hasNext()) {
			Structure struct = structIter.next().getValue();
			
			if (War.isWarTime()) {
				/* Don't do any work once it's war time. */
				break;
			}
			
			if (!struct.isActive()) {
				continue;
			}
			
			if (struct.isIgnoreFloating()) {
				continue;
			}
			
			try {
				CivLog.warning("Doing a structure validate... "+struct.getDisplayName());
				struct.validate(null);
				struct.updateSignText();
			} catch (CivException e) {
				e.printStackTrace();
			}
			
			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
