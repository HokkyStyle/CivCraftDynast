package com.dynast.civcraft.questions;

import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Resident;
import com.dynast.civcraft.trade.TradeInventoryListener;
import com.dynast.civcraft.trade.TradeInventoryPair;
import com.dynast.civcraft.util.CivColor;

public class TradeRequest implements QuestionResponseInterface {

	public Resident resident;
	public Resident trader;
	
	@Override
	public void processResponse(String param) {
		if (param.equalsIgnoreCase("accept")) {
			TradeInventoryPair pair = new TradeInventoryPair();
			pair.inv = trader.startTradeWith(resident);
			if (pair.inv == null) {
				return;
			}
			
			pair.otherInv = resident.startTradeWith(trader);
			if (pair.otherInv == null) {
				return;
			}
			
			pair.resident = trader;
			pair.otherResident = resident;
			TradeInventoryListener.tradeInventories.put(TradeInventoryListener.getTradeInventoryKey(trader), pair);
			
			TradeInventoryPair otherPair = new TradeInventoryPair();
			otherPair.inv = pair.otherInv;
			otherPair.otherInv = pair.inv;
			otherPair.resident = pair.otherResident;
			otherPair.otherResident = pair.resident;
			TradeInventoryListener.tradeInventories.put(TradeInventoryListener.getTradeInventoryKey(resident), otherPair);
		} else {
			CivMessage.send(trader, CivColor.LightGray+CivSettings.localize.localizedString("var_trade_declined",resident.getName()));
		}
	}

	@Override
	public void processResponse(String response, Resident responder) {
		processResponse(response);		
	}
}
