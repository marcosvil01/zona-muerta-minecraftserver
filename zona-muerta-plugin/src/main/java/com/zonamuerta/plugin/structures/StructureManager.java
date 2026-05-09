package com.zonamuerta.plugin.structures;

import com.zonamuerta.plugin.ZonaMuerta;
import com.zonamuerta.plugin.structures.RuinedHospital;
import com.zonamuerta.plugin.structures.RuinedHouse1;
import com.zonamuerta.plugin.structures.RuinedHouse2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class StructureManager {
    private final ZonaMuerta plugin;
    private File structuresFile;
    private YamlConfiguration structuresConfig;
    private File structuresDataFile;
    private YamlConfiguration structuresDataConfig;
    private final Map<String, StructureConfig> structureConfigs = new HashMap<String, StructureConfig>();
    private final Set<String> generatedChunks = new HashSet<String>();
    private final Random random = new Random();

    public StructureManager(ZonaMuerta plugin) {
        this.plugin = plugin;
    }

    public void loadStructuresConfig() {
        this.structuresFile = new File(this.plugin.getDataFolder(), "structures.yml");
        if (!this.structuresFile.exists()) {
            this.createDefaultStructuresConfig();
        }
        this.structuresConfig = YamlConfiguration.loadConfiguration((File)this.structuresFile);
        this.ensureStructureDefaults();
        this.structuresDataFile = new File(this.plugin.getDataFolder(), "structures_data.yml");
        if (!this.structuresDataFile.exists()) {
            try {
                this.structuresDataFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to create structures_data.yml: " + e.getMessage());
            }
        }
        this.structuresDataConfig = YamlConfiguration.loadConfiguration((File)this.structuresDataFile);
        List generatedList = this.structuresDataConfig.getStringList("generated_chunks");
        this.generatedChunks.addAll(generatedList);
        this.structureConfigs.clear();
        ConfigurationSection structuresSection = this.structuresConfig.getConfigurationSection("structures");
        if (structuresSection != null) {
            for (String structureName : structuresSection.getKeys(false)) {
                ConfigurationSection structureData = structuresSection.getConfigurationSection(structureName);
                if (structureData == null) continue;
                StructureConfig config = new StructureConfig();
                config.name = structureName;
                config.enabled = structureData.getBoolean("enabled", true);
                config.spawnChance = structureData.getDouble("spawn_chance", 10.0);
                config.biomes = new HashSet<String>(structureData.getStringList("biomes"));
                config.minY = structureData.getInt("min_y", 60);
                config.maxY = structureData.getInt("max_y", 100);
                this.structureConfigs.put(structureName, config);
            }
        }
        this.plugin.getLogger().info("Loaded " + this.structureConfigs.size() + " structure configurations.");
    }

    private void ensureStructureDefaults() {
        boolean changed = false;
        if (!this.structuresConfig.contains("structures.ruined_house_2")) {
            this.structuresConfig.set("structures.ruined_house_2.enabled", true);
            this.structuresConfig.set("structures.ruined_house_2.spawn_chance", 6.0);
            this.structuresConfig.set("structures.ruined_house_2.biomes", Arrays.asList("PLAINS", "FOREST", "BIRCH_FOREST", "DARK_FOREST", "TAIGA", "SNOWY_PLAINS", "SUNFLOWER_PLAINS", "MEADOW", "SAVANNA"));
            this.structuresConfig.set("structures.ruined_house_2.min_y", 60);
            this.structuresConfig.set("structures.ruined_house_2.max_y", 120);
            changed = true;
        }
        if (!this.structuresConfig.contains("structures.ruined_hospital")) {
            this.structuresConfig.set("structures.ruined_hospital.enabled", true);
            this.structuresConfig.set("structures.ruined_hospital.spawn_chance", 3.0);
            this.structuresConfig.set("structures.ruined_hospital.biomes", Arrays.asList("PLAINS", "FOREST", "BIRCH_FOREST", "DARK_FOREST", "TAIGA", "SNOWY_PLAINS", "SUNFLOWER_PLAINS", "MEADOW", "SAVANNA"));
            this.structuresConfig.set("structures.ruined_hospital.min_y", 60);
            this.structuresConfig.set("structures.ruined_hospital.max_y", 120);
            changed = true;
        }
        if (changed) {
            try {
                this.structuresConfig.save(this.structuresFile);
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to update structures.yml defaults: " + e.getMessage());
            }
        }
    }

    private void createDefaultStructuresConfig() {
        this.structuresFile.getParentFile().mkdirs();
        YamlConfiguration defaultConfig = new YamlConfiguration();
        defaultConfig.set("structures.ruined_house_1.enabled", true);
        defaultConfig.set("structures.ruined_house_1.spawn_chance", 10.0);
        defaultConfig.set("structures.ruined_house_1.biomes", Arrays.asList("PLAINS", "FOREST", "BIRCH_FOREST", "DARK_FOREST", "TAIGA", "SNOWY_PLAINS", "SUNFLOWER_PLAINS", "MEADOW", "SAVANNA"));
        defaultConfig.set("structures.ruined_house_1.min_y", 60);
        defaultConfig.set("structures.ruined_house_1.max_y", 120);
        defaultConfig.set("structures.ruined_house_2.enabled", true);
        defaultConfig.set("structures.ruined_house_2.spawn_chance", 6.0);
        defaultConfig.set("structures.ruined_house_2.biomes", Arrays.asList("PLAINS", "FOREST", "BIRCH_FOREST", "DARK_FOREST", "TAIGA", "SNOWY_PLAINS", "SUNFLOWER_PLAINS", "MEADOW", "SAVANNA"));
        defaultConfig.set("structures.ruined_house_2.min_y", 60);
        defaultConfig.set("structures.ruined_house_2.max_y", 120);
        defaultConfig.set("structures.ruined_hospital.enabled", true);
        defaultConfig.set("structures.ruined_hospital.spawn_chance", 3.0);
        defaultConfig.set("structures.ruined_hospital.biomes", Arrays.asList("PLAINS", "FOREST", "BIRCH_FOREST", "DARK_FOREST", "TAIGA", "SNOWY_PLAINS", "SUNFLOWER_PLAINS", "MEADOW", "SAVANNA"));
        defaultConfig.set("structures.ruined_hospital.min_y", 60);
        defaultConfig.set("structures.ruined_hospital.max_y", 120);
        try {
            defaultConfig.save(this.structuresFile);
            this.plugin.getLogger().info("Created default structures.yml");
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to create default structures.yml: " + e.getMessage());
        }
    }

    public void saveStructuresData() {
        this.structuresDataConfig.set("generated_chunks", new ArrayList<String>(this.generatedChunks));
        try {
            this.structuresDataConfig.save(this.structuresDataFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save structures_data.yml: " + e.getMessage());
        }
    }

    public void tryGenerateStructure(Chunk chunk) {
        String chunkKey = chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
        if (this.generatedChunks.contains(chunkKey)) {
            return;
        }
        World world = chunk.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        for (StructureConfig config : this.structureConfigs.values()) {
            int z;
            int x;
            int y;
            if (!config.enabled || !(this.random.nextDouble() * 100.0 < config.spawnChance) || (y = world.getHighestBlockYAt(x = chunk.getX() * 16 + this.random.nextInt(16), z = chunk.getZ() * 16 + this.random.nextInt(16))) < config.minY || y > config.maxY) continue;
            Location loc = new Location(world, (double)x, (double)y, (double)z);
            Biome biome = world.getBiome(x, y, z);
            if (!config.biomes.isEmpty() && !config.biomes.contains(biome.getKey().getKey())) continue;
            this.generateStructure(config.name, loc);
            this.generatedChunks.add(chunkKey);
            this.saveStructuresData();
            this.plugin.getLogger().info("Generated structure '" + config.name + "' at " + x + ", " + y + ", " + z);
            return;
        }
        this.generatedChunks.add(chunkKey);
    }

    private void generateStructure(String structureName, Location location) {
        switch (structureName.toLowerCase()) {
            case "ruined_house_1": {
                RuinedHouse1.generate(this.plugin, location);
                break;
            }
            case "ruined_house_2": {
                RuinedHouse2.generate(this.plugin, location);
                break;
            }
            case "ruined_hospital": {
                RuinedHospital.generate(this.plugin, location);
                break;
            }
            default: {
                this.plugin.getLogger().warning("Unknown structure type: " + structureName);
            }
        }
    }

    public static class StructureConfig {
        public String name;
        public boolean enabled;
        public double spawnChance;
        public Set<String> biomes;
        public int minY;
        public int maxY;
    }
}

