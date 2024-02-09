package com.dynast.civcraft.threading.tasks;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class DropItemsTask implements Runnable {
    private HashMap<Integer, ItemStack> stacks;
    private Player player;

    public DropItemsTask(HashMap<Integer, ItemStack>  stacks, Player player) {
        this.stacks = stacks;
        this.player = player;
    }

    @Override
    public void run() {
        for (ItemStack is : stacks.values()) {
            player.getWorld().dropItem(player.getLocation(), is);
        }
    }
}
