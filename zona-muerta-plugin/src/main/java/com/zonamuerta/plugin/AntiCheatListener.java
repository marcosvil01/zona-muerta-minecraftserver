package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AntiCheatListener implements Listener {

    private final ZonaMuerta plugin;
    private final Map<UUID, Long> playerLoginTime = new HashMap<>();
    private final Map<UUID, Integer> playerBlocksPlaced = new HashMap<>();
    private final Map<UUID, Integer> playerBlocksBroken = new HashMap<>();
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private final Map<UUID, Long> lastMoveTime = new HashMap<>();
    private final Map<UUID, List<Long>> damageLog = new HashMap<>();
    private final Map<UUID, Double> lastLocation = new HashMap<>();

    private static final int MAX_BLOCKS_PER_SECOND = 20;
    private static final int MAX_BLOCKS_BROKEN_PER_SECOND = 15;
    private static final long DAMAGE_LOG_INTERVAL = 60000L;
    private static final int MAX_DAMAGE_PER_MINUTE = 100;
    private static final double MAX_SPEED = 20.0;
    private static final int MAX_INVENTORY_CLICKS = 10;

    public AntiCheatListener(ZonaMuerta plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startMonitoringTask();
    }

    private void startMonitoringTask() {
        new BukkitRunnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void checkPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (!playerLoginTime.containsKey(uuid)) {
            playerLoginTime.put(uuid, System.currentTimeMillis());
        }

        long loginTime = playerLoginTime.get(uuid);
        long hoursSinceLogin = (System.currentTimeMillis() - loginTime) / 3600000;

        if (hoursSinceLogin > 48) {
            if (!player.hasPermission("zonamuerta.admin")) {
                player.sendMessage(ChatColor.YELLOW + "[ZM] Has estado AFK por mas de 48 horas. Seras expulsado por inactividad.");
                player.kickPlayer(ChatColor.GRAY + "AFK por mas de 48 horas.");
            }
        }

        playerBlocksPlaced.put(uuid, 0);
        playerBlocksBroken.put(uuid, 0);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("zonamuerta.admin")) return;

        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        int placed = playerBlocksPlaced.getOrDefault(uuid, 0) + 1;
        playerBlocksPlaced.put(uuid, placed);

        if (placed > MAX_BLOCKS_PER_SECOND) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Colocando bloques muy rapido. Espera.");
            flagPlayer(player, "FAST_PLACE", "Colocando >" + MAX_BLOCKS_PER_SECOND + " bloques/segundo");
        }

        Block block = event.getBlock();
        if (isIllegalBlock(block)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] No puedes colocar este bloque.");
            flagPlayer(player, "ILLEGAL_BLOCK", "Bloque ilegal: " + block.getType().name());
        }

        long hourOfDay = (System.currentTimeMillis() / 3600000) % 24;
        if (hourOfDay >= 6 && hourOfDay < 20) {
            if (block.getType() == Material.TNT || block.getType() == Material.LAVA || block.getType() == Material.MAGMA_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[AntiCheat] No puedes colocar explosivos durante el dia.");
                flagPlayer(player, "EXPLOSIVE_DAY", "Explosivos durante el dia");
            }
        }

        if (plugin.getSafezoneManager() != null && plugin.getSafezoneManager().isInSafezone(block.getLocation())) {
            if (!player.hasPermission("zonamuerta.safezone.build")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "[AntiCheat] No puedes construir en zona segura.");
                flagPlayer(player, "BUILD_SAFEZONE", "Construyendo en zona segura");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("zonamuerta.admin")) return;
        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        int broken = playerBlocksBroken.getOrDefault(uuid, 0) + 1;
        playerBlocksBroken.put(uuid, broken);

        if (broken > MAX_BLOCKS_BROKEN_PER_SECOND) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Rompiendo bloques muy rapido. Espera.");
            flagPlayer(player, "FAST_BREAK", "Rompiendo >" + MAX_BLOCKS_BROKEN_PER_SECOND + " bloques/segundo");
        }

        Block block = event.getBlock();
        if (isProtectedBlock(block)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Este bloque esta protegido.");
            flagPlayer(player, "BREAK_PROTECTED", "Bloque protegido: " + block.getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        if (player.hasPermission("zonamuerta.admin")) return;
        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        List<Long> log = damageLog.computeIfAbsent(uuid, k -> new ArrayList<>());
        log.removeIf(t -> now - t > DAMAGE_LOG_INTERVAL);
        log.add(now);

        if (log.size() > MAX_DAMAGE_PER_MINUTE) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Dano excesivo detectado. Espera un momento.");
            flagPlayer(player, "DAMAGE_HACK", "Dano excesivo por minuto");
        }

        lastDamageTime.put(uuid, now);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (player.hasPermission("zonamuerta.admin")) return;
        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        if (isIllegalItem(event.getCurrentItem())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Item no permitido detectado.");
            flagPlayer(player, "ILLEGAL_ITEM", "Item ilegal en inventario: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType().name() : "null"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("zonamuerta.admin")) return;
        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        ItemStack item = event.getItem();
        if (item != null && isIllegalItem(item)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "[AntiCheat] Uso de item no permitido.");
            flagPlayer(player, "USE_ILLEGAL_ITEM", "Item ilegal usado: " + item.getType().name());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.hasPermission("zonamuerta.admin")) return;
        if (plugin.getConfig().getBoolean("anticheat.enabled", true) == false) return;

        double distance = 0;
        if (lastLocation.containsKey(uuid)) {
            distance = player.getLocation().distance(new org.bukkit.Location(
                player.getWorld(),
                lastLocation.get(uuid),
                player.getLocation().getYaw(),
                player.getLocation().getPitch()
            ));
        }

        double speed = distance * 20;
        if (speed > MAX_SPEED) {
            player.sendMessage(ChatColor.RED + "[AntiCheat] Movimiento anormal detectado.");
            flagPlayer(player, "SPEED_HACK", "Velocidad anormal: " + String.format("%.2f", speed));
        }

        lastLocation.put(uuid, player.getLocation().getX());

        long now = System.currentTimeMillis();
        if (lastMoveTime.containsKey(uuid)) {
            long elapsed = now - lastMoveTime.get(uuid);
            if (elapsed < 50 && distance > 0.1) {
                player.sendMessage(ChatColor.RED + "[AntiCheat] Movimiento ilegal detectado.");
                flagPlayer(player, "NOCLIP_LIKE", "Noclip detectado");
            }
        }
        lastMoveTime.put(uuid, now);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playerBlocksPlaced.remove(uuid);
        playerBlocksBroken.remove(uuid);
        lastDamageTime.remove(uuid);
        lastMoveTime.remove(uuid);
        lastLocation.remove(uuid);
        damageLog.remove(uuid);
    }

    private boolean isIllegalBlock(Block block) {
        Material type = block.getType();
        return type == Material.BEDROCK ||
               type == Material.COMMAND_BLOCK ||
               type == Material.CHAIN_COMMAND_BLOCK ||
               type == Material.REPEATING_COMMAND_BLOCK ||
               type == Material.COMMAND_BLOCK_MINECART ||
               type == Material.BARRIER;
    }

    private boolean isProtectedBlock(Block block) {
        Material type = block.getType();
        return type == Material.END_PORTAL ||
               type == Material.END_PORTAL_FRAME ||
               type == Material.NETHER_PORTAL ||
               type == Material.BEDROCK;
    }

    private boolean isIllegalItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Material type = item.getType();
        if (type == Material.COMMAND_BLOCK ||
            type == Material.CHAIN_COMMAND_BLOCK ||
            type == Material.REPEATING_COMMAND_BLOCK ||
            type == Material.BARRIER ||
            type == Material.KNOWLEDGE_BOOK) {
            return true;
        }
        if (item.getItemMeta().hasEnchant(org.bukkit.enchantments.Enchantment.getByName("WORLD_EATER"))) {
            return true;
        }
        if (item.getAmount() > 127 || item.getAmount() < -127) {
            return true;
        }
        return false;
    }

    private void flagPlayer(Player player, String hackType, String details) {
        plugin.getLogger().warning("[AntiCheat] " + player.getName() + " | " + hackType + " | " + details);
        if (plugin.getConfig().getBoolean("anticheat.log_only", false) == false) {
            if (plugin.getConfig().getStringList("anticheat.autoban_hacks").contains(hackType)) {
                player.sendMessage(ChatColor.RED + "[AntiCheat] Has sido expulsado por hack: " + hackType);
                player.kickPlayer(ChatColor.RED + "Hack detectado: " + hackType);
            }
        }
    }

    public int getTotalBlocksPlaced(UUID uuid) {
        return playerBlocksPlaced.getOrDefault(uuid, 0);
    }

    public int getTotalBlocksBroken(UUID uuid) {
        return playerBlocksBroken.getOrDefault(uuid, 0);
    }

    public long getLastDamageTime(UUID uuid) {
        return lastDamageTime.getOrDefault(uuid, 0L);
    }

    public void resetPlayerData(UUID uuid) {
        playerBlocksPlaced.remove(uuid);
        playerBlocksBroken.remove(uuid);
        lastDamageTime.remove(uuid);
        lastMoveTime.remove(uuid);
        lastLocation.remove(uuid);
        damageLog.remove(uuid);
    }
}
