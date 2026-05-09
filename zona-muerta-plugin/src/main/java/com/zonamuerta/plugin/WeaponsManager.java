package com.zonamuerta.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Sistema de armas artesanales de Zona Muerta.
 * Todas las armas usan CustomModelData para que el resource pack
 * las muestre con modelos personalizados.
 *
 * CMD asignados:
 *   1001 → zm_machete
 *   1002 → zm_cuchillo
 *   1003 → zm_hacha_oxidada
 *   1004 → zm_lanza_improvisada
 *   1005 → zm_escudo_chatarra
 *   1006 → zm_antorcha_improvisada
 *   1007 → zm_ballesta_artesanal (usa CROSSBOW)
 */
public class WeaponsManager {

    // ── CustomModelData IDs ───────────────────────────────────────────────────
    public static final int CMD_MACHETE           = 1001;
    public static final int CMD_CUCHILLO          = 1002;
    public static final int CMD_HACHA_OXIDADA     = 1003;
    public static final int CMD_LANZA             = 1004;
    public static final int CMD_ESCUDO_CHATARRA   = 1005;
    public static final int CMD_ANTORCHA          = 1006;
    public static final int CMD_BALLESTA          = 1007;

    private final ZonaMuerta plugin;
    private final NamespacedKey weaponKey;

    public WeaponsManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        this.weaponKey = new NamespacedKey((Plugin) plugin, "zm_weapon");
    }

    /** Registra todas las recetas artesanales en el servidor. */
    public void registerRecipes() {
        registerMachete();
        registerCuchillo();
        registerHachaOxidada();
        registerLanza();
        registerEscudoChatarra();
        registerAntorcha();
        registerBallesta();
        plugin.getLogger().info("[ZM-Weapons] 7 recetas de armas registradas.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MACHETE  (IRON_SWORD, CMD 1001)
    //  F = Flint  |  S = Stick  |  I = Iron Ingot
    //  Receta:    I
    //             F
    //             S
    // ─────────────────────────────────────────────────────────────────────────
    private void registerMachete() {
        ItemStack item = createMachete();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_machete");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("I", "F", "S");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('F', Material.FLINT);
        recipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createMachete() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Machete Oxidado");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Forjado con chatarra de la ciudad.",
                ChatColor.DARK_GRAY + "No es bonito, pero corta.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+5 ♦",
                ChatColor.YELLOW + "Velocidad: " + ChatColor.WHITE + "Normal"
        ));
        meta.setCustomModelData(CMD_MACHETE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "machete");
        item.setItemMeta(meta);
        // Atributos de daño
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 2);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CUCHILLO DE SUPERVIVENCIA (IRON_SWORD, CMD 1002) — arma rápida
    //  Receta (shapeless): flint + stick
    // ─────────────────────────────────────────────────────────────────────────
    private void registerCuchillo() {
        ItemStack item = createCuchillo();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_cuchillo");
        ShapelessRecipe recipe = new ShapelessRecipe(key, item);
        recipe.addIngredient(Material.FLINT);
        recipe.addIngredient(Material.FLINT);
        recipe.addIngredient(Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createCuchillo() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Cuchillo de Supervivencia");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Pequeño pero efectivo.",
                ChatColor.DARK_GRAY + "Ideal para ataques sigilosos.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+3 ♦",
                ChatColor.YELLOW + "Velocidad: " + ChatColor.GREEN + "Rápido"
        ));
        meta.setCustomModelData(CMD_CUCHILLO);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "cuchillo");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HACHA OXIDADA (IRON_AXE, CMD 1003) — daño alto, lenta
    //  I I
    //  I S
    //    S
    // ─────────────────────────────────────────────────────────────────────────
    private void registerHachaOxidada() {
        ItemStack item = createHachaOxidada();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_hacha_oxidada");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("II", "IS", " S");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createHachaOxidada() {
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Hacha de la Horda");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Pesada. Brutal. Sin piedad.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+8 ♦",
                ChatColor.YELLOW + "Velocidad: " + ChatColor.RED + "Lenta",
                ChatColor.GOLD + "Bonus: " + ChatColor.WHITE + "Desactiva escudos"
        ));
        meta.setCustomModelData(CMD_HACHA_OXIDADA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "hacha_oxidada");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 2);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LANZA IMPROVISADA (TRIDENT → modelo, IRON_SWORD base, CMD 1004)
    //  Alcance extra, se usa como STONE_SWORD en stats pero visual distintivo
    //   I
    //  IS
    //  S
    // ─────────────────────────────────────────────────────────────────────────
    private void registerLanza() {
        ItemStack item = createLanzaImprovisada();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_lanza");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape(" I", "IS", "S ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createLanzaImprovisada() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Lanza Improvisada");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Acero y madera. Simple y mortal.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+6 ♦",
                ChatColor.GOLD + "Bonus: " + ChatColor.WHITE + "Alcance extendido"
        ));
        meta.setCustomModelData(CMD_LANZA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "lanza");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        item.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ESCUDO DE CHATARRA (SHIELD, CMD 1005)
    //  I P I
    //  I I I
    //    I
    // ─────────────────────────────────────────────────────────────────────────
    private void registerEscudoChatarra() {
        ItemStack item = createEscudoChatarra();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_escudo");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("IPI", "III", " I ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('P', Material.OAK_PLANKS);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createEscudoChatarra() {
        ItemStack item = new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Escudo de Chatarra");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Planchas de hierro soldadas con barro.",
                ChatColor.DARK_GRAY + "No te protegerá de todo... pero algo es algo.",
                "",
                ChatColor.BLUE + "Defensa: " + ChatColor.WHITE + "Media"
        ));
        meta.setCustomModelData(CMD_ESCUDO_CHATARRA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "escudo_chatarra");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 2);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ANTORCHA IMPROVISADA (BLAZE_ROD, CMD 1006) — arma + luz
    //  B
    //  C
    //  S     (B=blaze rod, C=coal, S=stick)
    // ─────────────────────────────────────────────────────────────────────────
    private void registerAntorcha() {
        ItemStack item = createAntorchaImprovisada();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_antorcha");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("B", "C", "S");
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('C', Material.COAL);
        recipe.setIngredient('S', Material.STICK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createAntorchaImprovisada() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Antorcha Improvisada");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Arde. Ahuyenta. Quema.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+3 ♦ " + ChatColor.GOLD + "(fuego)",
                ChatColor.YELLOW + "Efecto: " + ChatColor.WHITE + "Prende fuego al objetivo"
        ));
        meta.setCustomModelData(CMD_ANTORCHA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "antorcha");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 2);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        return item;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BALLESTA ARTESANAL (CROSSBOW, CMD 1007)
    //  SIS
    //  S S   (I=Iron, S=String, T=Tripwire Hook)
    //  STS
    // ─────────────────────────────────────────────────────────────────────────
    private void registerBallesta() {
        ItemStack item = createBallestaArtesanal();
        NamespacedKey key = new NamespacedKey((Plugin) plugin, "zm_ballesta");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("SIS", "S S", "STS");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('T', Material.TRIPWIRE_HOOK);
        plugin.getServer().addRecipe(recipe);
    }

    public ItemStack createBallestaArtesanal() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_RED + "Ballesta Artesanal");
        meta.setLore(List.of(
                ChatColor.DARK_GRAY + "Madera, hierro y desesperación.",
                ChatColor.DARK_GRAY + "Para cuando necesitas distancia.",
                "",
                ChatColor.RED + "Daño: " + ChatColor.WHITE + "+8 ♦ (disparo)",
                ChatColor.GOLD + "Bonus: " + ChatColor.WHITE + "Multishot"
        ));
        meta.setCustomModelData(CMD_BALLESTA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "ballesta");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.MULTISHOT, 1);
        item.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 2);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        return item;
    }

    // ── Util ──────────────────────────────────────────────────────────────────

    public boolean isZMWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(weaponKey, PersistentDataType.STRING);
    }

    public String getZMWeaponType(ItemStack item) {
        if (!isZMWeapon(item)) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(weaponKey, PersistentDataType.STRING);
    }

    /** Da al jugador el kit básico de supervivencia. */
    public void giveStarterKit(Player player) {
        player.getInventory().addItem(createMachete());
        player.getInventory().addItem(createCuchillo());
        player.getInventory().addItem(createAntorchaImprovisada());
        player.sendMessage(ChatColor.GREEN + "✔ Kit de supervivencia Zona Muerta recibido.");
    }

    /** Machete mejorado para logros. Sharpness 4 + Fire Aspect 1. */
    public ItemStack createMacheteEnriched() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Machete del Cazador");
        meta.setLore(List.of(
            ChatColor.DARK_GRAY + "Forjado con los kills de tus enemigos.",
            ChatColor.DARK_GRAY + "Solo los verdaderos supervivientes lo lograron.",
            "",
            ChatColor.RED + "Daño: " + ChatColor.WHITE + "+7 ♦",
            ChatColor.YELLOW + "Velocidad: " + ChatColor.WHITE + "Normal",
            ChatColor.DARK_PURPLE + "Bonus: " + ChatColor.GRAY + "Fire Aspect I"
        ));
        meta.setCustomModelData(CMD_MACHETE);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(weaponKey, PersistentDataType.STRING, "machete_enriched");
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, 4);
        item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        item.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 1);
        return item;
    }
}
