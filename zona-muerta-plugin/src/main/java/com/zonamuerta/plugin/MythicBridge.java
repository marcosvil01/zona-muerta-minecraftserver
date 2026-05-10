package com.zonamuerta.plugin;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class MythicBridge implements Listener {

    private static final Set<String> ZM_ZOMBIE_TYPES = Set.of(
            "ZombiCaminante", "ZombiCorredor", "ZombiSoldado",
            "ZombiMutante", "ZombiExplosivo", "ZombiTrepador", "ElPatron"
    );

    private final ZonaMuerta plugin;
    private final Infected infected;
    private final Set<UUID> trackedZombieUUIDs = new HashSet<>();
    private boolean mythicAvailable = false;
    private boolean bloodMoonActive = false;
    private int currentDay = 1;

    public MythicBridge(ZonaMuerta plugin, Infected infected) {
        this.plugin = plugin;
        this.infected = infected;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            plugin.getLogger().warning("[ZM-Bridge] MythicMobs no encontrado - bridge desactivado.");
            return false;
        }
        mythicAvailable = true;
        plugin.getLogger().info("[ZM-Bridge] MythicMobs activo - bridge operativo.");
        return true;
    }

    public boolean isMythicAvailable() {
        return mythicAvailable;
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public void setBloodMoonActive(boolean active) {
        this.bloodMoonActive = active;
        if (mythicAvailable) {
            refreshAllMobScaling();
        }
    }

    public void setCurrentDay(int day) {
        this.currentDay = day;
        if (mythicAvailable) {
            refreshAllMobScaling();
        }
    }

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

    public String getMythicTypeName(Entity entity) {
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

    public Optional<ActiveMob> getActiveMob(Entity entity) {
        if (!mythicAvailable) return Optional.empty();
        try {
            BukkitAPIHelper api = MythicBukkit.inst().getAPIHelper();
            if (!api.isMythicMob(entity)) return Optional.empty();
            return Optional.ofNullable(api.getMythicMobInstance(entity));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Set<UUID> getTrackedMobs() {
        return trackedZombieUUIDs;
    }

    // ── Blood Moon Scaling (applied via MM skills + manual refresh) ──────────

    public void refreshAllMobScaling() {
        if (!mythicAvailable) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID uid : trackedZombieUUIDs) {
                Entity e = Bukkit.getEntity(uid);
                if (!(e instanceof LivingEntity le) || !le.isValid()) {
                    trackedZombieUUIDs.remove(uid);
                    continue;
                }
                applyScaling(le);
            }
        });
    }

    private void applyScaling(LivingEntity le) {
        double dayMultiplier = 1.0 + (currentDay * 0.05);
        double bloodMoonHpMultiplier = bloodMoonActive ? 1.5 : 1.0;
        double bloodMoonSpeedMultiplier = bloodMoonActive ? 1.3 : 1.0;

        double baseMaxHealth = le.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        double scaledHealth = baseMaxHealth * dayMultiplier * (bloodMoonActive ? 1.0 : 1.0);
        scaledHealth = Math.min(scaledHealth * bloodMoonHpMultiplier, baseMaxHealth * 3.0);
        le.getAttribute(Attribute.MAX_HEALTH).setBaseValue(scaledHealth);
        le.setHealth(Math.min(le.getHealth(), scaledHealth));

        double currentSpeed = le.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue();
        double targetSpeed = Math.min(currentSpeed * bloodMoonSpeedMultiplier, 0.4);
        le.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(targetSpeed);
    }

    // ── Events ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMythicSpawn(MythicMobSpawnEvent event) {
        String typeName = event.getMobType().getInternalName();
        if (!ZM_ZOMBIE_TYPES.contains(typeName)) return;

        UUID uid = event.getEntity().getUniqueId();
        trackedZombieUUIDs.add(uid);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Entity e = Bukkit.getEntity(uid);
            if (!(e instanceof LivingEntity le) || !le.isValid()) return;
            applyScaling(le);
        }, 3L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMythicDespawn(MythicMobDespawnEvent event) {
        String typeName = event.getMobType().getInternalName();
        if (!ZM_ZOMBIE_TYPES.contains(typeName)) return;
        trackedZombieUUIDs.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMythicDeath(MythicMobDeathEvent event) {
        String typeName = event.getMobType().getInternalName();
        if (!ZM_ZOMBIE_TYPES.contains(typeName)) return;

        UUID uid = event.getEntity().getUniqueId();
        trackedZombieUUIDs.remove(uid);

        if (event.getKiller() instanceof Player killer) {
            int xp = getXpReward(typeName);
            plugin.awardZombieExp(killer, xp, typeName);
        }
    }

    private int getXpReward(String typeName) {
        return switch (typeName) {
            case "ZombiCaminante" -> 5;
            case "ZombiCorredor" -> 10;
            case "ZombiSoldado" -> 15;
            case "ZombiMutante" -> 25;
            case "ZombiExplosivo" -> 12;
            case "ZombiTrepador" -> 10;
            case "ElPatron" -> 100;
            default -> 5;
        };
    }

    // ── Infection from MM zombies ─────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMythicZombieAttack(EntityDamageByEntityEvent event) {
        if (!mythicAvailable) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isZMZombie(event.getDamager())) return;

        String type = getMythicTypeName(event.getDamager());
        double extraChance = switch (type != null ? type : "") {
            case "ZombiMutante" -> 25.0;
            case "ZombiSoldado" -> 10.0;
            case "ZombiExplosivo" -> 8.0;
            case "ElPatron" -> 40.0;
            default -> 0.0;
        };

        infected.infect(player, extraChance);
    }

    // ── Vanilla zombie death cleanup (non-MM) ──────────────────────────────────

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVanillaZombieDeath(EntityDeathEvent event) {
        if (mythicAvailable && isZMZombie(event.getEntity())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    // ── Programmatic Spawning ─────────────────────────────────────────────────

    public boolean spawnMythicMob(String mobType, Location loc, double level) {
        if (!mythicAvailable) return false;
        try {
            Optional<MythicMob> mobOpt = MythicBukkit.inst().getMobManager().getMythicMob(mobType);
            if (mobOpt.isEmpty()) {
                plugin.getLogger().warning("[ZM-Bridge] Tipo mob no encontrado: " + mobType);
                return false;
            }
            MythicBukkit.inst().getMobManager().spawnMob(mobType, loc, level);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("[ZM-Bridge] Error spawning " + mobType + ": " + e.getMessage());
            return false;
        }
    }

    public boolean spawnMythicMobAtPlayer(String mobType, Player player, double level) {
        return spawnMythicMob(mobType, player.getLocation(), level);
    }

    // ── MM Direct API Access ───────────────────────────────────────────────────

    public BukkitAPIHelper getAPI() {
        return mythicAvailable ? MythicBukkit.inst().getAPIHelper() : null;
    }

    public void forEachActiveMob(Consumer<ActiveMob> action) {
        if (!mythicAvailable) return;
        MythicBukkit.inst().getMobManager().getActiveMobs().forEach(action);
    }

    public long getActiveMobCount() {
        if (!mythicAvailable) return 0;
        return MythicBukkit.inst().getMobManager().getActiveMobs().size();
    }

    public long getZMZombieCount() {
        return trackedZombieUUIDs.stream().filter(uid -> {
            Entity e = Bukkit.getEntity(uid);
            return e instanceof LivingEntity le && le.isValid();
        }).count();
    }
}