
package com.dynast.civcraft.components;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.sessiondb.SessionEntry;
import com.dynast.civcraft.structure.Buildable;

public class NonMemberFeeComponent extends Component {

	private Buildable buildable;
	private double feeRate = 0.00;
	
	public NonMemberFeeComponent(Buildable buildable) {
		this.buildable = buildable;
	}
	
	
	private String getKey() {
		return buildable.getConfigId()+":"+buildable.getId()+":"+"fee";
	}
	
	@Override
	public void onLoad() {
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getKey());
		
		if (entries.size() == 0) {
			buildable.sessionAdd(getKey(), ""+feeRate);
			return;
		}
		
		feeRate = Double.valueOf(entries.get(0).value);
		
	}

	@Override
	public void onSave() {
		
		ArrayList<SessionEntry> entries = CivGlobal.getSessionDB().lookup(getKey());
		
		if (entries.size() == 0) {
			buildable.sessionAdd(getKey(), ""+feeRate);
			return;
		}
		CivGlobal.getSessionDB().update(entries.get(0).request_id, getKey(), ""+feeRate);		
	}


	public double getFeeRate() {
		return feeRate;
	}


	public void setFeeRate(double feeRate) {
		this.feeRate = feeRate;
		onSave();
	}


	public Buildable getBuildable() {
		return buildable;
	}
	
	public String getFeeString() {
		DecimalFormat df = new DecimalFormat();
		return ""+df.format(this.getFeeRate()*100)+"%";
	}
	
}
