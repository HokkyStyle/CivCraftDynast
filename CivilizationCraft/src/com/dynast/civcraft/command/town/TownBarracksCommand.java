package com.dynast.civcraft.command.town;

import com.dynast.civcraft.command.CommandBase;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.main.CivMessage;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.structure.Barracks;
import org.bukkit.entity.Player;

public class TownBarracksCommand extends CommandBase {

    @Override
    public void init() {
        command = "/town barracks";
        displayName = CivSettings.localize.localizedString("cmd_town_barracks_name");

        commands.put("progress", CivSettings.localize.localizedString("cmd_town_barracks_progress"));
        commands.put("stoptrain", CivSettings.localize.localizedString("cmd_town_barracks_stoptrain"));
    }

    public void progress_cmd() throws CivException{
        CivMessage.sendHeading(sender, CivSettings.localize.localizedString("cmd_town_barracks_heading"));
        Town town = getSelectedTown();
        Barracks barracks = (Barracks)town.getStructureByType("s_barracks");

        if (barracks == null) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_barracks_nothave"));
        }


    }

    @Override
    public void doDefaultAction() throws CivException {
        showHelp();
    }

    @Override
    public void showHelp() {
        showBasicHelp();
    }

    @Override
    public void permissionCheck() throws CivException {
        Town town = getSelectedTown();
        Player player = getPlayer();

        if (!town.playerIsInGroupName("mayors", player) && !town.playerIsInGroupName("assistants", player) && !town.getCiv().getLeaderGroup().hasMember(getResident())) {
            throw new CivException(CivSettings.localize.localizedString("cmd_town_claimNoPerm"));
        }
    }
}
