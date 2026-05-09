package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EconomyManager {

    private final ZonaMuerta plugin;
    private final Map<UUID, Double> playerBalances = new HashMap<>();
    private final Map<UUID, List<Transaction>> transactionHistory = new HashMap<>();
    private File balancesFile;
    private org.bukkit.configuration.file.FileConfiguration balancesConfig;

    private final Map<String, TraderItem> buyItems = new LinkedHashMap<>();
    private final Map<String, TraderItem> sellItems = new LinkedHashMap<>();

    private final String CURRENCY_NAME = "ZP";
    private final String CURRENCY_SYMBOL = "$";

    public EconomyManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadBalances();
        loadTraderItems();
    }

    private void loadBalances() {
        balancesFile = new File(plugin.getDataFolder(), "economy.yml");
        if (!balancesFile.exists()) {
            try {
                balancesFile.getParentFile().mkdirs();
                balancesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create economy.yml: " + e.getMessage());
            }
        }
        balancesConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(balancesFile);

        for (String key : balancesConfig.getKeys(false)) {
            if (balancesConfig.isDouble(key)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    playerBalances.put(uuid, balancesConfig.getDouble(key, 100.0));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveBalances() {
        for (Map.Entry<UUID, Double> entry : playerBalances.entrySet()) {
            balancesConfig.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            balancesConfig.save(balancesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save economy.yml: " + e.getMessage());
        }
    }

    private void loadTraderItems() {
        buyItems.put("BREAD", new TraderItem(new ItemStack(Material.BREAD), 5, "Mercader"));
        buyItems.put("TORCH", new TraderItem(new ItemStack(Material.TORCH, 16), 15, "Mercader"));
        buyItems.put("IRON_INGOT", new TraderItem(new ItemStack(Material.IRON_INGOT), 10, "Mercader"));
        buyItems.put("GOLD_INGOT", new TraderItem(new ItemStack(Material.GOLD_INGOT), 20, "Mercader"));
        buyItems.put("CHAINMAIL_HELMET", new TraderItem(new ItemStack(Material.CHAINMAIL_HELMET), 30, "Mercader"));
        buyItems.put("CHAINMAIL_CHESTPLATE", new TraderItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE), 50, "Mercader"));
        buyItems.put("CHAINMAIL_LEGGINGS", new TraderItem(new ItemStack(Material.CHAINMAIL_LEGGINGS), 45, "Mercader"));
        buyItems.put("CHAINMAIL_BOOTS", new TraderItem(new ItemStack(Material.CHAINMAIL_BOOTS), 25, "Mercader"));
        buyItems.put("IRON_SWORD", new TraderItem(new ItemStack(Material.IRON_SWORD), 30, "Arsenalero"));
        buyItems.put("IRON_CHESTPLATE", new TraderItem(new ItemStack(Material.IRON_CHESTPLATE), 60, "Arsenalero"));
        buyItems.put("SHIELD", new TraderItem(new ItemStack(Material.SHIELD), 40, "Arsenalero"));
        buyItems.put("BOW", new TraderItem(new ItemStack(Material.BOW), 35, "Arsenalero"));
        buyItems.put("CROSSBOW", new TraderItem(new ItemStack(Material.CROSSBOW), 80, "Arsenalero"));
        buyItems.put("ARROW", new TraderItem(new ItemStack(Material.ARROW, 16), 10, "Arsenalero"));
        buyItems.put("POTION_HEALING", new TraderItem(createPotion(org.bukkit.potion.PotionType.HEALING), 50, "Medico"));
        buyItems.put("POTION_REGEN", new TraderItem(createPotion(org.bukkit.potion.PotionType.REGENERATION), 50, "Medico"));
        buyItems.put("POTION_STRENGTH", new TraderItem(createPotion(org.bukkit.potion.PotionType.STRENGTH), 30, "Medico"));
        buyItems.put("GOLDEN_APPLE", new TraderItem(new ItemStack(Material.GOLDEN_APPLE), 100, "Medico"));
        buyItems.put("ENCHANTED_GOLDEN_APPLE", new TraderItem(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 300, "Medico"));
        buyItems.put("CURE_POTION", new TraderItem(createCurePotion(), 200, "Medico"));

        sellItems.put("ROTTEN_FLESH", new TraderItem(new ItemStack(Material.ROTTEN_FLESH), 2, "Mercader"));
        sellItems.put("BONE", new TraderItem(new ItemStack(Material.BONE), 3, "Mercader"));
        sellItems.put("IRON_NUGGET", new TraderItem(new ItemStack(Material.IRON_NUGGET), 1, "Mercader"));
        sellItems.put("GOLD_NUGGET", new TraderItem(new ItemStack(Material.GOLD_NUGGET), 2, "Mercader"));
        sellItems.put("STRING", new TraderItem(new ItemStack(Material.STRING), 2, "Mercader"));
        sellItems.put("GUNPOWDER", new TraderItem(new ItemStack(Material.GUNPOWDER), 5, "Mercader"));
        sellItems.put("SPIDER_EYE", new TraderItem(new ItemStack(Material.SPIDER_EYE), 4, "Mercader"));
        sellItems.put("BLAZE_POWDER", new TraderItem(new ItemStack(Material.BLAZE_POWDER), 10, "Arsenalero"));
        sellItems.put("FLINT", new TraderItem(new ItemStack(Material.FLINT), 2, "Arsenalero"));
        sellItems.put("COAL", new TraderItem(new ItemStack(Material.COAL), 5, "Mercader"));
        sellItems.put("EMERALD", new TraderItem(new ItemStack(Material.EMERALD), 50, "Mercader"));
        sellItems.put("DIAMOND", new TraderItem(new ItemStack(Material.DIAMOND), 150, "Arsenalero"));
        sellItems.put("GHAST_TEAR", new TraderItem(new ItemStack(Material.GHAST_TEAR), 25, "Medico"));
    }

    private ItemStack createPotion(org.bukkit.potion.PotionType potionType) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.PotionMeta potionMeta) {
            potionMeta.setBasePotionType(potionType);
            item.setItemMeta(potionMeta);
        }
        return item;
    }

    private ItemStack createCurePotion() {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Pocion de Cura");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Cura la infection completamente.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.UNBREAKING, 1);
        return item;
    }

    public double getBalance(Player player) {
        return playerBalances.getOrDefault(player.getUniqueId(), 100.0);
    }

    public void setBalance(Player player, double amount) {
        playerBalances.put(player.getUniqueId(), Math.max(0, amount));
    }

    public boolean withdraw(Player player, double amount) {
        double current = getBalance(player);
        if (current < amount) {
            player.sendMessage(ChatColor.RED + "No tienes suficiente " + CURRENCY_NAME + ". Balance: " + current + " | Necesitas: " + amount);
            return false;
        }
        playerBalances.put(player.getUniqueId(), current - amount);
        player.sendMessage(ChatColor.YELLOW + "-" + (int)amount + " " + CURRENCY_NAME + " | Balance: " + (int)getBalance(player));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f);
        logTransaction(player.getUniqueId(), "WITHDRAW", amount, "General");
        return true;
    }

    public boolean deposit(Player player, double amount) {
        double current = getBalance(player);
        playerBalances.put(player.getUniqueId(), current + amount);
        player.sendMessage(ChatColor.GREEN + "+" + (int)amount + " " + CURRENCY_NAME + " | Balance: " + (int)getBalance(player));
        logTransaction(player.getUniqueId(), "DEPOSIT", amount, "General");
        return true;
    }

    public boolean depositPlayer(Player player, double amount) {
        return deposit(player, amount);
    }

    public boolean transfer(Player from, Player to, double amount) {
        if (!withdraw(from, amount)) return false;
        deposit(to, amount);
        from.sendMessage(ChatColor.YELLOW + "Transferiste " + (int)amount + " " + CURRENCY_NAME + " a " + to.getName());
        to.sendMessage(ChatColor.GREEN + from.getName() + " te envio " + (int)amount + " " + CURRENCY_NAME);
        return true;
    }

    public void giveStartingBalance(Player player) {
        if (!playerBalances.containsKey(player.getUniqueId())) {
            playerBalances.put(player.getUniqueId(), 100.0);
            player.sendMessage(ChatColor.GOLD + "[ZM] Bienvenido! +100 " + CURRENCY_NAME + " como regalo de bienvenida.");
        }
    }

    private void logTransaction(UUID uuid, String type, double amount, String category) {
        List<Transaction> history = transactionHistory.computeIfAbsent(uuid, k -> new ArrayList<>());
        history.add(new Transaction(type, amount, category, System.currentTimeMillis()));
        if (history.size() > 50) {
            history.remove(0);
        }
    }

    public void openTraderMenu(Player player, String traderType) {
        Inventory inv;
        String title;

        switch (traderType.toUpperCase()) {
            case "MERCADER":
                title = ChatColor.DARK_GREEN + "Mercader - Comprar";
                inv = Bukkit.createInventory(null, 27, title);
                fillTraderInventory(inv, "MERCADER", true);
                break;
            case "ARSENALERO":
                title = ChatColor.DARK_GRAY + "Arsenalero - Comprar";
                inv = Bukkit.createInventory(null, 27, title);
                fillTraderInventory(inv, "ARSENALERO", true);
                break;
            case "MEDICO":
                title = ChatColor.DARK_RED + "Medico - Comprar";
                inv = Bukkit.createInventory(null, 27, title);
                fillTraderInventory(inv, "MEDICO", true);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Trader no encontrado: " + traderType);
                return;
        }

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
    }

    public void openSellMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Vender Items");
        fillSellInventory(inv);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.2f);
    }

    private void fillTraderInventory(Inventory inv, String trader, boolean isBuy) {
        inv.setItem(0, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver al menu"));
        int slot = 9;
        for (Map.Entry<String, TraderItem> entry : (isBuy ? buyItems : sellItems).entrySet()) {
            TraderItem ti = entry.getValue();
            if (!ti.trader.equals(trader)) continue;
            if (slot >= 26) break;

            ItemStack item = ti.item.clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Precio: " + ChatColor.YELLOW + (int)ti.price + " " + CURRENCY_NAME);
            if (isBuy) {
                lore.add(ChatColor.GRAY + "Click para comprar");
            } else {
                lore.add(ChatColor.GRAY + "Click para vender");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }

        for (int i = slot; i < 27; i++) {
            if (i < 9 || i >= 18) {
                inv.setItem(i, createFillerItem(Material.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    private void fillSellInventory(Inventory inv) {
        inv.setItem(0, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"));
        int slot = 9;
        for (Map.Entry<String, TraderItem> entry : sellItems.entrySet()) {
            if (slot >= 26) break;
            TraderItem ti = entry.getValue();
            ItemStack item = ti.item.clone();
            ItemMeta meta = item.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Te pagan: " + ChatColor.YELLOW + (int)ti.price + " " + CURRENCY_NAME);
            lore.add(ChatColor.GRAY + "Click para vender");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot++;
        }
    }

    private ItemStack createMenuItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createFillerItem(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    public boolean buyItem(Player player, ItemStack item, int price) {
        if (price > getBalance(player)) {
            player.sendMessage(ChatColor.RED + "No tienes suficiente " + CURRENCY_NAME + "!");
            player.sendMessage(ChatColor.GRAY + "Precio: " + price + " | Tu balance: " + (int)getBalance(player));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Inventario lleno!");
            return false;
        }

        withdraw(player, price);
        player.getInventory().addItem(item);
        player.sendMessage(ChatColor.GREEN + "Compraste: " + item.getType().name() + " x" + item.getAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.2f);
        logTransaction(player.getUniqueId(), "BUY", price, item.getType().name());
        return true;
    }

    public boolean sellItem(Player player, ItemStack item, int price) {
        if (!player.getInventory().containsAtLeast(item, item.getAmount())) {
            player.sendMessage(ChatColor.RED + "No tienes ese item!");
            return false;
        }

        player.getInventory().removeItem(item);
        deposit(player, price);
        player.sendMessage(ChatColor.GREEN + "Vendiste: " + item.getType().name() + " x" + item.getAmount() + " por " + price + " " + CURRENCY_NAME);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        logTransaction(player.getUniqueId(), "SELL", price, item.getType().name());
        return true;
    }

    public void showBalance(Player player) {
        double balance = getBalance(player);
        player.sendMessage(ChatColor.GOLD + "=========");
        player.sendMessage(ChatColor.YELLOW + "Balance: " + ChatColor.WHITE + (int)balance + " " + CURRENCY_NAME);
        player.sendMessage(ChatColor.GRAY + "Jugador: " + player.getName());
        player.sendMessage(ChatColor.GOLD + "=========");
    }

    public String formatBalance(double amount) {
        return (int)amount + " " + CURRENCY_NAME;
    }

    public void onPlayerJoin(Player player) {
        if (!playerBalances.containsKey(player.getUniqueId())) {
            playerBalances.put(player.getUniqueId(), 100.0);
        }
    }

    public void onDisable() {
        saveBalances();
    }

    public class TraderItem {
        public ItemStack item;
        public double price;
        public String trader;

        public TraderItem(ItemStack item, double price, String trader) {
            this.item = item;
            this.price = price;
            this.trader = trader;
        }
    }

    public class Transaction {
        public String type;
        public double amount;
        public String category;
        public long timestamp;

        public Transaction(String type, double amount, String category, long timestamp) {
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.timestamp = timestamp;
        }
    }
}