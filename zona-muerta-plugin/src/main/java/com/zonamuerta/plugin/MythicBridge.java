package com.zonamuerta.plugin;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Puente entre MythicMobs y ZonaMuerta.
 * Aplica infección, drops ZM y efectos de luna de sangre
 * a los mobs personalizados de MythicMobs.
 */
public class MythicBridge implements Listener {

    private static final Set<String> ZM_ZOMBIE_TYPES = Set.of(
            "ZombiCaminante", "ZombiCorredor", "ZombiSoldado",
            "ZombiMutante", "ZombiExplosivo", "ZombiTrepador", "ElPatron"
    );

    private final ZonaMuerta plugin;
    private final Infected infected;
    // mobId -> bloodMoonBoosted flag
    private final Map<UUID, Boolean> trackedMobs = new HashMap<>();
    private boolean mythicAvailable = false;

    public MythicBridge(ZonaMuerta plugin, Infected infected) {
        this.plugin = plugin;
        this.infected = infected;
    }

    /** Comprueba si MythicMobs está disponible en el servidor. */
    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            plugin.getLogger().info("[ZM-Bridge] MythicMobs no encontrado, bridge desactivado.");
            return false;
        }
        mythicAvailable = true;
        plugin.getLogger().info("[ZM-Bridge] MythicMobs detectado. Bridge activo.");
        return true;
    }

    public boolean isMythicAvailable() {
        return mythicAvailable;
    }

    /** Devuelve true si la entidad es un mob MM de tipo zombie ZM. */
    public boolean isZMZombie(Entity entity) {
        if (!mythicAvailable) return false;
        try {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            if (!api.isMythicMob(entity)) return false;
            ActiveMob am = api.getMythicMobInstance(entity);
            return am != null && ZM_ZOMBIE_TYPES.contains(am.getType().getInternalName());
        } catch (Exception e) {
            return false;
        }
    }

    /** Devuelve el nombre interno del MythicMob, o null. */
    public String getMythicType(Entity entity) {
        if (!mythicAvailable) return null;
        try {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            if (!api.isMythicMob(entity)) return null;
            ActiveMob am = api.getMythicMobInstance(entity);
            return am != null ? am.getType().getInternalName() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ── Eventos MythicMobs ────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMythicSpawn(MythicMobSpawnEvent event) {
        if (!ZM_ZOMBIE_TYPES.contains(event.getMobType().getInternalName())) return;

        UUID uid = event.getEntity().getUniqueId();
        trackedMobs.put(uid, plugin.isBloodMoon());

        // Aplicar sangre + escala de dia segun dia actual
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Entity e = Bukkit.getEntity(uid);
            if (!(e instanceof LivingEntity le) || !le.isValid()) return;
            applyStatScaling(le, plugin.getCurrentDay(), plugin.isBloodMoon());
        }, 2L);
    }

    private void applyStatScaling(LivingEntity le, int currentDay, boolean bloodMoon) {
        // Escala segun dia: +5% HP y dano por dia
        double dayMultiplier = 1.0 + (currentDay * 0.05);

        // Blood moon: +50% HP, +30% velocidad
        double bloodMoonHpMultiplier = bloodMoon ? 1.5 : 1.0;
        double bloodMoonSpeedMultiplier = bloodMoon ? 1.3 : 1.0;

        double finalHp = le.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * dayMultiplier * bloodMoonHpMultiplier;
        le.getAttribute(Attribute.MAX_HEALTH).setBaseValue(finalHp);
        le.setHealth(Math.min(le.getHealth(), finalHp));

        double baseSpeed = 0.23 * bloodMoonSpeedMultiplier;
        le.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMythicDeath(MythicMobDeathEvent event) {
        String type = event.getMobType().getInternalName();
        if (!ZM_ZOMBIE_TYPES.contains(type)) return;

        trackedMobs.remove(event.getEntity().getUniqueId());

        if (event.getKiller() instanceof Player killer) {
            int xpReward = switch (type) {
                case "ZombiCaminante" -> 5;
                case "ZombiCorredor" -> 10;
                case "ZombiSoldado" -> 15;
                case "ZombiMutante" -> 25;
                case "ZombiExplosivo" -> 12;
                case "ZombiTrepador" -> 10;
                case "ElPatron" -> 100;
                default -> 5;
            };
            plugin.awardZombieExp(killer, xpReward, type);
        }
    }

    /** Refresca stats de todos los mobs activos cuando cambia sangre o dia. */
    public void refreshAllMobStats() {
        for (UUID uid : trackedMobs.keySet()) {
            Entity e = Bukkit.getEntity(uid);
            if (!(e instanceof LivingEntity le) || !le.isValid()) continue;
            applyStatScaling(le, plugin.getCurrentDay(), plugin.isBloodMoon());
        }
    }

    // ── Infección desde MM zombies ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMythicZombieAttack(EntityDamageByEntityEvent event) {
        if (!mythicAvailable) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isZMZombie(event.getDamager())) return;

        // Delegar al sistema de infección existente con probabilidad según tipo
        String type = getMythicType(event.getDamager());
        double extraChance = switch (type != null ? type : "") {
            case "ZombiMutante" -> 25.0;
            case "ZombiSoldado" -> 10.0;
            case "ZombiExplosivo" -> 8.0;
            case "ElPatron" -> 40.0;
            default -> 0.0; // usa la base de config
        };

        infected.infect(player, extraChance);
    }

    // ── Muertos vanilla que NO son MM → los gestiona ZonaMuerta normal ────────

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVanillaZombieDeath(EntityDeathEvent event) {
        if (!mythicAvailable) return;
        if (!isZMZombie(event.getEntity())) return;
        event.getDrops().clear();
        event.setDroppedExp(0);
    }

    /** Fuerza el spawn de un MM mob en una location usando el API. */
    public void spawnMythicMob(String mobType, org.bukkit.Location loc, double level) {
        if (!mythicAvailable) return;
        try {
            MythicBukkit.inst().getMobManager().getMythicMob(mobType).ifPresent(mythicMob -> {
                MythicBukkit.inst().getMobManager().spawnMob(mobType, loc, level);
            });
        } catch (Exception e) {
            plugin.getLogger().warning("[ZM-Bridge] Error spawning " + mobType + ": " + e.getMessage());
        }
    }
}
