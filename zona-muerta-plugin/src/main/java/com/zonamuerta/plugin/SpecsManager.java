package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpecsManager implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, PlayerSpec> playerSpecs = new HashMap<>();
    private final Map<UUID, Long> specCooldowns = new HashMap<>();

    public enum SpecType {
        APOCALYPSE_SPECIALIST("Apocalypse Specialist", "Maestro del apocalipsis. +50% dano, counter-attack, immune knockback",
            Material.BROWN_WOOL, 50),
        PLAGUE_CARRIER("Plague Carrier", "Portador de la plaga. Aura de毒雾, puede curar infectados",
            Material.GREEN_WOOL, 50),
        DARK_ARTS("Dark Arts", "Artes oscuras. Raise zombies como slaves, 3 minions max, 50% XP bonus",
            Material.PURPLE_WOOL, 50),
        FIELD_MEDIC("Field Medic", "Medico de campo. -50% cure cost, AOE heal, bonus ZP",
            Material.RED_WOOL, 50);

        private final String displayName;
        private final String description;
        private final Material icon;
        private final int requiredLevel;

        SpecType(String displayName, String description, Material icon, int requiredLevel) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.requiredLevel = requiredLevel;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public int getRequiredLevel() { return requiredLevel; }
    }

    public SpecsManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startPassiveEffects();
    }

    private void startPassiveEffects() {
        new org.bukkit.scheduler.BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerSpec spec = playerSpecs.get(player.getUniqueId());
                    if (spec != null) {
                        applyPassiveEffect(player, spec.type);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private void applyPassiveEffect(Player player, SpecType type) {
        switch (type) {
            case APOCALYPSE_SPECIALIST:
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 40, 0));
                break;
            case PLAGUE_CARRIER:
                player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 40, 0));
                break;
            case DARK_ARTS:
                if (player.hasMetadata("zm_minion_count")) {
                    int count = player.getMetadata("zm_minion_count").get(0).asInt();
                    if (count < 3) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 40, 0));
                    }
                }
                break;
            case FIELD_MEDIC:
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 0));
                break;
        }
    }

    public boolean canSelectSpec(Player player) {
        int level = plugin.getProgressionManager().getPlayerLevel(player);
        return level >= 50;
    }

    public boolean selectSpec(Player player, SpecType type) {
        int level = plugin.getProgressionManager().getPlayerLevel(player);

        if (level < type.getRequiredLevel()) {
            player.sendMessage(ChatColor.RED + "Necesitas nivel " + type.getRequiredLevel() + " para seleccionar " + type.getDisplayName());
            return false;
        }

        if (playerSpecs.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Ya tienes una especializacion. Usa /spec change para cambiarla.");
            return false;
        }

        PlayerSpec spec = new PlayerSpec(type, System.currentTimeMillis());
        playerSpecs.put(player.getUniqueId(), spec);

        player.sendMessage(ChatColor.GOLD + "====================");
        player.sendMessage(ChatColor.DARK_PURPLE + "  ESPECIALIZACION ELEGIDA");
        player.sendMessage(ChatColor.WHITE + "  " + type.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "  " + type.getDescription());
        player.sendMessage(ChatColor.GOLD + "====================");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

        return true;
    }

    public boolean changeSpec(Player player, SpecType newType) {
        if (!playerSpecs.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No tienes una especializacion para cambiar.");
            return false;
        }

        int level = plugin.getProgressionManager().getPlayerLevel(player);
        if (level < newType.getRequiredLevel()) {
            player.sendMessage(ChatColor.RED + "Necesitas nivel " + newType.getRequiredLevel() + " para " + newType.getDisplayName());
            return false;
        }

        PlayerSpec oldSpec = playerSpecs.get(player.getUniqueId());
        playerSpecs.put(player.getUniqueId(), new PlayerSpec(newType, System.currentTimeMillis()));

        player.sendMessage(ChatColor.YELLOW + "Especializacion cambiada de " + oldSpec.type.getDisplayName() + " a " + newType.getDisplayName());
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);

        return true;
    }

    public boolean useSpecAbility(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSpec spec = playerSpecs.get(uuid);

        if (spec == null) {
            player.sendMessage(ChatColor.RED + "No tienes especializacion.");
            return false;
        }

        if (specCooldowns.containsKey(uuid)) {
            long elapsed = (System.currentTimeMillis() - specCooldowns.get(uuid)) / 1000;
            if (elapsed < 300) {
                player.sendMessage(ChatColor.RED + "Habilidad en cooldown: " + (300 - elapsed) + "s");
                return false;
            }
        }

        boolean success = executeAbility(player, spec.type);
        if (success) {
            specCooldowns.put(uuid, System.currentTimeMillis());
        }
        return success;
    }

    private boolean executeAbility(Player player, SpecType type) {
        switch (type) {
            case APOCALYPSE_SPECIALIST:
                return executeApocalypseAbility(player);
            case PLAGUE_CARRIER:
                return executePlagueAbility(player);
            case DARK_ARTS:
                return executeDarkArtsAbility(player);
            case FIELD_MEDIC:
                return executeFieldMedicAbility(player);
            default:
                return false;
        }
    }

    private boolean executeApocalypseAbility(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 0));
        player.sendMessage(ChatColor.DARK_RED + "[APOCALYPSE] Modo apocalipsis activado! 30s de poder absoluto.");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.7f);
        return true;
    }

    private boolean executePlagueAbility(Player player) {
        int radius = 20;
        int affected = 0;
        for (Player nearby : player.getLocation().getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= radius) {
                if (!nearby.equals(player)) {
                    double infection = plugin.getInfected().getInfectionLevel(nearby);
                    plugin.getInfected().decreasePlayerInfection(nearby, 30);
                    affected++;
                }
            }
        }
        player.sendMessage(ChatColor.DARK_GREEN + "[PLAGUE] Aura de cura activada! " + affected + " jugadores curados.");
        player.playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 1.0f, 1.0f);
        return true;
    }

    private boolean executeDarkArtsAbility(Player player) {
        int count = 0;
        if (player.hasMetadata("zm_minion_count")) {
            count = player.getMetadata("zm_minion_count").get(0).asInt();
        }

        if (count >= 3) {
            player.sendMessage(ChatColor.DARK_PURPLE + "[DARK ARTS] Maximo de minions activos (3).");
            return false;
        }

        player.sendMessage(ChatColor.DARK_PURPLE + "[DARK ARTS] Invocando zombie aliado...");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 0.8f);

        return true;
    }

    private boolean executeFieldMedicAbility(Player player) {
        int radius = 30;
        int healed = 0;
        for (Player nearby : player.getLocation().getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= radius) {
                double infection = plugin.getInfected().getInfectionLevel(nearby);
                if (infection > 0) {
                    double bonus = plugin.getProgressionManager().getCureBonus(player);
                    double cureAmount = 30 * (1 + bonus / 100);
                    plugin.getInfected().decreasePlayerInfection(nearby, cureAmount);
                    healed++;
                }
            }
        }

        plugin.getEconomyManager().deposit(player, healed * 25);
        player.sendMessage(ChatColor.RED + "[FIELD MEDIC] AOE Heal! " + healed + " jugadores curados, +" + (healed * 25) + " ZP bonus.");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        PlayerSpec spec = playerSpecs.get(attacker.getUniqueId());

        if (spec == null) return;

        switch (spec.type) {
            case APOCALYPSE_SPECIALIST:
                double extraDamage = event.getFinalDamage() * 0.5;
                event.setDamage(event.getFinalDamage() + extraDamage);
                break;
            case DARK_ARTS:
                if (plugin.getProgressionManager().getPlayerLevel(attacker) >= 50) {
                    int bonusXp = (int) (event.getFinalDamage() * 0.5);
                    plugin.getProgressionManager().getPlayerProgression(attacker).xp += bonusXp;
                }
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (playerSpecs.containsKey(player.getUniqueId())) {
            PlayerSpec spec = playerSpecs.get(player.getUniqueId());
            player.sendMessage(ChatColor.GOLD + "Tu especializacion: " + ChatColor.WHITE + spec.type.getDisplayName());
        }
    }

    public PlayerSpec getPlayerSpec(Player player) {
        return playerSpecs.get(player.getUniqueId());
    }

    public boolean hasSpec(Player player) {
        return playerSpecs.containsKey(player.getUniqueId());
    }

    public void clearSpec(Player player) {
        playerSpecs.remove(player.getUniqueId());
        specCooldowns.remove(player.getUniqueId());
    }

    public class PlayerSpec {
        public SpecType type;
        public long selectedAt;

        public PlayerSpec(SpecType type, long selectedAt) {
            this.type = type;
            this.selectedAt = selectedAt;
        }
    }
}
