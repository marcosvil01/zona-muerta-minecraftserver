package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MonthlyEventManager implements org.bukkit.event.Listener {

    private final ZonaMuerta plugin;
    private final Map<String, EventInfo> activeEvents = new HashMap<>();
    private final Map<String, Long> eventHistory = new HashMap<>();
    private boolean eventActive = false;
    private String currentEventId = null;
    private long eventStartTime = 0;
    private int eventDurationHours = 24;

    public enum EventType {
        HORDE_MODE("Horde Mode", "Todos los zombies +100% spawn, todos los dias son sangre", ChatColor.DARK_RED),
        BOSS_RAID("Boss Raid", "El Patron aparece cada hora con minions", ChatColor.DARK_PURPLE),
        DOUBLE_LOOT("Double Loot", "Todo el loot dropeado x2", ChatColor.GOLD),
        INFECTION_OUTBREAK("Infection Outbreak", "Todos los jugadores se infectan al 50%", ChatColor.DARK_GREEN),
        DARKNESS_FALLS("Darkness Falls", "Lunas de sangre permanentes hasta que la mayoria muera", ChatColor.DARK_GRAY),
        GOLDEN_ARMY("Golden Army", "100 zombies con oro aparecen con recompensas 5x", ChatColor.YELLOW);

        private final String displayName;
        private final String description;
        private final ChatColor color;

        EventType(String displayName, String description, ChatColor color) {
            this.displayName = displayName;
            this.description = description;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public ChatColor getColor() { return color; }
    }

    public MonthlyEventManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        checkAndStartScheduledEvent();
        startEventMonitor();
    }

    private void checkAndStartScheduledEvent() {
        Calendar now = Calendar.getInstance();
        int dayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        if (dayOfMonth == 1 && !eventActive) {
            startRandomEvent();
        }
    }

    private void startEventMonitor() {
        new BukkitRunnable() {
            public void run() {
                if (eventActive) {
                    long elapsed = System.currentTimeMillis() - eventStartTime;
                    long durationMs = eventDurationHours * 3600000L;
                    if (elapsed >= durationMs) {
                        endCurrentEvent();
                    }

                    if (currentEventId != null && activeEvents.containsKey(currentEventId)) {
                        EventInfo info = activeEvents.get(currentEventId);
                        if (info.isHourly) {
                            long hourlyElapsed = System.currentTimeMillis() - info.lastHourlyTrigger;
                            if (hourlyElapsed >= 3600000L) {
                                triggerHourlyEvent();
                                info.lastHourlyTrigger = System.currentTimeMillis();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }

    public boolean startRandomEvent() {
        if (eventActive) {
            return false;
        }

        EventType[] types = EventType.values();
        EventType randomType = types[new Random().nextInt(types.length)];
        return startEvent(randomType);
    }

    public boolean startEvent(EventType type) {
        if (eventActive) {
            return false;
        }

        eventActive = true;
        currentEventId = type.name();
        eventStartTime = System.currentTimeMillis();

        EventInfo info = new EventInfo(type);
        info.startTime = eventStartTime;
        info.isHourly = (type == EventType.BOSS_RAID);
        activeEvents.put(currentEventId, info);

        broadcastEventStart(type);

        applyEventEffects(type);

        return true;
    }

    public boolean endCurrentEvent() {
        if (!eventActive || currentEventId == null) {
            return false;
        }

        EventInfo info = activeEvents.get(currentEventId);
        EventType type = info.type;

        removeEventEffects(type);

        eventHistory.put(currentEventId, eventStartTime);

        broadcastEventEnd(type);

        eventActive = false;
        currentEventId = null;

        return true;
    }

    private void applyEventEffects(EventType type) {
        switch (type) {
            case HORDE_MODE:
                applyHordeModeEffects();
                break;
            case BOSS_RAID:
                applyBossRaidEffects();
                break;
            case DOUBLE_LOOT:
                applyDoubleLootEffects();
                break;
            case INFECTION_OUTBREAK:
                applyInfectionOutbreakEffects();
                break;
            case DARKNESS_FALLS:
                applyDarknessFallsEffects();
                break;
            case GOLDEN_ARMY:
                applyGoldenArmyEffects();
                break;
        }
    }

    private void applyHordeModeEffects() {
        plugin.getLogger().info("[EVENT] Horde Mode activated - spawn rates doubled");
    }

    private void applyBossRaidEffects() {
        plugin.getLogger().info("[EVENT] Boss Raid activated - El Patron spawning hourly");
        new BukkitRunnable() {
            public void run() {
                if (!eventActive) {
                    cancel();
                    return;
                }
                spawnBossRaid();
            }
        }.runTaskTimer(plugin, 3600000L, 3600000L);
    }

    private void spawnBossRaid() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (plugin.getMythicBridge() != null && plugin.getMythicBridge().isMythicAvailable()) {
                plugin.getMythicBridge().spawnMythicMob("ElPatron", player.getLocation(), 10.0);
                player.sendMessage(ChatColor.DARK_PURPLE + "[EVENT] El Patron ha aparecido!");
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_AGITATED, 1.0f, 1.0f);
            }
        }
    }

    private void triggerHourlyEvent() {
        if (currentEventId != null && activeEvents.containsKey(currentEventId)) {
            EventInfo info = activeEvents.get(currentEventId);
            if (info.type == EventType.BOSS_RAID) {
                spawnBossRaid();
            }
        }
    }

    private void applyDoubleLootEffects() {
        plugin.getLogger().info("[EVENT] Double Loot activated");
    }

    private void applyInfectionOutbreakEffects() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            double currentInfection = plugin.getInfected().getInfectionLevel(player);
            if (currentInfection < 50) {
                plugin.getInfected().increasePlayerInfection(player, 50 - currentInfection);
            }
        }
        plugin.getLogger().info("[EVENT] Infection Outbreak - all players set to 50% infection");
    }

    private void applyDarknessFallsEffects() {
        if (!plugin.isBloodMoon()) {
            plugin.triggerBloodMoon();
        }
        plugin.getLogger().info("[EVENT] Darkness Falls - blood moon forced");
    }

    private void applyGoldenArmyEffects() {
        plugin.getLogger().info("[EVENT] Golden Army spawned");
    }

    private void removeEventEffects(EventType type) {
        switch (type) {
            case DARKNESS_FALLS:
                plugin.stopBloodMoon();
                break;
            case INFECTION_OUTBREAK:
                break;
        }
    }

    private void broadcastEventStart(EventType type) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(type.getColor() + "================================");
        Bukkit.broadcastMessage(type.getColor() + "  �15 EVENTO ESPECIAL: " + type.getDisplayName());
        Bukkit.broadcastMessage(ChatColor.WHITE + "  " + type.getDescription());
        Bukkit.broadcastMessage(type.getColor() + "  Duracion: " + eventDurationHours + " horas");
        Bukkit.broadcastMessage(type.getColor() + "================================");
        Bukkit.broadcastMessage("");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 2, 0), 50);
        }
    }

    private void broadcastEventEnd(EventType type) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.GREEN + "================================");
        Bukkit.broadcastMessage(ChatColor.GREEN + "  EVENTO TERMINADO: " + type.getDisplayName());
        Bukkit.broadcastMessage(ChatColor.GRAY + "  Duraste " + getEventDuration() + " horas");
        Bukkit.broadcastMessage(ChatColor.GREEN + "================================");
        Bukkit.broadcastMessage("");

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1.0f);
        }
    }

    public String getEventDuration() {
        if (!eventActive) {
            return "0";
        }
        long elapsed = System.currentTimeMillis() - eventStartTime;
        long hours = elapsed / 3600000L;
        return String.valueOf(hours);
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public String getCurrentEventName() {
        if (!eventActive || currentEventId == null) {
            return null;
        }
        return currentEventId;
    }

    public EventInfo getCurrentEvent() {
        if (currentEventId != null) {
            return activeEvents.get(currentEventId);
        }
        return null;
    }

    public boolean forceEvent(EventType type) {
        if (eventActive) {
            endCurrentEvent();
        }
        return startEvent(type);
    }

    public void setEventDuration(int hours) {
        this.eventDurationHours = hours;
    }

    public class EventInfo {
        public EventType type;
        public long startTime;
        public long lastHourlyTrigger;
        public boolean isHourly;
        public Map<String, Integer> participantCount = new HashMap<>();

        public EventInfo(EventType type) {
            this.type = type;
        }
    }
}
