package com.zonamuerta.plugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class ClassManager {

    public enum PlayerClass {
        SURVIVOR("Survivor", "Clase por defecto. +10% XP bonus.", "survivor_passive"),
        MEDIC("Medic", "Curacion mejorada. Tiene habilidad de primeros auxilios.", "medic_passive"),
        SOLDIER("Soldier", "+15% dano. Habilidad: Rallying Cry.", "soldier_passive"),
        SCAVENGER("Scavenger", "+25% drop chance. Habilidad: Sense Loot.", "scavenger_passive"),
        ENGINEER("Engineer", "Puede fortificar area. Habilidad: Fortify.", "engineer_passive");

        private final String displayName;
        private final String description;
        private final String permissionNode;

        PlayerClass(String displayName, String description, String permissionNode) {
            this.displayName = displayName;
            this.description = description;
            this.permissionNode = permissionNode;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getPermissionNode() { return permissionNode; }
    }

    private final ZonaMuerta plugin;
    private final Map<UUID, PlayerClass> playerClasses = new HashMap<>();
    private final Map<UUID, Long> skillCooldowns = new HashMap<>();
    private final Map<UUID, UUID> skillTargets = new HashMap<>();

    private final Map<PlayerClass, List<ItemStack>> startingKits = new HashMap<>();
    private final Map<PlayerClass, Long> skillCooldownsSeconds = new HashMap<>();

    public ClassManager(ZonaMuerta plugin) {
        this.plugin = plugin;
        loadKits();
    }

    private void loadKits() {
        startingKits.put(PlayerClass.SURVIVOR, Arrays.asList(
            createMachete(), new ItemStack(Material.TORCH, 3), new ItemStack(Material.BREAD)
        ));

        startingKits.put(PlayerClass.MEDIC, Arrays.asList(
            new ItemStack(Material.POTION, 2),
            new ItemStack(Material.BREAD),
            createBandage()
        ));

        startingKits.put(PlayerClass.SOLDIER, Arrays.asList(
            new ItemStack(Material.IRON_SWORD),
            new ItemStack(Material.CHAINMAIL_HELMET),
            new ItemStack(Material.CHAINMAIL_CHESTPLATE),
            new ItemStack(Material.BREAD)
        ));

        startingKits.put(PlayerClass.SCAVENGER, Arrays.asList(
            new ItemStack(Material.TORCH, 3),
            new ItemStack(Material.STRING, 5),
            new ItemStack(Material.BREAD, 2)
        ));

        startingKits.put(PlayerClass.ENGINEER, Arrays.asList(
            new ItemStack(Material.IRON_INGOT, 5),
            new ItemStack(Material.TORCH, 2),
            new ItemStack(Material.BREAD)
        ));

        skillCooldownsSeconds.put(PlayerClass.MEDIC, 180L);
        skillCooldownsSeconds.put(PlayerClass.SOLDIER, 300L);
        skillCooldownsSeconds.put(PlayerClass.SCAVENGER, 120L);
        skillCooldownsSeconds.put(PlayerClass.ENGINEER, 1800L);
    }

    private ItemStack createMachete() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GRAY + "Machete Oxidado");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_GRAY + "Forjado con chatarra.");
        lore.add(ChatColor.DARK_GRAY + "No es bonito, pero corta.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.SHARPNESS, 2);
        return item;
    }

    private ItemStack createBandage() {
        ItemStack item = new ItemStack(Material.PAPER);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Bandage");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Restaura 10% de infection.");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void selectClass(Player player, PlayerClass newClass) {
        UUID uuid = player.getUniqueId();
        PlayerClass oldClass = playerClasses.get(uuid);

        if (oldClass != null) {
            player.sendMessage(ChatColor.RED + "Ya tienes una clase: " + oldClass.getDisplayName());
            player.sendMessage(ChatColor.GRAY + "Usa /zm class <clase> para cambiar (pierdes progreso).");
            return;
        }

        playerClasses.put(uuid, newClass);
        giveStartingKit(player, newClass);
        player.sendMessage(ChatColor.GREEN + "===================");
        player.sendMessage(ChatColor.GOLD + "  CLASE SELECCIONADA");
        player.sendMessage(ChatColor.WHITE + "  " + newClass.getDisplayName());
        player.sendMessage(ChatColor.GRAY + "  " + newClass.getDescription());
        player.sendMessage(ChatColor.GREEN + "===================");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
    }

    public void changeClass(Player player, PlayerClass newClass) {
        UUID uuid = player.getUniqueId();
        PlayerClass oldClass = playerClasses.get(uuid);

        if (oldClass != null) {
            player.sendMessage(ChatColor.YELLOW + "Cambiando clase de " + oldClass.getDisplayName() + " a " + newClass.getDisplayName());
            player.sendMessage(ChatColor.RED + "ADVERTENCIA: Perderas tu progreso de clase.");
        }

        playerClasses.put(uuid, newClass);
        clearInventory(player);
        giveStartingKit(player, newClass);
        player.sendMessage(ChatColor.GREEN + "Clase cambiada a " + newClass.getDisplayName() + "!");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 1.0f);
    }

    public PlayerClass getPlayerClass(Player player) {
        return playerClasses.getOrDefault(player.getUniqueId(), PlayerClass.SURVIVOR);
    }

    public boolean hasClass(Player player) {
        return playerClasses.containsKey(player.getUniqueId());
    }

    private void giveStartingKit(Player player, PlayerClass playerClass) {
        List<ItemStack> kit = startingKits.get(playerClass);
        if (kit == null) return;
        PlayerInventory inv = player.getInventory();
        for (ItemStack item : kit) {
            if (item != null) {
                inv.addItem(item);
            }
        }
    }

    private void clearInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public boolean useSkill(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerClass pClass = getPlayerClass(player);

        if (pClass == PlayerClass.SURVIVOR) {
            player.sendMessage(ChatColor.GRAY + "Supervivientes no tienen habilidad activa.");
            return false;
        }

        if (skillCooldowns.containsKey(uuid)) {
            long elapsed = (System.currentTimeMillis() - skillCooldowns.get(uuid)) / 1000;
            long cooldown = skillCooldownsSeconds.getOrDefault(pClass, 300L);
            if (elapsed < cooldown) {
                player.sendMessage(ChatColor.RED + "Habilidad en cooldown: " + (cooldown - elapsed) + "s");
                return false;
            }
        }

        boolean success = executeSkill(player, pClass);
        if (success) {
            skillCooldowns.put(uuid, System.currentTimeMillis());
        }
        return success;
    }

    private boolean executeSkill(Player player, PlayerClass pClass) {
        switch (pClass) {
            case MEDIC:
                return executeMedicSkill(player);
            case SOLDIER:
                return executeSoldierSkill(player);
            case SCAVENGER:
                return executeScavengerSkill(player);
            case ENGINEER:
                return executeEngineerSkill(player);
            default:
                return false;
        }
    }

    private boolean executeMedicSkill(Player player) {
        Player target = getTargetPlayer(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Usa: /zm skill <jugador> para apuntar a alguien.");
            skillTargets.put(player.getUniqueId(), null);
            return false;
        }

        double currentInfection = plugin.getInfected().getInfectionLevel(target);
        if (currentInfection <= 0) {
            player.sendMessage(ChatColor.RED + target.getName() + " no esta infectado.");
            return false;
        }

        double cureAmount = 20.0;
        double bonus = plugin.getProgressionManager().getCureBonus(player);
        if (bonus > 0) {
            cureAmount *= (1.0 + bonus / 100.0);
        }

        plugin.getInfected().decreasePlayerInfection(target, cureAmount);
        player.sendMessage(ChatColor.GREEN + "Primeros auxilios aplicados a " + target.getName() + "! Infection -" + (int)cureAmount + "%");
        target.sendMessage(ChatColor.GREEN + player.getName() + " te curo! Infection -" + (int)cureAmount + "%");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        return true;
    }

    private boolean executeSoldierSkill(Player player) {
        int radius = 20;
        int count = 0;
        for (Player nearby : player.getLocation().getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= radius) {
                nearby.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 600, 0));
                nearby.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH, 600, 0));
                count++;
            }
        }
        player.sendMessage(ChatColor.GOLD + "[RALLYING CRY] Speed + Strength a " + count + " jugadores por 30s!");
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.8f);
        return true;
    }

    private boolean executeScavengerSkill(Player player) {
        player.sendMessage(ChatColor.YELLOW + "[SENSE LOOT] Revelando cofres cercanos...");
        player.sendMessage(ChatColor.GRAY + "Usa /zm skill para revelar loot en 200 blocks por 10s.");
        int radius = 200;
        int count = 0;
        for (org.bukkit.Location loc : findNearbyChests(player.getLocation(), radius)) {
            player.sendMessage(ChatColor.GOLD + "  Chest en: " + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ());
            player.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, loc, 10, 0.5, 0.5, 0.5, 0.02);
            count++;
        }
        player.sendMessage(ChatColor.GRAY + "Total: " + count + " cofres detectados.");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.5f);
        return true;
    }

    private boolean executeEngineerSkill(Player player) {
        player.sendMessage(ChatColor.YELLOW + "[FORTIFY] Construyendo defensa...");
        org.bukkit.Location loc = player.getLocation();
        org.bukkit.Material[] blocks = new org.bukkit.Material[]{
            Material.OAK_PLANKS, Material.OAK_PLANKS, Material.OAK_PLANKS,
            Material.OAK_FENCE, Material.OAK_FENCE, Material.OAK_FENCE
        };
        int idx = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    loc.clone().add(x, 0, z).getBlock().setType(blocks[idx % blocks.length]);
                    idx++;
                }
            }
        }
        player.sendMessage(ChatColor.GREEN + "Fortificacion completada! 8 bloques colocados.");
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        return true;
    }

    private List<org.bukkit.Location> findNearbyChests(org.bukkit.Location center, int radius) {
        List<org.bukkit.Location> chests = new ArrayList<>();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x += 16) {
            for (int z = cz - radius; z <= cz + radius; z += 16) {
                for (int y = Math.max(0, cy - radius); y <= cy + radius; y++) {
                    org.bukkit.block.Block block = center.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.CHEST) {
                        chests.add(block.getLocation());
                    }
                }
            }
        }
        return chests;
    }

    private Player getTargetPlayer(Player caster) {
        UUID casterUuid = caster.getUniqueId();
        if (skillTargets.containsKey(casterUuid)) {
            UUID targetUuid = skillTargets.get(casterUuid);
            if (targetUuid != null) {
                return org.bukkit.Bukkit.getPlayer(targetUuid);
            }
        }
        return null;
    }

    public void setSkillTarget(Player caster, Player target) {
        skillTargets.put(caster.getUniqueId(), target.getUniqueId());
        caster.sendMessage(ChatColor.GREEN + "Objetivo establecido: " + target.getName());
    }

    public void giveKit(Player player, String kitName) {
        switch (kitName.toUpperCase()) {
            case "STARKIT":
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().addItem(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 3));
                player.getInventory().addItem(new ItemStack(Material.TORCH, 16));
                player.sendMessage(ChatColor.GOLD + "STAR KIT recibido!");
                break;
            case "MEDICALKIT":
            case "MEDICAL":
                player.getInventory().addItem(new ItemStack(Material.POTION, 3));
                player.getInventory().addItem(createBandage());
                player.getInventory().addItem(createBandage());
                player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
                player.sendMessage(ChatColor.GREEN + "MEDICAL KIT recibido!");
                break;
            case "EXPLORERKIT":
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                player.getInventory().addItem(new ItemStack(Material.SHIELD));
                player.getInventory().addItem(new ItemStack(Material.TORCH, 32));
                player.getInventory().addItem(new ItemStack(Material.BREAD, 3));
                player.getInventory().addItem(new ItemStack(Material.COMPASS));
                player.sendMessage(ChatColor.YELLOW + "EXPLORER KIT recibido!");
                break;
            case "ZOMBIEHUNTERKIT":
                player.getInventory().addItem(new ItemStack(Material.CROSSBOW));
                player.getInventory().addItem(new ItemStack(Material.ARROW, 24));
                player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
                player.getInventory().addItem(new ItemStack(Material.SPLASH_POTION, 2));
                player.getInventory().addItem(new ItemStack(Material.TORCH, 8));
                player.sendMessage(ChatColor.DARK_RED + "ZOMBIE HUNTER KIT recibido!");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Kit desconocido: " + kitName);
        }
    }

    public String[] getClassList() {
        return Arrays.stream(PlayerClass.values())
            .map(c -> c.getDisplayName() + ": " + c.getDescription())
            .toArray(String[]::new);
    }

    public boolean hasSurvivorPassive(Player player) {
        PlayerClass pClass = getPlayerClass(player);
        return pClass == PlayerClass.SURVIVOR;
    }

    public double getClassDamageBonus(Player player) {
        PlayerClass pClass = getPlayerClass(player);
        if (pClass == PlayerClass.SOLDIER) return 0.15;
        return 0.0;
    }

    public double getClassDropBonus(Player player) {
        PlayerClass pClass = getPlayerClass(player);
        if (pClass == PlayerClass.SCAVENGER) return 0.25;
        return 0.0;
    }
}