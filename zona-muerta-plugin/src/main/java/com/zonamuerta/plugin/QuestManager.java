package com.zonamuerta.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestManager implements Listener {

    public enum QuestType {
        KILL_MOBS,
        COLLECT_ITEMS,
        CURE_PLAYER,
        CLEAR_ZONE,
        EXPLORE_STRUCTURES,
        SURVIVE_DAYS,
        BUILD_STRUCTURE,
        KILL_BOSS
    }

    public enum QuestCategory {
        DAILY,
        WEEKLY,
        MAIN
    }

    private final ZonaMuerta plugin;
    private final Map<UUID, List<ActiveQuest>> playerActiveQuests = new HashMap<>();
    private final Map<UUID, List<String>> completedMainQuests = new HashMap<>();
    private final Map<String, Quest> questsById = new LinkedHashMap<>();
    private File questsDataFile;
    private org.bukkit.configuration.file.FileConfiguration questsDataConfig;

    public QuestManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadQuestsConfig();
        loadQuestsData();
    }

    private void loadQuestsConfig() {
        questsById.clear();
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        org.bukkit.configuration.ConfigurationSection dailySection = config.getConfigurationSection("quests.daily_quests");
        if (dailySection != null) {
            for (String questId : dailySection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection qs = dailySection.getConfigurationSection(questId);
                if (qs == null) continue;
                Quest quest = new Quest();
                quest.id = questId;
                quest.name = qs.getString("name", "&c" + questId);
                quest.description = qs.getString("description", "");
                quest.category = QuestCategory.DAILY;
                quest.type = QuestType.valueOf(qs.getString("type", "KILL_MOBS"));
                quest.target = qs.getInt("target", 10);
                quest.targetMobs = new HashSet<>(qs.getStringList("target_mobs"));
                quest.requiredItems = new HashSet<>(qs.getStringList("required_items"));
                quest.rewardXp = qs.getInt("rewards.xp", 0);
                quest.rewardZp = qs.getInt("rewards.zp", 0);
                quest.rewardItem = qs.getString("rewards.item", null);
                quest.radius = qs.getInt("radius", 20);
                quest.displayIcon = Material.valueOf(qs.getString("display_icon", "DIAMOND_SWORD"));
                quest.priority = qs.getInt("priority", 5);
                questsById.put(questId, quest);
            }
        }
        org.bukkit.configuration.ConfigurationSection weeklySection = config.getConfigurationSection("quests.weekly_quests");
        if (weeklySection != null) {
            for (String questId : weeklySection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection qs = weeklySection.getConfigurationSection(questId);
                if (qs == null) continue;
                Quest quest = new Quest();
                quest.id = questId;
                quest.name = qs.getString("name", "&c" + questId);
                quest.description = qs.getString("description", "");
                quest.category = QuestCategory.WEEKLY;
                quest.type = QuestType.valueOf(qs.getString("type", "KILL_MOBS"));
                quest.target = qs.getInt("target", 10);
                quest.targetMobs = new HashSet<>(qs.getStringList("target_mobs"));
                quest.rewardXp = qs.getInt("rewards.xp", 0);
                quest.rewardZp = qs.getInt("rewards.zp", 0);
                quest.rewardItem = qs.getString("rewards.item", null);
                quest.displayIcon = Material.valueOf(qs.getString("display_icon", "NETHERITE_SWORD"));
                quest.cooldownDays = qs.getInt("cooldown_days", 7);
                questsById.put(questId, quest);
            }
        }
        org.bukkit.configuration.ConfigurationSection mainSection = config.getConfigurationSection("quests.main_quests");
        if (mainSection != null) {
            for (String questId : mainSection.getKeys(false)) {
                org.bukkit.configuration.ConfigurationSection qs = mainSection.getConfigurationSection(questId);
                if (qs == null) continue;
                Quest quest = new Quest();
                quest.id = questId;
                quest.name = qs.getString("name", "&7" + questId);
                quest.description = qs.getString("description", "");
                quest.category = QuestCategory.MAIN;
                quest.type = QuestType.valueOf(qs.getString("type", "KILL_MOBS"));
                quest.target = qs.getInt("target", 1);
                quest.targetMobs = new HashSet<>(qs.getStringList("target_mobs"));
                quest.rewardXp = qs.getInt("rewards.xp", 0);
                quest.rewardZp = qs.getInt("rewards.zp", 0);
                quest.rewardItem = qs.getString("rewards.item", null);
                quest.displayIcon = Material.valueOf(qs.getString("display_icon", "PLAYER_HEAD"));
                questsById.put(questId, quest);
            }
        }
        plugin.getLogger().info("Loaded " + questsById.size() + " quests.");
    }

    public void loadQuestsData() {
        questsDataFile = new File(plugin.getDataFolder(), "quests_data.yml");
        if (!questsDataFile.exists()) {
            try {
                questsDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create quests_data.yml: " + e.getMessage());
            }
        }
        questsDataConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(questsDataFile);
        for (String uuidStr : questsDataConfig.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            List<String> completed = questsDataConfig.getStringList(uuidStr + ".completed_main");
            if (!completed.isEmpty()) {
                completedMainQuests.put(uuid, completed);
            }
            List<String> activeList = questsDataConfig.getStringList(uuidStr + ".active_quests");
            List<ActiveQuest> active = new ArrayList<>();
            for (String questId : activeList) {
                Quest q = questsById.get(questId);
                if (q != null) {
                    active.add(new ActiveQuest(q));
                }
            }
            if (!active.isEmpty()) {
                playerActiveQuests.put(uuid, active);
            }
        }
    }

    public void saveQuestsData() {
        for (Map.Entry<UUID, List<ActiveQuest>> entry : playerActiveQuests.entrySet()) {
            String uuidStr = entry.getKey().toString();
            List<String> activeIds = new ArrayList<>();
            for (ActiveQuest aq : entry.getValue()) {
                activeIds.add(aq.quest.id);
            }
            questsDataConfig.set(uuidStr + ".active_quests", activeIds);
        }
        for (Map.Entry<UUID, List<String>> entry : completedMainQuests.entrySet()) {
            String uuidStr = entry.getKey().toString();
            questsDataConfig.set(uuidStr + ".completed_main", entry.getValue());
        }
        try {
            questsDataConfig.save(questsDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save quests_data.yml: " + e.getMessage());
        }
    }

    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerActiveQuests.containsKey(uuid)) {
            List<ActiveQuest> dailyQuests = assignDailyQuests(player);
            playerActiveQuests.put(uuid, dailyQuests);
        }
        player.sendMessage(ChatColor.YELLOW + "=== MISIONES DIARIAS ===");
        List<ActiveQuest> active = playerActiveQuests.getOrDefault(uuid, Collections.emptyList());
        if (active.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No tienes misiones activas.");
        } else {
            for (ActiveQuest aq : active) {
                int progress = (int) (aq.progress * 100.0 / aq.quest.target);
                player.sendMessage(ChatColor.WHITE + aq.quest.name + ChatColor.GRAY + " - " + ChatColor.YELLOW + (int) aq.progress + "/" + aq.quest.target + " (" + progress + "%)");
            }
        }
    }

    private List<ActiveQuest> assignDailyQuests(Player player) {
        List<ActiveQuest> result = new ArrayList<>();
        List<Quest> dailyQuests = new ArrayList<>();
        for (Quest q : questsById.values()) {
            if (q.category == QuestCategory.DAILY) {
                dailyQuests.add(q);
            }
        }
        Collections.shuffle(dailyQuests);
        int count = Math.min(4, dailyQuests.size());
        for (int i = 0; i < count; i++) {
            result.add(new ActiveQuest(dailyQuests.get(i)));
        }
        return result;
    }

    public void onMobKill(Player killer, String mobType) {
        UUID uuid = killer.getUniqueId();
        List<ActiveQuest> activeList = playerActiveQuests.get(uuid);
        if (activeList == null) return;
        for (ActiveQuest aq : activeList) {
            if (aq.quest.type == QuestType.KILL_MOBS || aq.quest.type == QuestType.KILL_BOSS) {
                if (aq.quest.targetMobs.isEmpty() || aq.quest.targetMobs.contains(mobType)) {
                    aq.progress++;
                    if (aq.progress >= aq.quest.target) {
                        completeQuest(killer, aq);
                    }
                }
            }
        }
    }

    public void onPlayerCured(Player healer, Player infected) {
        UUID uuid = healer.getUniqueId();
        List<ActiveQuest> activeList = playerActiveQuests.get(uuid);
        if (activeList == null) return;
        for (ActiveQuest aq : activeList) {
            if (aq.quest.type == QuestType.CURE_PLAYER) {
                aq.progress++;
                if (aq.progress >= aq.quest.target) {
                    completeQuest(healer, aq);
                }
            }
        }
    }

    public void onStructureExplored(Player player, String structureType) {
        UUID uuid = player.getUniqueId();
        List<ActiveQuest> activeList = playerActiveQuests.get(uuid);
        if (activeList == null) return;
        for (ActiveQuest aq : activeList) {
            if (aq.quest.type == QuestType.EXPLORE_STRUCTURES) {
                aq.progress++;
                if (aq.progress >= aq.quest.target) {
                    completeQuest(player, aq);
                }
            }
        }
    }

    private void completeQuest(Player player, ActiveQuest aq) {
        player.sendMessage(ChatColor.GREEN + "===================");
        player.sendMessage(ChatColor.GOLD + "  MISION COMPLETADA!");
        player.sendMessage(ChatColor.WHITE + "  " + ChatColor.stripColor(aq.quest.name));
        if (aq.quest.rewardXp > 0) {
            plugin.getProgressionManager().onMythicMobKill(player, "QUEST_REWARD", aq.quest.rewardXp);
        }
        if (aq.quest.rewardZp > 0) {
            plugin.getEconomyManager().depositPlayer(player, aq.quest.rewardZp);
            player.sendMessage(ChatColor.YELLOW + "  +" + aq.quest.rewardZp + " ZP");
        }
        if (aq.quest.rewardItem != null) {
            ItemStack reward = parseRewardItem(aq.quest.rewardItem);
            if (reward != null) {
                player.getInventory().addItem(reward);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "  +" + reward.getType().name());
            }
        }
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        UUID uuid = player.getUniqueId();
        List<ActiveQuest> active = playerActiveQuests.get(uuid);
        if (active != null) {
            active.remove(aq);
        }
        if (aq.quest.category == QuestCategory.MAIN) {
            completedMainQuests.computeIfAbsent(uuid, k -> new ArrayList<>()).add(aq.quest.id);
        }
        saveQuestsData();
        player.sendMessage(ChatColor.GREEN + "===================");
    }

    private ItemStack parseRewardItem(String itemStr) {
        if (itemStr == null) return null;
        try {
            if (itemStr.contains(":")) {
                String[] parts = itemStr.split(":");
                Material mat = Material.getMaterial(parts[0]);
                int amount = Integer.parseInt(parts[1]);
                return new ItemStack(mat, amount);
            } else {
                Material mat = Material.getMaterial(itemStr);
                return new ItemStack(mat);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid reward item: " + itemStr);
            return null;
        }
    }

    public void showQuestMenu(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "=========================");
        player.sendMessage(ChatColor.YELLOW + "  MISIONES");
        UUID uuid = player.getUniqueId();
        List<ActiveQuest> active = playerActiveQuests.getOrDefault(uuid, Collections.emptyList());
        int i = 1;
        for (ActiveQuest aq : active) {
            int progress = (int) (aq.progress * 100.0 / aq.quest.target);
            player.sendMessage(String.valueOf(ChatColor.WHITE) + i++ + ". " + aq.quest.name + ChatColor.GRAY + " [" + progress + "%]");
            player.sendMessage(ChatColor.GRAY + "   " + aq.quest.description);
            player.sendMessage(ChatColor.GRAY + "   Progreso: " + ChatColor.YELLOW + (int) aq.progress + "/" + aq.quest.target);
            player.sendMessage(ChatColor.GRAY + "   Recompensa: " + ChatColor.GOLD + aq.quest.rewardXp + " XP" + (aq.quest.rewardZp > 0 ? " | " + aq.quest.rewardZp + " ZP" : ""));
        }
        if (active.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "No hay misiones activas.");
        }
        player.sendMessage(ChatColor.DARK_GRAY + "=========================");
    }

    public void onDayChange() {
        for (UUID uuid : playerActiveQuests.keySet()) {
            List<ActiveQuest> active = playerActiveQuests.get(uuid);
            if (active == null || active.isEmpty()) continue;
            List<ActiveQuest> remaining = new ArrayList<>();
            for (ActiveQuest aq : active) {
                if (aq.quest.category == QuestCategory.WEEKLY) {
                    remaining.add(aq);
                }
            }
            Player player = plugin.getServer().getPlayer(uuid);
            if (remaining.isEmpty()) {
                List<ActiveQuest> newDaily = assignDailyQuests(player != null ? player : null);
                playerActiveQuests.put(uuid, newDaily);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "[ZM] Nuevas misiones diarias asignadas!");
                }
            } else {
                playerActiveQuests.put(uuid, remaining);
            }
        }
        saveQuestsData();
    }

    public void shutdown() {
        saveQuestsData();
    }

    public class Quest {
        public String id;
        public String name;
        public String description;
        public QuestCategory category;
        public QuestType type;
        public int target = 10;
        public Set<String> targetMobs = new HashSet<>();
        public Set<String> requiredItems = new HashSet<>();
        public int rewardXp = 0;
        public int rewardZp = 0;
        public String rewardItem;
        public int radius = 20;
        public Material displayIcon = Material.DIAMOND_SWORD;
        public int priority = 5;
        public int cooldownDays = 0;
    }

    public class ActiveQuest {
        public Quest quest;
        public double progress = 0;

        public ActiveQuest(Quest quest) {
            this.quest = quest;
        }
    }
}