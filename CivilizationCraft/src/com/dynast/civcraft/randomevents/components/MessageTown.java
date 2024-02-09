package com.dynast.civcraft.randomevents.components;


import com.dynast.civcraft.randomevents.RandomEventComponent;

public class MessageTown extends RandomEventComponent {

	@Override
	public void process() {
		String message = this.getString("message");
		sendMessage(message);
	}	
}
