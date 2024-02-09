package com.dynast.civcraft.structure;

import java.util.List;

import com.dynast.civcraft.util.BlockCoord;

public interface RespawnLocationHolder {

	String getRespawnName();
	List<BlockCoord> getRespawnPoints();
	BlockCoord getRandomRevivePoint();
}
