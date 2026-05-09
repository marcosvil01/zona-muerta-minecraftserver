package com.zonamuerta.plugin;

import com.zonamuerta.plugin.CommandHandler;
import com.zonamuerta.plugin.Infected;
import com.zonamuerta.plugin.SafezoneManager;
import com.zonamuerta.plugin.WorldConfig;
import com.zonamuerta.plugin.admin.AdminCommands;
import com.zonamuerta.plugin.admin.AdminManager;
import com.zonamuerta.plugin.database.DatabaseManager;
import com.zonamuerta.plugin.database.PermissionManagerV2;
import com.zonamuerta.plugin.gui.ModernMenuListener;
import com.zonamuerta.plugin.gui.ModernMenuProvider;
import com.zonamuerta.plugin.portals.ModernPortalManager;
import com.zonamuerta.plugin.selection.SelectionListener;
import com.zonamuerta.plugin.selection.SelectionManager;
import com.zonamuerta.plugin.structures.StructureManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ZonaMuerta
extends JavaPlugin
implements Listener {
    private static final String BASE_SPEED = "zombie_stats.base_speed";
    private static final String BASE_DAMAGE = "zombie_stats.base_damage";
    private static final String BASE_MUTATION = "mutation_rates.base";
    private static final String MUTATION_INCREASE = "mutation_rates.daily_increase";
    private static final String SPAWN_TIME_MODE = "zombies.spawn_time_mode";
    private static final int TICKS_PER_DAY = 24000;
    private static final int SENSOR_RANGE = 50;
    public final NamespacedKey zombieTypeKey = new NamespacedKey((Plugin)this, "zombie_type");
    public final NamespacedKey mutationDayKey = new NamespacedKey((Plugin)this, "mutation_day");
    public final NamespacedKey zombieLevelKey = new NamespacedKey((Plugin)this, "zombie_level");
    public final NamespacedKey zombieExpKey = new NamespacedKey((Plugin)this, "zombie_exp");
    private final NamespacedKey leaderKey = new NamespacedKey((Plugin)this, "leader");
    private final NamespacedKey followLeaderKey = new NamespacedKey((Plugin)this, "follow_leader");
    private final NamespacedKey breakingCooldownKey = new NamespacedKey((Plugin)this, "breaking_cooldown");
    private final NamespacedKey curePotionKey = new NamespacedKey((Plugin)this, "cure_potion");
    private final NamespacedKey zombieHeartKey = new NamespacedKey((Plugin)this, "zombie_heart");
    private final NamespacedKey purifiedZombieHeartKey = new NamespacedKey((Plugin)this, "purified_zombie_heart");
    private final NamespacedKey extraHeartsKey = new NamespacedKey((Plugin)this, "extra_hearts");
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private DatabaseManager databaseManager;
    private PermissionManagerV2 permissionManagerV2;
    private AdminManager adminManager;
    private SelectionManager selectionManager;
    private ModernPortalManager portalManager;
    private int currentDay = 0;
    private double currentMutationRate;
    private boolean isBloodMoon = false;
    private final Map<String, Integer> zombieChunkCount = new HashMap<String, Integer>();
    private final Random random = new Random();
    private boolean showMutationNamesAboveZombies;
    private Map<String, Boolean> tankAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> chargerAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> assassinAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> blightAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> bursterAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> spitterAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> frostAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> shriekerAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> cryoAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> infernoAbilities = new HashMap<String, Boolean>();
    private Map<String, Boolean> leechAbilities = new HashMap<String, Boolean>();
    private Map<String, List<ItemStack>> mutationLootDrops = new HashMap<String, List<ItemStack>>();
    private Infected infected;
    private CommandHandler commandHandler;
    private String spawnTimeMode = "both";
    private long memoryDurationMs = 5000L;
    private final Map<UUID, Map<UUID, Long>> zombieTargetMemory = new HashMap<UUID, Map<UUID, Long>>();
    private int sprintNoiseRange = 20;
    private int eatingNoiseRange = 12;
    private int bowNoiseRange = 25;
    private int torchNoiseRange = 30;
    private double leaderChance = 0.3;
    private int packRadius = 15;
    private int followRadius = 10;
    private final Set<Material> breakableBlocks = new HashSet<Material>();
    private final Map<Material, Integer> breakableBlocksCooldown = new HashMap<Material, Integer>();
    private int defaultBreakingCooldownTicks = 60;
    private int breakingRadius = 2;
    private boolean breakingEnabled = true;
    private double maxSpeed;
    private double maxDamage;
    private int maxZombiesPerChunk = 20;
    private int maxZombiesWorldwide = 100;
    private boolean bloodmoonEnabled = true;
    private int bloodmoonFrequency = 5;
    private double bloodMoonDefaultSpawnMultiplier = 2.0;
    private double bloodMoonDefaultMutationMultiplier = 10.0;
    private boolean bloodMoonScheduleEnabled = false;
    private final List<BloodMoonScheduleEntry> bloodMoonSchedule = new ArrayList<BloodMoonScheduleEntry>();
    private double activeBloodMoonSpawnMultiplier = 2.0;
    private double activeBloodMoonMutationMultiplier = 10.0;
    private boolean bloodMoonForced = false;
    private String bloodMoonSource = "none";
    private String activeBloodMoonScheduleId = null;
    private int closestSpawnDistance = 2;
    private int furthestSpawnDistance = 30;
    private List<String> whitelistedWorlds;
    private boolean evolutionEnabled = true;
    private double expPerPassiveMob = 1.0;
    private double expPerPlayerKill = 10.0;
    private int maxLevel = 10;
    private boolean debugMode = false;
    private String currentPreset = "normal";
    private Map<String, WorldConfig> worldConfigs = new HashMap<String, WorldConfig>();
    private SafezoneManager safezoneManager;
    private StructureManager structureManager;
    private MythicBridge mythicBridge;
    private ProgressionManager progressionManager;
    private ClassManager classManager;
    private EconomyManager economyManager;
    private WeaponsManager weaponsManager;
    private AuctionHouse auctionHouse;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private AntiCheatListener antiCheatListener;
    private MenuManager menuManager;
    private SpecsManager specsManager;
    private MonthlyEventManager monthlyEventManager;

    public void onEnable() {
        this.saveDefaultConfig();
        this.loadDebugConfig();
        this.whitelistedWorlds = this.getConfig().getStringList("world-whitelist");
        if (this.whitelistedWorlds.isEmpty()) {
            this.getLogger().warning("No se especificaron mundos en 'world-whitelist' en config.yml. El plugin estara activo en todos los mundos.");
        } else {
            this.getLogger().info("Plugin activo en mundos permitidos: " + String.join((CharSequence)", ", this.whitelistedWorlds));
        }
        this.loadMutationConfig();
        this.loadLootDropsConfig();
        this.loadAbilityConfig();
        this.loadSpawnTimeModeConfig();
        this.loadSmarterTargetingConfig();
        this.loadGroupBehaviorConfig();
        this.loadBlockBreakingConfig();
        this.loadZombieStatCaps();
        this.loadZombieLimitsConfig();
        this.loadBloodmoonConfig();
        this.loadEvolutionConfig();
        this.loadSpawnDistanceConfig();
        this.loadPresetConfig();
        this.loadWorldConfigs();
        this.safezoneManager = new SafezoneManager(this);
        this.safezoneManager.loadSafezones();
        this.structureManager = new StructureManager(this);
        this.structureManager.loadStructuresConfig();
        this.infected = new Infected(this);
        this.infected.loadInfectionConfig();
        this.infected.loadPlayersData();
        this.infected.restoreInfectedPlayers();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)this.infected, (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new SunlightProtectionListener(), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new NoiseLightListener(), (Plugin)this);
        this.getServer().getPluginManager().registerEvents((Listener)new ChunkLoadListener(), (Plugin)this);
        this.startDayCycle();
        this.startBloodMoonScheduleSystem();
        // startSpawningSystem() REMOVIDO - MythicMobs gestiona spawns via RandomSpawns.yml
        // Internally spawned zombies were duplicating MM spawns y creando inconsistencias
        // Blood moon y infection se manejan via MythicBridge y eventos MM
        this.startAIUpdateSystem();
        new BukkitRunnable(){

            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Zombie zombie : world.getEntitiesByClass(Zombie.class)) {
                        if (!ZonaMuerta.this.isManagedZombie((Entity)zombie) || !"leech".equals(zombie.getPersistentDataContainer().get(ZonaMuerta.this.zombieTypeKey, PersistentDataType.STRING)) || !ZonaMuerta.this.leechAbilities.getOrDefault("life_steal_aura", true).booleanValue()) continue;
                        ConfigurationSection cfg = ZonaMuerta.this.getConfig().getConfigurationSection("mutation_types.leech");
                        double auraRadius = cfg.getDouble("leech_aura_radius", 5.0);
                        double auraDamage = cfg.getDouble("leech_aura_damage", 1.0);
                        for (Entity entity : zombie.getNearbyEntities(auraRadius, auraRadius, auraRadius)) {
                            LivingEntity livingEntity;
                            if (!(entity instanceof LivingEntity) || entity.equals(zombie) || !((livingEntity = (LivingEntity)entity) instanceof Player) && !ZonaMuerta.this.isManagedZombie((Entity)livingEntity)) continue;
                            livingEntity.damage(auraDamage, (Entity)zombie);
                            double currentHealth = zombie.getHealth();
                            double maxHealth = zombie.getAttribute(Attribute.MAX_HEALTH).getValue();
                            zombie.setHealth(Math.min(maxHealth, currentHealth + auraDamage));
                        }
                    }
                }
            }
        }.runTaskTimer((Plugin)this, 0L, 20L);
        this.deactivateBloodMoon(false);
        this.currentDay = this.getConfig().getInt("data.current_day", 0);
        this.commandHandler = new CommandHandler(this, this.infected, this.safezoneManager);
        // ── Integración MythicMobs ─────────────────────────────────────────────
        if (this.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            MythicBridge bridge = new MythicBridge(this, this.infected);
            if (bridge.init()) {
                this.mythicBridge = bridge;
                this.getServer().getPluginManager().registerEvents(bridge, this);
                this.getLogger().info("[ZM] MythicMobs bridge activado. Spawn unificado activo.");
            }
        } else {
            this.getLogger().warning("[ZM] MythicMobs no encontrado — bridge desactivado.");
        }
        // ── Nuevos managers de mc_core fusionados ────────────────────────────
        this.databaseManager = new DatabaseManager(this);
        this.permissionManagerV2 = new PermissionManagerV2(this.databaseManager);
        this.selectionManager = new SelectionManager();
        this.adminManager = new AdminManager(this);
        this.portalManager = new ModernPortalManager(this, this.selectionManager);
        this.getServer().getPluginManager().registerEvents(new SelectionListener(this, this.selectionManager), this);
        this.getServer().getPluginManager().registerEvents(new ModernMenuListener(), this);
        this.getServer().getPluginManager().registerEvents(this.adminManager, this);
        this.getServer().getPluginManager().registerEvents(this.portalManager, this);
        this.getCommand("vanish").setExecutor(new AdminCommands(this.adminManager, this));
        this.getCommand("freeze").setExecutor(new AdminCommands(this.adminManager, this));
        this.getCommand("spectate").setExecutor(new AdminCommands(this.adminManager, this));
        // ── Managers de Fase 1 ─────────────────────────────────────────────
        this.progressionManager = new ProgressionManager(this);
        this.classManager = new ClassManager(this);
        this.economyManager = new EconomyManager(this);
        this.auctionHouse = new AuctionHouse(this);
        this.statsManager = new StatsManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.antiCheatListener = new AntiCheatListener(this);
        this.menuManager = new MenuManager(this);
        this.specsManager = new SpecsManager(this);
        this.monthlyEventManager = new MonthlyEventManager(this);
        this.commandHandler.setManagers(this.auctionHouse, this.statsManager, this.scoreboardManager);
        // ── Armas artesanales ─────────────────────────────────────────────────
        this.weaponsManager = new WeaponsManager(this);
        this.weaponsManager.registerRecipes();
        // ── PlaceholderAPI ────────────────────────────────────────────────────
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ZMPlaceholderExpansion(this, this.infected).register();
            this.getLogger().info("[ZM] PlaceholderAPI expansion registrada.");
        }
        this.getLogger().info("Zona Muerta iniciado! Dia " + this.currentDay + ", Spawn Time Mode: " + this.spawnTimeMode + ", Preset: " + this.currentPreset);
    }

    public Map<String, Boolean> getBlightAbilities() {
        return this.blightAbilities;
    }

    public Map<String, Boolean> getFrostAbilities() {
        return this.frostAbilities;
    }

    /** Indica si la Luna de Sangre está activa en este momento. */
    public boolean isBloodMoon() {
        return this.isBloodMoon;
    }

    /** Retorna el día actual de la apocalipsis. */
    public int getCurrentDay() {
        return this.currentDay;
    }

    /** Retorna la tasa de mutación actual. */
    public double getCurrentMutationRate() {
        return this.currentMutationRate;
    }

    /**
     * Premia al jugador con PX de Zona Muerta al matar un mob de MythicMobs.
     * Muestra un mensaje de título breve y registra el progreso.
     */
    public void awardZombieExp(Player player, int amount, String mobType) {
        if (player == null || amount <= 0) return;
        if (this.mythicBridge != null && this.mythicBridge.isMythicAvailable()) {
            this.progressionManager.onMythicMobKill(player, mobType, amount);
        } else {
            player.sendMessage(ChatColor.GOLD + "[ZM] +" + amount + " XP por matar " + ChatColor.WHITE + mobType);
        }
    }

    public ProgressionManager getProgressionManager() { return this.progressionManager; }
    public ClassManager getClassManager() { return this.classManager; }
    public EconomyManager getEconomyManager() { return this.economyManager; }
    public Infected getInfected() { return this.infected; }
    public WeaponsManager getWeaponsManager() { return this.weaponsManager; }
    public AuctionHouse getAuctionHouse() { return this.auctionHouse; }
    public StatsManager getStatsManager() { return this.statsManager; }
    public ScoreboardManager getScoreboardManager() { return this.scoreboardManager; }
    public AntiCheatListener getAntiCheatListener() { return this.antiCheatListener; }
    public MenuManager getMenuManager() { return this.menuManager; }
    public SpecsManager getSpecsManager() { return this.specsManager; }
    public MonthlyEventManager getMonthlyEventManager() { return this.monthlyEventManager; }
    public SafezoneManager getSafezoneManager() { return this.safezoneManager; }
    public StructureManager getStructureManager() { return this.structureManager; }
    public MythicBridge getMythicBridge() { return this.mythicBridge; }
    public AdminManager getAdminManager() { return this.adminManager; }
    public SelectionManager getSelectionManager() { return this.selectionManager; }
    public PermissionManagerV2 getPermissionManagerV2() { return this.permissionManagerV2; }
    public ModernPortalManager getPortalManager() { return this.portalManager; }
    public MiniMessage getMiniMessage() { return this.miniMessage; }
    public static ZonaMuerta getInstance() { return getPlugin(ZonaMuerta.class); }

    public void onDisable() {
        this.infected.savePlayersData();
        this.infected.removeAllBossBars();
        this.safezoneManager.saveSafezones();
        this.structureManager.saveStructuresData();
        this.progressionManager.shutdown();
        this.economyManager.onDisable();
        if (this.auctionHouse != null) this.auctionHouse.onDisable();
        if (this.statsManager != null) this.statsManager.onDisable();
        if (this.databaseManager != null) this.databaseManager.close();
        this.getLogger().info("Zona Muerta apagado correctamente. Todos los cambios guardados.");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return this.commandHandler.onCommand(sender, command, label, args);
    }

    public void loadMutationConfig() {
        FileConfiguration cfg = this.getConfig();
        this.showMutationNamesAboveZombies = cfg.getBoolean("mutation_system.show_mutation_names_above_zombies", true);
    }

    public void loadAbilityConfig() {
        FileConfiguration cfg = this.getConfig();
        this.tankAbilities.clear();
        ConfigurationSection tankAbilitiesSection = cfg.getConfigurationSection("mutation_types.tank.abilities");
        if (tankAbilitiesSection != null) {
            for (Object ability : tankAbilitiesSection.getKeys(false)) {
                this.tankAbilities.put((String)ability, tankAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.tankAbilities.put("resistance", true);
        }
        this.chargerAbilities.clear();
        ConfigurationSection chargerAbilitiesSection = cfg.getConfigurationSection("mutation_types.charger.abilities");
        if (chargerAbilitiesSection != null) {
            for (Object ability : chargerAbilitiesSection.getKeys(false)) {
                this.chargerAbilities.put((String)ability, chargerAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.chargerAbilities.put("speed_boost", true);
        }
        this.assassinAbilities.clear();
        ConfigurationSection assassinAbilitiesSection = cfg.getConfigurationSection("mutation_types.assassin.abilities");
        if (assassinAbilitiesSection != null) {
            for (Object ability : assassinAbilitiesSection.getKeys(false)) {
                this.assassinAbilities.put((String)ability, assassinAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.assassinAbilities.put("invisibility", true);
            this.assassinAbilities.put("particle_aura", true);
        }
        this.blightAbilities.clear();
        ConfigurationSection blightAbilitiesSection = cfg.getConfigurationSection("mutation_types.blight.abilities");
        if (blightAbilitiesSection != null) {
            for (Object ability : blightAbilitiesSection.getKeys(false)) {
                this.blightAbilities.put((String)ability, blightAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.blightAbilities.put("poison_effect", true);
            this.blightAbilities.put("particle_aura", true);
        }
        this.bursterAbilities.clear();
        ConfigurationSection bursterAbilitiesSection = cfg.getConfigurationSection("mutation_types.burster.abilities");
        if (bursterAbilitiesSection != null) {
            for (Object ability : bursterAbilitiesSection.getKeys(false)) {
                this.bursterAbilities.put((String)ability, bursterAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.bursterAbilities.put("explosion_on_death", true);
            this.bursterAbilities.put("poison_cloud", true);
            this.bursterAbilities.put("speed_boost_on_chase", true);
        }
        this.spitterAbilities.clear();
        ConfigurationSection spitterAbilitiesSection = cfg.getConfigurationSection("mutation_types.spitter.abilities");
        if (spitterAbilitiesSection != null) {
            for (Object ability : spitterAbilitiesSection.getKeys(false)) {
                this.spitterAbilities.put((String)ability, spitterAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.spitterAbilities.put("projectile_attack", true);
        }
        this.frostAbilities.clear();
        ConfigurationSection frostAbilitiesSection = cfg.getConfigurationSection("mutation_types.frost.abilities");
        if (frostAbilitiesSection != null) {
            for (Object ability : frostAbilitiesSection.getKeys(false)) {
                this.frostAbilities.put((String)ability, frostAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.frostAbilities.put("slowness_effect", true);
        }
        this.shriekerAbilities.clear();
        ConfigurationSection shriekerAbilitiesSection = cfg.getConfigurationSection("mutation_types.shrieker.abilities");
        if (shriekerAbilitiesSection != null) {
            for (Object ability : shriekerAbilitiesSection.getKeys(false)) {
                this.shriekerAbilities.put((String)ability, shriekerAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.shriekerAbilities.put("aggro_nearby_zombies", true);
        }
        this.cryoAbilities.clear();
        ConfigurationSection cryoAbilitiesSection = cfg.getConfigurationSection("mutation_types.cryo.abilities");
        if (cryoAbilitiesSection != null) {
            for (Object ability : cryoAbilitiesSection.getKeys(false)) {
                this.cryoAbilities.put((String)ability, cryoAbilitiesSection.getBoolean((String)ability, true));
            }
        } else {
            this.cryoAbilities.put("frozen_trail", true);
            this.cryoAbilities.put("flash_freeze", true);
            this.cryoAbilities.put("shatter_explosion", true);
            this.cryoAbilities.put("slowness_effect", true);
        }
        this.infernoAbilities.clear();
        ConfigurationSection infernoAbilitiesSection = cfg.getConfigurationSection("mutation_types.inferno.abilities");
        if (infernoAbilitiesSection != null) {
            for (String ability : infernoAbilitiesSection.getKeys(false)) {
                this.infernoAbilities.put(ability, infernoAbilitiesSection.getBoolean(ability, true));
            }
        } else {
            this.infernoAbilities.put("fireball_attack", true);
            this.infernoAbilities.put("scorched_earth_trail", true);
            this.infernoAbilities.put("lava_on_death", true);
        }
        this.leechAbilities.clear();
        ConfigurationSection leechAbilitiesSection = cfg.getConfigurationSection("mutation_types.leech.abilities");
        if (leechAbilitiesSection != null) {
            for (String ability : leechAbilitiesSection.getKeys(false)) {
                this.leechAbilities.put(ability, leechAbilitiesSection.getBoolean(ability, true));
            }
        } else {
            this.leechAbilities.put("life_steal_aura", true);
            this.leechAbilities.put("life_steal_on_hit", true);
            this.leechAbilities.put("healing_explosion", true);
        }
    }

    public void loadLootDropsConfig() {
        FileConfiguration cfg = this.getConfig();
        this.mutationLootDrops.clear();
        ConfigurationSection lootSection = cfg.getConfigurationSection("loot_drops");
        if (lootSection == null) return;
        for (String type : lootSection.getKeys(false)) {
            ConfigurationSection typeSection = lootSection.getConfigurationSection(type + ".detailed_items");
            if (typeSection == null) continue;
            List<ItemStack> drops = new ArrayList<ItemStack>();
            for (String itemKey : typeSection.getKeys(false)) {
                ConfigurationSection itemSection = typeSection.getConfigurationSection(itemKey);
                if (itemSection == null) continue;
                String materialName = itemSection.getString("material");
                Material mat = Material.getMaterial(materialName.toUpperCase(Locale.ROOT));
                if (mat == null) continue;
                ItemStack item = new ItemStack(mat);
                drops.add(item);
            }
            this.mutationLootDrops.put(type.toLowerCase(Locale.ROOT), drops);
        }
    }

    public void loadSpawnTimeModeConfig() {
        FileConfiguration cfg = this.getConfig();
        this.getLogger().info("Attempting to load spawn_time_mode from config...");
        String mode = cfg.getString(SPAWN_TIME_MODE, "both").toLowerCase(Locale.ROOT);
        this.getLogger().info("Found spawn_time_mode in config: " + mode);
        if (!(mode.equals("day") || mode.equals("night") || mode.equals("both"))) {
            this.getLogger().warning("Invalid spawn_time_mode in config: " + mode + ". Defaulting to 'both'.");
            this.spawnTimeMode = "both";
        } else {
            this.spawnTimeMode = mode;
            this.getLogger().info("Successfully loaded spawn_time_mode: " + this.spawnTimeMode);
        }
    }

    public void loadSmarterTargetingConfig() {
        FileConfiguration cfg = this.getConfig();
        this.memoryDurationMs = cfg.getLong("ai.memory_duration", 5000L);
        this.sprintNoiseRange = cfg.getInt("ai.noise.sprint_range", 20);
        this.eatingNoiseRange = cfg.getInt("ai.noise.eating_range", 12);
        this.bowNoiseRange = cfg.getInt("ai.noise.bow_range", 25);
        this.torchNoiseRange = cfg.getInt("ai.noise.torch_range", 30);
    }

    public void loadGroupBehaviorConfig() {
        FileConfiguration cfg = this.getConfig();
        this.leaderChance = cfg.getDouble("ai.group.leader_chance", 0.3);
        this.packRadius = cfg.getInt("ai.pack.radius", 15);
        this.followRadius = cfg.getInt("ai.group.follow_radius", 10);
    }

    public void loadBlockBreakingConfig() {
        FileConfiguration cfg = this.getConfig();
        this.breakingEnabled = cfg.getBoolean("ai.breaking.enabled", true);
        this.breakingRadius = cfg.getInt("ai.breaking.radius", 2);
        this.defaultBreakingCooldownTicks = cfg.getInt("ai.breaking.default_cooldown_ticks", 60);
        this.breakableBlocks.clear();
        this.breakableBlocksCooldown.clear();
        ConfigurationSection blocksSection = cfg.getConfigurationSection("ai.breaking.blocks");
        if (blocksSection != null) {
            for (String blockName : blocksSection.getKeys(false)) {
                Material mat = Material.getMaterial((String)blockName.toUpperCase());
                if (mat != null) {
                    this.breakableBlocks.add(mat);
                    ConfigurationSection blockCfg = blocksSection.getConfigurationSection(blockName);
                    int cooldown = this.defaultBreakingCooldownTicks;
                    if (blockCfg != null) {
                        cooldown = blockCfg.getInt("cooldown_ticks", this.defaultBreakingCooldownTicks);
                    }
                    this.breakableBlocksCooldown.put(mat, cooldown);
                    continue;
                }
                this.getLogger().warning("Invalid breakable block in config: " + blockName);
            }
        }
    }

    public void loadZombieStatCaps() {
        FileConfiguration cfg = this.getConfig();
        this.maxSpeed = cfg.getDouble("zombie_stats.max_speed", 0.35);
        this.maxDamage = cfg.getDouble("zombie_stats.max_damage", 40.0);
    }

    public void loadZombieLimitsConfig() {
        FileConfiguration cfg = this.getConfig();
        this.maxZombiesPerChunk = cfg.getInt("zombies.max_per_chunk", 20);
        this.maxZombiesWorldwide = cfg.getInt("zombies.max_worldwide", 100);
    }

    public void loadBloodmoonConfig() {
        FileConfiguration cfg = this.getConfig();
        this.bloodmoonEnabled = cfg.getBoolean("zombies.bloodmoon_enabled", true);
        this.bloodmoonFrequency = Math.max(1, cfg.getInt("zombies.bloodmoon_frequency", 5));
        this.bloodMoonDefaultSpawnMultiplier = Math.max(0.1, cfg.getDouble("events.bloodmoon.default_spawn_multiplier", 2.0));
        this.bloodMoonDefaultMutationMultiplier = Math.max(0.1, cfg.getDouble("events.bloodmoon.default_mutation_multiplier", 10.0));
        this.bloodMoonScheduleEnabled = cfg.getBoolean("events.bloodmoon.schedule_enabled", false);
        this.bloodMoonSchedule.clear();
        List<?> scheduleEntries = cfg.getMapList("events.bloodmoon.schedule");
        for (int i = 0; i < scheduleEntries.size(); ++i) {
            Map<?, ?> entryMap = (Map<?, ?>)scheduleEntries.get(i);
            String id = this.mapString(entryMap, "id", "window_" + i);
            String dayPattern = this.mapString(entryMap, "days", "*");
            int startTick = this.normalizeWorldTick(this.mapInt(entryMap, "start_tick", 13000));
            int endTick = this.normalizeWorldTick(this.mapInt(entryMap, "end_tick", 23000));
            double spawnMultiplier = Math.max(0.1, this.mapDouble(entryMap, "spawn_multiplier", this.bloodMoonDefaultSpawnMultiplier));
            double mutationMultiplier = Math.max(0.1, this.mapDouble(entryMap, "mutation_multiplier", this.bloodMoonDefaultMutationMultiplier));
            this.bloodMoonSchedule.add(new BloodMoonScheduleEntry(id, dayPattern, startTick, endTick, spawnMultiplier, mutationMultiplier));
        }
    }

    private String mapString(Map<?, ?> map, String key, String def) {
        Object value = map.get(key);
        if (value == null) {
            return def;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? def : text;
    }

    private int mapInt(Map<?, ?> map, String key, int def) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(value.toString().trim());
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return def;
    }

    private double mapDouble(Map<?, ?> map, String key, double def) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        if (value != null) {
            try {
                return Double.parseDouble(value.toString().trim());
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return def;
    }

    public void loadEvolutionConfig() {
        FileConfiguration cfg = this.getConfig();
        this.evolutionEnabled = cfg.getBoolean("evolution.enabled", true);
        this.expPerPassiveMob = cfg.getDouble("evolution.exp_per_passive_mob", 1.0);
        this.expPerPlayerKill = cfg.getDouble("evolution.exp_per_player_kill", 10.0);
        this.maxLevel = cfg.getInt("evolution.max_level", 10);
    }

    public void loadSpawnDistanceConfig() {
        FileConfiguration cfg = this.getConfig();
        this.closestSpawnDistance = cfg.getInt("zombies.closest_to_player", 2);
        this.furthestSpawnDistance = cfg.getInt("zombies.furthest_from_player", 30);
        if (this.closestSpawnDistance < 0) {
            this.closestSpawnDistance = 0;
            this.getLogger().warning("closest_to_player cannot be negative. Setting to 0.");
        }
        if (this.furthestSpawnDistance < this.closestSpawnDistance) {
            this.furthestSpawnDistance = this.closestSpawnDistance + 10;
            this.getLogger().warning("furthest_from_player cannot be less than closest_to_player. Setting to " + this.furthestSpawnDistance);
        }
        this.getLogger().info("Loaded spawn distances - closest: " + this.closestSpawnDistance + ", furthest: " + this.furthestSpawnDistance);
    }

    public void loadDebugConfig() {
        FileConfiguration cfg = this.getConfig();
        this.debugMode = cfg.getBoolean("debug.enabled", false);
        if (this.debugMode) {
            this.getLogger().info("Debug mode is ENABLED");
        }
    }

    public void loadPresetConfig() {
        FileConfiguration cfg = this.getConfig();
        this.currentPreset = cfg.getString("difficulty.current_preset", "normal").toLowerCase();
        this.applyPreset(this.currentPreset);
    }

    public void applyPreset(String presetName) {
        FileConfiguration cfg = this.getConfig();
        ConfigurationSection presetSection = cfg.getConfigurationSection("difficulty.presets." + presetName);
        if (presetSection == null) {
            this.getLogger().warning("Preset '" + presetName + "' not found. Using normal.");
            presetName = "normal";
            presetSection = cfg.getConfigurationSection("difficulty.presets.normal");
            if (presetSection == null) {
                return;
            }
        }
        this.currentPreset = presetName;
        cfg.set("difficulty.current_preset", presetName);
        if (presetSection.contains("mutation_rate_base")) {
            cfg.set(BASE_MUTATION, presetSection.getDouble("mutation_rate_base"));
        }
        if (presetSection.contains("zombie_speed_multiplier")) {
            double baseSpeed = 0.23 * presetSection.getDouble("zombie_speed_multiplier");
            cfg.set(BASE_SPEED, baseSpeed);
        }
        if (presetSection.contains("zombie_damage_multiplier")) {
            double baseDamage = 2.0 * presetSection.getDouble("zombie_damage_multiplier");
            cfg.set(BASE_DAMAGE, baseDamage);
        }
        if (presetSection.contains("golden_apple_cure")) {
            cfg.set("infection_system.allow_golden_apple_cure", presetSection.getBoolean("golden_apple_cure"));
        }
        if (presetSection.contains("max_zombies_worldwide")) {
            cfg.set("zombies.max_worldwide", presetSection.getInt("max_zombies_worldwide"));
        }
        if (presetSection.contains("bloodmoon_frequency")) {
            cfg.set("zombies.bloodmoon_frequency", presetSection.getInt("bloodmoon_frequency"));
        }
        this.saveConfig();
        this.loadMutationConfig();
        this.loadZombieLimitsConfig();
        this.loadZombieStatCaps();
        this.getLogger().info("Applied difficulty preset: " + presetName);
    }

    public void loadWorldConfigs() {
        FileConfiguration cfg = this.getConfig();
        this.worldConfigs.clear();
        ConfigurationSection worldsSection = cfg.getConfigurationSection("world_settings");
        if (worldsSection == null) {
            return;
        }
        for (String worldName : worldsSection.getKeys(false)) {
            List<String> disabledMutations;
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection == null) continue;
            WorldConfig wc = new WorldConfig();
            wc.enabled = worldSection.getBoolean("enabled", true);
            wc.mutationRateMultiplier = worldSection.getDouble("mutation_rate_multiplier", 1.0);
            wc.spawnRateMultiplier = worldSection.getDouble("spawn_rate_multiplier", 1.0);
            wc.damageMultiplier = worldSection.getDouble("damage_multiplier", 1.0);
            wc.speedMultiplier = worldSection.getDouble("speed_multiplier", 1.0);
            wc.maxZombies = worldSection.getInt("max_zombies", this.maxZombiesWorldwide);
            wc.fireImmune = worldSection.getBoolean("fire_immune", false);
            wc.canTeleport = worldSection.getBoolean("can_teleport", false);
            List<String> enabledMutations = worldSection.getStringList("enabled_mutations");
            if (!enabledMutations.isEmpty()) {
                wc.enabledMutations = new HashSet<String>();
                for (String mutation : enabledMutations) {
                    wc.enabledMutations.add(mutation.toLowerCase(Locale.ROOT));
                }
            }
            if (!(disabledMutations = worldSection.getStringList("disabled_mutations")).isEmpty()) {
                wc.disabledMutations = new HashSet<String>();
                for (String mutation : disabledMutations) {
                    wc.disabledMutations.add(mutation.toLowerCase(Locale.ROOT));
                }
            }
            this.worldConfigs.put(worldName, wc);
            this.getLogger().info("Loaded world config for: " + worldName);
        }
    }

    public WorldConfig getWorldConfig(World world) {
        return this.worldConfigs.getOrDefault(world.getName(), new WorldConfig());
    }

    public String getCurrentPreset() {
        return this.currentPreset;
    }

    public void debug(String message) {
        if (this.debugMode) {
            this.getLogger().info("[DEBUG] " + message);
        }
    }

    public boolean isMutationAllowedInWorld(World world, String mutationType) {
        if (mutationType == null || mutationType.equalsIgnoreCase("normal")) {
            return true;
        }
        String key = mutationType.toLowerCase(Locale.ROOT);
        WorldConfig wc = this.getWorldConfig(world);
        if (wc.enabledMutations != null && !wc.enabledMutations.isEmpty() && !wc.enabledMutations.contains(key)) {
            return false;
        }
        return wc.disabledMutations == null || !wc.disabledMutations.contains(key);
    }

    private void startDayCycle() {
        new BukkitRunnable(){

            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    return;
                }
                ++ZonaMuerta.this.currentDay;
                ZonaMuerta.this.currentMutationRate = ZonaMuerta.this.getConfig().getDouble(ZonaMuerta.BASE_MUTATION) + (double)ZonaMuerta.this.currentDay * ZonaMuerta.this.getConfig().getDouble(ZonaMuerta.MUTATION_INCREASE);
                if (!ZonaMuerta.this.bloodMoonScheduleEnabled && !ZonaMuerta.this.bloodMoonForced) {
                    if (ZonaMuerta.this.bloodmoonEnabled && ZonaMuerta.this.currentDay % ZonaMuerta.this.bloodmoonFrequency == 0) {
                        ZonaMuerta.this.activateBloodMoon("periodic", null, ZonaMuerta.this.bloodMoonDefaultSpawnMultiplier, ZonaMuerta.this.bloodMoonDefaultMutationMultiplier, true);
                    }
                }
                boolean scheduledEnded = ZonaMuerta.this.bloodMoonScheduleEnabled && "schedule".equalsIgnoreCase(ZonaMuerta.this.bloodMoonSource) && !ZonaMuerta.this.bloodMoonForced;
                boolean periodicEnded = !ZonaMuerta.this.bloodMoonScheduleEnabled && "periodic".equalsIgnoreCase(ZonaMuerta.this.bloodMoonSource) && !ZonaMuerta.this.bloodMoonForced;
                boolean forcedEnded = ZonaMuerta.this.bloodMoonForced && !ZonaMuerta.this.isBloodMoon;
                if (scheduledEnded || periodicEnded || forcedEnded) {
                    ZonaMuerta.this.deactivateBloodMoon(true);
                }
                ZonaMuerta.this.getLogger().info("Day " + ZonaMuerta.this.currentDay + " - Mutation: " + ZonaMuerta.this.currentMutationRate + "%");
            }
        }.runTaskTimer((Plugin)this, 0L, 24000L);
    }

    private void startBloodMoonScheduleSystem() {
        new BukkitRunnable(){

            public void run() {
                ZonaMuerta.this.updateScheduledBloodMoonState();
            }
        }.runTaskTimer((Plugin)this, 0L, 100L);
    }

    private void updateScheduledBloodMoonState() {
        if (!this.bloodmoonEnabled || !this.bloodMoonScheduleEnabled || this.bloodMoonSchedule.isEmpty()) {
            if (!this.bloodMoonForced && "schedule".equalsIgnoreCase(this.bloodMoonSource)) {
                this.deactivateBloodMoon(true);
            }
            return;
        }
        if (this.bloodMoonForced || Bukkit.getWorlds().isEmpty()) {
            return;
        }
        World referenceWorld = (World)Bukkit.getWorlds().get(0);
        int worldTick = this.normalizeWorldTick((int)referenceWorld.getTime());
        BloodMoonScheduleEntry activeEntry = this.findActiveBloodMoonScheduleEntry(this.currentDay, worldTick);
        if (activeEntry != null) {
            boolean alreadyActiveEntry;
            boolean bl = alreadyActiveEntry = this.isBloodMoon && "schedule".equalsIgnoreCase(this.bloodMoonSource) && Objects.equals(this.activeBloodMoonScheduleId, activeEntry.id);
            if (!alreadyActiveEntry) {
                this.activateBloodMoon("schedule", activeEntry.id, activeEntry.spawnMultiplier, activeEntry.mutationMultiplier, true);
            }
        } else if ("schedule".equalsIgnoreCase(this.bloodMoonSource)) {
            this.deactivateBloodMoon(true);
        }
    }

    private BloodMoonScheduleEntry findActiveBloodMoonScheduleEntry(int day, int worldTick) {
        for (BloodMoonScheduleEntry entry : this.bloodMoonSchedule) {
            if (!this.matchesDayPattern(entry.dayPattern, day) || !this.isTickInWindow(worldTick, entry.startTick, entry.endTick)) continue;
            return entry;
        }
        return null;
    }

    private boolean matchesDayPattern(String dayPattern, int day) {
        String[] parts;
        if (dayPattern == null || dayPattern.isBlank() || "*".equals(dayPattern.trim())) {
            return true;
        }
        int normalizedDay = Math.max(1, day);
        for (String part : parts = dayPattern.split(",")) {
            String token = part.trim();
            if (token.isEmpty()) continue;
            if (token.startsWith("*/")) {
                try {
                    int step = Integer.parseInt(token.substring(2));
                    if (step > 0 && normalizedDay % step == 0) {
                        return true;
                    }
                }
                catch (NumberFormatException step) {}
                continue;
            }
            if (token.contains("-")) {
                String[] bounds = token.split("-", 2);
                if (bounds.length != 2) continue;
                try {
                    int min = Integer.parseInt(bounds[0].trim());
                    int max = Integer.parseInt(bounds[1].trim());
                    if (normalizedDay >= min && normalizedDay <= max) {
                        return true;
                    }
                }
                catch (NumberFormatException numberFormatException) {}
                continue;
            }
            try {
                if (normalizedDay != Integer.parseInt(token)) continue;
                return true;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return false;
    }

    private boolean isTickInWindow(int tick, int startTick, int endTick) {
        int end;
        int normalizedTick = this.normalizeWorldTick(tick);
        int start = this.normalizeWorldTick(startTick);
        if (start <= (end = this.normalizeWorldTick(endTick))) {
            return normalizedTick >= start && normalizedTick <= end;
        }
        return normalizedTick >= start || normalizedTick <= end;
    }

    private int normalizeWorldTick(int tick) {
        int normalized = tick % 24000;
        if (normalized < 0) {
            normalized += 24000;
        }
        return normalized;
    }

    private void activateBloodMoon(String source, String scheduleId, double spawnMultiplier, double mutationMultiplier, boolean announce) {
        boolean wasActive = this.isBloodMoon;
        this.isBloodMoon = true;
        this.bloodMoonSource = source;
        this.activeBloodMoonScheduleId = scheduleId;
        this.activeBloodMoonSpawnMultiplier = Math.max(0.1, spawnMultiplier);
        this.activeBloodMoonMutationMultiplier = Math.max(0.1, mutationMultiplier);
        if (announce && !wasActive) {
            Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.DARK_RED) + "\u2726 LUNA DE SANGRE \u2726"));
            Bukkit.getOnlinePlayers().forEach(p -> {
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
                p.spawnParticle(Particle.LAVA, p.getLocation().add(0.0, 2.0, 0.0), 50);
            });
            if (this.mythicBridge != null) {
                this.mythicBridge.refreshAllMobStats();
            }
        }
    }

    private void deactivateBloodMoon(boolean announce) {
        if (!this.isBloodMoon) {
            return;
        }
        this.isBloodMoon = false;
        this.bloodMoonSource = "none";
        this.activeBloodMoonScheduleId = null;
        this.activeBloodMoonSpawnMultiplier = this.bloodMoonDefaultSpawnMultiplier;
        this.activeBloodMoonMutationMultiplier = this.bloodMoonDefaultMutationMultiplier;
        if (announce) {
            Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.GREEN) + "\u2726 La Luna de Sangre ha terminado \u2726"));
            Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.5f, 0.7f));
            if (this.mythicBridge != null) {
                this.mythicBridge.refreshAllMobStats();
            }
        }
    }

    public void triggerBloodMoon() {
        this.bloodMoonForced = true;
        this.activateBloodMoon("forced", null, this.bloodMoonDefaultSpawnMultiplier, this.bloodMoonDefaultMutationMultiplier, false);
        Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.DARK_RED) + "\u2726 LUNA DE SANGRE \u2726"));
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 0.7f);
            p.spawnParticle(Particle.LAVA, p.getLocation().add(0.0, 2.0, 0.0), 50);
        });
    }

    private void startSpawningSystem() {
        new BukkitRunnable(){

            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (ZonaMuerta.this.random.nextInt(5) < 2 && ZonaMuerta.this.canSpawnZombiesAt(player.getWorld(), player.getLocation())) {
                        ZonaMuerta.this.spawnZombieGroup((Player)player);
                    }
                });
            }
        }.runTaskTimer((Plugin)this, 0L, 100L);
    }

    private boolean canSpawnZombiesAt(World world, Location loc) {
        WorldConfig wc = this.getWorldConfig(world);
        if (!wc.enabled) {
            return false;
        }
        if (this.safezoneManager.isInSafezone(loc)) {
            return false;
        }
        long time = world.getTime();
        switch (this.spawnTimeMode) {
            case "day": {
                if (time >= 0L && time <= 12299L) break;
                return false;
            }
            case "night": {
                if (time >= 12300L && time <= 23999L) break;
                return false;
            }
        }
        FileConfiguration cfg = this.getConfig();
        boolean requireDarkness = cfg.getBoolean("zombies.require_darkness", true);
        int maxLight = cfg.getInt("zombies.max_light_level", 7);
        boolean bloodMoonIgnoresDarkness = cfg.getBoolean("zombies.bloodmoon_ignores_darkness", true);
        if (!requireDarkness) {
            return true;
        }
        if (this.isBloodMoon && bloodMoonIgnoresDarkness) {
            return true;
        }
        byte light = loc.getBlock().getLightLevel();
        return light <= maxLight;
    }

    private boolean isManagedZombie(Entity entity) {
        // Cuenta todos los EntityType.ZOMBIE (tanto mobs ZM-MythicMobs como vanilla fallback).
        // SkeletalKnight/SkeletonKing son WITHER_SKELETON → no pasan el instanceof Zombie.
        return entity instanceof Zombie && entity.getType() == EntityType.ZOMBIE;
    }

    private int countManagedZombies(World world) {
        int count = 0;
        for (Entity entity : world.getEntities()) {
            if (!this.isManagedZombie(entity)) continue;
            ++count;
        }
        return count;
    }

    private void spawnZombieGroup(Player player) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL && world.getEnvironment() != World.Environment.NETHER && world.getEnvironment() != World.Environment.THE_END) {
            return;
        }
        WorldConfig wc = this.getWorldConfig(world);
        if (!wc.enabled) {
            return;
        }
        long time = world.getTime();
        if (!this.canSpawnZombiesAt(world, player.getLocation())) {
            return;
        }
        int totalZombies = 0;
        for (World w : Bukkit.getWorlds()) {
            totalZombies += this.countManagedZombies(w);
        }
        int worldMax = wc.maxZombies > 0 ? wc.maxZombies : this.maxZombiesWorldwide;
        int worldZombies = this.countManagedZombies(world);
        if (worldZombies >= worldMax) {
            return;
        }
        if (totalZombies >= this.maxZombiesWorldwide) {
            return;
        }
        int minZombies = 2;
        int maxZombies = Math.min(6 + this.currentDay, 15);
        int zombieCount = this.random.nextInt(maxZombies - minZombies + 1) + minZombies;
        zombieCount = (int)((double)zombieCount * wc.spawnRateMultiplier);
        if (this.isBloodMoon) {
            zombieCount = Math.max(1, (int)Math.round((double)zombieCount * this.activeBloodMoonSpawnMultiplier));
        }
        double mobLevel = 1.0 + (this.currentDay * 0.5);
        for (int i = 0; i < zombieCount; ++i) {
            String chunkKey;
            Location spawnLoc = this.findSafeSpawnLocation(player.getLocation());
            if (spawnLoc == null || this.safezoneManager.isInSafezone(spawnLoc) || this.zombieChunkCount.getOrDefault(chunkKey = spawnLoc.getChunk().getX() + "," + spawnLoc.getChunk().getZ(), 0) >= this.maxZombiesPerChunk) continue;
            if (this.mythicBridge != null && this.mythicBridge.isMythicAvailable()) {
                String mobType = this.chooseMythicMobType();
                this.mythicBridge.spawnMythicMob(mobType, spawnLoc, mobLevel);
            } else {
                Zombie zombie = (Zombie)world.spawnEntity(spawnLoc, EntityType.ZOMBIE);
                this.initializeZombie(zombie);
                if (wc.fireImmune) {
                    zombie.setFireTicks(0);
                    zombie.setVisualFire(false);
                }
            }
            this.trackChunkDensity(spawnLoc);
        }
    }

    /** Elige un tipo de mob MythicMobs según el día actual y probabilidades ponderadas. */
    private String chooseMythicMobType() {
        double r = this.random.nextDouble();
        if (this.currentDay < 4) {
            if (r < 0.65) return "ZombiCaminante";
            if (r < 0.85) return "ZombiCorredor";
            if (r < 0.97) return "ZombiSoldado";
            return "ZombiExplosivo";
        } else if (this.currentDay < 8) {
            if (r < 0.45) return "ZombiCaminante";
            if (r < 0.65) return "ZombiCorredor";
            if (r < 0.78) return "ZombiSoldado";
            if (r < 0.88) return "ZombiExplosivo";
            if (r < 0.95) return "ZombiTrepador";
            return "ZombiMutante";
        } else {
            if (r < 0.30) return "ZombiCaminante";
            if (r < 0.50) return "ZombiCorredor";
            if (r < 0.65) return "ZombiSoldado";
            if (r < 0.75) return "ZombiExplosivo";
            if (r < 0.85) return "ZombiTrepador";
            return "ZombiMutante";
        }
    }

    private Location findSafeSpawnLocation(Location center) {
        for (int i = 0; i < 10; ++i) {
            int offsetZ;
            int offsetX;
            double actualDistance;
            while ((actualDistance = Math.sqrt((offsetX = this.random.nextInt(this.furthestSpawnDistance * 2 + 1) - this.furthestSpawnDistance) * offsetX + (offsetZ = this.random.nextInt(this.furthestSpawnDistance * 2 + 1) - this.furthestSpawnDistance) * offsetZ)) < (double)this.closestSpawnDistance || actualDistance > (double)this.furthestSpawnDistance) {
            }
            Location attempt = center.clone().add((double)offsetX, 0.0, (double)offsetZ);
            attempt.setY((double)(center.getWorld().getHighestBlockYAt(attempt) + 1));
            if (!this.isValidSpawnPoint(attempt) || this.safezoneManager.isInSafezone(attempt)) continue;
            return attempt;
        }
        return null;
    }

    private boolean isValidSpawnPoint(Location loc) {
        return loc.getBlock().getType().isAir() && loc.clone().add(0.0, 1.0, 0.0).getBlock().getType().isAir() && loc.clone().add(0.0, -1.0, 0.0).getBlock().getType().isSolid() && !loc.getBlock().isLiquid() && loc.getY() > 0.0;
    }

    private void trackChunkDensity(Location loc) {
        String chunkKey = loc.getChunk().getX() + "," + loc.getChunk().getZ();
        int count = this.zombieChunkCount.getOrDefault(chunkKey, 0) + 1;
        this.zombieChunkCount.put(chunkKey, count);
        if (count >= 20) {
            Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.DARK_RED) + "\u2726 OLEADA DE MUTACIONES EN ZONA [" + chunkKey + "] \u2726"));
        }
    }

    private void handleCryoTrail(final Zombie zombie) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.cryo");
        final int trailDuration = cfg.getInt("frozen_trail_duration", 5);
        new BukkitRunnable(){

            public void run() {
                Location loc;
                if (!zombie.isDead() && zombie.isValid() && (loc = zombie.getLocation()).getBlock().getType().isAir() && loc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType().isSolid()) {
                    loc.getBlock().setType(Material.SNOW);
                    new BukkitRunnable(){

                        public void run() {
                            if (loc.getBlock().getType() == Material.SNOW) {
                                loc.getBlock().setType(Material.AIR);
                            }
                        }
                    }.runTaskLater((Plugin)ZonaMuerta.this, (long)(trailDuration * 20));
                }
            }
        }.runTask((Plugin)this);
    }

    private void handleInfernoTrail(final Zombie zombie) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.inferno");
        final int trailDuration = cfg.getInt("scorched_earth_duration", 5);
        new BukkitRunnable(){

            public void run() {
                Material below;
                Location loc;
                if (!zombie.isDead() && zombie.isValid() && (loc = zombie.getLocation()).getBlock().getType().isAir() && (below = loc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType()).isFlammable()) {
                    loc.getBlock().setType(Material.FIRE);
                    new BukkitRunnable(){

                        public void run() {
                            if (loc.getBlock().getType() == Material.FIRE) {
                                loc.getBlock().setType(Material.AIR);
                            }
                        }
                    }.runTaskLater((Plugin)ZonaMuerta.this, (long)(trailDuration * 20));
                }
            }
        }.runTask((Plugin)this);
    }

    private void startAIUpdateSystem() {
        new BukkitRunnable(){

            public void run() {
                Bukkit.getWorlds().forEach(world -> {
                    WorldConfig wc = ZonaMuerta.this.getWorldConfig((World)world);
                    for (Entity entity : world.getEntities()) {
                        if (!ZonaMuerta.this.isManagedZombie(entity) || !entity.isValid()) continue;
                        Zombie zombie = (Zombie)entity;
                        if (wc.fireImmune && zombie.getFireTicks() > 0) {
                            zombie.setFireTicks(0);
                        }
                        ZonaMuerta.this.updateZombieAI(zombie);
                        String zombieType = (String)zombie.getPersistentDataContainer().get(ZonaMuerta.this.zombieTypeKey, PersistentDataType.STRING);
                        if (zombieType == null) continue;
                        switch (zombieType) {
                            case "cryo": {
                                if (!ZonaMuerta.this.cryoAbilities.getOrDefault("frozen_trail", true).booleanValue()) break;
                                ZonaMuerta.this.handleCryoTrail(zombie);
                                break;
                            }
                            case "inferno": {
                                if (!ZonaMuerta.this.infernoAbilities.getOrDefault("scorched_earth_trail", true).booleanValue()) break;
                                ZonaMuerta.this.handleInfernoTrail(zombie);
                            }
                        }
                        if (!wc.canTeleport || zombie.getTarget() == null) continue;
                        ZonaMuerta.this.handleEndermanTeleport(zombie);
                    }
                });
            }
        }.runTaskTimer((Plugin)this, 0L, 10L);
    }

    private void handleEndermanTeleport(final Zombie zombie) {
        if (zombie.hasMetadata("teleport_cooldown")) {
            return;
        }
        LivingEntity target = zombie.getTarget();
        if (target == null) {
            return;
        }
        double distance = zombie.getLocation().distance(target.getLocation());
        if (distance > 10.0 && distance < 30.0 && this.random.nextDouble() < 0.1) {
            Location targetLoc = target.getLocation();
            Location teleportLoc = targetLoc.clone().add((double)(this.random.nextInt(5) - 2), 0.0, (double)(this.random.nextInt(5) - 2));
            teleportLoc.setY((double)(teleportLoc.getWorld().getHighestBlockYAt(teleportLoc) + 1));
            if (this.isValidSpawnPoint(teleportLoc) && !this.safezoneManager.isInSafezone(teleportLoc)) {
                zombie.getWorld().spawnParticle(Particle.PORTAL, zombie.getLocation(), 50);
                zombie.teleport(teleportLoc);
                zombie.getWorld().spawnParticle(Particle.PORTAL, teleportLoc, 50);
                zombie.getWorld().playSound(teleportLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                zombie.setMetadata("teleport_cooldown", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
                new BukkitRunnable(){

                    public void run() {
                        zombie.removeMetadata("teleport_cooldown", (Plugin)ZonaMuerta.this);
                    }
                }.runTaskLater((Plugin)this, 100L);
            }
        }
    }

    private void initializeZombie(Zombie zombie) {
        zombie.setCanPickupItems(false);
        zombie.setPersistent(true);
        World world = zombie.getWorld();
        WorldConfig wc = this.getWorldConfig(world);
        zombie.getPersistentDataContainer().set(this.zombieLevelKey, PersistentDataType.INTEGER, 1);
        zombie.getPersistentDataContainer().set(this.zombieExpKey, PersistentDataType.INTEGER, 0);
        double dayScale = 1.0 + 0.05 * (double)this.currentDay;
        double speed = this.getConfig().getDouble(BASE_SPEED, 0.23) * dayScale * wc.speedMultiplier;
        double damage = this.getConfig().getDouble(BASE_DAMAGE, 2.0) * dayScale * wc.damageMultiplier;
        if (speed > this.maxSpeed) {
            speed = this.maxSpeed;
        }
        if (damage > this.maxDamage) {
            damage = this.maxDamage;
        }
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, speed);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, damage);
        double effectiveMutationRate = this.currentMutationRate * wc.mutationRateMultiplier;
        if (this.isBloodMoon) {
            effectiveMutationRate *= this.activeBloodMoonMutationMultiplier;
        }
        effectiveMutationRate = Math.min(100.0, effectiveMutationRate);
        if (this.random.nextDouble() * 100.0 < effectiveMutationRate) {
            this.applyRandomMutation(zombie, speed, damage);
        } else {
            this.setupNormalZombie(zombie);
        }
        if (this.isBloodMoon) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false));
        }
        if (wc.fireImmune) {
            zombie.setFireTicks(0);
            zombie.setVisualFire(false);
        }
        this.updateZombieNameWithLevel(zombie);
    }

    private void setAttribute(Zombie zombie, Attribute attribute, double value) {
        if (zombie.getAttribute(attribute) != null) {
            zombie.getAttribute(attribute).setBaseValue(value);
        }
    }

    private void applyRandomMutation(Zombie zombie, double baseSpeed, double baseDamage) {
        World world = zombie.getWorld();
        double roll = this.random.nextDouble() * 100.0;
        ConfigurationSection mutations = this.getConfig().getConfigurationSection("mutation_types");
        if (mutations == null) {
            return;
        }
        double cumulativeChance = 0.0;
        for (String key : mutations.getKeys(false)) {
            ConfigurationSection mutationSection;
            String mutationKey = key.toLowerCase(Locale.ROOT);
            if (!this.isMutationAllowedInWorld(world, mutationKey) || (mutationSection = mutations.getConfigurationSection(key)) == null) continue;
            double spawnChance = mutationSection.getDouble("spawn_chance", 0.0);
            if (spawnChance == 0.0) {
                this.getLogger().warning("Mutation '" + key + "' has a spawn chance of 0. If you did this purposely, ignore this message.");
                continue;
            }
            if (roll < cumulativeChance + spawnChance) {
                switch (mutationKey) {
                    case "tank": {
                        this.createTank(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "charger": {
                        this.createCharger(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "assassin": {
                        this.createAssassin(zombie, baseDamage);
                        break;
                    }
                    case "blight": {
                        this.createBlightZombie(zombie, baseDamage);
                        break;
                    }
                    case "burster": {
                        this.createBurster(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "spitter": {
                        this.createSpitter(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "frost": {
                        this.createFrostZombie(zombie, baseDamage);
                        break;
                    }
                    case "shrieker": {
                        this.createShrieker(zombie, baseSpeed);
                        break;
                    }
                    case "cryo": {
                        this.createCryoZombie(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "inferno": {
                        this.createInfernoZombie(zombie, baseSpeed, baseDamage);
                        break;
                    }
                    case "leech": {
                        this.createLeech(zombie, baseSpeed, baseDamage);
                    }
                }
                zombie.getPersistentDataContainer().set(this.mutationDayKey, PersistentDataType.INTEGER, this.currentDay);
                return;
            }
            cumulativeChance += spawnChance;
        }
    }

    private void setupNormalZombie(Zombie zombie) {
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "normal");
        zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        zombie.setCustomNameVisible(true);
    }

    private void createTank(Zombie zombie, double speed, double damage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.tank");
        double health = 40.0 * cfg.getDouble("health_multiplier", 3.0);
        this.setAttribute(zombie, Attribute.MAX_HEALTH, health);
        zombie.setHealth(health);
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, speed * cfg.getDouble("speed_multiplier", 0.6));
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, damage * cfg.getDouble("damage_multiplier", 1.5));
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "tank");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.DARK_GREEN) + "TANQUE [" + (int)health + "\u2764]");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.tankAbilities.getOrDefault("resistance", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
        }
    }

    private void createCharger(Zombie zombie, double speed, double damage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.charger");
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0 * cfg.getDouble("health_multiplier", 0.8));
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, speed * cfg.getDouble("speed_multiplier", 2.2));
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, damage + cfg.getDouble("damage_bonus", 1.5));
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "charger");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.RED) + "\u26a1 EMBESTIDOR");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.chargerAbilities.getOrDefault("speed_boost", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        }
    }

    private void createAssassin(final Zombie zombie, double damage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.assassin");
        this.setAttribute(zombie, Attribute.MAX_HEALTH, cfg.getDouble("base_health", 8.0));
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, damage * cfg.getDouble("damage_multiplier", 2.5));
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "assassin");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.DARK_PURPLE) + "ASESINO");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.assassinAbilities.getOrDefault("invisibility", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }
        if (this.assassinAbilities.getOrDefault("particle_aura", true).booleanValue()) {
            new BukkitRunnable(){

                public void run() {
                    if (zombie.isDead() || !zombie.isValid()) {
                        this.cancel();
                        return;
                    }
                    zombie.getWorld().spawnParticle(Particle.WITCH, zombie.getLocation().add(0.0, 1.5, 0.0), 3, 0.2, 0.2, 0.2, 0.1);
                }
            }.runTaskTimer((Plugin)this, 0L, 20L);
        }
    }

    private void createBlightZombie(final Zombie zombie, double baseDamage) {
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "blight");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.GREEN) + "ZOMBIE PLAGA");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.blightAbilities.getOrDefault("poison_effect", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));
        }
        if (this.blightAbilities.getOrDefault("particle_aura", true).booleanValue()) {
            new BukkitRunnable(){

                public void run() {
                    if (zombie.isDead() || !zombie.isValid()) {
                        this.cancel();
                        return;
                    }
                    zombie.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, zombie.getLocation(), 5);
                }
            }.runTaskTimer((Plugin)this, 0L, 20L);
        }
    }

    private void createBurster(Zombie zombie, double baseSpeed, double baseDamage) {
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 10.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed * 1.2);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "burster");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.YELLOW) + "REVENTADOR");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        zombie.setMetadata("burster", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
    }

    private void createSpitter(Zombie zombie, double baseSpeed, double baseDamage) {
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 16.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed * 0.5);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage * 0.5);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "spitter");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.BLUE) + "ESCUPIDOR");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
    }

    private void createFrostZombie(Zombie zombie, double baseDamage) {
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "frost");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.AQUA) + "ZOMBIE HELADO");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.frostAbilities.getOrDefault("slowness_effect", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
        }
    }

    private void createShrieker(Zombie zombie, double baseSpeed) {
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 10.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed * 1.5);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, 0.0);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "shrieker");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.LIGHT_PURPLE) + "CHILLADOR");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
    }

    private void createCryoZombie(Zombie zombie, double baseSpeed, double baseDamage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.cryo");
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed * 0.8);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "cryo");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.AQUA) + "Zombie Cryo");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
        if (this.cryoAbilities.getOrDefault("slowness_effect", true).booleanValue()) {
            zombie.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, false, false));
        }
    }

    private void createInfernoZombie(Zombie zombie, double baseSpeed, double baseDamage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.inferno");
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage);
        zombie.setFireTicks(0);
        zombie.setVisualFire(false);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "inferno");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.GOLD) + "Zombie Inferno");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
    }

    private void createLeech(Zombie zombie, double baseSpeed, double baseDamage) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.leech");
        double healthMultiplier = cfg.getDouble("health_multiplier", 1.2);
        double damageMultiplier = cfg.getDouble("damage_multiplier", 0.8);
        this.setAttribute(zombie, Attribute.MAX_HEALTH, 20.0 * healthMultiplier);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue());
        this.setAttribute(zombie, Attribute.MOVEMENT_SPEED, baseSpeed * 0.9);
        this.setAttribute(zombie, Attribute.ATTACK_DAMAGE, baseDamage * damageMultiplier);
        zombie.getPersistentDataContainer().set(this.zombieTypeKey, PersistentDataType.STRING, "leech");
        if (this.showMutationNamesAboveZombies) {
            zombie.setCustomName(String.valueOf(ChatColor.DARK_RED) + "Sanguijuela");
        } else {
            zombie.setCustomName(String.valueOf(ChatColor.WHITE) + "Zombie");
        }
        zombie.setCustomNameVisible(true);
    }

    private void handleBursterDeath(Zombie zombie) {
        Location location = zombie.getLocation();
        zombie.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 3.0f, false, false);
        for (Entity entity : zombie.getWorld().getNearbyEntities(location, 5.0, 3.0, 5.0)) {
            if (!(entity instanceof LivingEntity) || entity instanceof Zombie) continue;
            ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
        }
        zombie.getWorld().spawnParticle(Particle.CLOUD, location, 30, 2.0, 2.0, 2.0, 0.1);
        zombie.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
    }

    private void handleCryoDeath(Zombie zombie) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.cryo");
        double shatterDamage = cfg.getDouble("shatter_damage", 5.0);
        Location loc = zombie.getLocation();
        loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 100, 1.0, 1.0, 1.0, 0.1);
        loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, 5.0, 5.0, 5.0)) {
            if (!(entity instanceof LivingEntity) || entity instanceof Zombie) continue;
            ((LivingEntity)entity).damage(shatterDamage);
            ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
        }
    }

    private void handleInfernoDeath(Zombie zombie) {
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.inferno");
        boolean meltdownEnabled = cfg.getBoolean("meltdown_enabled", true);
        if (meltdownEnabled) {
            Location loc = zombie.getLocation();
            loc.getBlock().setType(Material.LAVA);
            loc.clone().subtract(0.0, 1.0, 0.0).getBlock().setType(Material.LAVA);
        }
    }

    @EventHandler
    public void onZombieDeath(EntityDeathEvent event) {
        if (this.isManagedZombie((Entity)event.getEntity())) {
            ConfigurationSection lootSection;
            String type;
            Zombie zombie = (Zombie)event.getEntity();
            String chunkKey = zombie.getLocation().getChunk().getX() + "," + zombie.getLocation().getChunk().getZ();
            this.zombieChunkCount.put(chunkKey, this.zombieChunkCount.getOrDefault(chunkKey, 1) - 1);
            String zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
            if (zombieType != null) {
                switch (zombieType) {
                    case "burster": {
                        if (!this.bursterAbilities.getOrDefault("explosion_on_death", true).booleanValue()) break;
                        this.handleBursterDeath(zombie);
                        break;
                    }
                    case "cryo": {
                        if (!this.cryoAbilities.getOrDefault("shatter_explosion", true).booleanValue()) break;
                        this.handleCryoDeath(zombie);
                        break;
                    }
                    case "inferno": {
                        if (!this.infernoAbilities.getOrDefault("lava_on_death", true).booleanValue()) break;
                        this.handleInfernoDeath(zombie);
                        break;
                    }
                    case "leech": {
                        if (!this.leechAbilities.getOrDefault("healing_explosion", true).booleanValue()) break;
                        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.leech");
                        double healAmount = cfg.getDouble("death_heal_amount", 10.0);
                        double healRadius = cfg.getDouble("death_heal_radius", 8.0);
                        for (Entity entity : zombie.getNearbyEntities(healRadius, healRadius, healRadius)) {
                            if (!this.isManagedZombie(entity)) continue;
                            Zombie nearbyZombie = (Zombie)entity;
                            double currentHealth = nearbyZombie.getHealth();
                            double maxHealth = nearbyZombie.getAttribute(Attribute.MAX_HEALTH).getValue();
                            nearbyZombie.setHealth(Math.min(maxHealth, currentHealth + healAmount));
                        }
                        break;
                    }
                }
            }
            if ((type = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING)) == null) {
                type = "normal";
            }
            if ((lootSection = this.getConfig().getConfigurationSection("loot_drops." + type)) != null && lootSection.getBoolean("enabled", true)) {
                ConfigurationSection detailedItemsSection = lootSection.getConfigurationSection("detailed_items");
                if (detailedItemsSection != null && !detailedItemsSection.getKeys(false).isEmpty()) {
                    for (String detailedItemKey : detailedItemsSection.getKeys(false)) {
                        ConfigurationSection itemSection = detailedItemsSection.getConfigurationSection(detailedItemKey);
                        if (itemSection == null) continue;
                        String materialName = itemSection.getString("material");
                        double individualDropChance = itemSection.getDouble("drop_chance", 100.0);
                        if (materialName == null || !(this.random.nextDouble() * 100.0 < individualDropChance)) continue;
                        Material mat = Material.getMaterial((String)materialName.toUpperCase());
                        if (mat != null) {
                            event.getDrops().add(new ItemStack(mat));
                            continue;
                        }
                        this.getLogger().warning("Invalid material in loot_drops.detailed_items for " + type + ": " + materialName);
                    }
                } else {
                    List<String> itemNames = lootSection.getStringList("items");
                    double itemDropChance = lootSection.getDouble("item_drop_chance", 100.0);
                    for (String itemName : itemNames) {
                        if (!(this.random.nextDouble() * 100.0 < itemDropChance)) continue;
                        Material mat = Material.getMaterial((String)itemName.toUpperCase());
                        if (mat != null) {
                            event.getDrops().add(new ItemStack(mat));
                            continue;
                        }
                        this.getLogger().warning("Invalid material in loot_drops for " + type + ": " + itemName);
                    }
                }
            }
            if (this.random.nextDouble() * 100.0 < 5.0) {
                event.getDrops().add(this.createZombieHeart());
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (this.isManagedZombie(event.getDamager()) && event.getEntity() instanceof Player) {
            Zombie zombie = (Zombie)event.getDamager();
            Player player = (Player)event.getEntity();
            String zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
            if (zombieType != null) {
                if (zombieType.equals("cryo")) {
                    if (this.cryoAbilities.getOrDefault("flash_freeze", true).booleanValue()) {
                        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.cryo");
                        double flashFreezeChance = cfg.getDouble("flash_freeze_chance", 25.0);
                        if (this.random.nextDouble() * 100.0 < flashFreezeChance) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 4));
                        }
                    }
                } else if (zombieType.equals("leech") && this.leechAbilities.getOrDefault("life_steal_on_hit", true).booleanValue()) {
                    ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.leech");
                    double healMultiplier = cfg.getDouble("leech_on_hit_heal_multiplier", 0.5);
                    double damage = event.getDamage();
                    double currentHealth = zombie.getHealth();
                    double maxHealth = zombie.getAttribute(Attribute.MAX_HEALTH).getValue();
                    zombie.setHealth(Math.min(maxHealth, currentHealth + damage * healMultiplier));
                }
            }
        }
    }

    private void updateZombieAI(Zombie zombie) {
        long now;
        Map<UUID, Long> memory;
        Player target = this.findPriorityTarget(zombie);
        if (target == null && (memory = this.zombieTargetMemory.get(zombie.getUniqueId())) != null) {
            now = System.currentTimeMillis();
            UUID rememberedPlayerUUID = null;
            long closestExpiry = Long.MIN_VALUE;
            for (Map.Entry<UUID, Long> entry : memory.entrySet()) {
                if (entry.getValue() <= now || entry.getValue() <= closestExpiry) continue;
                closestExpiry = entry.getValue();
                rememberedPlayerUUID = entry.getKey();
            }
            if (rememberedPlayerUUID != null) {
                Player rememberedPlayer = Bukkit.getPlayer(rememberedPlayerUUID);
                if (rememberedPlayer != null && rememberedPlayer.isOnline() && !this.infected.isPlayerFullyInfected(rememberedPlayer)) {
                    target = rememberedPlayer;
                } else {
                    memory.remove(rememberedPlayerUUID);
                }
            }
        }
        if (target != null) {
            if (this.infected.isPlayerFullyInfected(target)) {
                zombie.setTarget(null);
                return;
            }
            if (zombie.getTarget() == null || !zombie.getTarget().equals(target)) {
                zombie.setTarget((LivingEntity)target);
            }
            this.zombieTargetMemory.computeIfAbsent(zombie.getUniqueId(), k -> new HashMap()).put(target.getUniqueId(), System.currentTimeMillis() + this.memoryDurationMs);
            String zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
            if (zombieType != null) {
                switch (zombieType) {
                    case "spitter": {
                        if (!this.spitterAbilities.getOrDefault("projectile_attack", true).booleanValue()) break;
                        this.handleSpitterAbility(zombie, target);
                        break;
                    }
                    case "burster": {
                        this.handleBursterAbility(zombie, target);
                        break;
                    }
                    case "shrieker": {
                        if (!this.shriekerAbilities.getOrDefault("aggro_nearby_zombies", true).booleanValue()) break;
                        this.handleShriekerAbility(zombie, target);
                        break;
                    }
                    case "inferno": {
                        if (!this.infernoAbilities.getOrDefault("fireball_attack", true).booleanValue()) break;
                        this.handleInfernoAbility(zombie, target);
                    }
                }
            }
            this.aggroNearbyZombies(zombie, target, this.packRadius);
            this.handleLeaderFollow(zombie, target);
            if (this.breakingEnabled) {
                this.handleBlockBreaking(zombie, target);
            }
        } else {
            String leaderUUIDStr;
            zombie.setTarget(null);
            memory = this.zombieTargetMemory.get(zombie.getUniqueId());
            if (memory != null) {
                now = System.currentTimeMillis();
                final long nowFinal = now;
                memory.entrySet().removeIf(e -> (Long)e.getValue() < nowFinal);
                if (memory.isEmpty()) {
                    this.zombieTargetMemory.remove(zombie.getUniqueId());
                }
            }
            if (zombie.getPersistentDataContainer().has(this.followLeaderKey, PersistentDataType.STRING) && (leaderUUIDStr = (String)zombie.getPersistentDataContainer().get(this.followLeaderKey, PersistentDataType.STRING)) != null) {
                try {
                    UUID leaderUUID = UUID.fromString(leaderUUIDStr);
                    Entity leader = Bukkit.getEntity((UUID)leaderUUID);
                    if (leader instanceof Zombie && leader.isValid()) {
                        zombie.getPathfinder().moveTo(leader.getLocation());
                    }
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
        }
    }

    private Player findPriorityTarget(Zombie zombie) {
        Location zLoc = zombie.getLocation();
        double closestDistanceSq = 2500.0;
        Player closest = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double distanceSq;
            if (!player.getWorld().equals(zombie.getWorld()) || this.infected.isPlayerFullyInfected(player) || this.safezoneManager.isInSafezone(player.getLocation()) || !((distanceSq = player.getLocation().distanceSquared(zLoc)) < closestDistanceSq)) continue;
            closestDistanceSq = distanceSq;
            closest = player;
        }
        return closest;
    }

    private void handleSpitterAbility(final Zombie spitter, Player target) {
        Location targetLoc;
        if (spitter.hasMetadata("spitter_cooldown")) {
            return;
        }
        Location spitterLoc = spitter.getLocation();
        if (spitterLoc.distance(targetLoc = target.getLocation()) <= 15.0) {
            ThrownPotion potion = (ThrownPotion)spitter.launchProjectile(ThrownPotion.class);
            potion.setItem(new ItemStack(Material.SPLASH_POTION));
            PotionMeta meta = (PotionMeta)potion.getItem().getItemMeta();
            if (meta != null) {
                meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 1), true);
                potion.getItem().setItemMeta((ItemMeta)meta);
            }
            Vector direction = targetLoc.toVector().subtract(spitterLoc.toVector()).normalize().multiply(1.2);
            potion.setVelocity(direction);
            spitter.setMetadata("spitter_cooldown", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
            new BukkitRunnable(){

                public void run() {
                    spitter.removeMetadata("spitter_cooldown", (Plugin)ZonaMuerta.this);
                }
            }.runTaskLater((Plugin)this, 100L);
        }
    }

    private void handleBursterAbility(Zombie burster, Player target) {
        double distance;
        if (!burster.isDead() && burster.isValid() && (distance = burster.getLocation().distance(target.getLocation())) <= 5.0 && this.bursterAbilities.getOrDefault("speed_boost_on_chase", true).booleanValue()) {
            burster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
        }
    }

    private void handleShriekerAbility(final Zombie shrieker, Player target) {
        if (shrieker.hasMetadata("shrieker_cooldown")) {
            return;
        }
        shrieker.getWorld().playSound(shrieker.getLocation(), Sound.ENTITY_GHAST_SCREAM, 2.0f, 0.5f);
        shrieker.getWorld().spawnParticle(Particle.SONIC_BOOM, shrieker.getLocation(), 1);
        this.aggroNearbyZombies(shrieker, target, 50);
        shrieker.setMetadata("shrieker_cooldown", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
        new BukkitRunnable(){

            public void run() {
                shrieker.removeMetadata("shrieker_cooldown", (Plugin)ZonaMuerta.this);
            }
        }.runTaskLater((Plugin)this, 200L);
    }

    private void handleInfernoAbility(final Zombie zombie, Player target) {
        Location targetLoc;
        if (zombie.hasMetadata("inferno_cooldown")) {
            return;
        }
        ConfigurationSection cfg = this.getConfig().getConfigurationSection("mutation_types.inferno");
        int fireballCooldown = cfg.getInt("fireball_cooldown", 5);
        Location zombieLoc = zombie.getLocation();
        if (zombieLoc.distance(targetLoc = target.getLocation()) <= 20.0) {
            zombie.launchProjectile(SmallFireball.class, targetLoc.toVector().subtract(zombieLoc.toVector()).normalize());
            zombie.setMetadata("inferno_cooldown", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
            new BukkitRunnable(){

                public void run() {
                    zombie.removeMetadata("inferno_cooldown", (Plugin)ZonaMuerta.this);
                }
            }.runTaskLater((Plugin)this, (long)(fireballCooldown * 20));
        }
    }

    private void aggroNearbyZombies(Zombie sourceZombie, Player target, int radius) {
        if (target == null) {
            return;
        }
        Location loc = sourceZombie.getLocation();
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, (double)radius, (double)radius, (double)radius)) {
            Zombie z;
            if (!this.isManagedZombie(entity) || (z = (Zombie)entity).getTarget() != null && z.getTarget().equals(target)) continue;
            z.setTarget((LivingEntity)target);
            this.zombieTargetMemory.computeIfAbsent(z.getUniqueId(), k -> new HashMap()).put(target.getUniqueId(), System.currentTimeMillis() + this.memoryDurationMs);
        }
    }

    private void handleLeaderFollow(Zombie zombie, Player target) {
        String leaderUUIDStr;
        if (zombie.getPersistentDataContainer().has(this.leaderKey, PersistentDataType.INTEGER)) {
            return;
        }
        if (zombie.getPersistentDataContainer().has(this.followLeaderKey, PersistentDataType.STRING) && (leaderUUIDStr = (String)zombie.getPersistentDataContainer().get(this.followLeaderKey, PersistentDataType.STRING)) != null) {
            try {
                UUID leaderUUID = UUID.fromString(leaderUUIDStr);
                Entity leader = Bukkit.getEntity((UUID)leaderUUID);
                if (leader instanceof Zombie && leader.isValid()) {
                    Zombie leaderZombie = (Zombie)leader;
                    if (leaderZombie.getTarget() != null) {
                        if (zombie.getTarget() == null || !zombie.getTarget().equals(leaderZombie.getTarget())) {
                            zombie.setTarget(leaderZombie.getTarget());
                        }
                    } else if (zombie.getLocation().distance(leader.getLocation()) > (double)this.followRadius) {
                        zombie.getPathfinder().moveTo(leader.getLocation());
                    }
                }
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
    }

    private void handleBlockBreaking(Zombie zombie, Player target) {
        Location targetLoc;
        if (!this.whitelistedWorlds.isEmpty() && !this.whitelistedWorlds.contains(zombie.getWorld().getName())) {
            return;
        }
        if (target == null || !target.isOnline()) {
            return;
        }
        if (!this.breakingEnabled) {
            return;
        }
        if (zombie.getPersistentDataContainer().has(this.breakingCooldownKey, PersistentDataType.LONG)) {
            long cooldownEnd = (Long)zombie.getPersistentDataContainer().get(this.breakingCooldownKey, PersistentDataType.LONG);
            if (System.currentTimeMillis() < cooldownEnd) {
                return;
            }
        }
        if (!zombie.getWorld().equals(target.getWorld())) {
            return;
        }
        Location zombieLoc = zombie.getLocation();
        if (zombieLoc.distance(targetLoc = target.getLocation()) > (double)this.breakingRadius) {
            return;
        }
        Vector direction = targetLoc.toVector().subtract(zombieLoc.toVector()).normalize();
        Location checkLoc = zombieLoc.clone().add(direction);
        for (int i = 0; i <= 2; ++i) {
            final Location blockLoc = checkLoc.clone().add(direction.clone().multiply(i));
            blockLoc.setY((double)zombieLoc.getBlockY());
            Material blockType = blockLoc.getBlock().getType();
            if (!this.breakableBlocks.contains(blockType)) continue;
            zombie.getWorld().playSound(blockLoc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            this.showZombieBlockDamage(zombie, blockLoc, 0.5f);
            final int blockCooldownTicks = this.breakableBlocksCooldown.getOrDefault(blockType, this.defaultBreakingCooldownTicks);
            new BukkitRunnable(){
                int ticksPassed = 0;

                public void run() {
                    if (this.ticksPassed >= blockCooldownTicks) {
                        blockLoc.getBlock().breakNaturally();
                        this.cancel();
                    } else {
                        ++this.ticksPassed;
                    }
                }
            }.runTaskTimer((Plugin)this, 0L, 1L);
            zombie.getPersistentDataContainer().set(this.breakingCooldownKey, PersistentDataType.LONG, (System.currentTimeMillis() + (long)blockCooldownTicks * 50L));
            break;
        }
    }

    @EventHandler
    public void onCreatureTarget(EntityTargetEvent event) {
        Creature creature;
        Player player;
        if (!this.infected.isInfectionEnabled()) {
            return;
        }
        Entity target = event.getTarget();
        Entity entity = event.getEntity();
        if (target instanceof Player && this.infected.isPlayerFullyInfected(player = (Player)target) && entity instanceof Creature && ((creature = (Creature)entity).getType() == EntityType.VILLAGER || creature.getType() == EntityType.COW || creature.getType() == EntityType.SHEEP)) {
            event.setCancelled(true);
        }
    }

    public void stopBloodMoon() {
        this.bloodMoonForced = false;
        this.deactivateBloodMoon(false);
        Bukkit.broadcastMessage((String)(String.valueOf(ChatColor.GREEN) + "\u2726 La Luna de Sangre ha terminado \u2726"));
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.5f, 0.7f));
    }

    void sendHelp(CommandSender sender) {
        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "Comandos de Zona Muerta:");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm reload " + String.valueOf(ChatColor.GRAY) + "- Recargar config");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm info " + String.valueOf(ChatColor.GRAY) + "- Mostrar estado actual");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm spawn <tipo> " + String.valueOf(ChatColor.GRAY) + "- Spawnear un zombie");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm matarzombies " + String.valueOf(ChatColor.GRAY) + "- Elimina todos los zombies");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm limpiarzombies <radio> " + String.valueOf(ChatColor.GRAY) + "- Limpiar zombies en radio");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm evento <iniciar/parar> <lunaroja> " + String.valueOf(ChatColor.GRAY) + "- Controlar eventos especiales");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm forzarlunaroja " + String.valueOf(ChatColor.GRAY) + "- Forzar luna de sangre");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm pararlunaroja " + String.valueOf(ChatColor.GRAY) + "- Desactivar luna de sangre");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm cura dar <jugador> <cantidad> " + String.valueOf(ChatColor.GRAY) + "- Dar pociones de cura");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm dificultad <nombre> " + String.valueOf(ChatColor.GRAY) + "- Cambiar dificultad");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm zonasegura <crear|eliminar|lista|expandir> [nombre] " + String.valueOf(ChatColor.GRAY) + "- Gestionar zonas seguras");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "/zm crafteo " + String.valueOf(ChatColor.GRAY) + "- Abrir menu de crafteo");
        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "Tipos disponibles: normal, tank, charger, assassin, blight, burster, spitter, frost, shrieker, cryo, inferno");
        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "Dificultades disponibles: casual, normal, hardcore, pesadilla");
    }

    void sendStatusInfo(CommandSender sender) {
        sender.sendMessage(String.valueOf(ChatColor.GOLD) + "\u2726 Estado de Zona Muerta \u2726");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Dia actual: " + String.valueOf(ChatColor.WHITE) + this.currentDay);
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Tasa de mutacion: " + String.valueOf(ChatColor.WHITE) + this.currentMutationRate + "%");
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Dificultad: " + String.valueOf(ChatColor.WHITE) + this.currentPreset);
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Luna de Sangre: " + (this.isBloodMoon ? String.valueOf(ChatColor.DARK_RED) + "ACTIVA" : String.valueOf(ChatColor.GREEN) + "inactiva"));
        int totalZombies = 0;
        HashMap<String, Integer> typeCounts = new HashMap<String, Integer>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!this.isManagedZombie(entity)) continue;
                ++totalZombies;
                String type = (String)((Zombie)entity).getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
                if (type != null) {
                    typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
                    continue;
                }
                typeCounts.put("normal", typeCounts.getOrDefault("normal", 0) + 1);
            }
        }
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Zombies activos: " + String.valueOf(ChatColor.WHITE) + totalZombies);
        for (Map.Entry entry : typeCounts.entrySet()) {
            sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "  " + (String)entry.getKey() + ": " + String.valueOf(ChatColor.WHITE) + String.valueOf(entry.getValue()));
        }
        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Zonas seguras activas: " + String.valueOf(ChatColor.WHITE) + this.safezoneManager.getSafezoneCount());
    }

    public void handleZombieSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Especifica tipo: normal, tank, charger, assassin, blight, burster, spitter, frost, shrieker, cryo, inferno");
            return;
        }
        if (!this.canSpawnZombiesAt(player.getWorld(), player.getLocation())) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "No se pueden spawnear zombies en este momento o ubicacion por la configuracion del plugin.");
            return;
        }
        String type = args[1].toLowerCase();
        if (!this.isMutationAllowedInWorld(player.getWorld(), type)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "El tipo de mutacion '" + type + "' esta desactivado en este mundo.");
            return;
        }
        double speed = this.getConfig().getDouble(BASE_SPEED) * (1.0 + 0.05 * (double)this.currentDay);
        double damage = this.getConfig().getDouble(BASE_DAMAGE) * (1.0 + 0.05 * (double)this.currentDay);
        Zombie zombie = (Zombie)player.getWorld().spawnEntity(player.getLocation().add(3.0, 0.0, 3.0), EntityType.ZOMBIE);
        zombie.setCanPickupItems(false);
        switch (type) {
            case "tank": {
                this.createTank(zombie, speed, damage);
                break;
            }
            case "charger": {
                this.createCharger(zombie, speed, damage);
                break;
            }
            case "assassin": {
                this.createAssassin(zombie, damage);
                break;
            }
            case "blight": {
                this.createBlightZombie(zombie, damage);
                break;
            }
            case "burster": {
                this.createBurster(zombie, speed, damage);
                break;
            }
            case "spitter": {
                this.createSpitter(zombie, speed, damage);
                break;
            }
            case "frost": {
                this.createFrostZombie(zombie, damage);
                break;
            }
            case "shrieker": {
                this.createShrieker(zombie, speed);
                break;
            }
            case "cryo": {
                this.createCryoZombie(zombie, speed, damage);
                break;
            }
            case "inferno": {
                this.createInfernoZombie(zombie, speed, damage);
                break;
            }
            default: {
                this.setupNormalZombie(zombie);
            }
        }
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "Zombie " + type + " spawneado!");
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1.0f, 0.8f);
    }

    public void killCustomZombies() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!this.isManagedZombie(entity) || !entity.getPersistentDataContainer().has(this.zombieTypeKey, PersistentDataType.STRING)) continue;
                entity.remove();
            }
        }
    }

    public void clearZombiesInRadius(Player player, int radius) {
        Location center = player.getLocation();
        World world = center.getWorld();
        for (Entity entity : world.getNearbyEntities(center, (double)radius, (double)radius, (double)radius)) {
            if (!this.isManagedZombie(entity) || !entity.getPersistentDataContainer().has(this.zombieTypeKey, PersistentDataType.STRING) && !entity.getPersistentDataContainer().has(this.zombieLevelKey, PersistentDataType.INTEGER)) continue;
            entity.remove();
        }
    }

    public NamespacedKey getNamespacedKey(String keyName) {
        return new NamespacedKey((Plugin)this, keyName);
    }

    public int getExpForLevel(int level) {
        return 10 * level;
    }

    public int getZombieLevel(Zombie zombie) {
        return zombie.getPersistentDataContainer().get(this.zombieLevelKey, PersistentDataType.INTEGER) != null ? (Integer)zombie.getPersistentDataContainer().get(this.zombieLevelKey, PersistentDataType.INTEGER) : 1;
    }

    public void setZombieLevel(Zombie zombie, int level) {
        zombie.getPersistentDataContainer().set(this.zombieLevelKey, PersistentDataType.INTEGER, level);
        this.updateZombieLevelStats(zombie, level);
    }

    public void updateZombieLevelStats(Zombie zombie, int level) {
        String zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
        double baseSpeed = zombie.getAttribute(Attribute.MOVEMENT_SPEED).getDefaultValue();
        double baseDamage = zombie.getAttribute(Attribute.ATTACK_DAMAGE).getDefaultValue();
        double baseHealth = zombie.getAttribute(Attribute.MAX_HEALTH).getDefaultValue();
        double speedMultiplier = 1.0 + (double)level * 0.01;
        double damageMultiplier = 1.0 + (double)level * 0.01;
        double healthMultiplier = 1.0 + (double)level * 0.01;
        zombie.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
        zombie.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(baseDamage * damageMultiplier);
        double currentHealthPercent = zombie.getHealth() / zombie.getAttribute(Attribute.MAX_HEALTH).getValue();
        zombie.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth * healthMultiplier);
        zombie.setHealth(zombie.getAttribute(Attribute.MAX_HEALTH).getValue() * currentHealthPercent);
    }

    public void addZombieExp(Zombie zombie, double exp) {
        if (!this.evolutionEnabled) {
            return;
        }
        int currentLevel = this.getZombieLevel(zombie);
        int currentExp = zombie.getPersistentDataContainer().get(this.zombieExpKey, PersistentDataType.INTEGER) != null ? (Integer)zombie.getPersistentDataContainer().get(this.zombieExpKey, PersistentDataType.INTEGER) : 0;
        currentExp += (int)exp;
        int expForNextLevel = this.getExpForLevel(currentLevel);
        while (currentExp >= expForNextLevel && currentLevel < this.maxLevel) {
            String zombieType;
            currentExp -= expForNextLevel;
            if (++currentLevel >= this.maxLevel && ((zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING)) == null || zombieType.equals("normal"))) {
                double speed = zombie.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
                double damage = zombie.getAttribute(Attribute.ATTACK_DAMAGE).getBaseValue();
                this.applyRandomMutation(zombie, speed, damage);
                break;
            }
            this.setZombieLevel(zombie, currentLevel);
            expForNextLevel = this.getExpForLevel(currentLevel);
        }
        zombie.getPersistentDataContainer().set(this.zombieExpKey, PersistentDataType.INTEGER, currentExp);
        this.updateZombieNameWithLevel(zombie);
    }

    public void updateZombieNameWithLevel(Zombie zombie) {
        String baseName;
        if (!this.showMutationNamesAboveZombies) {
            return;
        }
        String zombieType = (String)zombie.getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING);
        if (zombieType == null) {
            zombieType = "normal";
        }
        int level = this.getZombieLevel(zombie);
        String string = baseName = zombie.getCustomName() != null ? zombie.getCustomName() : "Zombie";
        if (baseName.contains(" LV")) {
            baseName = baseName.substring(0, baseName.indexOf(" LV"));
        }
        String newName = baseName + " LV" + level;
        if (zombieType.equals("normal") || zombieType.equals("tank") || zombieType.equals("charger") || zombieType.equals("assassin") || zombieType.equals("blight") || zombieType.equals("burster") || zombieType.equals("spitter") || zombieType.equals("frost") || zombieType.equals("shrieker") || zombieType.equals("cryo") || zombieType.equals("inferno") || zombieType.equals("leech")) {
            zombie.setCustomName(newName);
        }
    }

    private void showZombieBlockDamage(Zombie zombie, Location blockLoc, float progress) {
        float clamped = Math.max(0.0f, Math.min(progress, 1.0f));
        try {
            for (Player viewer : zombie.getWorld().getPlayers()) {
                if (!(viewer.getLocation().distanceSquared(blockLoc) < 400.0)) continue;
                viewer.sendBlockDamage(blockLoc, clamped);
            }
        }
        catch (NoSuchMethodError e) {
            zombie.getWorld().playSound(blockLoc, Sound.BLOCK_WOOD_HIT, 1.0f, 1.0f);
            zombie.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, blockLoc.clone().add(0.5, 0.5, 0.5), 10, 0.3, 0.3, 0.3, blockLoc.getBlock().getBlockData());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getWhoClicked();
        boolean inCureGui = player.hasMetadata("cure_gui_open");
        boolean inCraftingMenu = player.hasMetadata("crafting_menu_open");
        boolean inPurifiedHeartGui = player.hasMetadata("purified_heart_gui_open");
        if (!(inCureGui || inCraftingMenu || inPurifiedHeartGui)) {
            return;
        }
        event.setCancelled(true);
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory().equals(player.getInventory())) {
            return;
        }
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        if (inCraftingMenu) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                if (meta.getDisplayName().contains("Cura")) {
                    player.closeInventory();
                    this.openCurePotionGUI(player);
                } else if (meta.getDisplayName().contains("Corazon de Zombie Purificado")) {
                    player.closeInventory();
                    this.openPurifiedZombieHeartGUI(player);
                } else if (meta.getDisplayName().contains("Cerrar")) {
                    player.closeInventory();
                }
            }
            return;
        }
        if (inCureGui) {
            ItemMeta meta;
            if (clickedItem.getType() == Material.CRAFTING_TABLE && (meta = clickedItem.getItemMeta()) != null && meta.hasDisplayName() && meta.getDisplayName().contains("Craftear Pocion de Cura")) {
                this.craftCurePotion(player);
                player.closeInventory();
            }
            return;
        }
        if (inPurifiedHeartGui) {
            ItemMeta meta;
            if (clickedItem.getType() == Material.CRAFTING_TABLE && (meta = clickedItem.getItemMeta()) != null && meta.hasDisplayName() && meta.getDisplayName().contains("Craftear Corazon Purificado")) {
                this.craftPurifiedZombieHeart(player);
                player.closeInventory();
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player)event.getPlayer();
        if (player.hasMetadata("cure_gui_open")) {
            player.removeMetadata("cure_gui_open", (Plugin)this);
        }
        if (player.hasMetadata("crafting_menu_open")) {
            player.removeMetadata("crafting_menu_open", (Plugin)this);
        }
        if (player.hasMetadata("purified_heart_gui_open")) {
            player.removeMetadata("purified_heart_gui_open", (Plugin)this);
        }
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        Zombie zombie;
        if (this.isManagedZombie(event.getEntity()) && ((zombie = (Zombie)event.getEntity()).getPersistentDataContainer().has(this.zombieTypeKey, PersistentDataType.STRING) || zombie.getPersistentDataContainer().has(this.zombieLevelKey, PersistentDataType.INTEGER)) && event.getTransformReason() == EntityTransformEvent.TransformReason.DROWNED) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onZombieKillEntity(EntityDeathEvent event) {
        Zombie zombie;
        String zombieType;
        if (!this.evolutionEnabled) {
            return;
        }
        Player killer = event.getEntity().getKiller();
        if (this.isManagedZombie((Entity)killer) && ((zombieType = (String)(zombie = (Zombie)killer).getPersistentDataContainer().get(this.zombieTypeKey, PersistentDataType.STRING)) != null || zombie.getPersistentDataContainer().has(this.zombieLevelKey, PersistentDataType.INTEGER))) {
            if (event.getEntity() instanceof Player) {
                this.addZombieExp(zombie, this.expPerPlayerKill);
            } else if (event.getEntity() instanceof Animals || event.getEntity() instanceof Villager) {
                this.addZombieExp(zombie, this.expPerPassiveMob);
            }
        }
    }

    public void openCurePotionGUI(Player player) {
        this.debug("CureGUI: Opening cure potion GUI for " + player.getName());
        Inventory gui = Bukkit.createInventory(null, (int)27, (String)(String.valueOf(ChatColor.DARK_RED) + "Receta de Pocion de Cura"));
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        for (int i = 0; i < 27; ++i) {
            gui.setItem(i, glassPane);
        }
        ItemStack waterBottle = new ItemStack(Material.POTION);
        ItemMeta waterMeta = waterBottle.getItemMeta();
        waterMeta.setDisplayName(String.valueOf(ChatColor.AQUA) + "Botella de Agua");
        waterBottle.setItemMeta(waterMeta);
        ItemStack rottenFlesh = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta fleshMeta = rottenFlesh.getItemMeta();
        fleshMeta.setDisplayName(String.valueOf(ChatColor.DARK_GREEN) + "Carne Podrida");
        rottenFlesh.setItemMeta(fleshMeta);
        ItemStack diamondBlock = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta diamondMeta = diamondBlock.getItemMeta();
        diamondMeta.setDisplayName(String.valueOf(ChatColor.AQUA) + "Bloque de Diamante");
        diamondBlock.setItemMeta(diamondMeta);
        gui.setItem(13, waterBottle);
        gui.setItem(12, rottenFlesh);
        gui.setItem(14, rottenFlesh);
        gui.setItem(4, diamondBlock);
        gui.setItem(22, diamondBlock);
        ItemStack craftButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftButton.getItemMeta();
        craftMeta.setDisplayName(String.valueOf(ChatColor.GREEN) + "Craftear Pocion de Cura");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(String.valueOf(ChatColor.GRAY) + "Haz clic para craftear la pocion de cura");
        lore.add(String.valueOf(ChatColor.GRAY) + "si tienes los materiales necesarios");
        craftMeta.setLore(lore);
        craftButton.setItemMeta(craftMeta);
        gui.setItem(26, craftButton);
        ItemStack curePotion = this.createCurePotion();
        gui.setItem(15, curePotion);
        player.openInventory(gui);
        player.setMetadata("cure_gui_open", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
    }

    public void giveCurePotions(CommandSender sender, String targetName, int amount) {
        int stackSize;
        Player target = Bukkit.getPlayerExact((String)targetName);
        if (target == null) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "El jugador '" + targetName + "' no esta conectado.");
            return;
        }
        for (int remaining = amount; remaining > 0; remaining -= stackSize) {
            stackSize = Math.min(64, remaining);
            ItemStack stack = this.createCurePotion();
            stack.setAmount(stackSize);
            HashMap<Integer, ItemStack> leftovers = target.getInventory().addItem(new ItemStack[]{stack});
            for (ItemStack leftover : leftovers.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            }
        }
        sender.sendMessage(String.valueOf(ChatColor.GREEN) + "Se dieron " + amount + " pocion(es) de cura a " + target.getName() + ".");
        if (!sender.getName().equalsIgnoreCase(target.getName())) {
            target.sendMessage(String.valueOf(ChatColor.GREEN) + "Recibiste " + amount + " pocion(es) de cura.");
        }
    }

    public ItemStack createCurePotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta)potion.getItemMeta();
        meta.setDisplayName(String.valueOf(ChatColor.DARK_RED) + "Pocion de Cura");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(String.valueOf(ChatColor.GRAY) + "Una pocion poderosa que");
        lore.add(String.valueOf(ChatColor.GRAY) + "cura la infeccion y");
        lore.add(String.valueOf(ChatColor.GRAY) + "convierte a los zombies");
        lore.add(String.valueOf(ChatColor.GRAY) + "de vuelta en jugadores");
        meta.setLore(lore);
        meta.setCustomModelData(Integer.valueOf(999));
        potion.setItemMeta((ItemMeta)meta);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(this.curePotionKey, PersistentDataType.STRING, "cure_potion");
        potion.setItemMeta((ItemMeta)meta);
        return potion;
    }

    public ItemStack createZombieHeart() {
        ItemStack heart = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta meta = heart.getItemMeta();
        meta.setDisplayName(String.valueOf(ChatColor.DARK_RED) + "Corazon de Zombie");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(String.valueOf(ChatColor.GRAY) + "Un corazon que aun late");
        lore.add(String.valueOf(ChatColor.GRAY) + "arrancado de un zombie");
        meta.setLore(lore);
        meta.setMaxStackSize(Integer.valueOf(64));
        meta.getPersistentDataContainer().set(this.zombieHeartKey, PersistentDataType.STRING, "zombie_heart");
        heart.setItemMeta(meta);
        return heart;
    }

    public ItemStack createPurifiedZombieHeart() {
        ItemStack heart = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta meta = heart.getItemMeta();
        meta.setDisplayName(String.valueOf(ChatColor.LIGHT_PURPLE) + "Corazon de Zombie Purificado");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(String.valueOf(ChatColor.GRAY) + "Un corazon de zombie purificado");
        lore.add(String.valueOf(ChatColor.GRAY) + "infundido con la cura");
        lore.add("");
        lore.add(String.valueOf(ChatColor.GREEN) + "Come para ganar +2 corazones permanentes");
        meta.setLore(lore);
        meta.setMaxStackSize(Integer.valueOf(64));
        meta.getPersistentDataContainer().set(this.purifiedZombieHeartKey, PersistentDataType.STRING, "purified_zombie_heart");
        heart.setItemMeta(meta);
        return heart;
    }

    public void openCraftingMenuGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, (int)27, (String)(String.valueOf(ChatColor.GOLD) + "Menu de Crafteo"));
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        for (int i = 0; i < 27; ++i) {
            gui.setItem(i, glassPane);
        }
        ItemStack cureButton = new ItemStack(Material.POTION);
        ItemMeta cureMeta = cureButton.getItemMeta();
        cureMeta.setDisplayName(String.valueOf(ChatColor.DARK_RED) + "Cura");
        ArrayList<String> cureLore = new ArrayList<String>();
        cureLore.add(String.valueOf(ChatColor.GRAY) + "Haz clic para abrir la");
        cureLore.add(String.valueOf(ChatColor.GRAY) + "receta de Pocion de Cura");
        cureMeta.setLore(cureLore);
        cureButton.setItemMeta(cureMeta);
        gui.setItem(0, cureButton);
        ItemStack purifiedButton = new ItemStack(Material.ROTTEN_FLESH);
        ItemMeta purifiedMeta = purifiedButton.getItemMeta();
        purifiedMeta.setDisplayName(String.valueOf(ChatColor.LIGHT_PURPLE) + "Corazon de Zombie Purificado");
        ArrayList<String> purifiedLore = new ArrayList<String>();
        purifiedLore.add(String.valueOf(ChatColor.GRAY) + "Haz clic para abrir la");
        purifiedLore.add(String.valueOf(ChatColor.GRAY) + "receta de Corazon Purificado");
        purifiedMeta.setLore(purifiedLore);
        purifiedButton.setItemMeta(purifiedMeta);
        gui.setItem(1, purifiedButton);
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(String.valueOf(ChatColor.RED) + "Cerrar");
        ArrayList<String> closeLore = new ArrayList<String>();
        closeLore.add(String.valueOf(ChatColor.GRAY) + "Haz clic para cerrar este menu");
        closeMeta.setLore(closeLore);
        closeButton.setItemMeta(closeMeta);
        gui.setItem(26, closeButton);
        player.openInventory(gui);
        player.setMetadata("crafting_menu_open", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
    }

    public void openPurifiedZombieHeartGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, (int)27, (String)(String.valueOf(ChatColor.LIGHT_PURPLE) + "Receta de Corazon Purificado"));
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        for (int i = 0; i < 27; ++i) {
            gui.setItem(i, glassPane);
        }
        ItemStack zombieHeart = this.createZombieHeart();
        ItemStack curePotion = this.createCurePotion();
        gui.setItem(3, zombieHeart.clone());
        gui.setItem(4, zombieHeart.clone());
        gui.setItem(5, zombieHeart.clone());
        gui.setItem(12, zombieHeart.clone());
        gui.setItem(13, curePotion);
        gui.setItem(14, zombieHeart.clone());
        gui.setItem(21, zombieHeart.clone());
        gui.setItem(22, zombieHeart.clone());
        gui.setItem(23, zombieHeart.clone());
        ItemStack result = this.createPurifiedZombieHeart();
        gui.setItem(15, result);
        ItemStack craftButton = new ItemStack(Material.CRAFTING_TABLE);
        ItemMeta craftMeta = craftButton.getItemMeta();
        craftMeta.setDisplayName(String.valueOf(ChatColor.GREEN) + "Craftear Corazon Purificado");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(String.valueOf(ChatColor.GRAY) + "Haz clic para craftear si tienes");
        lore.add(String.valueOf(ChatColor.GRAY) + "los materiales necesarios");
        craftMeta.setLore(lore);
        craftButton.setItemMeta(craftMeta);
        gui.setItem(26, craftButton);
        player.openInventory(gui);
        player.setMetadata("purified_heart_gui_open", (MetadataValue)new FixedMetadataValue((Plugin)this, true));
    }

    public void craftPurifiedZombieHeart(Player player) {
        int zombieHearts = 0;
        int curePotions = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (this.isZombieHeart(item)) {
                zombieHearts += item.getAmount();
                continue;
            }
            if (!this.isCurePotionItem(item)) continue;
            curePotions += item.getAmount();
        }
        if (zombieHearts >= 8 && curePotions >= 1) {
            this.removeCustomItems((Inventory)player.getInventory(), this.zombieHeartKey, 8);
            this.removeCurePotionItems((Inventory)player.getInventory(), 1);
            ItemStack purifiedHeart = this.createPurifiedZombieHeart();
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(new ItemStack[]{purifiedHeart});
            for (ItemStack leftoverItem : leftOver.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Has crafteado un Corazon de Zombie Purificado!");
        } else {
            player.sendMessage(String.valueOf(ChatColor.RED) + "No tienes los materiales necesarios!");
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Need: 8x Zombie Hearts, 1x Cure Potion");
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Tienes: " + zombieHearts + " corazones de zombie, " + curePotions + " pociones de cura");
        }
    }

    private boolean isZombieHeart(ItemStack item) {
        if (item == null || item.getType() != Material.ROTTEN_FLESH) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(this.zombieHeartKey, PersistentDataType.STRING);
    }

    private boolean isCurePotionItem(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(this.curePotionKey, PersistentDataType.STRING);
    }

    private boolean isPurifiedZombieHeart(ItemStack item) {
        if (item == null || item.getType() != Material.ROTTEN_FLESH) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(this.purifiedZombieHeartKey, PersistentDataType.STRING);
    }

    private void removeCustomItems(Inventory inventory, NamespacedKey key, int amount) {
        int removed = 0;
        for (int i = 0; i < inventory.getSize() && removed < amount; ++i) {
            ItemMeta meta;
            ItemStack item = inventory.getItem(i);
            if (item == null || (meta = item.getItemMeta()) == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) continue;
            int toRemove = Math.min(item.getAmount(), amount - removed);
            if (item.getAmount() <= toRemove) {
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - toRemove);
            }
            removed += toRemove;
        }
    }

    private void removeCurePotionItems(Inventory inventory, int amount) {
        int removed = 0;
        for (int i = 0; i < inventory.getSize() && removed < amount; ++i) {
            ItemStack item = inventory.getItem(i);
            if (item == null || !this.isCurePotionItem(item)) continue;
            int toRemove = Math.min(item.getAmount(), amount - removed);
            if (item.getAmount() <= toRemove) {
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - toRemove);
            }
            removed += toRemove;
        }
    }

    @EventHandler
    public void onPlayerConsumePurifiedHeart(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (this.isPurifiedZombieHeart(item)) {
            event.setCancelled(true);
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (this.isPurifiedZombieHeart(handItem)) {
                if (handItem.getAmount() > 1) {
                    handItem.setAmount(handItem.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
            } else {
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (this.isPurifiedZombieHeart(offHandItem)) {
                    if (offHandItem.getAmount() > 1) {
                        offHandItem.setAmount(offHandItem.getAmount() - 1);
                    } else {
                        player.getInventory().setItemInOffHand(null);
                    }
                }
            }
            double currentMax = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMax + 4.0);
            player.setHealth(Math.min(player.getHealth() + 4.0, player.getAttribute(Attribute.MAX_HEALTH).getBaseValue()));
            int currentExtraHearts = 0;
            if (player.getPersistentDataContainer().has(this.extraHeartsKey, PersistentDataType.INTEGER)) {
                currentExtraHearts = (Integer)player.getPersistentDataContainer().get(this.extraHeartsKey, PersistentDataType.INTEGER);
            }
            player.getPersistentDataContainer().set(this.extraHeartsKey, PersistentDataType.INTEGER, (currentExtraHearts + 2));
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Ganaste +2 corazones permanentes del Corazon de Zombie Purificado!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }

    public void craftCurePotion(Player player) {
        this.debug("CurePotion: Starting craft process for " + player.getName());
        int waterBottles = 0;
        int rottenFlesh = 0;
        int diamondBlocks = 0;
        this.debug("CurePotion: Scanning inventory...");
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            if (item.getType() == Material.POTION) {
                PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
                if (potionMeta == null) continue;
                PotionType baseType = potionMeta.getBasePotionType();
                this.debug("CurePotion: Found potion with base type: " + String.valueOf(baseType));
                if (baseType == PotionType.WATER || baseType == PotionType.AWKWARD || baseType == PotionType.MUNDANE || baseType == PotionType.THICK) {
                    waterBottles += item.getAmount();
                    this.debug("CurePotion: Added " + item.getAmount() + " water bottles (matched type)");
                }
                if (baseType != null) continue;
                waterBottles += item.getAmount();
                this.debug("CurePotion: Added " + item.getAmount() + " water bottles (null type)");
                continue;
            }
            if (item.getType() == Material.ROTTEN_FLESH) {
                rottenFlesh += item.getAmount();
                this.debug("CurePotion: Found " + item.getAmount() + " rotten flesh");
                continue;
            }
            if (item.getType() != Material.DIAMOND_BLOCK) continue;
            diamondBlocks += item.getAmount();
            this.debug("CurePotion: Found " + item.getAmount() + " diamond blocks");
        }
        this.debug("CurePotion: Inventory totals - Water: " + waterBottles + ", Flesh: " + rottenFlesh + ", Diamond: " + diamondBlocks);
        if (waterBottles >= 1 && rottenFlesh >= 2 && diamondBlocks >= 2) {
            this.debug("CurePotion: Requirements met, removing items...");
            this.removeWaterBottles((Inventory)player.getInventory(), 1);
            this.debug("CurePotion: Removed 1 water bottle");
            this.removeItems((Inventory)player.getInventory(), Material.ROTTEN_FLESH, 2);
            this.debug("CurePotion: Removed 2 rotten flesh");
            this.removeItems((Inventory)player.getInventory(), Material.DIAMOND_BLOCK, 2);
            this.debug("CurePotion: Removed 2 diamond blocks");
            this.debug("CurePotion: Creating cure potion...");
            ItemStack curePotion = this.createCurePotion();
            HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(new ItemStack[]{curePotion});
            this.debug("CurePotion: Added cure potion to inventory");
            for (ItemStack leftoverPotion : leftOver.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverPotion);
                this.debug("CurePotion: Inventory full, dropped potion on ground");
            }
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Has crafteado una Pocion de Cura!");
            this.debug("CurePotion: Craft successful!");
        } else {
            this.debug("CurePotion: Requirements NOT met");
            player.sendMessage(String.valueOf(ChatColor.RED) + "No tienes los materiales necesarios!");
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Need: 1x Water Bottle, 2x Rotten Flesh, 2x Diamond Blocks");
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "Tienes: " + waterBottles + " botellas de agua, " + rottenFlesh + " carne podrida, " + diamondBlocks + " bloques de diamante");
        }
    }

    private void removeWaterBottles(Inventory inventory, int amount) {
        int removed = 0;
        for (int i = 0; i < inventory.getSize() && removed < amount; ++i) {
            PotionType baseType;
            PotionMeta potionMeta;
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() != Material.POTION || (potionMeta = (PotionMeta)item.getItemMeta()) == null || (baseType = potionMeta.getBasePotionType()) != PotionType.WATER && baseType != PotionType.AWKWARD && baseType != PotionType.MUNDANE && baseType != PotionType.THICK && baseType != null) continue;
            int toRemove = Math.min(item.getAmount(), amount - removed);
            if (item.getAmount() <= toRemove) {
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - toRemove);
            }
            removed += toRemove;
        }
    }

    private void removeItems(Inventory inventory, Material material, int amount) {
        int removed = 0;
        for (int i = 0; i < inventory.getSize() && removed < amount; ++i) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() != material) continue;
            int toRemove = Math.min(item.getAmount(), amount - removed);
            if (item.getAmount() <= toRemove) {
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - toRemove);
            }
            removed += toRemove;
        }
    }

    private class SunlightProtectionListener
    implements Listener {
        private SunlightProtectionListener() {
        }

        @EventHandler
        public void onEntityCombust(EntityCombustEvent event) {
            if (event.getEntityType() == EntityType.ZOMBIE && !(event instanceof EntityCombustByBlockEvent) && !(event instanceof EntityCombustByEntityEvent)) {
                if (!ZonaMuerta.this.getConfig().getBoolean("zombies.burn_in_sunlight", false)) {
                    event.setCancelled(true);
                }
                Zombie zombie = (Zombie)event.getEntity();
                WorldConfig wc = ZonaMuerta.this.getWorldConfig(zombie.getWorld());
                if (wc.fireImmune) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private class NoiseLightListener
    implements Listener {
        private NoiseLightListener() {
        }

        private void aggroNearbyZombies(Player player, int range) {
            Location loc = player.getLocation();
            for (Entity entity : loc.getWorld().getNearbyEntities(loc, (double)range, (double)range, (double)range)) {
                Zombie zombie;
                if (!ZonaMuerta.this.isManagedZombie(entity) || (zombie = (Zombie)entity).getTarget() != null && zombie.getTarget().equals(player)) continue;
                zombie.setTarget((LivingEntity)player);
                ZonaMuerta.this.zombieTargetMemory.computeIfAbsent(zombie.getUniqueId(), k -> new HashMap()).put(player.getUniqueId(), System.currentTimeMillis() + ZonaMuerta.this.memoryDurationMs);
            }
        }

        @EventHandler
        public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
            if (event.isSprinting()) {
                this.aggroNearbyZombies(event.getPlayer(), ZonaMuerta.this.sprintNoiseRange);
            }
        }

        @EventHandler
        public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
            this.aggroNearbyZombies(event.getPlayer(), ZonaMuerta.this.eatingNoiseRange);
        }

        @EventHandler
        public void onEntityShootBow(EntityShootBowEvent event) {
            if (event.getEntity() instanceof Player) {
                this.aggroNearbyZombies((Player)event.getEntity(), ZonaMuerta.this.bowNoiseRange);
            }
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent event) {
            Material placed = event.getBlockPlaced().getType();
            if (placed == Material.TORCH || placed == Material.LANTERN || placed == Material.SOUL_TORCH || placed == Material.SOUL_LANTERN || placed == Material.FIRE) {
                this.aggroNearbyZombies(event.getPlayer(), ZonaMuerta.this.torchNoiseRange);
            }
        }
    }

    private class ChunkLoadListener
    implements Listener {
        private ChunkLoadListener() {
        }

        @EventHandler
        public void onChunkLoad(ChunkLoadEvent event) {
            if (event.isNewChunk()) {
                ZonaMuerta.this.structureManager.tryGenerateStructure(event.getChunk());
            }
        }
    }

    private static class BloodMoonScheduleEntry {
        private final String id;
        private final String dayPattern;
        private final int startTick;
        private final int endTick;
        private final double spawnMultiplier;
        private final double mutationMultiplier;

        private BloodMoonScheduleEntry(String id, String dayPattern, int startTick, int endTick, double spawnMultiplier, double mutationMultiplier) {
            this.id = id;
            this.dayPattern = dayPattern;
            this.startTick = startTick;
            this.endTick = endTick;
            this.spawnMultiplier = spawnMultiplier;
            this.mutationMultiplier = mutationMultiplier;
        }
    }
}

