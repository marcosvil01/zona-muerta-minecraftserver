package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, PlayerStats> statsMap = new HashMap<>();
    private File statsFile;
    private org.bukkit.configuration.file.FileConfiguration statsConfig;

    public StatsManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadStats();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadStats() {
        statsFile = new File(plugin.getDataFolder(), "stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.getParentFile().mkdirs();
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create stats.yml: " + e.getMessage());
            }
        }
        statsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(statsFile);
    }

    public void saveStats() {
        for (Map.Entry<UUID, PlayerStats> entry : statsMap.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerStats stats = entry.getValue();
            String path = uuid.toString();
            statsConfig.set(path + ".total_kills", stats.totalKills);
            statsConfig.set(path + ".zombie_kills", stats.zombieKills);
            statsConfig.set(path + ".deaths", stats.deaths);
            statsConfig.set(path + ".damage_dealt", stats.damageDealt);
            statsConfig.set(path + ".damage_taken", stats.damageTaken);
            statsConfig.set(path + ".distance_traveled", stats.distanceTraveled);
            statsConfig.set(path + ".blocks_broken", stats.blocksBroken);
            statsConfig.set(path + ".blocks_placed", stats.blocksPlaced);
            statsConfig.set(path + ".play_time_ticks", stats.playTimeTicks);
            statsConfig.set(path + ".quests_completed", stats.questsCompleted);
            statsConfig.set(path + ".trades_made", stats.tradesMade);
            statsConfig.set(path + ".auctions_won", stats.auctionsWon);
            statsConfig.set(path + ".infection_cures", stats.infectionCures);
            statsConfig.set(path + ".bloodmoon_participations", stats.bloodmoonParticipations);
            statsConfig.set(path + ".structure_visits", stats.structureVisits);
        }
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save stats.yml: " + e.getMessage());
        }
    }

    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerStats stats = loadPlayerStats(uuid);
        statsMap.put(uuid, stats);
    }

    public void onPlayerQuit(Player player) {
        PlayerStats stats = statsMap.get(player.getUniqueId());
        if (stats != null) {
            stats.playTimeTicks += 1200;
        }
        saveStats();
    }

    private PlayerStats loadPlayerStats(UUID uuid) {
        PlayerStats stats = new PlayerStats();
        String path = uuid.toString();
        stats.totalKills = statsConfig.getInt(path + ".total_kills", 0);
        stats.zombieKills = statsConfig.getInt(path + ".zombie_kills", 0);
        stats.deaths = statsConfig.getInt(path + ".deaths", 0);
        stats.damageDealt = statsConfig.getDouble(path + ".damage_dealt", 0);
        stats.damageTaken = statsConfig.getDouble(path + ".damage_taken", 0);
        stats.distanceTraveled = statsConfig.getDouble(path + ".distance_traveled", 0);
        stats.blocksBroken = statsConfig.getInt(path + ".blocks_broken", 0);
        stats.blocksPlaced = statsConfig.getInt(path + ".blocks_placed", 0);
        stats.playTimeTicks = statsConfig.getLong(path + ".play_time_ticks", 0);
        stats.questsCompleted = statsConfig.getInt(path + ".quests_completed", 0);
        stats.tradesMade = statsConfig.getInt(path + ".trades_made", 0);
        stats.auctionsWon = statsConfig.getInt(path + ".auctions_won", 0);
        stats.infectionCures = statsConfig.getInt(path + ".infection_cures", 0);
        stats.bloodmoonParticipations = statsConfig.getInt(path + ".bloodmoon_participations", 0);
        stats.structureVisits = statsConfig.getInt(path + ".structure_visits", 0);
        return stats;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        UUID victimUuid = victim.getUniqueId();

        PlayerStats victimStats = statsMap.get(victimUuid);
        if (victimStats != null) {
            victimStats.deaths++;
            victimStats.playTimeTicks += 1200;
        }

        if (killer != null) {
            PlayerStats killerStats = statsMap.get(killer.getUniqueId());
            if (killerStats != null) {
                killerStats.totalKills++;
                if (victim.getType().name().equals("ZOMBIE")) {
                    killerStats.zombieKills++;
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        PlayerStats stats = statsMap.get(attacker.getUniqueId());
        if (stats != null) {
            stats.damageDealt += event.getFinalDamage();
        }

        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerStats vStats = statsMap.get(victim.getUniqueId());
            if (vStats != null) {
                vStats.damageTaken += event.getFinalDamage();
            }
        }
    }

    @EventHandler
    public void onPlayerJoinForStats(PlayerJoinEvent event) {
        onPlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitForStats(PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer());
    }

    public PlayerStats getPlayerStats(Player player) {
        return statsMap.getOrDefault(player.getUniqueId(), new PlayerStats());
    }

    public void incrementKill(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.totalKills++;
        stats.zombieKills++;
    }

    public void incrementDeath(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.deaths++;
    }

    public void addDamageDealt(Player player, double amount) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.damageDealt += amount;
    }

    public void addDistanceTraveled(Player player, double distance) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.distanceTraveled += distance;
    }

    public void incrementBlocksBroken(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.blocksBroken++;
    }

    public void incrementBlocksPlaced(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.blocksPlaced++;
    }

    public void incrementQuestsCompleted(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.questsCompleted++;
    }

    public void incrementTradesMade(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.tradesMade++;
    }

    public void incrementAuctionsWon(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.auctionsWon++;
    }

    public void incrementInfectionCures(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.infectionCures++;
    }

    public void incrementBloodmoonParticipation(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.bloodmoonParticipations++;
    }

    public void incrementStructureVisits(Player player) {
        PlayerStats stats = statsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerStats());
        stats.structureVisits++;
    }

    public void showStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        player.sendMessage(ChatColor.DARK_GRAY + "=========================");
        player.sendMessage(ChatColor.RED + "  ESTADISTICAS DE " + ChatColor.WHITE + player.getName());
        player.sendMessage(ChatColor.DARK_GRAY + "=========================");
        player.sendMessage(ChatColor.GRAY + "Zombies eliminados: " + ChatColor.GREEN + stats.zombieKills);
        player.sendMessage(ChatColor.GRAY + "Total de kills: " + ChatColor.GREEN + stats.totalKills);
        player.sendMessage(ChatColor.GRAY + "Muertes: " + ChatColor.RED + stats.deaths);
        player.sendMessage(ChatColor.GRAY + "K/D: " + ChatColor.WHITE + String.format("%.2f", stats.getKD()));
        player.sendMessage(ChatColor.GRAY + "Dano infligido: " + ChatColor.YELLOW + String.format("%.0f", stats.damageDealt));
        player.sendMessage(ChatColor.GRAY + "Dano recibido: " + ChatColor.YELLOW + String.format("%.0f", stats.damageTaken));
        player.sendMessage(ChatColor.GRAY + "Distancia viajada: " + ChatColor.WHITE + String.format("%.0f", stats.distanceTraveled) + " bloques");
        player.sendMessage(ChatColor.GRAY + "Bloques rotos: " + ChatColor.WHITE + stats.blocksBroken);
        player.sendMessage(ChatColor.GRAY + "Bloques colocados: " + ChatColor.WHITE + stats.blocksPlaced);
        player.sendMessage(ChatColor.GRAY + "Quests completadas: " + ChatColor.AQUA + stats.questsCompleted);
        player.sendMessage(ChatColor.GRAY + "Tiempo de juego: " + ChatColor.WHITE + stats.getPlayTimeString());
        player.sendMessage(ChatColor.GRAY + "Curas de infeccion: " + ChatColor.LIGHT_PURPLE + stats.infectionCures);
        player.sendMessage(ChatColor.GRAY + "Lunas de sangre: " + ChatColor.DARK_RED + stats.bloodmoonParticipations);
        player.sendMessage(ChatColor.DARK_GRAY + "=========================");
    }

    public void onDisable() {
        saveStats();
    }

    public class PlayerStats {
        public int totalKills = 0;
        public int zombieKills = 0;
        public int deaths = 0;
        public double damageDealt = 0;
        public double damageTaken = 0;
        public double distanceTraveled = 0;
        public int blocksBroken = 0;
        public int blocksPlaced = 0;
        public long playTimeTicks = 0;
        public int questsCompleted = 0;
        public int tradesMade = 0;
        public int auctionsWon = 0;
        public int infectionCures = 0;
        public int bloodmoonParticipations = 0;
        public int structureVisits = 0;

        public double getKD() {
            if (deaths == 0) return totalKills;
            return (double) totalKills / deaths;
        }

        public String getPlayTimeString() {
            long totalSeconds = playTimeTicks / 20;
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            if (hours > 0) {
                return hours + "h " + minutes + "m " + seconds + "s";
            } else if (minutes > 0) {
                return minutes + "m " + seconds + "s";
            } else {
                return seconds + "s";
            }
        }
    }
}
