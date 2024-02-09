package com.dynast.civcraft.npctraits;

import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.structure.Buildable;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.plugin.java.JavaPlugin;

@TraitName("GeneralTrait")
public class GeneralTrait extends Trait {
    private CivCraft plugin;
    public Buildable buildable;

    public GeneralTrait() {
        super("GeneralTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
    }

    public GeneralTrait(Buildable buildable) {
        super("GeneralTrait");
        plugin = JavaPlugin.getPlugin(CivCraft.class);
        this.buildable = buildable;
    }

}
