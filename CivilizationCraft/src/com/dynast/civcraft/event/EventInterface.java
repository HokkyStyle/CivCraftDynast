package com.dynast.civcraft.event;

import java.util.Calendar;

import com.dynast.civcraft.exception.InvalidConfiguration;

public interface EventInterface {
	void process();
	Calendar getNextDate() throws InvalidConfiguration;
}
