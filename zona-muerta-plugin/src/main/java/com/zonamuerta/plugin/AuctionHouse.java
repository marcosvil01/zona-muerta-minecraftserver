package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AuctionHouse implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, List<AuctionItem>> playerAuctions = new HashMap<>();
    private final Map<UUID, List<AuctionBid>> playerBids = new HashMap<>();
    private final Map<UUID, Long> auctionFees = new HashMap<>();
    private final List<AuctionItem> activeAuctions = new ArrayList<>();
    private File auctionsFile;
    private org.bukkit.configuration.file.FileConfiguration auctionsConfig;

    private static final int MAX_AUCTIONS_PER_PLAYER = 10;
    private static final int LISTING_FEE_PERCENT = 5;
    private static final long AUCTION_DURATION_TICKS = 1728000L;
    private static final String CURRENCY_NAME = "ZP";

    public AuctionHouse(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadAuctionsData();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadAuctionsData() {
        auctionsFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionsFile.exists()) {
            try {
                auctionsFile.getParentFile().mkdirs();
                auctionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create auctions.yml: " + e.getMessage());
            }
        }
        auctionsConfig = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(auctionsFile);
    }

    public void saveAuctionsData() {
        for (Map.Entry<UUID, List<AuctionItem>> entry : playerAuctions.entrySet()) {
            String path = entry.getKey().toString();
            List<Map<String, Object>> serialized = new ArrayList<>();
            for (AuctionItem item : entry.getValue()) {
                serialized.add(item.serialize());
            }
            auctionsConfig.set(path + ".auctions", serialized);
        }
        try {
            auctionsConfig.save(auctionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save auctions.yml: " + e.getMessage());
        }
    }

    public boolean createAuction(Player player, ItemStack item, int startingPrice, int buyoutPrice, int durationHours) {
        UUID uuid = player.getUniqueId();
        List<AuctionItem> auctions = playerAuctions.getOrDefault(uuid, new ArrayList<>());

        if (auctions.size() >= MAX_AUCTIONS_PER_PLAYER) {
            player.sendMessage(ChatColor.RED + "Limite de subastas alcanzado (" + MAX_AUCTIONS_PER_PLAYER + ").");
            return false;
        }

        double fee = Math.ceil(startingPrice * LISTING_FEE_PERCENT / 100.0);
        if (!plugin.getEconomyManager().withdraw(player, fee)) {
            player.sendMessage(ChatColor.RED + "No puedes pagar la cuota de entrada: " + (int)fee + " " + CURRENCY_NAME);
            return false;
        }

        auctionFees.put(uuid, auctionFees.getOrDefault(uuid, 0L) + (long)fee);

        AuctionItem auction = new AuctionItem(player.getUniqueId(), item.clone(), startingPrice, buyoutPrice, durationHours);
        auctions.add(auction);
        activeAuctions.add(auction);
        playerAuctions.put(uuid, auctions);

        player.sendMessage(ChatColor.GREEN + "Subasta creada: " + item.getType().name() + " x" + item.getAmount() + " | Precio inicial: " + startingPrice + " " + CURRENCY_NAME);
        player.sendMessage(ChatColor.GRAY + "Duracion: " + durationHours + "h | Cuota pagada: " + (int)fee + " " + CURRENCY_NAME);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);

        return true;
    }

    public boolean bid(Player player, int auctionIndex, int bidAmount) {
        if (auctionIndex < 0 || auctionIndex >= activeAuctions.size()) {
            player.sendMessage(ChatColor.RED + "Subasta no encontrada.");
            return false;
        }

        AuctionItem auction = activeAuctions.get(auctionIndex);

        if (auction.owner.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No puedes pujar en tu propia subasta.");
            return false;
        }

        int currentBid = auction.currentBid;
        if (auction.bids.isEmpty()) {
            currentBid = auction.startingPrice;
        }

        if (bidAmount <= currentBid) {
            player.sendMessage(ChatColor.RED + "La puja debe ser mayor a la puja actual: " + currentBid + " " + CURRENCY_NAME);
            return false;
        }

        if (bidAmount < auction.startingPrice) {
            player.sendMessage(ChatColor.RED + "La puja debe ser al menos el precio inicial: " + auction.startingPrice + " " + CURRENCY_NAME);
            return false;
        }

        if (!plugin.getEconomyManager().withdraw(player, bidAmount)) {
            player.sendMessage(ChatColor.RED + "No tienes suficiente " + CURRENCY_NAME + " para esta puja.");
            return false;
        }

        if (auction.currentBidder != null) {
            plugin.getEconomyManager().deposit(Bukkit.getPlayer(auction.currentBidder), currentBid);
        }

        auction.bids.add(new AuctionBid(player.getUniqueId(), bidAmount));
        auction.currentBid = bidAmount;
        auction.currentBidder = player.getUniqueId();

        List<AuctionBid> bids = playerBids.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        bids.add(new AuctionBid(player.getUniqueId(), bidAmount));

        player.sendMessage(ChatColor.GREEN + "Puja exitosa: " + bidAmount + " " + CURRENCY_NAME + " por " + auction.item.getType().name());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    public boolean buyout(Player player, int auctionIndex) {
        if (auctionIndex < 0 || auctionIndex >= activeAuctions.size()) {
            player.sendMessage(ChatColor.RED + "Subasta no encontrada.");
            return false;
        }

        AuctionItem auction = activeAuctions.get(auctionIndex);

        if (auction.buyoutPrice <= 0) {
            player.sendMessage(ChatColor.RED + "Esta subasta no tiene precio de compra inmediata.");
            return false;
        }

        if (auction.owner.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No puedes comprar tu propia subasta.");
            return false;
        }

        if (!plugin.getEconomyManager().withdraw(player, auction.buyoutPrice)) {
            player.sendMessage(ChatColor.RED + "No tienes suficiente " + CURRENCY_NAME + " para la compra inmediata.");
            return false;
        }

        Player owner = Bukkit.getPlayer(auction.owner);
        if (owner != null) {
            plugin.getEconomyManager().deposit(owner, auction.buyoutPrice);
            owner.sendMessage(ChatColor.GREEN + "Tu " + auction.item.getType().name() + " fue comprado por " + player.getName() + "!");
        }

        if (auction.currentBidder != null) {
            plugin.getEconomyManager().deposit(Bukkit.getPlayer(auction.currentBidder), auction.currentBid);
        }

        player.getInventory().addItem(auction.item.clone());
        player.sendMessage(ChatColor.GOLD + "Compra inmediata exitosa: " + auction.item.getType().name() + " x" + auction.item.getAmount());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        removeAuction(auctionIndex);
        return true;
    }

    public void openAuctionHouse(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Casa de Subastas");
        fillAuctionInventory(inv, player);
        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.2f);
    }

    private void fillAuctionInventory(Inventory inv, Player viewer) {
        inv.setItem(0, createMenuItem(Material.ARROW, ChatColor.GREEN + "Tuyas"));
        inv.setItem(1, createMenuItem(Material.NETHER_STAR, ChatColor.GOLD + "Crear Subasta"));
        inv.setItem(2, createMenuItem(Material.BOOK, ChatColor.YELLOW + "Ayuda"));

        for (int i = 3; i < 9; i++) {
            inv.setItem(i, createFillerItem(Material.PURPLE_STAINED_GLASS_PANE));
        }

        int slot = 9;
        for (int i = 0; i < activeAuctions.size() && slot < 54; i++) {
            AuctionItem auction = activeAuctions.get(i);
            ItemStack display = auction.item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Vendedor: " + ChatColor.WHITE + getOwnerName(auction.owner));
            lore.add(ChatColor.GRAY + "Puja actual: " + ChatColor.YELLOW + auction.currentBid + " " + CURRENCY_NAME);
            if (auction.buyoutPrice > 0) {
                lore.add(ChatColor.GRAY + "Compra inmediata: " + ChatColor.GOLD + auction.buyoutPrice + " " + CURRENCY_NAME);
            }
            long remaining = (auction.endTime - System.currentTimeMillis()) / 3600000;
            lore.add(ChatColor.GRAY + "Tiempo: " + ChatColor.WHITE + remaining + "h");
            lore.add(ChatColor.DARK_GRAY + "Slot: " + i);
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slot, display);
            slot++;
        }

        for (int i = slot; i < 54; i++) {
            if (i < 45 || i >= 54) {
                inv.setItem(i, createFillerItem(Material.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    public void openMyAuctions(Player player) {
        UUID uuid = player.getUniqueId();
        List<AuctionItem> auctions = playerAuctions.getOrDefault(uuid, new ArrayList<>());

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Mis Subastas");
        int slot = 0;
        for (AuctionItem auction : auctions) {
            if (slot >= 18) break;
            ItemStack display = auction.item.clone();
            ItemMeta meta = display.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Puja actual: " + ChatColor.YELLOW + auction.currentBid + " " + CURRENCY_NAME);
            lore.add(ChatColor.GRAY + "Pujas: " + ChatColor.WHITE + auction.bids.size());
            long remaining = (auction.endTime - System.currentTimeMillis()) / 3600000;
            lore.add(ChatColor.GRAY + "Tiempo: " + ChatColor.WHITE + remaining + "h");
            meta.setLore(lore);
            display.setItemMeta(meta);
            inv.setItem(slot + 9, display);
            slot++;
        }

        inv.setItem(0, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"));
        for (int i = 1; i < 9; i++) {
            inv.setItem(i, createFillerItem(Material.GREEN_STAINED_GLASS_PANE));
        }
        for (int i = slot + 9; i < 27; i++) {
            inv.setItem(i, createFillerItem(Material.GRAY_STAINED_GLASS_PANE));
        }

        player.openInventory(inv);
    }

    public void openCreateAuction(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Crear Subasta");
        inv.setItem(0, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"));
        inv.setItem(2, createMenuItem(Material.PAPER, ChatColor.YELLOW + "Posicion 1: Item"));
        inv.setItem(4, createMenuItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Posicion 2: Precio Inicial"));
        inv.setItem(6, createMenuItem(Material.DIAMOND, ChatColor.YELLOW + "Posicion 3: Compra Inmediata"));
        inv.setItem(8, createMenuItem(Material.CLOCK, ChatColor.YELLOW + "Posicion 4: Duracion (horas)"));

        for (int i = 9; i < 27; i++) {
            inv.setItem(i, createFillerItem(Material.GRAY_STAINED_GLASS_PANE));
        }
        player.openInventory(inv);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.contains("Casa de Subastas")) {
            event.setCancelled(true);
            int slot = event.getRawSlot();

            if (slot == 0) {
                openMyAuctions(player);
            } else if (slot == 1) {
                openCreateAuction(player);
            } else if (slot >= 9 && slot < 54) {
                int auctionIndex = slot - 9;
                if (auctionIndex < activeAuctions.size()) {
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Subasta #" + auctionIndex + ": " + activeAuctions.get(auctionIndex).item.getType().name());
                    player.sendMessage(ChatColor.GRAY + "Usa /ah bid " + auctionIndex + " <cantidad> para pujar.");
                    player.sendMessage(ChatColor.GRAY + "Usa /ah buyout " + auctionIndex + " para compra inmediata.");
                }
            }
        } else if (title.contains("Mis Subastas")) {
            event.setCancelled(true);
        } else if (title.contains("Crear Subasta")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getEconomyManager().giveStartingBalance(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveAuctionsData();
    }

    private void removeAuction(int index) {
        if (index < 0 || index >= activeAuctions.size()) return;
        AuctionItem auction = activeAuctions.remove(index);
        List<AuctionItem> ownerList = playerAuctions.get(auction.owner);
        if (ownerList != null) {
            ownerList.remove(auction);
        }
    }

    private String getOwnerName(UUID uuid) {
        Player owner = Bukkit.getPlayer(uuid);
        return owner != null ? owner.getName() : "Desconectado";
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

    public void onDisable() {
        saveAuctionsData();
    }

    public class AuctionItem {
        public UUID owner;
        public ItemStack item;
        public int startingPrice;
        public int buyoutPrice;
        public int currentBid;
        public UUID currentBidder;
        public List<AuctionBid> bids = new ArrayList<>();
        public long endTime;
        public int durationHours;

        public AuctionItem(UUID owner, ItemStack item, int startingPrice, int buyoutPrice, int durationHours) {
            this.owner = owner;
            this.item = item;
            this.startingPrice = startingPrice;
            this.buyoutPrice = buyoutPrice;
            this.currentBid = startingPrice;
            this.durationHours = durationHours;
            this.endTime = System.currentTimeMillis() + (durationHours * 3600000L);
        }

        public Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();
            map.put("owner", owner.toString());
            map.put("item", item);
            map.put("startingPrice", startingPrice);
            map.put("buyoutPrice", buyoutPrice);
            map.put("currentBid", currentBid);
            map.put("currentBidder", currentBidder != null ? currentBidder.toString() : null);
            map.put("endTime", endTime);
            map.put("durationHours", durationHours);
            return map;
        }
    }

    public class AuctionBid {
        public UUID bidder;
        public int amount;
        public long timestamp;

        public AuctionBid(UUID bidder, int amount) {
            this.bidder = bidder;
            this.amount = amount;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
