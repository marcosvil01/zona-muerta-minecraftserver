package com.zonamuerta.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, org.bukkit.scoreboard.Scoreboard> playerBoards = new HashMap<>();
    private final Map<UUID, String> displayLines = new HashMap<>();
    private boolean active = true;

    public ScoreboardManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUpdateTask();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            public void run() {
                if (!active) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(player);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    public void updateScoreboard(Player player) {
        UUID uuid = player.getUniqueId();
        org.bukkit.scoreboard.Scoreboard board = player.getScoreboard();
        if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getMainScoreboard();
            player.setScoreboard(board);
        }

        String objectiveName = "zm_stats";

        org.bukkit.scoreboard.Objective obj = board.getObjective(objectiveName);
        if (obj == null) {
            obj = board.registerNewObjective(objectiveName, org.bukkit.scoreboard.Criteria.DUMMY,
                ChatColor.DARK_RED + "" + ChatColor.BOLD + "ZONA MUERTA");
            obj.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        }

        int score = 15;

        clearScores(board, objectiveName);

        org.bukkit.scoreboard.Team dayTeam = getOrCreateTeam(board, "day");
        dayTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Dia: "));
        dayTeam.suffix(net.kyori.adventure.text.Component.text(ChatColor.WHITE + String.valueOf(plugin.getCurrentDay())));
        dayTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Dia: " + ChatColor.WHITE + plugin.getCurrentDay());

        int level = 1;
        int xp = 0;
        int xpToNext = 10;
        if (plugin.getProgressionManager() != null) {
            try {
                level = plugin.getProgressionManager().getPlayerLevel(player);
                ProgressionManager.ProgressionResult result = plugin.getProgressionManager().getPlayerLevelInfo(player);
                xp = result.xp;
                xpToNext = result.xpToNext;
            } catch (Exception e) {
            }
        }
        org.bukkit.scoreboard.Team levelTeam = getOrCreateTeam(board, "level");
        levelTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Nivel: " + ChatColor.GOLD));
        levelTeam.suffix(net.kyori.adventure.text.Component.text(ChatColor.WHITE + String.valueOf(level)));
        levelTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Nivel: " + ChatColor.GOLD + level);

        org.bukkit.scoreboard.Team xpTeam = getOrCreateTeam(board, "xp");
        xpTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "XP: " + ChatColor.WHITE));
        xpTeam.suffix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "(" + xp + "/" + xpToNext + ")"));
        xpTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "XP: " + ChatColor.WHITE + xp + ChatColor.GRAY + "/" + xpToNext);

        org.bukkit.scoreboard.Team killsTeam = getOrCreateTeam(board, "kills");
        int kills = 0;
        if (plugin.getStatsManager() != null) {
            try {
                kills = plugin.getStatsManager().getPlayerStats(player).zombieKills;
            } catch (Exception e) {
            }
        }
        killsTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Kills: " + ChatColor.WHITE));
        killsTeam.suffix(net.kyori.adventure.text.Component.text(String.valueOf(kills)));
        killsTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Kills: " + ChatColor.WHITE + kills);

        org.bukkit.scoreboard.Team classTeam = getOrCreateTeam(board, "class");
        String className = "Ninguna";
        if (plugin.getClassManager() != null) {
            try {
                className = plugin.getClassManager().getPlayerClass(player).getDisplayName();
            } catch (Exception e) {
            }
        }
        classTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Clase: " + ChatColor.WHITE));
        classTeam.suffix(net.kyori.adventure.text.Component.text(String.valueOf(className)));
        classTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Clase: " + ChatColor.WHITE + className);

        double balance = 0;
        if (plugin.getEconomyManager() != null) {
            try {
                balance = plugin.getEconomyManager().getBalance(player);
            } catch (Exception e) {
            }
        }
        org.bukkit.scoreboard.Team balanceTeam = getOrCreateTeam(board, "balance");
        balanceTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Balance: " + ChatColor.YELLOW));
        balanceTeam.suffix(net.kyori.adventure.text.Component.text("ZP"));
        balanceTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Balance: " + ChatColor.YELLOW + (int)balance + " ZP");

        double health = player.getHealth();
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getBaseValue();
        org.bukkit.scoreboard.Team healthTeam = getOrCreateTeam(board, "health");
        healthTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Vida: " + ChatColor.RED));
        healthTeam.suffix(net.kyori.adventure.text.Component.text(String.format("%.1f/%.1f", health, maxHealth)));
        healthTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Vida: " + ChatColor.RED + String.format("%.1f", health));

        double infection = 0;
        if (plugin.getInfected() != null) {
            try {
                infection = plugin.getInfected().getInfectionLevel(player);
            } catch (Exception e) {
            }
        }
        org.bukkit.scoreboard.Team infectionTeam = getOrCreateTeam(board, "infection");
        infectionTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Infeccion: " + ChatColor.DARK_GREEN));
        infectionTeam.suffix(net.kyori.adventure.text.Component.text("%"));
        infectionTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Infeccion: " + ChatColor.DARK_GREEN + String.format("%.1f", infection) + "%");

        org.bukkit.scoreboard.Team bmTeam = getOrCreateTeam(board, "bloodmoon");
        String bmStatus = plugin.isBloodMoon() ? ChatColor.DARK_RED + "ACTIVA" : ChatColor.DARK_GREEN + "Inactiva";
        bmTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Luna de Sangre: "));
        bmTeam.suffix(net.kyori.adventure.text.Component.text(""));
        bmTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + "Luna: " + (plugin.isBloodMoon() ? ChatColor.DARK_RED + "ACTIVA" : ChatColor.DARK_GREEN + "Normal"));

        org.bukkit.scoreboard.Team sepTeam = getOrCreateTeam(board, "sep0");
        sepTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.DARK_GRAY + "----------------"));
        sepTeam.suffix(net.kyori.adventure.text.Component.text(""));
        sepTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score--) + ChatColor.DARK_GRAY + "----------------");

        org.bukkit.scoreboard.Team serverTeam = getOrCreateTeam(board, "server");
        serverTeam.prefix(net.kyori.adventure.text.Component.text(ChatColor.GRAY + "Server: " + ChatColor.WHITE));
        serverTeam.suffix(net.kyori.adventure.text.Component.text(""));
        serverTeam.addEntry(ChatColor.WHITE + "" + ChatColor.RESET + toColorCode(score) + "Server: " + ChatColor.WHITE + "ZonaMuerta");
    }

    private void clearScores(org.bukkit.scoreboard.Scoreboard board, String objectiveName) {
        org.bukkit.scoreboard.Objective obj = board.getObjective(objectiveName);
        if (obj == null) return;
        for (String entry : board.getEntries()) {
            if (board.getObjective("zm_stats") != null && board.getObjective("zm_stats").getScore(entry).isScoreSet()) {
                board.getObjective("zm_stats").getScore(entry).setScore(0);
            }
        }
    }

    private org.bukkit.scoreboard.Team getOrCreateTeam(org.bukkit.scoreboard.Scoreboard board, String name) {
        org.bukkit.scoreboard.Team team = board.getTeam(name);
        if (team == null) {
            team = board.registerNewTeam(name);
        }
        return team;
    }

    private String toColorCode(int score) {
        String[] codes = {"a","b","c","d","e","f","0","1","2","3","4","5","6","7","8","9"};
        if (score >= 0 && score < codes.length) {
            return ChatColor.COLOR_CHAR + codes[score];
        }
        return ChatColor.WHITE.toString();
    }

    public void showScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        updateScoreboard(player);
    }

    public void hideScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                updateScoreboard(player);
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        new BukkitRunnable() {
            public void run() {
                updateScoreboard(player);
            }
        }.runTaskLater(plugin, 10L);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
