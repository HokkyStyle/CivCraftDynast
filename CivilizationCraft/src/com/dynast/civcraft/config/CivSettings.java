package com.dynast.civcraft.config;

import localize.Localize;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import com.dynast.civcraft.camp.Camp;
import com.dynast.civcraft.endgame.ConfigEndCondition;
import com.dynast.civcraft.exception.CivException;
import com.dynast.civcraft.exception.InvalidConfiguration;
import com.dynast.civcraft.items.units.Unit;
import com.dynast.civcraft.loreenhancements.LoreEnhancement;
import com.dynast.civcraft.lorestorage.LoreCraftableMaterial;
import com.dynast.civcraft.main.CivCraft;
import com.dynast.civcraft.main.CivData;
import com.dynast.civcraft.main.CivGlobal;
import com.dynast.civcraft.main.CivLog;
import com.dynast.civcraft.object.Town;
import com.dynast.civcraft.randomevents.ConfigRandomEvent;
import com.dynast.civcraft.structure.FortifiedWall;
import com.dynast.civcraft.structure.Wall;
import com.dynast.civcraft.template.Template;
import com.dynast.global.perks.Perk;

public class CivSettings {
	
	public static CivCraft plugin;
	public static final long MOB_REMOVE_INTERVAL = 5000;
	/* Number of days that you can remain in debt before an action occurs. */

	//make this configurable.
	public static final int GRACE_DAYS = 3; 
	
	public static final int CIV_DEBT_GRACE_DAYS = 5;
	public static final int CIV_DEBT_SELL_DAYS = 10;
	public static final int CIV_DEBT_TOWN_SELL_DAYS = 15;
	public static final int TOWN_DEBT_GRACE_DAYS = 5;
	public static final int TOWN_DEBT_SELL_DAYS = 10;

	
	/* cached for faster access. */
	//public static float leather_speed;
	//public static float metal_speed;
	public static float T1_leather_speed;
	public static float T2_leather_speed;
	public static float T3_leather_speed;
	public static float T4_leather_speed;
	public static float T1_metal_speed;
	public static float T2_metal_speed;
	public static float T3_metal_speed;
	public static float T4_metal_speed;
	public static float normal_speed;
	public static double highjump;
	
	public static FileConfiguration townConfig; /* town.yml */
	public static Map<Integer, ConfigTownLevel> townLevels = new HashMap<>();
	public static Map<String, ConfigTownUpgrade> townUpgrades = new TreeMap<>();
	
	public static FileConfiguration civConfig; /* civ.yml */
	public static Map<String, ConfigEndCondition> endConditions = new HashMap<>();
	
	public static FileConfiguration cultureConfig; /* culture.yml */
	public static Map<Integer, ConfigCultureLevel> cultureLevels = new HashMap<>();
	private static Map<String, ConfigCultureBiomeInfo> cultureBiomes = new HashMap<>();

	public static FileConfiguration structureConfig; /* structures.yml */
	public static Map<String, ConfigBuildableInfo> structures = new HashMap<>();
	public static Map<Integer, ConfigGrocerLevel> grocerLevels = new HashMap<>();
	public static Map<Integer, ConfigCottageLevel> cottageLevels = new HashMap<>();
	public static Map<Integer, ConfigMineLevel> mineLevels = new HashMap<>();
	public static Map<Integer, ConfigSawmillLevel> sawmillLevels = new HashMap<>();
	public static Map<Integer, ConfigFactoryLevel> factoryLevels = new HashMap<>();
	public static Map<Integer, ConfigUniverLevel> univerLevels = new HashMap<>();
	public static Map<Integer, ConfigPastureLevel> pastureLevels = new HashMap<Integer, ConfigPastureLevel>();
	public static Map<Integer, ConfigTempleLevel> templeLevels = new HashMap<>();
	public static Map<Integer, ConfigTradeShipLevel> tradeShipLevels = new HashMap<>();
	
	public static FileConfiguration wonderConfig; /* wonders.yml */
	public static Map<String, ConfigBuildableInfo> wonders = new HashMap<>();
	public static Map<String, ConfigWonderBuff> wonderBuffs = new HashMap<>();
	
	public static FileConfiguration techsConfig; /* techs.yml */
	public static Map<String, ConfigTech> techs = new HashMap<>();
	public static Map<Integer, ConfigTechItem> techItems = new HashMap<>();
	public static Map<String, ConfigTechPotion> techPotions = new HashMap<>();
	
	public static FileConfiguration spawnersConfig; /* spawners.yml */
	public static Map<String, ConfigMobSpawner> spawners = new HashMap<>();
	public static Map<String, ConfigMobSpawner> landSpawners = new HashMap<>();
	public static Map<String, ConfigMobSpawner> waterSpawners = new HashMap<>();

	public static FileConfiguration goodsConfig; /* goods.yml */
	public static Map<String, ConfigTradeGood> goods = new HashMap<>();
	public static Map<String, ConfigTradeGood> landGoods = new HashMap<>();
	public static Map<String, ConfigTradeGood> waterGoods = new HashMap<>();
	public static Map<String, ConfigHemisphere> hemispheres = new HashMap<>();

	public static FileConfiguration buffConfig;
	public static Map<String, ConfigBuff> buffs = new HashMap<>();
	
	public static FileConfiguration unitConfig;
	public static Map<String, ConfigUnit> units = new HashMap<>();
	
	public static FileConfiguration espionageConfig;
	public static Map<String, ConfigMission> missions = new HashMap<>();
	
	public static FileConfiguration governmentConfig; /* governments.yml */
	public static Map<String, ConfigGovernment> governments = new HashMap<>();
	
	public static FileConfiguration publicinstConfig;
	public static Map<String, ConfigPublicInstitution> publicinsts = new HashMap<>();
	
	public static HashSet<Material> switchItems = new HashSet<>();
	public static Map<Material, Integer> restrictedItems = new HashMap<>();
	public static Map<Material, Integer> blockPlaceExceptions = new HashMap<>();
	public static Map<EntityType, Integer> restrictedSpawns = new HashMap<>();
	public static HashSet<EntityType> playerEntityWeapons = new HashSet<>();
	public static HashSet<Integer> alwaysCrumble = new HashSet<>();
	
	public static FileConfiguration warConfig; /* war.yml */
	
	public static FileConfiguration scoreConfig; /* score.yml */
	
	public static FileConfiguration perkConfig; /* perks.yml */
	public static Map<String, ConfigPerk> perks = new HashMap<>();
	public static Map<String, ConfigPerk> templates = new HashMap<>();

	public static FileConfiguration enchantConfig; /* enchantments.yml */
	public static Map<String, ConfigEnchant> enchants = new HashMap<>();
	public static float speedtoe_speed;
	public static double speedtoe_consume;
	public static int thorhammerchance;
	public static int punchoutchance;
	
	public static FileConfiguration campConfig; /* camp.yml */
	public static Map<Integer, ConfigCampLonghouseLevel> longhouseLevels = new HashMap<>();
	public static Map<String, ConfigCampUpgrade> campUpgrades = new HashMap<>();
	
	public static FileConfiguration marketConfig; /* market.yml */
	public static Map<Integer, ConfigMarketItem> marketItems = new HashMap<>();
	
	public static Set<ConfigStableItem> stableItems = new HashSet<>();
	public static HashMap<Integer, ConfigStableHorse> horses = new HashMap<>();
	
	public static FileConfiguration happinessConfig; /* happiness.yml */
	public static HashMap<Integer, ConfigTownHappinessLevel> townHappinessLevels = new HashMap<>();
	public static HashMap<Integer, ConfigHappinessState> happinessStates = new HashMap<>();
	
	public static FileConfiguration materialsConfig; /* materials.yml */
	public static HashMap<String, ConfigMaterial> materials = new HashMap<>();
	
	public static FileConfiguration randomEventsConfig; /* randomevents.yml */
	public static HashMap<String, ConfigRandomEvent> randomEvents = new HashMap<>();
	public static ArrayList<String> randomEventIDs = new ArrayList<>();
	
	public static FileConfiguration nocheatConfig; /* nocheatConfig.yml */
	public static HashMap<String, ConfigValidMod> validMods = new HashMap<>();
	
	public static FileConfiguration arenaConfig; /* arenas.yml */
	public static HashMap<String, ConfigArena> arenas = new HashMap<>();
	
	public static FileConfiguration fishingConfig; /* fishing.yml */
	public static ArrayList<ConfigFishing> fishingDrops = new ArrayList<>();
		
	public static double iron_rate;
	public static double gold_rate;
	public static double diamond_rate;
	public static double emerald_rate;
	public static double startingCoins;
	
	public static ArrayList<String> kitItems = new ArrayList<>();
	public static HashMap<Integer, ConfigRemovedRecipes> removedRecipies = new HashMap<>();
	public static HashSet<Material> restrictedUndoBlocks = new HashSet<>();
	public static boolean hasVanishNoPacket = false;
	
	public static final String MINI_ADMIN = "civ.admin";
	public static final String HACKER = "civ.hacker";
	public static final String MODERATOR = "civ.moderator";
	public static final String FREE_PERKS = "civ.freeperks";
	public static final String ECON = "civ.econ";
	public static final String TPALLY = "civ.tp.ally";
	public static final String TPNEUTRAL = "civ.tp.neutral";
	public static final String TPHOSTILE = "civ.tp.hostile";
	public static final String TPWAR = "civ.tp.war";
	public static final String TPPEACE = "civ.tp.peace";
	public static final String TPCAMP = "civ.tp.camp";
	public static final String TPALL = "civ.tp.*";
	public static final int MARKET_COIN_STEP = 5;
	public static final int MARKET_BUYSELL_COIN_DIFF = 30;
	public static final int MARKET_STEP_THRESHOLD = 2;
	public static String CURRENCY_NAME;
	
	public static Localize localize;

	public static boolean hasTitleAPI = false;
	public static boolean hasITag = false;

	public static boolean hasCustomMobs = false;

	public static Material previewMaterial = Material.GLASS;
	public static Boolean showPreview = true;
	
	public static Map<Integer, ConfigJobLevels> minerLevels = new HashMap<>();
	public static Map<Integer, ConfigJobLevels> woodcutterLevels = new HashMap<>();
	public static Map<Integer, ConfigJobLevels> diggerLevels = new HashMap<>();
	public static Map<Integer, ConfigJobLevels> fishermanLevels = new HashMap<>();
	public static Map<Integer, ConfigJobLevels> farmerLevels = new HashMap<>();
	public static Map<Integer, ConfigJobLevels> hunterLevels = new HashMap<>();

	public static FileConfiguration ruinCfg;
	public static Map<String, ConfigRuin> ruins = new HashMap<>();
	
	public static void init(JavaPlugin plugin) throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		CivSettings.plugin = (CivCraft)plugin;
		

		String languageFile = CivSettings.getStringBase("localization_file");
		localize = new Localize(plugin, languageFile);


		CivLog.debug(localize.localizedString("welcome_string","test",1337,100.50));
		CURRENCY_NAME = localize.localizedString("civ_currencyName");
		CivGlobal.fullMessage = CivSettings.localize.localizedString("civGlobal_serverFullMsg");
		
		// Check for required data folder, if it's not there export it.
		CivSettings.validateFiles();
		
		initRestrictedItems();
		initRestrictedUndoBlocks();
		initSwitchItems();
		initRestrictedSpawns();
		initBlockPlaceExceptions();
		initPlayerEntityWeapons();
		
		loadConfigFiles();
		loadConfigObjects();
		
		Perk.init();
		Unit.init();
		
		//CivSettings.leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.leather_speed");
		//CivSettings.metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.metal_speed");
		CivSettings.T1_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T1_leather_speed");
		CivSettings.T2_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T2_leather_speed");
		CivSettings.T3_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T3_leather_speed");
		CivSettings.T4_leather_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T4_leather_speed");
		CivSettings.T1_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T1_metal_speed");
		CivSettings.T2_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T2_metal_speed");
		CivSettings.T3_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T3_metal_speed");
		CivSettings.T4_metal_speed = (float)CivSettings.getDouble(CivSettings.unitConfig, "base.T4_metal_speed");
		CivSettings.normal_speed = 0.2f;	
		
		for (Object obj : civConfig.getList("global.start_kit")) {
			if (obj instanceof String) {
				kitItems.add((String)obj);
			}
		}
		
		
		CivGlobal.banWords.add("fuck");
		CivGlobal.banWords.add("shit");
		CivGlobal.banWords.add("nigger");
		CivGlobal.banWords.add("faggot");
		CivGlobal.banWords.add("gay");
		CivGlobal.banWords.add("rape");
		CivGlobal.banWords.add("http");
		CivGlobal.banWords.add("cunt");
		
		iron_rate = CivSettings.getDouble(civConfig, "ore_rates.iron");
		gold_rate = CivSettings.getDouble(civConfig, "ore_rates.gold");
		diamond_rate = CivSettings.getDouble(civConfig, "ore_rates.diamond");
		emerald_rate = CivSettings.getDouble(civConfig, "ore_rates.emerald");
		startingCoins = CivSettings.getDouble(civConfig, "global.starting_coins");
		
		alwaysCrumble.add(CivData.BEDROCK);
		alwaysCrumble.add(CivData.COAL_BLOCK);
		alwaysCrumble.add(CivData.EMERALD_BLOCK);
		alwaysCrumble.add(CivData.LAPIS_BLOCK);
		alwaysCrumble.add(CivData.SPONGE);
		alwaysCrumble.add(CivData.HAY_BALE);
		alwaysCrumble.add(CivData.GOLD_BLOCK);
		alwaysCrumble.add(CivData.DIAMOND_BLOCK);
		alwaysCrumble.add(CivData.IRON_BLOCK);
		alwaysCrumble.add(CivData.REDSTONE_BLOCK);
		alwaysCrumble.add(CivData.ENDER_CHEST);
		alwaysCrumble.add(CivData.BEACON);
		alwaysCrumble.add(CivData.SHULKER_BOX);
		alwaysCrumble.add(220);
		alwaysCrumble.add(221);
		alwaysCrumble.add(222);
		alwaysCrumble.add(223);
		alwaysCrumble.add(224);
		alwaysCrumble.add(225);
		alwaysCrumble.add(226);
		alwaysCrumble.add(227);
		alwaysCrumble.add(228);
		alwaysCrumble.add(229);
		alwaysCrumble.add(230);
		alwaysCrumble.add(231);
		alwaysCrumble.add(232);
		alwaysCrumble.add(233);
		alwaysCrumble.add(234);
		
		LoreEnhancement.init();
		LoreCraftableMaterial.buildStaticMaterials();
		LoreCraftableMaterial.buildRecipes();
		Template.initAttachableTypes();
		
		if (CivSettings.plugin.hasPlugin("VanishNoPacket")) {
			hasVanishNoPacket = true;
		} else {
			CivLog.warning("VanishNoPacket not found, not registering VanishNoPacket hooks. This is fine if you're not using VanishNoPacket.");
		}
		
		if (CivSettings.plugin.hasPlugin("TitleAPI")) {
			hasTitleAPI = true;
		} else {
			CivLog.warning("TitleAPI not found, not registering TitleAPI hooks. This is fine if you're not using TitleAPI.");
		}
		
		if (CivSettings.plugin.hasPlugin("CustomMobs") && CivSettings.getBoolean(spawnersConfig, "enable")) {
			hasCustomMobs = true;
		} else {
			CivLog.warning("CustomMobs not found or disabled, not registering CustomMob hooks. This is fine if you're not using Custom Mobs.");
		}
		
		try {
			String materialName = CivSettings.getString(structureConfig, "previewBlock");
			previewMaterial = Material.getMaterial(materialName);
		} catch (InvalidConfiguration e) {
			CivLog.warning("Unable to change Preview Block. Defaulting to Glass.");
		}

		try {
			showPreview = CivSettings.getBoolean(structureConfig, "shouldShowPreview");
		} catch (InvalidConfiguration e) {
			CivLog.warning("Unable to change Structure Preview Settings. Defaulting to True.");
		}

	}
	
	private static void initRestrictedUndoBlocks() {
		restrictedUndoBlocks.add(Material.LEGACY_CROPS);
		restrictedUndoBlocks.add(Material.CARROT);
		restrictedUndoBlocks.add(Material.POTATO);
		restrictedUndoBlocks.add(Material.REDSTONE);
		restrictedUndoBlocks.add(Material.REDSTONE_WIRE);
		restrictedUndoBlocks.add(Material.valueOf("LEGACY_REDSTONE_TORCH_OFF"));
		restrictedUndoBlocks.add(Material.valueOf("LEGACY_REDSTONE_TORCH_ON"));
		restrictedUndoBlocks.add(Material.REPEATER);
		restrictedUndoBlocks.add(Material.COMPARATOR);
		restrictedUndoBlocks.add(Material.STRING);
		restrictedUndoBlocks.add(Material.TRIPWIRE);
		restrictedUndoBlocks.add(Material.SUGAR_CANE);
		restrictedUndoBlocks.add(Material.BEETROOT_SEEDS);
		restrictedUndoBlocks.add(Material.TALL_GRASS);
		restrictedUndoBlocks.add(Material.POPPY);
		restrictedUndoBlocks.add(Material.RED_MUSHROOM);
		restrictedUndoBlocks.add(Material.LARGE_FERN);
		restrictedUndoBlocks.add(Material.CAKE);
		restrictedUndoBlocks.add(Material.CACTUS);
		restrictedUndoBlocks.add(Material.PISTON);
		restrictedUndoBlocks.add(Material.STICKY_PISTON);
		restrictedUndoBlocks.add(Material.TRIPWIRE_HOOK);
		restrictedUndoBlocks.add(Material.OAK_SAPLING);
		restrictedUndoBlocks.add(Material.SPRUCE_SAPLING);
		restrictedUndoBlocks.add(Material.BIRCH_SAPLING);
		restrictedUndoBlocks.add(Material.JUNGLE_SAPLING);
		restrictedUndoBlocks.add(Material.ACACIA_SAPLING);
		restrictedUndoBlocks.add(Material.DARK_OAK_SAPLING);
		restrictedUndoBlocks.add(Material.PUMPKIN_STEM);
		restrictedUndoBlocks.add(Material.MELON_STEM);
		
	}

	private static void initPlayerEntityWeapons() {
		playerEntityWeapons.add(EntityType.PLAYER);
		playerEntityWeapons.add(EntityType.ARROW);
		playerEntityWeapons.add(EntityType.SPECTRAL_ARROW);
		playerEntityWeapons.add(EntityType.TIPPED_ARROW);
		playerEntityWeapons.add(EntityType.EGG);
		playerEntityWeapons.add(EntityType.SNOWBALL);
		playerEntityWeapons.add(EntityType.SPLASH_POTION);
		playerEntityWeapons.add(EntityType.LINGERING_POTION);
		playerEntityWeapons.add(EntityType.FISHING_HOOK);
	}
	
	public static void validateFiles() {
//		if (plugin == null) {
//			CivLog.debug("null plugin");
//		}
//		
//		if (plugin.getDataFolder() == null) {
//			CivLog.debug("null data folder");
//		}
//		
//		if (plugin.getDataFolder().getPath() == null) {
//			CivLog.debug("path null");
//		}
		File data = new File(plugin.getDataFolder().getPath()+"/data");
		if (!data.exists()) {
			data.mkdirs();
		}
//		
	}
	
	public static void streamResourceToDisk(String filepath) throws IOException {
		URL inputUrl = plugin.getClass().getResource(filepath);
		File dest = new File(plugin.getDataFolder().getPath()+filepath);
		if (inputUrl == null) {
			CivLog.error("Destination is null: "+filepath);
		}
		FileUtils.copyURLToFile(inputUrl, dest);
	}

	public static FileConfiguration loadCivConfig(String filepath) throws FileNotFoundException, IOException, InvalidConfigurationException {

		File file = new File(plugin.getDataFolder().getPath()+"/data/"+filepath);
		if (!file.exists()) {
			CivLog.warning("Configuration file:"+filepath+" was missing. Streaming to disk from Jar.");
			streamResourceToDisk("/data/"+filepath);
		}
		
		CivLog.info("Loading Configuration file:"+filepath);
		// read the config.yml into memory
		YamlConfiguration cfg = new YamlConfiguration(); 
		cfg.load(file);
		return cfg;
	}
	
	public static void reloadGovConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration
	{
		CivSettings.governments.clear();
		governmentConfig = loadCivConfig("governments.yml");
		ConfigGovernment.loadConfig(governmentConfig, governments);
	}
		
	private static void loadConfigFiles() throws FileNotFoundException, IOException, InvalidConfigurationException {
		townConfig = loadCivConfig("town.yml");
		civConfig = loadCivConfig("civ.yml");
		cultureConfig = loadCivConfig("culture.yml");
		structureConfig = loadCivConfig("structures.yml");
		techsConfig = loadCivConfig("techs.yml");
		goodsConfig = loadCivConfig("goods.yml");
		spawnersConfig = loadCivConfig("spawners.yml");
		buffConfig = loadCivConfig("buffs.yml");
		governmentConfig = loadCivConfig("governments.yml");
		publicinstConfig = loadCivConfig("publicinsts.yml");
		warConfig = loadCivConfig("war.yml");
		wonderConfig = loadCivConfig("wonders.yml");
		unitConfig = loadCivConfig("units.yml");
		espionageConfig = loadCivConfig("espionage.yml");
		scoreConfig = loadCivConfig("score.yml");
		perkConfig = loadCivConfig("perks.yml");
		enchantConfig = loadCivConfig("enchantments.yml");
		campConfig = loadCivConfig("camp.yml");
		marketConfig = loadCivConfig("market.yml");
		happinessConfig = loadCivConfig("happiness.yml");
		materialsConfig = loadCivConfig("materials.yml");
		randomEventsConfig = loadCivConfig("randomevents.yml");
		nocheatConfig = loadCivConfig("nocheat.yml");
		arenaConfig = loadCivConfig("arena.yml");
		fishingConfig = loadCivConfig("fishing.yml");
		ruinCfg = loadCivConfig("ruins.yml");
	}
	
	public static void reloadPerks() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {
		perkConfig = loadCivConfig("perks.yml");
		ConfigPerk.loadConfig(perkConfig, perks);
		ConfigPerk.loadTemplates(perkConfig, templates);
	}
	
	public static void reloadNoCheat() throws FileNotFoundException, IOException, InvalidConfigurationException, InvalidConfiguration {

		nocheatConfig = loadCivConfig("nocheat.yml");
		ConfigValidMod.loadConfig(nocheatConfig, validMods);
	}

	private static void loadConfigObjects() throws InvalidConfiguration {
		ConfigTownLevel.loadConfig(townConfig, townLevels);
		ConfigTownUpgrade.loadConfig(townConfig, townUpgrades);
		ConfigCultureLevel.loadConfig(cultureConfig, cultureLevels);
		ConfigBuildableInfo.loadConfig(structureConfig, "structures", structures, false);
		ConfigBuildableInfo.loadConfig(wonderConfig, "wonders", wonders, true);
		ConfigTech.loadConfig(techsConfig, techs);
		ConfigTechItem.loadConfig(techsConfig, techItems);
		ConfigTechPotion.loadConfig(techsConfig, techPotions);
		ConfigHemisphere.loadConfig(goodsConfig, hemispheres);
		ConfigBuff.loadConfig(buffConfig, buffs);
		ConfigWonderBuff.loadConfig(wonderConfig, wonderBuffs);
		ConfigMobSpawner.loadConfig(spawnersConfig, spawners, landSpawners, waterSpawners);
		ConfigTradeGood.loadConfig(goodsConfig, goods, landGoods, waterGoods);
		ConfigGrocerLevel.loadConfig(structureConfig, grocerLevels);
		ConfigCottageLevel.loadConfig(structureConfig, cottageLevels);
		ConfigTempleLevel.loadConfig(structureConfig, templeLevels);
		ConfigMineLevel.loadConfig(structureConfig, mineLevels);
		ConfigSawmillLevel.loadConfig(structureConfig, sawmillLevels);
		ConfigFactoryLevel.loadConfig(structureConfig, factoryLevels);
		ConfigUniverLevel.loadConfig(structureConfig, univerLevels);
		ConfigPastureLevel.loadConfig(structureConfig, pastureLevels);
		ConfigGovernment.loadConfig(governmentConfig, governments);
		ConfigPublicInstitution.loadConfig(publicinstConfig, publicinsts);
		ConfigEnchant.loadConfig(enchantConfig, enchants);
		ConfigUnit.loadConfig(unitConfig, units);
		ConfigMission.loadConfig(espionageConfig, missions);
		ConfigPerk.loadConfig(perkConfig, perks);
		ConfigPerk.loadTemplates(perkConfig, templates);
		ConfigCampLonghouseLevel.loadConfig(campConfig, longhouseLevels);
		ConfigCampUpgrade.loadConfig(campConfig, campUpgrades);
		ConfigMarketItem.loadConfig(marketConfig, marketItems);
		ConfigStableItem.loadConfig(structureConfig, stableItems);
		ConfigStableHorse.loadConfig(structureConfig, horses);
		ConfigTownHappinessLevel.loadConfig(happinessConfig, townHappinessLevels);
		ConfigHappinessState.loadConfig(happinessConfig, happinessStates);
		ConfigCultureBiomeInfo.loadConfig(cultureConfig, cultureBiomes);
		ConfigMaterial.loadConfig(materialsConfig, materials);
		ConfigRandomEvent.loadConfig(randomEventsConfig, randomEvents, randomEventIDs);
		ConfigEndCondition.loadConfig(civConfig, endConditions);
		ConfigValidMod.loadConfig(nocheatConfig, validMods);
		ConfigArena.loadConfig(arenaConfig, arenas);
		ConfigFishing.loadConfig(fishingConfig, fishingDrops);
		ConfigTradeShipLevel.loadConfig(structureConfig, tradeShipLevels);
		ConfigJobLevels.loadConfig(civConfig, minerLevels, woodcutterLevels, diggerLevels, fishermanLevels, farmerLevels, hunterLevels);

		ConfigRuin.loadConfig(ruinCfg, ruins);
	
		ConfigRemovedRecipes.removeRecipes(materialsConfig, removedRecipies);

		CivGlobal.ruinPreGenerator.preGenerate();
		CivGlobal.tradeGoodPreGenerator.preGenerate();
		//CivGlobal.mobSpawnerPreGenerator.preGenerate();

		Wall.init_settings();
		FortifiedWall.init_settings();
	}

	private static void initRestrictedSpawns() {
		restrictedSpawns.put(EntityType.BLAZE, 0);
		restrictedSpawns.put(EntityType.CAVE_SPIDER, 0);
		restrictedSpawns.put(EntityType.CREEPER, 0);
		restrictedSpawns.put(EntityType.ENDER_DRAGON, 0);
		restrictedSpawns.put(EntityType.ENDERMAN, 0);
		restrictedSpawns.put(EntityType.GHAST, 0);
		restrictedSpawns.put(EntityType.GIANT, 0);
		restrictedSpawns.put(EntityType.PIG_ZOMBIE, 0);
		restrictedSpawns.put(EntityType.SILVERFISH, 0);
		restrictedSpawns.put(EntityType.SKELETON, 0);
		restrictedSpawns.put(EntityType.SLIME, 0);
		restrictedSpawns.put(EntityType.SPIDER, 0);
		restrictedSpawns.put(EntityType.WITCH, 0);
		restrictedSpawns.put(EntityType.WITHER, 0);
		restrictedSpawns.put(EntityType.ZOMBIE, 0);
		restrictedSpawns.put(EntityType.BAT, 0);
		restrictedSpawns.put(EntityType.ENDERMITE, 0);
		restrictedSpawns.put(EntityType.GUARDIAN, 0);
		restrictedSpawns.put(EntityType.HUSK, 0);
		restrictedSpawns.put(EntityType.STRAY, 0);
		restrictedSpawns.put(EntityType.ZOMBIE_VILLAGER, 0);
	}
	
	private static void initRestrictedItems() {
		// TODO make this configurable? 
		restrictedItems.put(Material.FLINT_AND_STEEL, 0);
		restrictedItems.put(Material.BUCKET, 0);
		restrictedItems.put(Material.WATER_BUCKET, 0);
		restrictedItems.put(Material.LAVA_BUCKET, 0);
		restrictedItems.put(Material.CAKE, 0);
		restrictedItems.put(Material.CAULDRON, 0);
		restrictedItems.put(Material.REPEATER, 0);
		restrictedItems.put(Material.INK_SAC, 0);
		restrictedItems.put(Material.ITEM_FRAME, 0);
		restrictedItems.put(Material.PAINTING, 0);
		restrictedItems.put(Material.SHEARS, 0);
		restrictedItems.put(Material.LAVA, 0);
		restrictedItems.put(Material.WATER, 0);
		restrictedItems.put(Material.TNT, 0);
	}

	private static void initSwitchItems() {
		//TODO make this configurable?
		switchItems.add(Material.ANVIL);
		switchItems.add(Material.BEACON);
		switchItems.add(Material.BREWING_STAND);
		switchItems.add(Material.FURNACE);
		switchItems.add(Material.CAKE);
		switchItems.add(Material.CAULDRON);
		switchItems.add(Material.CHEST);
		switchItems.add(Material.COMMAND_BLOCK);
		switchItems.add(Material.REPEATER);
		switchItems.add(Material.DISPENSER);
		switchItems.add(Material.OAK_FENCE_GATE);
		switchItems.add(Material.JUKEBOX);
		switchItems.add(Material.LEVER);
	//	switchItems.add(Material.LOCKED_CHEST);
		switchItems.add(Material.STONE_BUTTON);
		switchItems.add(Material.STONE_PLATE);
		switchItems.add(Material.IRON_DOOR);
		switchItems.add(Material.TNT);
		switchItems.add(Material.OAK_TRAPDOOR);
		switchItems.add(Material.SPRUCE_TRAPDOOR);
		switchItems.add(Material.BIRCH_TRAPDOOR);
		switchItems.add(Material.JUNGLE_TRAPDOOR);
		switchItems.add(Material.ACACIA_TRAPDOOR);
		switchItems.add(Material.DARK_OAK_TRAPDOOR);
		switchItems.add(Material.OAK_DOOR);
		switchItems.add(Material.OAK_PRESSURE_PLATE);
		switchItems.add(Material.SPRUCE_PRESSURE_PLATE);
		switchItems.add(Material.BIRCH_PRESSURE_PLATE);
		switchItems.add(Material.JUNGLE_PRESSURE_PLATE);
		switchItems.add(Material.ACACIA_PRESSURE_PLATE);
		switchItems.add(Material.DARK_OAK_PRESSURE_PLATE);
		switchItems.add(Material.STONE_PRESSURE_PLATE);
		//switchItems.put(Material.WOOD_BUTTON, 0); //intentionally left out
		
		// 1.5 additions.
		switchItems.add(Material.HOPPER);
		switchItems.add(Material.HOPPER_MINECART);
		switchItems.add(Material.DROPPER);
		switchItems.add(Material.COMPARATOR);
		switchItems.add(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
		switchItems.add(Material.HEAVY_WEIGHTED_PRESSURE_PLATE );
		switchItems.add(Material.IRON_TRAPDOOR);
		
		// 1.6 additions.
		switchItems.add(Material.SPRUCE_DOOR);
		switchItems.add(Material.BIRCH_DOOR);
		switchItems.add(Material.JUNGLE_DOOR);
		switchItems.add(Material.ACACIA_DOOR);
		switchItems.add(Material.DARK_OAK_DOOR);
		
		// 1.7 additions
		switchItems.add(Material.ACACIA_FENCE_GATE);
		switchItems.add(Material.BIRCH_FENCE_GATE);
		switchItems.add(Material.DARK_OAK_FENCE_GATE);
		switchItems.add(Material.SPRUCE_FENCE_GATE);
		switchItems.add(Material.JUNGLE_FENCE_GATE);
	}
	
	private static void initBlockPlaceExceptions() {
		/* These blocks can be placed regardless of permissions.
		 * this is currently used only for blocks that are generated
		 * by specific events such as portal or fire creation.
		 */
		blockPlaceExceptions.put(Material.FIRE, 0);
		blockPlaceExceptions.put(Material.NETHER_PORTAL, 0);
	}
	
	public static String getStringBase(String path) throws InvalidConfiguration {
		return getString(plugin.getConfig(), path);
	}
	
	public static double getDoubleTown(String path) throws InvalidConfiguration {
		return getDouble(townConfig, path);
	}
	
	public static double getDoubleCiv(String path) throws InvalidConfiguration {
		return getDouble(civConfig, path);
	}
	
	public static void saveGenID(String gen_id) {
		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/CivCraft/genid.data")));
			writer.write(gen_id);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getGenID() {
		String genid = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader("plugins/CivCraft/genid.data"));
			genid = br.readLine();
			br.close();
		} catch (IOException e) {
		}
		return genid;
	}
	
	public static Double getDoubleStructure(String path) {
		Double ret;
		try {
			ret = getDouble(structureConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0.0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Boolean getBooleanStructure(String path) {
		Boolean ret;
		try {
			ret = getBoolean(structureConfig, path);
		} catch (InvalidConfiguration e) {
			ret = false;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static int getIntegerStructure(String path) {
		Integer ret;
		try {
			ret = getInteger(structureConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Integer getIntegerGovernment(String path) {
		Integer ret;
		try {
			ret = getInteger(governmentConfig, path);
		} catch (InvalidConfiguration e) {
			ret = 0;
			e.printStackTrace();
		}
		return ret;
	}
	
	public static Integer getInteger(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration integer "+path);
		}
		
		int data = cfg.getInt(path);
		return data;
	}

	public static String getString(FileConfiguration cfg, String path) throws InvalidConfiguration {
		String data = cfg.getString(path);
		if (data == null) {
			throw new InvalidConfiguration("Could not get configuration string "+path);
		}
		return data;
	}
	
	public static double getDouble(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration double "+path);
		}
		
		double data = cfg.getDouble(path);
		return data;
	}
	
	public static boolean getBoolean(FileConfiguration cfg, String path) throws InvalidConfiguration {
		if (!cfg.contains(path)) {
			throw new InvalidConfiguration("Could not get configuration boolean "+path);
		}
		
		boolean data = cfg.getBoolean(path);
		return data;
	}

	public static int getMaxNameLength() {
		// TODO make this configurable?
		return 32;
	}

	public static String getNameCheckRegex() throws InvalidConfiguration {
		return getStringBase("regex.name_check_regex");
	}

	public static String getNameFilterRegex() throws InvalidConfiguration {
		return getStringBase("regex.name_filter_regex");
	}

	public static String getNameRemoveRegex() throws InvalidConfiguration {
		return getStringBase("regex.name_remove_regex");
	}

	public static ConfigTownUpgrade getUpgradeByName(String name) {
		for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
			if (upgrade.name.equalsIgnoreCase(name)) {
				return upgrade;
			}
		}
		return null;
	}

	public static ConfigHappinessState getHappinessState(double amount) {
		ConfigHappinessState closestState = happinessStates.get(0);
		
		for (int i = 0; i < happinessStates.size(); i++) {
			ConfigHappinessState state = happinessStates.get(i);
			amount = (double) Math.round(amount * 100) / 100;
			if (amount >= state.amount) {
				closestState = state;
			}
		}
		
		return closestState;
	}
	
	public static ConfigTownUpgrade getUpgradeByNameRegex(Town town, String name) throws CivException {
		ConfigTownUpgrade returnUpgrade = null;
		for (ConfigTownUpgrade upgrade : townUpgrades.values()) {
			if (!upgrade.isAvailable(town)) {
				continue;
			}
			
			if (name.equalsIgnoreCase(upgrade.name)) {
				return upgrade;
			}
			
			String loweredUpgradeName = upgrade.name.toLowerCase();
			String loweredName = name.toLowerCase();
			
			if (loweredUpgradeName.contains(loweredName)) {
				if (returnUpgrade == null) {
					returnUpgrade = upgrade;
				} else {
					throw new CivException(CivSettings.localize.localizedString("var_cmd_notSpecificUpgrade",name));
				}
			}
		}
		return returnUpgrade;
	}
	
	public static ConfigCampUpgrade getCampUpgradeByNameRegex(Camp camp, String name) throws CivException {
		ConfigCampUpgrade returnUpgrade = null;
		for (ConfigCampUpgrade upgrade : campUpgrades.values()) {
			if (!upgrade.isAvailable(camp)) {
				continue;
			}
			
			if (name.equalsIgnoreCase(upgrade.name)) {
				return upgrade;
			}
			
			String loweredUpgradeName = upgrade.name.toLowerCase();
			String loweredName = name.toLowerCase();
			
			if (loweredUpgradeName.contains(loweredName)) {
				if (returnUpgrade == null) {
					returnUpgrade = upgrade;
				} else {
					throw new CivException(CivSettings.localize.localizedString("var_cmd_notSpecificUpgrade",name));
				}
			}
		}
		return returnUpgrade;
	}
	
	public static ConfigBuildableInfo getBuildableInfoByName(String fullArgs) {
		for (ConfigBuildableInfo sinfo : structures.values()) {
			if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
				return sinfo;
			}
		}
		
		for (ConfigBuildableInfo sinfo : wonders.values()) {
			if (sinfo.displayName.equalsIgnoreCase(fullArgs)) {
				return sinfo;
			}
		}
		
		return null;
	}

	public static ConfigTech getTechByName(String techname) {
		for (ConfigTech tech : techs.values()) {
			if (tech.name.equalsIgnoreCase(techname)) {
				return tech;
			}
		}
		return null;
	}
	
	public static ConfigPublicInstitution getInstByName(String instname) {
		for (ConfigPublicInstitution inst : publicinsts.values()) {
			if (inst.displayName.equalsIgnoreCase(instname)) {
				return inst;
			}
		}
		return null;
	}

	public static int getCottageMaxLevel() {
		int returnLevel = 0;
		for (Integer level : cottageLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
	}
	
	public static int getTempleMaxLevel() {
		int returnLevel = 0;
		for (Integer level : templeLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		return returnLevel;
	}

	public static int getMineMaxLevel() {
		int returnLevel = 0;
		for (Integer level : mineLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
	}
	
	public static int getSawmillMaxLevel() {
		int returnLevel = 0;
		for (Integer level : sawmillLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
	}
	
	public static int getUniverMaxLevel() {
		int returnLevel = 0;
		for (Integer level : univerLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
	}

	public static int getPastureMaxLevel() {
		int returnLevel = 0;
		for (Integer level : pastureLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}

		return returnLevel;
	}
	
	public static int getFactoryMaxLevel() {
		int returnLevel = 0;
		for (Integer level : factoryLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
	}
	
	
	public static int getMaxCultureLevel() {
		int returnLevel = 0;
		for (Integer level : cultureLevels.keySet()) {
			if (returnLevel < level) {
				returnLevel = level;
			}
		}
		
		return returnLevel;
		
	}
	
	public static int getMaxJobMinerLevel() {
		int retunrnLevel = 0;
		for (Integer level : minerLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobHunterLevel() {
		int retunrnLevel = 0;
		for (Integer level : hunterLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobFarmerLevel() {
		int retunrnLevel = 0;
		for (Integer level : farmerLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobWoodcutterLevel() {
		int retunrnLevel = 0;
		for (Integer level : woodcutterLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobFishermanLevel() {
		int retunrnLevel = 0;
		for (Integer level : fishermanLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobDiggerLevel() {
		int retunrnLevel = 0;
		for (Integer level : diggerLevels.keySet()) {
			if (retunrnLevel < level) {
				retunrnLevel = level;
			}
		}

		return retunrnLevel;
	}

	public static int getMaxJobsLevel() {
		int levels[] = {getMaxJobDiggerLevel(),
						getMaxJobFishermanLevel(),
						getMaxJobMinerLevel(),
						getMaxJobFarmerLevel(),
						getMaxJobWoodcutterLevel(),
						getMaxJobHunterLevel()};


		Arrays.sort(levels);
		return levels[0];
	}

	
	public static ConfigCultureBiomeInfo getCultureBiome(String name) {
		ConfigCultureBiomeInfo biomeInfo = cultureBiomes.get(name);
		if (biomeInfo == null) {
			biomeInfo = cultureBiomes.get("UNKNOWN");
		}
		
		return biomeInfo;
	}
}
