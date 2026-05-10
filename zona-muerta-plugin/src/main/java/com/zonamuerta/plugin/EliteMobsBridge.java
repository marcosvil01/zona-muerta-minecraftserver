package com.zonamuerta.plugin;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.Bukkit;
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

public class EliteMobsBridge implements Listener {

    private static final Set<String> ZM_BOSS_TYPES = Set.of(
            "ZombiCaminante", "ZombiCorredor", "ZombiSoldado",
            "ZombiMutante", "ZombiExplosivo", "ZombiTrepador", "ElPatron"
    );

    private final ZonaMuerta plugin;
    private final Infected infected;
    private final Set<UUID> trackedZombieUUIDs = new HashSet<>();
    private boolean eliteMobsAvailable = false;
    private boolean bloodMoonActive = false;
    private int currentDay = 1;

    public EliteMobsBridge(ZonaMuerta plugin, Infected infected) {
        this.plugin = plugin;
        this.infected = infected;
    }

    public boolean init() {
        if (Bukkit.getPluginManager().getPlugin("EliteMobs") == null) {
            plugin.getLogger().warning("[ZM-Bridge] EliteMobs no encontrado - bridge desactivado.");
            return false;
        }
        eliteMobsAvailable = true;
        plugin.getLogger().info("[ZM-Bridge] EliteMobs activo - bridge operativo.");
        return true;
    }

    public boolean isEliteMobsAvailable() {
        return eliteMobsAvailable;
    }

    public boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public void setBloodMoonActive(boolean active) {
        this.bloodMoonActive = active;
        if (eliteMobsAvailable) {
            refreshAllMobScaling();
        }
    }

    public void setCurrentDay(int day) {
        this.currentDay = day;
        if (eliteMobsAvailable) {
            refreshAllMobScaling();
        }
    }

    public boolean isZMZombie(Entity entity) {
        if (!eliteMobsAvailable) return false;
        if (!(entity instanceof LivingEntity le)) return false;
        UUID uuid = le.getUniqueId();
        if (!trackedZombieUUIDs.contains(uuid)) return false;
        return true;
    }

    public String getBossTypeName(Entity entity) {
        if (!eliteMobsAvailable) return null;
        if (!(entity instanceof LivingEntity le)) return null;
        EliteEntity ee = getEliteEntity(entity).orElse(null);
        if (ee == null) return null;
        if (ee instanceof CustomBossEntity cbe) {
            return cbe.getCustomBossesConfigFields().getFilename().replace(".yml", "");
        }
        return null;
    }

    public Optional<EliteEntity> getEliteEntity(Entity entity) {
        if (!eliteMobsAvailable) return Optional.empty();
        if (!(entity instanceof LivingEntity le)) return Optional.empty();
        try {
            return EliteMobsPluginAccess.getEliteEntity(le);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Set<UUID> getTrackedMobs() {
        return trackedZombieUUIDs;
    }

    public void refreshAllMobScaling() {
        if (!eliteMobsAvailable) return;
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

        double baseMaxHealth = le.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue();
        double scaledHealth = baseMaxHealth * dayMultiplier * (bloodMoonActive ? 1.0 : 1.0);
        scaledHealth = Math.min(scaledHealth * bloodMoonHpMultiplier, baseMaxHealth * 3.0);
        le.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(scaledHealth);
        le.setHealth(Math.min(le.getHealth(), scaledHealth));

        double currentSpeed = le.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED).getBaseValue();
        double targetSpeed = Math.min(currentSpeed * bloodMoonSpeedMultiplier, 0.4);
        le.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED).setBaseValue(targetSpeed);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEliteMobSpawn(EliteMobSpawnEvent event) {
        EliteEntity eliteEntity = event.getEliteMobEntity();
        if (eliteEntity == null) return;

        String typeName = null;
        if (eliteEntity instanceof CustomBossEntity cbe && cbe.getCustomBossesConfigFields() != null) {
            typeName = cbe.getCustomBossesConfigFields().getFilename().replace(".yml", "");
        }

        if (typeName == null || !ZM_BOSS_TYPES.contains(typeName)) return;

        UUID uid = event.getEntity().getUniqueId();
        trackedZombieUUIDs.add(uid);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Entity e = Bukkit.getEntity(uid);
            if (!(e instanceof LivingEntity le) || !le.isValid()) return;
            applyScaling(le);
        }, 3L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEliteMobRemove(EliteMobsPluginAccess.EliteMobRemoveEvent event) {
        if (!eliteMobsAvailable) return;
        UUID uid = event.getEntityUUID();
        if (!trackedZombieUUIDs.contains(uid)) return;
        trackedZombieUUIDs.remove(uid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        EliteEntity eliteEntity = event.getEliteEntity();
        if (eliteEntity == null) return;

        String typeName = null;
        if (eliteEntity instanceof CustomBossEntity cbe && cbe.getCustomBossesConfigFields() != null) {
            typeName = cbe.getCustomBossesConfigFields().getFilename().replace(".yml", "");
        }

        if (typeName == null || !ZM_BOSS_TYPES.contains(typeName)) return;

        UUID uid = event.getEntity().getUniqueId();
        trackedZombieUUIDs.remove(uid);

        Player killer = null;
        if (eliteEntity.hasDamagers()) {
            killer = eliteEntity.getDamagers().keySet().stream().findFirst().orElse(null);
        }
        if (killer != null) {
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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEliteMobAttack(EntityDamageByEntityEvent event) {
        if (!eliteMobsAvailable) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isZMZombie(event.getDamager())) return;

        String type = getBossTypeName(event.getDamager());
        double extraChance = switch (type != null ? type : "") {
            case "ZombiMutante" -> 25.0;
            case "ZombiSoldado" -> 10.0;
            case "ZombiExplosivo" -> 8.0;
            case "ElPatron" -> 40.0;
            default -> 0.0;
        };

        infected.infect(player, extraChance);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVanillaZombieDeath(EntityDeathEvent event) {
        if (eliteMobsAvailable && isZMZombie(event.getEntity())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    public boolean spawnEliteBoss(String bossType, org.bukkit.Location loc, int level) {
        if (!eliteMobsAvailable) return false;
        try {
            CustomBossEntity boss = CustomBossEntity.createCustomBossEntity(bossType);
            if (boss == null) {
                plugin.getLogger().warning("[ZM-Bridge] Tipo boss no encontrado: " + bossType);
                return false;
            }
            boss.spawn(loc, level, false);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("[ZM-Bridge] Error spawning " + bossType + ": " + e.getMessage());
            return false;
        }
    }

    public boolean spawnEliteBossAtPlayer(String bossType, Player player, int level) {
        return spawnEliteBoss(bossType, player.getLocation(), level);
    }

    public long getActiveEliteMobCount() {
        if (!eliteMobsAvailable) return 0;
        return EliteMobsPluginAccess.getActiveEliteMobCount();
    }

    public long getZMZombieCount() {
        return trackedZombieUUIDs.stream().filter(uid -> {
            Entity e = Bukkit.getEntity(uid);
            return e instanceof LivingEntity le && le.isValid();
        }).count();
    }

    public static class EliteMobsPluginAccess {
        public static Optional<EliteEntity> getEliteEntity(LivingEntity le) {
            try {
                return Optional.ofNullable(
                        com.magmaguy.elitemobs.entitytracker.EntityTracker.getEliteMobEntity(le)
                );
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        public static long getActiveEliteMobCount() {
            try {
                return com.magmaguy.elitemobs.entitytracker.EntityTracker.getEliteMobEntities().size();
            } catch (Exception e) {
                return 0;
            }
        }

        public static class EliteMobRemoveEvent extends org.bukkit.event.Event {
            private static final org.bukkit.event.HandlerList handlers = new org.bukkit.event.HandlerList();
            private final UUID entityUUID;

            public EliteMobRemoveEvent(UUID entityUUID) {
                this.entityUUID = entityUUID;
            }

            public UUID getEntityUUID() {
                return entityUUID;
            }

            public static org.bukkit.event.HandlerList getHandlerList() {
                return handlers;
            }

            @Override
            public org.bukkit.event.HandlerList getHandlers() {
                return handlers;
            }
        }
    }
}