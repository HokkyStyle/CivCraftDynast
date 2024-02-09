package com.dynast.civcraft.populators;

import com.dynast.civcraft.config.ConfigRuin;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.items.units.*;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Ruin;
import com.dynast.civcraft.template.Template;
import com.dynast.civcraft.threading.sync.SyncBuildUpdateTask;
import com.dynast.civcraft.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class RuinPopulator extends BlockPopulator {

    public static void buildRuin(ConfigRuin ruin, BlockCoord coord1, World world, boolean sync) {
        Ruin new_ruin = new Ruin(ruin, new ChunkCoord(coord1));
        BlockCoord coord = new BlockCoord(world.getName(), coord1.getX(),coord1.getY(), coord1.getZ());
        CivGlobal.addRuin(new_ruin);

        String tpl_name = "";
        switch (ruin.id) {
            case "desert_ruin":
                tpl_name = "desert_ruin";
                break;
            case "land_ruin":
                tpl_name = "land_ruin";
                break;
            case "jungle_ruin":
                tpl_name = "jungle_ruin";
                break;
        }

        Template tpl = new Template();
        try {
            tpl = Template.getTemplate("templates/themes/" + tpl_name + ".def", null);
        } catch (IOException|CivException e) {
            CivLog.error("Ruin template not found!");
            e.printStackTrace();
        }
        Queue<SimpleBlock> sbs = new LinkedList<>();

        for (int x = 0; x < tpl.size_x; x++) {
            for (int y = 0; y < tpl.size_y; y++) {
                for (int z = 0; z < tpl.size_z; z++) {
                    BlockCoord bcoord = new BlockCoord(world.getName(), coord.getX()+x, coord.getY()+y, coord.getZ()+z);
                    SimpleBlock sb = tpl.blocks[x][y][z];
                    Block b = Bukkit.getWorld(world.getName()).getBlockAt(coord.getX()+x, coord.getY()+y, coord.getZ()+z);

                    sb.x = coord.getX()+x;
                    sb.y = coord.getY()+y;
                    sb.z = coord.getZ()+z;
                    sb.worldname = world.getName();
                    sb.buildable = null;

                    sbs.add(sb);
                    SyncBuildUpdateTask.queueSimpleBlock(sbs);
                    sbs.clear();

                    if (sb.getType() == 54) {
                        Chest chest;
                        try {
                            chest = (Chest)b.getState();
                        } catch (ClassCastException e) {
                            ItemManager.setTypeId(b, CivData.CHEST);
                            ItemManager.setTypeId(b.getState(), CivData.CHEST);
                            b.getState().update();
                            chest = (Chest)b.getState();
                        }

                        depositItemsInChests(ruin, chest);
                    }
                    //ProtectedBlock pb = new ProtectedBlock(bcoord, ProtectedBlock.Type.RUIN);
                    //CivGlobal.addProtectedBlock(pb);

                    //pb.save();
                }
            }
        }
        if (sync) {
            try {
                new_ruin.saveNow();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            new_ruin.save();
        }
        //new_ruin.save();
    }

    private static void depositItemsInChests(ConfigRuin ruin, Chest chest) {
        Inventory inv = chest.getBlockInventory();
        List<String> listItems = ruin.toDrop;
        Map<ItemStack, Integer> items = new LinkedHashMap<>();
        Random rand = new Random();

        for (String s : listItems) {
            String[] split = s.split(":");
            ItemStack stack = null;
            Material mat;
            int i = 0;
            if (Material.getMaterial(split[0]) != null) {
                stack = new ItemStack(Material.getMaterial(split[0]), rand.nextInt(Integer.valueOf(split[1]))+1);
            } else {
                LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(split[0]);
                if (craftMat == null) {
                    if (split[0].equals("fishrodlure2")) {
                        mat = Material.FISHING_ROD;
                        stack = new ItemStack(mat);
                        stack.addEnchantment(Enchantment.LURE, 2);
                    } else if (split[0].equals("unit")) {
                        int r = rand.nextInt(6)+1;
                        switch (r) {
                            case 1:
                                stack = Berserker.spawn();
                                break;
                            case 2:
                                stack = Scout.spawn();
                                break;
                            case 3:
                                stack = Defender.spawn();
                                break;
                            case 4:
                                stack = Bowman.spawn();
                                break;
                            case 5:
                                stack = Alchemist.spawn();
                                break;
                            case 6:
                                stack = Assassin.spawn();
                                break;
                        }
                    }
                } else {
                    stack = LoreCraftableMaterial.spawn(craftMat, rand.nextInt(Integer.valueOf(split[1]))+1);
                }
            }

            items.put(stack, Integer.valueOf(split[2]));
        }

        for (int i = 0; i < inv.getSize(); i++) {
            int random = rand.nextInt(100)+1;
            if (random > 50) {
                Set<Integer> randoms = new TreeSet<>();
                randoms.addAll(items.values());
                List<ItemStack> stacks = new ArrayList<>();

                //while (inv.getItem(i) == null || inv.getItem(i).getType() == Material.AIR) {
                    int rand1 = rand.nextInt(100) + 1;
                    int value = 0;

                    for (Integer random1 : randoms) {
                        if (rand1 <= random1) {
                            value = random1;
                            break;
                        }
                    }

                    for (Map.Entry<ItemStack, Integer> item : items.entrySet()) {
                        if (item.getValue() == value) {
                            stacks.add(item.getKey());
                        }
                    }
                    if (stacks.size() > 0) {
                        int randItem = rand.nextInt(stacks.size());
                        if (!inv.contains(stacks.get(randItem)))
                            inv.setItem(i, stacks.get(randItem));
                        else {
                            randItem = rand.nextInt(stacks.size());
                            if (!inv.contains(stacks.get(randItem)))
                                inv.setItem(i, stacks.get(randItem));
                        }
                    }
                //}
            }
        }
    }

    private static boolean checkForDuplicateRuin(String worldName, int centerX, int centerY, int centerZ) {
        BlockCoord coord = new BlockCoord(worldName, centerX, centerY, centerZ);
        for (int y = centerY; y > 0; y--) {
            coord.setY(y);

            if (CivGlobal.getRuin(coord) != null) {
				/* Already a trade goodie here. DONT Generate it. */
                return true;
            }
        }
        return false;
    }

    @Override
    public void populate(World world, Random random, Chunk source) {

        ChunkCoord cCoord = new ChunkCoord(source);
        RuinPick pick = CivGlobal.ruinPreGenerator.ruinPicks.get(cCoord);
        if (pick != null) {
            int centerX = source.getX() * 16;
            int centerZ = source.getZ() * 16;
            int centerY = world.getHighestBlockYAt(centerX, centerZ);
            //Material matOfHighest = world.getHighestBlockAt(centerX, centerZ).getType();

            if (ItemManager.getBlockTypeIdAt(world, centerX, centerY-1, centerZ) == CivData.WATER ||
                    ItemManager.getBlockTypeIdAt(world, centerX, centerY-1, centerZ) == 111) {
                return;
            }

            BlockCoord coord = new BlockCoord(world.getName(), centerX, centerY, centerZ);

            if (checkForDuplicateRuin(world.getName(), centerX, centerY, centerZ)) {
                return;
            }

            ConfigRuin ruin = null;

            if (pick.jungleRuin != null) {
                ruin = pick.jungleRuin;
            } else if (pick.landRuin != null) {
                ruin = pick.landRuin;
            } else if (pick.desertRuin != null) {
                ruin = pick.desertRuin;
            }

            if (ruin == null) {
                //System.out.println("Could not find suitable ruin type during populate! aborting.");
                return;
            }

            buildRuin(ruin, coord, world, false);
        }
    }
}
