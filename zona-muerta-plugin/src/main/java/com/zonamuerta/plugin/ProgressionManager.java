package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ProgressionManager implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, PlayerProgression> playersData = new HashMap<>();
    private int maxLevel = 100;
    private double baseXpPerLevel = 10;
    private Map<String, Double> mobXpRewards = new HashMap<>();
    private Map<Integer, Perk> perksByLevel = new LinkedHashMap<>();
    private File playersFile;
    private FileConfiguration playersConfig;

    public ProgressionManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadConfig();
        loadPlayersData();
        registerPerks();
    }

    private void loadConfig() {
        this.maxLevel = plugin.getConfig().getInt("progression.max_level", 100);
        this.baseXpPerLevel = plugin.getConfig().getDouble("progression.xp_per_level_base", 10.0);
        this.mobXpRewards.put("ZombiCaminante", 5.0);
        this.mobXpRewards.put("ZombiCorredor", 10.0);
        this.mobXpRewards.put("ZombiSoldado", 15.0);
        this.mobXpRewards.put("ZombiMutante", 25.0);
        this.mobXpRewards.put("ZombiExplosivo", 12.0);
        this.mobXpRewards.put("ZombiTrepador", 10.0);
        this.mobXpRewards.put("ElPatron", 500.0);
        this.mobXpRewards.put("normal", 3.0);
    }

    public void loadPlayersData() {
        this.playersFile = new File(plugin.getDataFolder(), "progression.yml");
        if (!playersFile.exists()) {
            try {
                playersFile.getParentFile().mkdirs();
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create progression.yml: " + e.getMessage());
            }
        }
        this.playersConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(playersFile);
    }

    public void savePlayersData() {
        for (Map.Entry<UUID, PlayerProgression> entry : playersData.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerProgression prog = entry.getValue();
            String path = uuid.toString();
            playersConfig.set(path + ".level", prog.level);
            playersConfig.set(path + ".xp", prog.xp);
            playersConfig.set(path + ".xpToNextLevel", prog.xpToNextLevel);
            playersConfig.set(path + ".perks", new ArrayList<>(prog.perks));
            playersConfig.set(path + ".totalKills", prog.totalKills);
            playersConfig.set(path + ".zombieKills", prog.zombieKills);
            playersConfig.set(path + ".survivalDays", prog.survivalDays);
            playersConfig.set(path + ".lastLoginDay", prog.lastLoginDay);
        }
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save progression.yml: " + e.getMessage());
        }
    }

    private void registerPerks() {
        perksByLevel.put(5, new Perk("zombie_slayer_1", "+15% damage vs zombies",
            p -> {}, p -> p.setMetadata("zm_damage_bonus_zombies", new FixedMetadataValue(plugin, 15))));
        perksByLevel.put(10, new Perk("survivor_1", "+2 max hearts",
            p -> applyHeartsBonus(p, 2), p -> {}));
        perksByLevel.put(15, new Perk("zombie_slayer_2", "+25% damage vs zombies",
            p -> {}, p -> p.setMetadata("zm_damage_bonus_zombies", new FixedMetadataValue(plugin, 25))));
        perksByLevel.put(20, new Perk("survivor_2", "+2 max hearts",
            p -> applyHeartsBonus(p, 2), p -> {}));
        perksByLevel.put(20, new Perk("scavenger_1", "+20% drop chance",
            p -> {}, p -> p.setMetadata("zm_drop_bonus", new FixedMetadataValue(plugin, 20))));
        perksByLevel.put(25, new Perk("zombie_slayer_3", "+35% damage vs zombies",
            p -> {}, p -> p.setMetadata("zm_damage_bonus_zombies", new FixedMetadataValue(plugin, 35))));
        perksByLevel.put(30, new Perk("survivor_3", "+2 max hearts",
            p -> applyHeartsBonus(p, 2), p -> {}));
        perksByLevel.put(35, new Perk("scavenger_2", "+30% drop chance",
            p -> {}, p -> p.setMetadata("zm_drop_bonus", new FixedMetadataValue(plugin, 30))));
        perksByLevel.put(40, new Perk("berserker", "Speed+Strength on kill (5s)",
            p -> {}, p -> {}));
        perksByLevel.put(50, new Perk("apocalypse_ready", "+5 hearts, immunity to first infection",
            p -> applyHeartsBonus(p, 5), p -> {}));
        perksByLevel.put(60, new Perk("解毒剂增强", "Golden apples cure 50% more infection",
            p -> {}, p -> p.setMetadata("zm_cure_bonus", new FixedMetadataValue(plugin, 50))));
        perksByLevel.put(75, new Perk("horde_destroyer", "Critical hits on zombies (x2 damage)",
            p -> {}, p -> p.setMetadata("zm_crit_chance", new FixedMetadataValue(plugin, 20))));
        perksByLevel.put(100, new Perk("末日求生者", "+10 hearts, all previous perks",
            p -> applyHeartsBonus(p, 10), p -> {}));
    }

    private void applyHeartsBonus(Player player, int hearts) {
        double bonus = hearts * 2.0;
        double currentMax = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMax + bonus);
        player.setHealth(Math.min(player.getHealth(), currentMax + bonus));
        player.sendMessage(ChatColor.GREEN + "+" + hearts + " hearts permanently!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerProgression prog = playersData.get(uuid);
        if (prog == null) {
            prog = loadPlayerData(uuid);
            playersData.put(uuid, prog);
        }
        prog.lastLoginDay = plugin.getCurrentDay();
        applyAllPerks(player);
        updateScoreboard(player);
        player.sendMessage(ChatColor.GOLD + "[ZM] Nivel: " + prog.level + " | XP: " + prog.xp + "/" + prog.xpToNextLevel);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayersData();
    }

    private PlayerProgression loadPlayerData(UUID uuid) {
        PlayerProgression prog = new PlayerProgression();
        String path = uuid.toString();
        prog.level = playersConfig.getInt(path + ".level", 1);
        prog.xp = playersConfig.getDouble(path + ".xp", 0);
        prog.xpToNextLevel = calculateXpForLevel(prog.level);
        List<String> perksList = playersConfig.getStringList(path + ".perks");
        prog.perks = new HashSet<>(perksList);
        prog.totalKills = playersConfig.getInt(path + ".totalKills", 0);
        prog.zombieKills = playersConfig.getInt(path + ".zombieKills", 0);
        prog.survivalDays = playersConfig.getInt(path + ".survivalDays", 0);
        prog.lastLoginDay = playersConfig.getInt(path + ".lastLoginDay", 0);
        return prog;
    }

    public void onMythicMobKill(Player killer, String mobType, int rawXp) {
        UUID uuid = killer.getUniqueId();
        PlayerProgression prog = playersData.computeIfAbsent(uuid, k -> new PlayerProgression());

        int bonusXp = 0;
        if (killer.hasPermission("zonamuerta.survivor_passive")) {
            bonusXp = (int) (rawXp * 0.10);
        }

        int totalXp = rawXp + bonusXp;
        prog.xp += totalXp;
        prog.zombieKills++;
        prog.totalKills++;

        killer.sendMessage(ChatColor.GOLD + "+" + totalXp + " XP" + (bonusXp > 0 ? ChatColor.GRAY + " (10% survivor bonus)" : ""));

        checkLevelUp(killer, prog);
        updateScoreboard(killer);
        checkAchievements(killer, prog, mobType);
    }

    private void checkLevelUp(Player player, PlayerProgression prog) {
        while (prog.xp >= prog.xpToNextLevel && prog.level < maxLevel) {
            prog.xp -= prog.xpToNextLevel;
            prog.level++;
            prog.xpToNextLevel = calculateXpForLevel(prog.level);
            player.sendMessage(ChatColor.GREEN + "====================");
            player.sendMessage(ChatColor.GOLD + "  SUBISTE DE NIVEL!");
            player.sendMessage(ChatColor.YELLOW + "  Nuevo nivel: " + ChatColor.WHITE + prog.level);
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            player.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50);

            List<Perk> newPerks = getPerksForLevel(prog.level);
            for (Perk perk : newPerks) {
                if (!prog.perks.contains(perk.id)) {
                    prog.perks.add(perk.id);
                    perk.onApply.accept(player);
                    player.sendMessage(ChatColor.DARK_PURPLE + "  [PERK] " + ChatColor.LIGHT_PURPLE + perk.id + ChatColor.GRAY + " - " + perk.description);
                }
            }

            if (prog.level == 50) {
                player.sendMessage(ChatColor.DARK_RED + "  Puedes especializarte! Usa /zm spec");
            }
        }
    }

    private int calculateXpForLevel(int level) {
        return (int) (level * baseXpPerLevel);
    }

    private List<Perk> getPerksForLevel(int level) {
        List<Perk> result = new ArrayList<>();
        for (Map.Entry<Integer, Perk> entry : perksByLevel.entrySet()) {
            if (entry.getKey() <= level) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    private void applyAllPerks(Player player) {
        PlayerProgression prog = playersData.get(player.getUniqueId());
        if (prog == null) return;
        for (String perkId : prog.perks) {
            Perk perk = getPerkById(perkId);
            if (perk != null) {
                perk.onApply.accept(player);
            }
        }
        int heartsFromPerks = 0;
        if (prog.perks.contains("survivor_1")) heartsFromPerks += 2;
        if (prog.perks.contains("survivor_2")) heartsFromPerks += 2;
        if (prog.perks.contains("survivor_3")) heartsFromPerks += 2;
        if (prog.perks.contains("apocalypse_ready")) heartsFromPerks += 5;
        if (prog.perks.contains("末日求生者")) heartsFromPerks += 10;
        if (heartsFromPerks > 0) {
            double currentMax = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
            double targetMax = 20.0 + (heartsFromPerks * 2.0);
            if (currentMax < targetMax) {
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(targetMax);
            }
        }
    }

    private Perk getPerkById(String id) {
        for (Perk p : perksByLevel.values()) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    private void checkAchievements(Player player, PlayerProgression prog, String mobType) {
        if (prog.totalKills == 1) {
            player.sendMessage(ChatColor.GREEN + "[LOGRO] Primer zombie! +50 XP");
            prog.xp += 50;
        }
        if (prog.totalKills == 100) {
            player.sendMessage(ChatColor.GOLD + "[LOGRO] Cazador de 100! Machete mejorado desbloqueado.");
            player.sendMessage(ChatColor.GRAY + "Usa /zm weapons para ver el machete mejorado.");
        }
        if ("ZombiMutante".equals(mobType)) {
            player.sendMessage(ChatColor.DARK_PURPLE + "[LOGRO] Mataste un Mutante! +200 XP");
            prog.xp += 200;
        }
        if ("ElPatron".equals(mobType)) {
            player.sendMessage(ChatColor.DARK_RED + "[LOGRO] EL PATRON CAIDO! ArmaLegendaria + 2000 ZP");
            prog.xp += 2000;
            plugin.getEconomyManager().depositPlayer(player, 2000);
            player.getInventory().addItem(plugin.getWeaponsManager().createMacheteEnriched());
        }
        int dayDiff = plugin.getCurrentDay() - prog.lastLoginDay;
        if (dayDiff >= 30) {
            player.sendMessage(ChatColor.GREEN + "[LOGRO] 30 dias sobrevivido! KIT MEDICO奖励.");
            plugin.getClassManager().giveKit(player, "MEDICAL");
        }
    }

    public void updateScoreboard(Player player) {
        PlayerProgression prog = playersData.get(player.getUniqueId());
        if (prog == null) return;

        org.bukkit.scoreboard.Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        org.bukkit.scoreboard.Team team = board.getTeam("zm_" + player.getUniqueId().toString().substring(0, 8));
        if (team == null) {
            team = board.registerNewTeam("zm_" + player.getUniqueId().toString().substring(0, 8));
        }

player.sendMessage(
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "\u2620 ZONA MUERTA \u2620\n" +
            ChatColor.GRAY + "Dia: " + ChatColor.WHITE + plugin.getCurrentDay() + "\n" +
            ChatColor.GRAY + "Nivel: " + ChatColor.GOLD + prog.level + ChatColor.GRAY + " (" + prog.xp + "/" + prog.xpToNextLevel + " XP)\n" +
            ChatColor.GRAY + "Kills: " + ChatColor.WHITE + prog.zombieKills + "\n" +
            ChatColor.GRAY + "Corazones: " + ChatColor.RED + String.format("%.1f", player.getHealth()) + "/" + String.format("%.1f", player.getAttribute(Attribute.MAX_HEALTH).getBaseValue()) + "\n" +
            ChatColor.GRAY + "Perks: " + ChatColor.DARK_PURPLE + prog.perks.size()
        );
    }

    public void triggerBerserkerEffect(Player player) {
        PlayerProgression prog = playersData.get(player.getUniqueId());
        if (prog == null || !prog.perks.contains("berserker")) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
        player.sendMessage(ChatColor.RED + "[BERSERKER] Modo berserker activado!");
    }

    public double getDamageBonusAgainstZombies(Player player) {
        if (player.hasMetadata("zm_damage_bonus_zombies")) {
            return player.getMetadata("zm_damage_bonus_zombies").get(0).asDouble();
        }
        return 0.0;
    }

    public double getDropChanceBonus(Player player) {
        if (player.hasMetadata("zm_drop_bonus")) {
            return player.getMetadata("zm_drop_bonus").get(0).asDouble();
        }
        return 0.0;
    }

    public double getCureBonus(Player player) {
        if (player.hasMetadata("zm_cure_bonus")) {
            return player.getMetadata("zm_cure_bonus").get(0).asDouble();
        }
        return 0.0;
    }

    public boolean hasCritChance(Player player) {
        return player.hasMetadata("zm_crit_chance");
    }

    public int getPlayerLevel(Player player) {
        PlayerProgression prog = playersData.get(player.getUniqueId());
        return prog != null ? prog.level : 1;
    }

    public PlayerProgression getPlayerProgression(Player player) {
        return playersData.get(player.getUniqueId());
    }

    public ProgressionResult getPlayerLevelInfo(Player player) {
        PlayerProgression prog = playersData.get(player.getUniqueId());
        if (prog == null) {
            return new ProgressionResult(1, 0, 10, "Ninguno");
        }
        String perksStr = prog.perks.isEmpty() ? "Ninguno" : String.join(", ", prog.perks);
        return new ProgressionResult(prog.level, (int)prog.xp, prog.xpToNextLevel, perksStr);
    }

    public void shutdown() {
        savePlayersData();
    }

    public class ProgressionResult {
        public int level;
        public int xp;
        public int xpToNext;
        public String perks;

        public ProgressionResult(int level, int xp, int xpToNext, String perks) {
            this.level = level;
            this.xp = xp;
            this.xpToNext = xpToNext;
            this.perks = perks;
        }
    }

    public class PlayerProgression {
        public int level = 1;
        public double xp = 0;
        public int xpToNextLevel = 10;
        public Set<String> perks = new HashSet<>();
        public int totalKills = 0;
        public int zombieKills = 0;
        public int survivalDays = 0;
        public int lastLoginDay = 0;
    }

    public class Perk {
        public String id;
        public String description;
        public java.util.function.Consumer<Player> onApply;
        public java.util.function.Consumer<Player> onTick;

        public Perk(String id, String description, java.util.function.Consumer<Player> onApply, java.util.function.Consumer<Player> onTick) {
            this.id = id;
            this.description = description;
            this.onApply = onApply;
            this.onTick = onTick;
        }
    }
}