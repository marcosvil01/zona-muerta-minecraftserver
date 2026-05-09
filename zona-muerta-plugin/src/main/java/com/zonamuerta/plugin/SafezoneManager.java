package com.zonamuerta.plugin;

import com.zonamuerta.plugin.ZonaMuerta;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SafezoneManager {
    private final ZonaMuerta plugin;
    private File safezonesFile;
    private FileConfiguration safezonesConfig;
    private final Map<String, Safezone> safezones = new HashMap<String, Safezone>();
    private int defaultRadius;
    private int maxRadius;
    private int maxSafezonesPerPlayer;

    public SafezoneManager(ZonaMuerta plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        FileConfiguration cfg = this.plugin.getConfig();
        this.defaultRadius = cfg.getInt("safezones.default_radius", 10);
        this.maxRadius = cfg.getInt("safezones.max_radius", 50);
        this.maxSafezonesPerPlayer = cfg.getInt("safezones.max_per_player", 3);
    }

    public void loadSafezones() {
        this.loadConfig();
        this.safezonesFile = new File(this.plugin.getDataFolder(), "safezones.yml");
        if (!this.safezonesFile.exists()) {
            try {
                this.safezonesFile.getParentFile().mkdirs();
                this.safezonesFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to create safezones.yml: " + e.getMessage());
            }
        }
        this.safezonesConfig = YamlConfiguration.loadConfiguration((File)this.safezonesFile);
        this.safezones.clear();
        ConfigurationSection zonesSection = this.safezonesConfig.getConfigurationSection("zones");
        if (zonesSection != null) {
            for (String key : zonesSection.getKeys(false)) {
                ConfigurationSection zone = zonesSection.getConfigurationSection(key);
                if (zone == null) continue;
                String worldName = zone.getString("world");
                double x = zone.getDouble("x");
                double y = zone.getDouble("y");
                double z = zone.getDouble("z");
                int radius = zone.getInt("radius");
                String ownerUUID = zone.getString("owner");
                World world = Bukkit.getWorld((String)worldName);
                if (world == null) continue;
                Location loc = new Location(world, x, y, z);
                UUID owner = null;
                try {
                    owner = UUID.fromString(ownerUUID);
                }
                catch (Exception exception) {
                    // empty catch block
                }
                Safezone safezone = new Safezone(key, loc, radius, owner);
                this.safezones.put(key.toLowerCase(), safezone);
            }
        }
        this.plugin.getLogger().info("Loaded " + this.safezones.size() + " safezones.");
    }

    public void saveSafezones() {
        this.safezonesConfig.set("zones", null);
        for (Map.Entry<String, Safezone> entry : this.safezones.entrySet()) {
            Safezone zone = entry.getValue();
            String path = "zones." + entry.getKey();
            this.safezonesConfig.set(path + ".world", zone.getCenter().getWorld().getName());
            this.safezonesConfig.set(path + ".x", zone.getCenter().getX());
            this.safezonesConfig.set(path + ".y", zone.getCenter().getY());
            this.safezonesConfig.set(path + ".z", zone.getCenter().getZ());
            this.safezonesConfig.set(path + ".radius", zone.getRadius());
            this.safezonesConfig.set(path + ".owner", (zone.getOwner() != null ? zone.getOwner().toString() : "server"));
        }
        try {
            this.safezonesConfig.save(this.safezonesFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save safezones.yml: " + e.getMessage());
        }
    }

    public boolean createSafezone(Player player, String name) {
        String key = name.toLowerCase();
        if (this.safezones.containsKey(key)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Ya existe una zona segura con ese nombre!");
            return false;
        }
        int playerZoneCount = 0;
        for (Safezone zone : this.safezones.values()) {
            if (zone.getOwner() == null || !zone.getOwner().equals(player.getUniqueId())) continue;
            ++playerZoneCount;
        }
        if (playerZoneCount >= this.maxSafezonesPerPlayer && !player.hasPermission("zonamuerta.admin")) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Has alcanzado el maximo de zonas seguras (" + this.maxSafezonesPerPlayer + ")!");
            return false;
        }
        Safezone zone = new Safezone(name, player.getLocation(), this.defaultRadius, player.getUniqueId());
        this.safezones.put(key, zone);
        this.saveSafezones();
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Zona segura '" + name + "' creada con radio " + this.defaultRadius + "!");
        return true;
    }

    public boolean removeSafezone(Player player, String name) {
        String key = name.toLowerCase();
        Safezone zone = this.safezones.get(key);
        if (zone == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Zona segura '" + name + "' no encontrada!");
            return false;
        }
        if (zone.getOwner() != null && !zone.getOwner().equals(player.getUniqueId()) && !player.hasPermission("zonamuerta.admin")) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1No eres dueno de esta zona segura!");
            return false;
        }
        this.safezones.remove(key);
        this.saveSafezones();
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Zona segura '" + name + "' eliminada!");
        return true;
    }

    public void listSafezones(Player player) {
        player.sendMessage(String.valueOf(ChatColor.GOLD) + "=== Zonas Seguras ===");
        if (this.safezones.isEmpty()) {
            player.sendMessage(String.valueOf(ChatColor.GRAY) + "No existen zonas seguras.");
            return;
        }
        for (Safezone zone : this.safezones.values()) {
            Object ownerName = "Servidor";
            if (zone.getOwner() != null) {
                Player owner = Bukkit.getPlayer((UUID)zone.getOwner());
                ownerName = owner != null ? owner.getName() : zone.getOwner().toString().substring(0, 8) + "...";
            }
            Location loc = zone.getCenter();
            player.sendMessage(String.valueOf(ChatColor.YELLOW) + zone.getName() + String.valueOf(ChatColor.GRAY) + " - Dueno: " + (String)ownerName + " | Radio: " + zone.getRadius() + " | Ubicacion: " + loc.getWorld().getName() + " (" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ() + ")");
        }
    }

    public boolean expandSafezone(Player player, String name, int amount) {
        String key = name.toLowerCase();
        Safezone zone = this.safezones.get(key);
        if (zone == null) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Zona segura '" + name + "' no encontrada!");
            return false;
        }
        if (zone.getOwner() != null && !zone.getOwner().equals(player.getUniqueId()) && !player.hasPermission("zonamuerta.admin")) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1No eres dueno de esta zona segura!");
            return false;
        }
        int newRadius = zone.getRadius() + amount;
        if (newRadius > this.maxRadius) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1El radio maximo es " + this.maxRadius + "!");
            return false;
        }
        zone.setRadius(newRadius);
        this.saveSafezones();
        player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Zona segura '" + name + "' expandida a radio " + newRadius + "!");
        return true;
    }

    public boolean isInSafezone(Location location) {
        for (Safezone zone : this.safezones.values()) {
            if (!zone.contains(location)) continue;
            return true;
        }
        return false;
    }

    public Safezone getSafezoneAt(Location location) {
        for (Safezone zone : this.safezones.values()) {
            if (!zone.contains(location)) continue;
            return zone;
        }
        return null;
    }

    public Collection<Safezone> getAllSafezones() {
        return this.safezones.values();
    }

    public int getSafezoneCount() {
        return this.safezones.size();
    }

    public static class Safezone {
        private final String name;
        private final Location center;
        private int radius;
        private final UUID owner;

        public Safezone(String name, Location center, int radius, UUID owner) {
            this.name = name;
            this.center = center;
            this.radius = radius;
            this.owner = owner;
        }

        public String getName() {
            return this.name;
        }

        public Location getCenter() {
            return this.center;
        }

        public int getRadius() {
            return this.radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public UUID getOwner() {
            return this.owner;
        }

        public boolean contains(Location location) {
            if (!location.getWorld().equals(this.center.getWorld())) {
                return false;
            }
            return location.distanceSquared(this.center) <= (double)(this.radius * this.radius);
        }
    }
}

