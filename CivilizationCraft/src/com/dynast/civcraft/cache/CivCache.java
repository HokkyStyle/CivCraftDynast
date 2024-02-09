package com.dynast.civcraft.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CivCache {

	/* Arrows fired that need to be updated. */
	public static Map<UUID, ArrowFiredCache> arrowsFired = new HashMap<>();
	
	/* Cannonballs fired that need to be updated. */
	public static Map<UUID, CannonFiredCache> cannonBallsFired = new HashMap<>();

	public static Map<UUID, LightningFiredCache> lightningFired = new HashMap<>();
	
}
