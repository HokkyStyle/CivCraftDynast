package com.dynast.civcraft.main;

import java.io.IOException;
import java.sql.SQLException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.dynast.civcraft.npctraits.Banker;
import com.dynast.civcraft.npctraits.GeneralTrait;
import com.dynast.civcraft.npctraits.TeleportMaster;
import com.dynast.civcraft.populators.RuinPopulator;
import com.dynast.civcraft.threading.tasks.*;
import com.dynast.civcraft.threading.timers.*;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import pvptimer.PvPListener;
import pvptimer.PvPTimer;

import com.dynast.anticheat.ACManager;
import com.dynast.civcraft.arena.ArenaListener;
import com.dynast.civcraft.command.AcceptCommand;
import com.dynast.civcraft.command.BuildCommand;
import com.dynast.civcraft.command.DenyCommand;
import com.dynast.civcraft.command.EconCommand;
import com.dynast.civcraft.command.HereCommand;
import com.dynast.civcraft.command.KillCommand;
import com.dynast.civcraft.command.PayCommand;
import com.dynast.civcraft.command.ReportCommand;
import com.dynast.civcraft.command.SelectCommand;
import com.dynast.civcraft.command.TradeCommand;
import com.dynast.civcraft.command.admin.AdminCommand;
import com.dynast.civcraft.command.camp.CampCommand;
import com.dynast.civcraft.command.civ.CivChatCommand;
import com.dynast.civcraft.command.civ.CivCommand;
import com.dynast.civcraft.command.debug.DebugCommand;
import com.dynast.civcraft.command.market.MarketCommand;
import com.dynast.civcraft.command.plot.PlotCommand;
import com.dynast.civcraft.command.resident.ResidentCommand;
import com.dynast.civcraft.command.team.TeamCommand;
import com.dynast.civcraft.command.town.TownChatCommand;
import com.dynast.civcraft.command.town.TownCommand;
import com.dynast.civcraft.config.CivSettings;
import com.dynast.civcraft.database.SQL;
import com.dynast.civcraft.database.SQLUpdate;
import com.dynast.civcraft.endgame.EndConditionNotificationTask;
import com.dynast.civcraft.event.EventTimerTask;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.fishing.FishingListener;
import com.dynast.civcraft.listener.BlockListener;
import com.dynast.civcraft.listener.BonusGoodieManager;
import com.dynast.civcraft.listener.ChatListener;
import com.dynast.civcraft.listener.CustomItemManager;
import com.dynast.civcraft.listener.DebugListener;
import com.dynast.civcraft.listener.DisableXPListener;
import com.dynast.civcraft.listener.HeroChatListener;
import com.dynast.civcraft.listener.MarkerPlacementManager;
import com.dynast.civcraft.listener.PlayerListener;
import com.dynast.civcraft.listener.TagAPIListener;
import com.dynast.civcraft.listener.armor.ArmorListener;
import com.dynast.civcraft.loreenhancements.LoreEnhancementArenaItem;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterialListener;
import com.dynast.civcraft.lorestorage.LoreGuiItemListener;
import com.dynast.civcraft.nocheat.NoCheatPlusSurvialFlyHandler;
import com.dynast.civcraft.populators.TradeGoodPopulator;
import com.dynast.civcraft.randomevents.RandomEventSweeper;
import com.dynast.civcraft.sessiondb.SessionDBAsyncTimer;
import com.dynast.civcraft.siege.CannonListener;
import com.dynast.civcraft.structure.Farm;
import com.dynast.civcraft.structure.farm.FarmGrowthSyncTask;
import com.dynast.civcraft.structure.farm.FarmPreCachePopulateTimer;
import com.dynast.civcraft.structurevalidation.StructureValidationChecker;
import com.dynast.civcraft.structurevalidation.StructureValidationPunisher;
import com.dynast.civcraft.threading.TaskMaster;
import com.dynast.civcraft.threading.sync.SyncBuildUpdateTask;
import com.dynast.civcraft.threading.sync.SyncGetChestInventory;
import com.dynast.civcraft.threading.sync.SyncGrowTask;
import com.dynast.civcraft.threading.sync.SyncLoadChunk;
import com.dynast.civcraft.threading.sync.SyncUpdateChunks;
import com.dynast.civcraft.threading.sync.SyncUpdateInventory;
import com.dynast.civcraft.trade.TradeInventoryListener;
import com.dynast.civcraft.util.BukkitObjects;
import com.dynast.civcraft.util.ChunkCoord;
import com.dynast.civcraft.util.TimeTools;
import com.dynast.civcraft.war.WarListener;
import com.dynast.global.scores.CalculateScoreTimer;
import com.dynast.sls.SLSManager;

public final class CivCraft extends JavaPlugin {

	private boolean isError = false;	
	private static JavaPlugin plugin;	
	public static boolean isDisable = false;
	
	private void startTimers() {
		
		TaskMaster.asyncTask("SQLUpdate", new SQLUpdate(), 0);
		
		// Sync Timers
		TaskMaster.syncTimer(SyncBuildUpdateTask.class.getName(), 
				new SyncBuildUpdateTask(), 0, 1);
		
		TaskMaster.syncTimer(SyncUpdateChunks.class.getName(), 
				new SyncUpdateChunks(), 0, TimeTools.toTicks(1));
		
		TaskMaster.syncTimer(SyncLoadChunk.class.getName(), 
				new SyncLoadChunk(), 0, 1);
		
		TaskMaster.syncTimer(SyncGetChestInventory.class.getName(),
				new SyncGetChestInventory(), 0, 1);
		
		TaskMaster.syncTimer(SyncUpdateInventory.class.getName(),
				new SyncUpdateInventory(), 0, 1);
		
		TaskMaster.syncTimer(SyncGrowTask.class.getName(),
				new SyncGrowTask(), 0, 1);
		
		TaskMaster.syncTimer(PlayerLocationCacheUpdate.class.getName(), 
				new PlayerLocationCacheUpdate(), 0, 10);
		
		TaskMaster.asyncTimer("RandomEventSweeper", new RandomEventSweeper(), 0, TimeTools.toTicks(10));
		
		// Structure event timers
		TaskMaster.asyncTimer("UpdateEventTimer", new UpdateEventTimer(), TimeTools.toTicks(1));
		TaskMaster.asyncTimer("UpdateMinuteEventTimer", new UpdateMinuteEventTimer(), TimeTools.toTicks(20));
		TaskMaster.asyncTimer("RegenTimer", new RegenTimer(), TimeTools.toTicks(5));

		TaskMaster.asyncTimer("BeakerTimer", new BeakerTimer(60), TimeTools.toTicks(60));
		TaskMaster.syncTimer("UnitTrainTimer", new UnitTrainTimer(), TimeTools.toTicks(1));
		TaskMaster.asyncTimer("ReduceExposureTimer", new ReduceExposureTimer(), 0, TimeTools.toTicks(5));

		try {
			double arrow_firerate = CivSettings.getDouble(CivSettings.warConfig, "arrow_tower.fire_rate");
			TaskMaster.syncTimer("arrowTower", new ProjectileComponentTimer(), (int)(arrow_firerate*20));	
			TaskMaster.syncTimer("ScoutTowerTask", new ScoutTowerTask(), TimeTools.toTicks(1));
			
		} catch (InvalidConfiguration e) {
			e.printStackTrace();
			return;
		}
		TaskMaster.syncTimer("arrowhomingtask", new ArrowProjectileTask(), 5);
			
		// Global Event timers		
		TaskMaster.syncTimer("FarmCropCache", new FarmPreCachePopulateTimer(), TimeTools.toTicks(30));
	
		TaskMaster.asyncTimer("FarmGrowthTimer",
				new FarmGrowthSyncTask(), TimeTools.toTicks(Farm.GROW_RATE));

		TaskMaster.asyncTimer("announcer", new AnnouncementTimer("tips.txt", 5), 0, TimeTools.toTicks(60*60));
		TaskMaster.asyncTimer("announcerwar", new AnnouncementTimer("war.txt", 60), 0, TimeTools.toTicks(60*60));
		
		TaskMaster.asyncTimer("ChangeGovernmentTimer", new ChangeGovernmentTimer(), TimeTools.toTicks(60));
		TaskMaster.asyncTimer("CalculateScoreTimer", new CalculateScoreTimer(), 0, TimeTools.toTicks(60));
		
		TaskMaster.asyncTimer(PlayerProximityComponentTimer.class.getName(), 
				new PlayerProximityComponentTimer(), TimeTools.toTicks(1));
		
		TaskMaster.asyncTimer(EventTimerTask.class.getName(), new EventTimerTask(), TimeTools.toTicks(5));

//		if (PlatinumManager.isEnabled()) {
//			TaskMaster.asyncTimer(PlatinumManager.class.getName(), new PlatinumManager(), TimeTools.toTicks(5));
//		}
		
		TaskMaster.syncTimer("WindmillTimer", new WindmillTimer(), TimeTools.toTicks(60));
		TaskMaster.asyncTimer("EndGameNotification", new EndConditionNotificationTask(), TimeTools.toTicks(3600));
				
		TaskMaster.asyncTask(new StructureValidationChecker(), TimeTools.toTicks(120));
		TaskMaster.asyncTimer("StructureValidationPunisher", new StructureValidationPunisher(), TimeTools.toTicks(3600));
		TaskMaster.asyncTimer("SessionDBAsyncTimer", new SessionDBAsyncTimer(), 10);
		TaskMaster.asyncTimer("pvptimer", new PvPTimer(), TimeTools.toTicks(30));
		
		//TaskMaster.syncTimer("ArenaTimer", new ArenaManager(), TimeTools.toTicks(30));
		//TaskMaster.syncTimer("ArenaTimeoutTimer", new ArenaTimer(), TimeTools.toTicks(1));


		TaskMaster.asyncTimer("ThreadingPlayerPvPTimer", new ThreadingPlayerPvPTimer(), TimeTools.toTicks(1));
		TaskMaster.syncTimer("ThreadingUnitFromPlayer", new ThreadingUnitFromPlayer(), TimeTools.toTicks(1));
		//TaskMaster.asyncTimer("NPCCleanerTimer", new NPCCleanerTimer(), TimeTools.toTicks(1));
		//TaskMaster.asyncTask(new SyncResidentJobLevel(), TimeTools.toTicks(60));

	}
	
	private void registerEvents() {
		final PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new BlockListener(), this);
		pluginManager.registerEvents(new ChatListener(), this);
		pluginManager.registerEvents(new BonusGoodieManager(), this);
		pluginManager.registerEvents(new MarkerPlacementManager(), this);
		pluginManager.registerEvents(new CustomItemManager(), this);
		pluginManager.registerEvents(new PlayerListener(), this);		
		pluginManager.registerEvents(new DebugListener(), this);
		pluginManager.registerEvents(new LoreCraftableMaterialListener(), this);
		pluginManager.registerEvents(new LoreGuiItemListener(), this);
		
		Boolean useEXPAsCurrency;
		try {
			useEXPAsCurrency = CivSettings.getBoolean(CivSettings.civConfig, "global.use_exp_as_currency");
		} catch (InvalidConfiguration e) {
			useEXPAsCurrency = true;
			CivLog.error("Unable to check if EXP should be enabled. Disabling.");
			e.printStackTrace();
		}
		
		if (useEXPAsCurrency) {
			pluginManager.registerEvents(new DisableXPListener(), this);
		}
		pluginManager.registerEvents(new TradeInventoryListener(), this);
		pluginManager.registerEvents(new ArenaListener(), this);
		pluginManager.registerEvents(new CannonListener(), this);
		pluginManager.registerEvents(new WarListener(), this);
		pluginManager.registerEvents(new FishingListener(), this);	
		pluginManager.registerEvents(new PvPListener(), this);
		pluginManager.registerEvents(new LoreEnhancementArenaItem(), this);

		//pluginManager.registerEvents(new Banker(), this);

		if ((hasPlugin("iTag") || hasPlugin("TagAPI")) && hasPlugin("ProtocolLib")) {
			CivSettings.hasITag = true;
			pluginManager.registerEvents(new TagAPIListener(), this);
			CivLog.debug("TagAPI Registered");
		} else {
			CivLog.warning("TagAPI not found, not registering TagAPI hooks. This is fine if you're not using TagAPI.");

		}
		
		if (hasPlugin("HeroChat")) {
			pluginManager.registerEvents(new HeroChatListener(), this);
		}

		pluginManager.registerEvents(new ArmorListener(getConfig().getStringList("blocked")), this);
	}
	
	private void registerNPCHooks() {
		NoCheatPlusSurvialFlyHandler.init();
	}
	
	@Override
	public void onEnable() {
		setPlugin(this);
		
		this.saveDefaultConfig();
		
		CivLog.init(this);
		BukkitObjects.initialize(this);
		
		//Load World Populators
		BukkitObjects.getWorlds().get(0).getPopulators().add(new TradeGoodPopulator());
		//BukkitObjects.getWorlds().get(0).getPopulators().add(new MobSpawnerPopulator());
		BukkitObjects.getWorlds().get(0).getPopulators().add(new RuinPopulator());
				
		try {
			CivSettings.init(this);
			
			SQL.initialize();
			SQL.initCivObjectTables();
			ChunkCoord.buildWorldList();
			CivGlobal.loadGlobals();
			
			ACManager.init();
			try {
				SLSManager.init();
			} catch (CivException e1) {
				e1.printStackTrace();
			}
			

		} catch (InvalidConfiguration | SQLException | IOException | InvalidConfigurationException | CivException | ClassNotFoundException e) {
			e.printStackTrace();
			setError(true);
			return;
		}
		
		// Init commands
		getCommand("town").setExecutor(new TownCommand());
		getCommand("resident").setExecutor(new ResidentCommand());
		getCommand("dbg").setExecutor(new DebugCommand());
		getCommand("plot").setExecutor(new PlotCommand());
		getCommand("accept").setExecutor(new AcceptCommand());
		getCommand("deny").setExecutor(new DenyCommand());
		getCommand("civ").setExecutor(new CivCommand());
		getCommand("tc").setExecutor(new TownChatCommand());
		getCommand("cc").setExecutor(new CivChatCommand());
		//getCommand("gc").setExecutor(new GlobalChatCommand());
		getCommand("ad").setExecutor(new AdminCommand());
		getCommand("econ").setExecutor(new EconCommand());
		getCommand("pay").setExecutor(new PayCommand());
		getCommand("build").setExecutor(new BuildCommand());
		getCommand("market").setExecutor(new MarketCommand());
		getCommand("select").setExecutor(new SelectCommand());
		getCommand("here").setExecutor(new HereCommand());
		getCommand("camp").setExecutor(new CampCommand());
		getCommand("report").setExecutor(new ReportCommand());
		getCommand("trade").setExecutor(new TradeCommand());
		getCommand("kill").setExecutor(new KillCommand());
		getCommand("team").setExecutor(new TeamCommand());
	
		registerEvents();
		
		if (hasPlugin("NoCheatPlus")) {
			registerNPCHooks();
		} else {
			CivLog.warning("NoCheatPlus not found, not registering NCP hooks. This is fine if you're not using NCP.");
		}
		
		startTimers();

		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(Banker.class).withName("BankerTrait"));
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(GeneralTrait.class).withName("GeneralTrait"));
		CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(TeleportMaster.class).withName("TeleportMasterTrait"));

		for (Entity e : Bukkit.getWorld("world_the_end").getEntities()) {
			e.remove();
		}
				
		//creativeInvPacketManager.init(this);		
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		isDisable = true;
		SQLUpdate.save();
		CivGlobal.saveMaxBankRate();
		CitizensAPI.getNPCRegistry().deregisterAll();
		for (Entity e : Bukkit.getWorld("world_the_end").getEntities()) {
			e.remove();
		}
	}
	
	public boolean hasPlugin(String name) {
		Plugin p;
		p = getServer().getPluginManager().getPlugin(name);
		return (p != null);
	}

	public boolean isError() {
		return isError;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}


	public static JavaPlugin getPlugin() {
		return plugin;
	}


	public static void setPlugin(JavaPlugin plugin) {
		CivCraft.plugin = plugin;
	}
}
