package com.zonamuerta.plugin;

import com.zonamuerta.plugin.ZonaMuerta;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Infected
implements Listener {
    private final ZonaMuerta plugin;
    private final NamespacedKey infectedKey;
    private final NamespacedKey infectedMutationKey;
    private final NamespacedKey infectedKillerKey;
    private boolean infectionEnabled;
    private double infectionChanceOnHit;
    private double infectionIncreasePercent;
    private double maxInfectionPercent;
    private boolean showInfectionBar;
    private boolean allowGoldenAppleCure;
    private double goldenAppleCurePercent;
    private double enchantedGoldenAppleCurePercent;
    private File playersFile;
    private YamlConfiguration playersConfig;
    private final Map<UUID, Double> infectionMap = new HashMap<UUID, Double>();
    private final Map<UUID, BossBar> infectionBossBars = new HashMap<UUID, BossBar>();
    private final Random random = new Random();

    public Infected(ZonaMuerta plugin) {
        this.plugin = plugin;
        this.infectedKey = plugin.getNamespacedKey("infected");
        this.infectedMutationKey = plugin.getNamespacedKey("infected_mutation");
        this.infectedKillerKey = plugin.getNamespacedKey("infected_killer");
    }

    private boolean isManagedZombie(Entity entity) {
        return entity instanceof Zombie && entity.getType() == EntityType.ZOMBIE;
    }

    public void loadInfectionConfig() {
        FileConfiguration cfg = this.plugin.getConfig();
        this.infectionEnabled = cfg.getBoolean("infection_system.enabled", true);
        this.infectionChanceOnHit = cfg.getDouble("infection_system.chance_on_hit", 5.0);
        this.infectionIncreasePercent = cfg.getDouble("infection_system.infection_increase", 10.0);
        this.maxInfectionPercent = cfg.getDouble("infection_system.max_infection", 100.0);
        this.showInfectionBar = cfg.getBoolean("infection_system.show_infection_bar", true);
        this.allowGoldenAppleCure = cfg.getBoolean("infection_system.allow_golden_apple_cure", true);
        this.goldenAppleCurePercent = cfg.getDouble("infection_system.golden_apple_cure_percent", 20.0);
        this.enchantedGoldenAppleCurePercent = cfg.getDouble("infection_system.enchanted_golden_apple_cure_percent", 50.0);
    }

    public void loadPlayersData() {
        this.playersFile = new File(this.plugin.getDataFolder(), "players.yml");
        if (!this.playersFile.exists()) {
            this.playersFile.getParentFile().mkdirs();
            try {
                this.playersFile.createNewFile();
            }
            catch (IOException e) {
                this.plugin.getLogger().severe("Failed to create players.yml: " + e.getMessage());
            }
        }
        this.playersConfig = YamlConfiguration.loadConfiguration((File)this.playersFile);
    }

    public void savePlayersData() {
        for (UUID uuid : this.infectionMap.keySet()) {
            double infection = this.infectionMap.get(uuid);
            this.playersConfig.set(uuid.toString() + ".infection", infection);
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null || !this.isPlayerFullyInfected(p)) continue;
            String mutation = (String)p.getPersistentDataContainer().get(this.infectedMutationKey, PersistentDataType.STRING);
            this.playersConfig.set(uuid.toString() + ".mutation", mutation);
        }
        try {
            this.playersConfig.save(this.playersFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save players.yml: " + e.getMessage());
        }
    }

    public void restoreInfectedPlayers() {
        for (String key : this.playersConfig.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            double infection = this.playersConfig.getDouble(key + ".infection", 0.0);
            if (infection >= this.maxInfectionPercent) {
                infection = this.maxInfectionPercent;
            }
            this.infectionMap.put(uuid, infection);
            String mutation = this.playersConfig.getString(key + ".mutation", null);
            Player p = Bukkit.getPlayer((UUID)uuid);
            if (p == null || !(infection >= this.maxInfectionPercent) || mutation == null) continue;
            this.applyMutationToPlayer(p, mutation);
            this.setupInfectedPlayerVisuals(p, mutation);
            this.setupInfectionBossBar(p);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!this.infectionEnabled) {
            return;
        }
        if (this.isManagedZombie(event.getDamager()) && event.getEntity() instanceof Player) {
            String zombieType;
            Zombie zombie = (Zombie)event.getDamager();
            Player player = (Player)event.getEntity();
            if (this.isPlayerFullyInfected(player)) {
                event.setCancelled(true);
                return;
            }
            if (this.random.nextDouble() * 100.0 < this.infectionChanceOnHit) {
                this.increasePlayerInfection(player, this.infectionIncreasePercent);
            }
            if ((zombieType = (String)zombie.getPersistentDataContainer().get(this.plugin.zombieTypeKey, PersistentDataType.STRING)) != null) {
                switch (zombieType) {
                    case "blight": {
                        if (this.plugin.getBlightAbilities().getOrDefault("poison_effect", true).booleanValue()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
                        }
                        player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Has sido infectado con plaga!");
                        break;
                    }
                    case "frost": {
                        if (this.plugin.getFrostAbilities().getOrDefault("slowness_effect", true).booleanValue()) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
                        }
                        player.sendMessage(String.valueOf(ChatColor.AQUA) + "\u00a1Sientes un frio helador hasta los huesos!");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (!this.infectionEnabled) {
            return;
        }
        Player player = event.getPlayer();
        if (!this.infectionMap.containsKey(player.getUniqueId())) {
            return;
        }
        ItemStack item = event.getItem();
        if (this.isCurePotion(item)) {
            event.setCancelled(true);
            this.useCurePotion(player);
            return;
        }
        if (!this.allowGoldenAppleCure) {
            return;
        }
        switch (item.getType()) {
            case GOLDEN_APPLE: {
                if (this.isPlayerFullyInfected(player)) break;
                this.decreasePlayerInfection(player, this.goldenAppleCurePercent);
                break;
            }
            case ENCHANTED_GOLDEN_APPLE: {
                if (this.isPlayerFullyInfected(player)) break;
                this.decreasePlayerInfection(player, this.enchantedGoldenAppleCurePercent);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!this.infectionEnabled) {
            return;
        }
        if (this.infectionMap.containsKey(player.getUniqueId())) {
            this.setupInfectionBossBar(player);
            this.updateInfectionBossBar(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.removeInfectionBossBar(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!this.infectionEnabled) {
            return;
        }
        if (this.infectionMap.containsKey(player.getUniqueId())) {
            this.setupInfectionBossBar(player);
            this.updateInfectionBossBar(player);
        }
    }

    @EventHandler
    public void onEntityDeathInfectionSpread(EntityDeathEvent event) {
        if (!this.infectionEnabled) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            Player dead = (Player)event.getEntity();
            Player killerEntity = dead.getKiller();
            if (killerEntity == null) {
                return;
            }
            if (killerEntity instanceof Player) {
                Player killer = killerEntity;
                if (this.isPlayerInfected(killer)) {
                    dead.getPersistentDataContainer().set(this.infectedKillerKey, PersistentDataType.STRING, killer.getUniqueId().toString());
                    this.infectPlayer(dead);
                    double maxHealth = killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                    killer.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth + 2.0);
                    killer.setHealth(Math.min(killer.getHealth() + 2.0, killer.getAttribute(Attribute.MAX_HEALTH).getBaseValue()));
                    killer.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Ganaste +1 corazon maximo por infectar a un jugador!");
                }
            } else if (this.isManagedZombie((Entity)killerEntity)) {
                dead.getPersistentDataContainer().remove(this.infectedKillerKey);
                this.infectPlayer(dead);
            }
        }
    }

    public void increasePlayerInfection(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = this.infectionMap.getOrDefault(uuid, 0.0);
        if (current >= this.maxInfectionPercent) {
            return;
        }
        if ((current += amount) > this.maxInfectionPercent) {
            current = this.maxInfectionPercent;
        }
        this.infectionMap.put(uuid, current);
        this.updateInfectionBossBar(player);
        if (current >= this.maxInfectionPercent) {
            this.infectPlayer(player);
        }
    }

    public void decreasePlayerInfection(Player player, double amount) {
        UUID uuid = player.getUniqueId();
        double current = this.infectionMap.getOrDefault(uuid, 0.0);
        if (current >= this.maxInfectionPercent) {
            return;
        }
        if ((current -= amount) < 0.0) {
            current = 0.0;
        }
        this.infectionMap.put(uuid, current);
        this.updateInfectionBossBar(player);
    }

    public void infectPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        this.infectionMap.put(uuid, this.maxInfectionPercent);
        String mutation = this.getRandomMutationType();
        this.applyMutationToPlayer(player, mutation);
        this.setupInfectedPlayerVisuals(player, mutation);
        player.sendTitle(String.valueOf(ChatColor.RED) + "\u00a1Ahora eres un zombie!", String.valueOf(ChatColor.GRAY) + "Tu mutacion es " + mutation.toUpperCase() + ". \u00a1Caza jugadores para hacerte mas fuerte!", 10, 70, 20);
        this.setupInfectionBossBar(player);
        this.updateInfectionBossBar(player);
        this.playersConfig.set(uuid.toString() + ".infection", this.maxInfectionPercent);
        this.playersConfig.set(uuid.toString() + ".mutation", mutation);
        try {
            this.playersConfig.save(this.playersFile);
        }
        catch (IOException e) {
            this.plugin.getLogger().severe("Failed to save players.yml: " + e.getMessage());
        }
    }

    /**
     * Intenta infectar a un jugador con probabilidad base + extra.
     * Llamado por MythicBridge cuando un mob de MM golpea al jugador.
     *
     * @param player     El jugador que recibió el ataque
     * @param extraChance Porcentaje adicional de probabilidad (0-100)
     */
    public void infect(Player player, double extraChance) {
        if (!this.infectionEnabled) return;
        if (this.isPlayerFullyInfected(player)) return;
        double totalChance = this.infectionChanceOnHit + extraChance;
        if (this.random.nextDouble() * 100.0 < totalChance) {
            this.increasePlayerInfection(player, this.infectionIncreasePercent);
        }
    }

    private String getRandomMutationType() {
        ConfigurationSection mutations = this.plugin.getConfig().getConfigurationSection("mutation_types");
        if (mutations == null) {
            return "tank";
        }
        ArrayList<String> keys = new ArrayList<String>(mutations.getKeys(false));
        if (keys.isEmpty()) {
            return "tank";
        }
        return keys.get(this.random.nextInt(keys.size()));
    }

    public void applyMutationToPlayer(Player player, String mutation) {
        PotionEffectType[] toRemove;
        for (PotionEffectType type : toRemove = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.RESISTANCE, PotionEffectType.POISON, PotionEffectType.INVISIBILITY, PotionEffectType.SLOWNESS}) {
            player.removePotionEffect(type);
        }
        NamespacedKey extraHeartsKey = this.plugin.getNamespacedKey("extra_hearts");
        int extraHearts = 0;
        if (player.getPersistentDataContainer().has(extraHeartsKey, PersistentDataType.INTEGER)) {
            extraHearts = (Integer)player.getPersistentDataContainer().get(extraHeartsKey, PersistentDataType.INTEGER);
        }
        double extraHealth = (double)extraHearts * 2.0;
        switch (mutation.toLowerCase()) {
            case "tank": {
                double baseHealth = 40.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 1, false, false));
                break;
            }
            case "charger": {
                double baseHealth = 20.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                break;
            }
            case "assassin": {
                double baseHealth = 8.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                break;
            }
            case "blight": {
                double baseHealth = 20.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, Integer.MAX_VALUE, 1, false, false));
                break;
            }
            case "burster": {
                double baseHealth = 10.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
                break;
            }
            case "spitter": {
                double baseHealth = 16.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                break;
            }
            case "frost": {
                double baseHealth = 20.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 1, false, false));
                break;
            }
            default: {
                double baseHealth = 20.0;
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(baseHealth + extraHealth);
                player.setHealth(baseHealth + extraHealth);
            }
        }
        player.getPersistentDataContainer().set(this.infectedMutationKey, PersistentDataType.STRING, mutation.toLowerCase());
        player.getPersistentDataContainer().set(this.infectedKey, PersistentDataType.INTEGER, 1);
    }

    public void setupInfectedPlayerVisuals(Player player, String mutation) {
        player.setCustomName(String.valueOf(ChatColor.DARK_RED) + "(Infectado " + player.getName() + ")");
        player.setCustomNameVisible(true);
        player.setMetadata("infected_player", (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, true));
    }

    public boolean isPlayerInfected(Player player) {
        return this.infectionMap.getOrDefault(player.getUniqueId(), 0.0) >= 1.0;
    }

    public boolean isPlayerFullyInfected(Player player) {
        return this.infectionMap.getOrDefault(player.getUniqueId(), 0.0) >= this.maxInfectionPercent;
    }

    /** Retorna el nivel de infección actual del jugador (0–maxInfectionPercent). */
    public double getInfectionLevel(Player player) {
        return this.infectionMap.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void setupInfectionBossBar(Player player) {
        if (!this.showInfectionBar) {
            return;
        }
        UUID uuid = player.getUniqueId();
        if (this.infectionBossBars.containsKey(uuid)) {
            return;
        }
        BossBar bar = Bukkit.createBossBar((String)(String.valueOf(ChatColor.RED) + "Infeccion: 0%"), (BarColor)BarColor.RED, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        bar.addPlayer(player);
        this.infectionBossBars.put(uuid, bar);
    }

    public void updateInfectionBossBar(Player player) {
        double infection;
        double progress;
        if (!this.showInfectionBar) {
            return;
        }
        UUID uuid = player.getUniqueId();
        BossBar bar = this.infectionBossBars.get(uuid);
        if (bar == null) {
            this.setupInfectionBossBar(player);
            bar = this.infectionBossBars.get(uuid);
        }
        if ((progress = (infection = this.infectionMap.getOrDefault(uuid, 0.0).doubleValue()) / this.maxInfectionPercent) > 1.0) {
            progress = 1.0;
        }
        bar.setProgress(progress);
        bar.setTitle(String.valueOf(ChatColor.RED) + "Infeccion: " + (int)infection + "% / " + (int)this.maxInfectionPercent + "%");
        if (infection >= this.maxInfectionPercent) {
            bar.setColor(BarColor.PURPLE);
        } else {
            bar.setColor(BarColor.RED);
        }
    }

    public void removeInfectionBossBar(Player player) {
        UUID uuid = player.getUniqueId();
        BossBar bar = this.infectionBossBars.remove(uuid);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public void removeAllBossBars() {
        for (BossBar bar : this.infectionBossBars.values()) {
            bar.removeAll();
        }
        this.infectionBossBars.clear();
    }

    public boolean isInfectionEnabled() {
        return this.infectionEnabled;
    }

    private boolean isCurePotion(ItemStack item) {
        ItemMeta meta2;
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        try {
            NamespacedKey key;
            PersistentDataContainer container;
            meta2 = item.getItemMeta();
            if (meta2 != null && (container = meta2.getPersistentDataContainer()).has(key = new NamespacedKey((Plugin)this.plugin, "cure_potion"), PersistentDataType.STRING)) {
                return true;
            }
        }
        catch (Exception ex) {
            // empty catch block
        }
        meta2 = item.getItemMeta();
        if (meta2 != null && meta2.hasDisplayName()) {
            return meta2.getDisplayName().equals(String.valueOf(ChatColor.DARK_RED) + "Pocion de Cura") || meta2.getDisplayName().equals(String.valueOf(ChatColor.DARK_RED) + "Cure Potion");
        }
        return false;
    }

    private void useCurePotion(Player player) {
        if (!this.isPlayerInfected(player)) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1No estas infectado, la pocion de cura no tiene efecto!");
            return;
        }
        UUID uuid = player.getUniqueId();
        boolean wasFullyInfected = this.isPlayerFullyInfected(player);
        this.infectionMap.put(uuid, 0.0);
        if (wasFullyInfected) {
            PotionEffectType[] toRemove;
            for (PotionEffectType type : toRemove = new PotionEffectType[]{PotionEffectType.SPEED, PotionEffectType.RESISTANCE, PotionEffectType.POISON, PotionEffectType.INVISIBILITY, PotionEffectType.SLOWNESS}) {
                player.removePotionEffect(type);
            }
            NamespacedKey extraHeartsKey = this.plugin.getNamespacedKey("extra_hearts");
            int extraHearts = 0;
            if (player.getPersistentDataContainer().has(extraHeartsKey, PersistentDataType.INTEGER)) {
                extraHearts = (Integer)player.getPersistentDataContainer().get(extraHeartsKey, PersistentDataType.INTEGER);
            }
            double extraHealth = (double)extraHearts * 2.0;
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0 + extraHealth);
            player.setHealth(20.0 + extraHealth);
            player.getPersistentDataContainer().remove(this.infectedMutationKey);
            player.getPersistentDataContainer().remove(this.infectedKey);
            if (player.hasMetadata("infected_player")) {
                player.removeMetadata("infected_player", (Plugin)this.plugin);
            }
            player.setCustomNameVisible(false);
            player.setCustomName(null);
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1La pocion de cura te ha restaurado a la normalidad! Tu infeccion ha sido curada.");
        } else {
            player.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1La pocion de cura ha reducido tu infeccion a 0%!");
        }
        this.updateInfectionBossBar(player);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (this.isCurePotion(itemInHand)) {
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        } else {
            ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
            if (this.isCurePotion(itemInOffHand)) {
                if (itemInOffHand.getAmount() > 1) {
                    itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }
        }
    }
}

