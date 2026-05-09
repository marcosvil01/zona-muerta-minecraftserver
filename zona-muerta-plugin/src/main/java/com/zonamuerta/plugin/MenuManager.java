package com.zonamuerta.plugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

public class MenuManager {

    private final ZonaMuerta plugin;
    private final Map<UUID, Menu> openMenus = new HashMap<>();
    private final Map<String, Menu> registeredMenus = new HashMap<>();

    public MenuManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new MenuListener(), plugin);
    }

    public void registerMenu(String id, Menu menu) {
        registeredMenus.put(id.toLowerCase(), menu);
    }

    public Menu getMenu(String id) {
        return registeredMenus.get(id.toLowerCase());
    }

    public void openMenu(Player player, String menuId) {
        Menu menu = registeredMenus.get(menuId.toLowerCase());
        if (menu == null) {
            player.sendMessage(ChatColor.RED + "Menu no encontrado: " + menuId);
            return;
        }
        openMenu(player, menu);
    }

    public void openMenu(Player player, Menu menu) {
        menu.open(player);
        openMenus.put(player.getUniqueId(), menu);
    }

    public void openMainMenu(Player player) {
        Menu menu = registeredMenus.get("main");
        if (menu == null) {
            menu = createMainMenu();
            registerMenu("main", menu);
        }
        openMenu(player, menu);
    }

    private Menu createMainMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.DARK_RED + "" + ChatColor.BOLD + "☠ ZONA MUERTA ☠");

        menu.setItem(0, createMenuItem(Material.NETHER_STAR, ChatColor.GOLD + "Progresion"),
            (p) -> openMenu(p, "progression"));

        menu.setItem(1, createMenuItem(Material.PLAYER_HEAD, ChatColor.GREEN + "Clase"),
            (p) -> openMenu(p, "class"));

        menu.setItem(2, createMenuItem(Material.GOLD_INGOT, ChatColor.YELLOW + "Economia"),
            (p) -> openMenu(p, "economy"));

        menu.setItem(3, createMenuItem(Material.BOOK, ChatColor.AQUA + "Quests"),
            (p) -> openMenu(p, "quests"));

        menu.setItem(4, createMenuItem(Material.CHEST, ChatColor.GOLD + "Subastas"),
            (p) -> plugin.getAuctionHouse().openAuctionHouse(p));

        menu.setItem(5, createMenuItem(Material.DIAMOND_SWORD, ChatColor.RED + "Estadisticas"),
            (p) -> plugin.getStatsManager().showStats(p));

        menu.setItem(6, createMenuItem(Material.EXPERIENCE_BOTTLE, ChatColor.LIGHT_PURPLE + "Kits"),
            (p) -> openMenu(p, "kits"));

        menu.setItem(7, createMenuItem(Material.COMPASS, ChatColor.YELLOW + "Teleport"),
            (p) -> openMenu(p, "teleport"));

        menu.setItem(8, createMenuItem(Material.BARRIER, ChatColor.DARK_GRAY + "Cerrar"),
            (p) -> p.closeInventory());

        return menu;
    }

    private Menu createProgressionMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.GOLD + "Progresion");

        ProgressionManager.ProgressionResult result = plugin.getProgressionManager().getPlayerLevelInfo(null);
        int level = result != null ? result.level : 1;

        menu.setItem(11, createMenuItem(Material.EMERALD, ChatColor.GREEN + "Tu Nivel",
            Arrays.asList(
                ChatColor.GRAY + "Nivel: " + ChatColor.WHITE + level,
                ChatColor.GRAY + "XP: " + ChatColor.WHITE + result.xp + "/" + result.xpToNext,
                "",
                ChatColor.DARK_GRAY + "Perks desbloqueados:"
            )), null);

        menu.setItem(13, createMenuItem(Material.GOLDEN_APPLE, ChatColor.YELLOW + "Estadisticas",
            Arrays.asList(
                ChatColor.GRAY + "Zombies eliminados: " + ChatColor.WHITE + "N/A",
                ChatColor.GRAY + "Dias sobrevivido: " + ChatColor.WHITE + plugin.getCurrentDay()
            )), null);

        menu.setItem(15, createMenuItem(Material.DIAMOND, ChatColor.AQUA + "Logros",
            Arrays.asList(
                ChatColor.GRAY + "Ver logros y titulos"
            )), (p) -> openMenu(p, "achievements"));

menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private Menu createClassMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.GREEN + "Seleccion de Clase");

        ClassManager.PlayerClass pClass = plugin.getClassManager().getPlayerClass(null);
        String className = pClass != null ? pClass.getDisplayName() : "Ninguna";

        menu.setItem(4, createMenuItem(Material.PLAYER_HEAD, ChatColor.GOLD + "Tu Clase: " + ChatColor.WHITE + className,
            Arrays.asList(
                ChatColor.GRAY + "Descripcion: " + ChatColor.WHITE + (pClass != null ? pClass.getDescription() : "N/A")
            )), null);

        int slot = 10;
        for (ClassManager.PlayerClass cls : ClassManager.PlayerClass.values()) {
            String desc = cls.getDescription();
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + desc);
            lore.add("");
            lore.add(ChatColor.DARK_GREEN + "Click para seleccionar");

            Material icon = switch (cls) {
                case SURVIVOR -> Material.IRON_SWORD;
                case MEDIC -> Material.POTION;
                case SOLDIER -> Material.IRON_CHESTPLATE;
                case SCAVENGER -> Material.CHEST;
                case ENGINEER -> Material.IRON_INGOT;
            };

            final ClassManager.PlayerClass finalCls = cls;
            menu.setItem(slot, createMenuItem(icon, ChatColor.WHITE + cls.getDisplayName(), lore),
                (p) -> {
                    if (!plugin.getClassManager().hasClass(p)) {
                        plugin.getClassManager().selectClass(p, finalCls);
                    } else {
                        plugin.getClassManager().changeClass(p, finalCls);
                    }
                    p.closeInventory();
                });
            slot++;
            if (slot == 12) slot = 14;
            if (slot == 16) slot = 19;
        }

        menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private Menu createEconomyMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.YELLOW + "Economia");

        menu.setItem(2, createMenuItem(Material.GOLD_INGOT, ChatColor.GOLD + "Mercader",
            Arrays.asList(ChatColor.GRAY + "Comida y materiales basicos")),
            (p) -> plugin.getEconomyManager().openTraderMenu(p, "mercader"));

        menu.setItem(4, createMenuItem(Material.IRON_SWORD, ChatColor.DARK_GRAY + "Arsenalelo",
            Arrays.asList(ChatColor.GRAY + "Armas y armaduras")),
            (p) -> plugin.getEconomyManager().openTraderMenu(p, "arsenalero"));

        menu.setItem(6, createMenuItem(Material.POTION, ChatColor.DARK_RED + "Medico",
            Arrays.asList(ChatColor.GRAY + "Pociones y curas")),
            (p) -> plugin.getEconomyManager().openTraderMenu(p, "medico"));

        menu.setItem(11, createMenuItem(Material.EMERALD, ChatColor.GREEN + "Vender Items",
            Arrays.asList(ChatColor.GRAY + "Vende loot por ZP")),
            (p) -> plugin.getEconomyManager().openSellMenu(p));

        menu.setItem(15, createMenuItem(Material.CHEST, ChatColor.GOLD + "Subastas",
            Arrays.asList(ChatColor.GRAY + "Compra y vende items")),
            (p) -> plugin.getAuctionHouse().openAuctionHouse(p));

        menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private Menu createQuestsMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.AQUA + "Quests");

        menu.setItem(2, createMenuItem(Material.BOOK, ChatColor.YELLOW + "Quests Diarias",
            Arrays.asList(ChatColor.GRAY + "4 quests diarias disponibles")),
            (p) -> openMenu(p, "daily_quests"));

        menu.setItem(4, createMenuItem(Material.MAP, ChatColor.DARK_PURPLE + "Quests Semanales",
            Arrays.asList(ChatColor.GRAY + "Quests desafiantes semana")),
            (p) -> openMenu(p, "weekly_quests"));

        menu.setItem(6, createMenuItem(Material.NETHER_STAR, ChatColor.RED + "Quests Principales",
            Arrays.asList(ChatColor.GRAY + "Historia del apocalipsis")),
            (p) -> openMenu(p, "main_quests"));

        menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private Menu createKitsMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.LIGHT_PURPLE + "Kits Disponibles");

        menu.setItem(2, createMenuItem(Material.DIAMOND_SWORD, ChatColor.GOLD + "STARKIT",
            Arrays.asList(ChatColor.GRAY + "500 ZP", ChatColor.DARK_GRAY + "Diamond Sword, Armor, Gapples")),
            (p) -> plugin.getClassManager().giveKit(p, "STARKIT"));

        menu.setItem(4, createMenuItem(Material.POTION, ChatColor.GREEN + "MEDICALKIT",
            Arrays.asList(ChatColor.GRAY + "300 ZP", ChatColor.DARK_GRAY + "Cure potions, Bandages")),
            (p) -> plugin.getClassManager().giveKit(p, "MEDICAL"));

        menu.setItem(6, createMenuItem(Material.COMPASS, ChatColor.YELLOW + "EXPLORERKIT",
            Arrays.asList(ChatColor.GRAY + "400 ZP", ChatColor.DARK_GRAY + "Exploration gear")),
            (p) -> plugin.getClassManager().giveKit(p, "EXPLORER"));

        menu.setItem(13, createMenuItem(Material.BOW, ChatColor.DARK_RED + "ZOMBIEHUNTERKIT",
            Arrays.asList(ChatColor.GRAY + "600 ZP", ChatColor.DARK_GRAY + "Combat gear")),
            (p) -> plugin.getClassManager().giveKit(p, "ZOMBIEHUNTER"));

        menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private Menu createTeleportMenu() {
        Menu menu = new Menu(plugin, 27, ChatColor.YELLOW + "Teleport");

        menu.setItem(2, createMenuItem(Material.ENDER_PEARL, ChatColor.GOLD + "Spawn",
            Arrays.asList(ChatColor.GRAY + "Teleport a spawn")),
            (p) -> p.teleport(p.getWorld().getSpawnLocation()));

        menu.setItem(4, createMenuItem(Material.HOPPER, ChatColor.GRAY + "Mercader",
            Arrays.asList(ChatColor.GRAY + "Posicion: -10, 64, 200")),
            (p) -> {});

        menu.setItem(6, createMenuItem(Material.HOPPER, ChatColor.GRAY + "Arsenalero",
            Arrays.asList(ChatColor.GRAY + "Posicion: -25, 64, 180")),
            (p) -> {});

        menu.setItem(13, createMenuItem(Material.RED_BED, ChatColor.DARK_RED + "Zonas Seguras",
            Arrays.asList(ChatColor.GRAY + "Tus zonas seguras")),
            (p) -> {});

        menu.setItem(22, createMenuItem(Material.ARROW, ChatColor.GREEN + "Volver"),
            (p) -> openMenu(p, "main"));

        return menu;
    }

    private ItemStack createMenuItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMenuItem(Material material, String name, String price) {
        List<String> lore = new ArrayList<>();
        if (price != null) {
            lore.add(price);
        }
        return createMenuItem(material, name, lore);
    }

    private ItemStack createMenuItem(Material material, String name) {
        return createMenuItem(material, name, (List<String>) null);
    }

    private ItemStack createMenuItem(Material material, String name, List<String> lore, Consumer<Player> action) {
        ItemStack item = createMenuItem(material, name, lore);
        return item;
    }

    private class MenuListener implements org.bukkit.event.Listener {
        @EventHandler(priority = EventPriority.HIGH)
        public void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();

            if (event.getView().getTitle().contains("ZONA MUERTA") ||
                event.getView().getTitle().contains("Progresion") ||
                event.getView().getTitle().contains("Clase") ||
                event.getView().getTitle().contains("Economia") ||
                event.getView().getTitle().contains("Quests") ||
                event.getView().getTitle().contains("Kits") ||
                event.getView().getTitle().contains("Teleport")) {

                event.setCancelled(true);

                int slot = event.getRawSlot();
                Menu menu = openMenus.get(player.getUniqueId());
                if (menu != null && menu.hasAction(slot)) {
                    menu.execute(slot, player);
                }
            }
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            openMenus.remove(event.getPlayer().getUniqueId());
        }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getPlayer().isSneaking() && event.getPlayer().getItemInHand() == null) {
                openMainMenu(event.getPlayer());
            }
        }
    }

    public static class Menu {
        private final ZonaMuerta plugin;
        private final int size;
        private final String title;
        private final Map<Integer, ItemStack> items = new HashMap<>();
        private final Map<Integer, Consumer<Player>> actions = new HashMap<>();

        public Menu(ZonaMuerta plugin, int size, String title) {
            this.plugin = plugin;
            this.size = size;
            this.title = title;
        }

        public void setItem(int slot, ItemStack item, Consumer<Player> action) {
            items.put(slot, item);
            if (action != null) {
                actions.put(slot, action);
            }
        }

        public void setItem(int slot, ItemStack item) {
            setItem(slot, item, null);
        }

        public boolean hasAction(int slot) {
            return actions.containsKey(slot);
        }

        public void execute(int slot, Player player) {
            Consumer<Player> action = actions.get(slot);
            if (action != null) {
                action.accept(player);
            }
        }

        public void open(Player player) {
            Inventory inv = Bukkit.createInventory(null, size, title);
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                inv.setItem(entry.getKey(), entry.getValue());
            }
            player.openInventory(inv);
        }
    }
}
