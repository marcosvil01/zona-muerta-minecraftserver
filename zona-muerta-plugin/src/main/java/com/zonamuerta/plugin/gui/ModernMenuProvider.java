package com.zonamuerta.plugin.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.stream.Collectors;
import java.util.List;

public class ModernMenuProvider {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Component MENU_TITLE = miniMessage.deserialize("<dark_gray>Panel de Control v2</dark_gray>");

    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);

        inv.setItem(11, createItem(Material.FEATHER, "<yellow>Habilidad: Doble Salto</yellow>",
                List.of("<gray>Click para alternar</gray>", "<green>Estado: ACTIVADO</green>")));

        inv.setItem(13, createItem(Material.SUGAR, "<aqua>Habilidad: Velocidad</aqua>",
                List.of("<gray>Click para alternar</gray>", "<green>Estado: ACTIVADO</green>")));

        inv.setItem(15, createItem(Material.ENDER_PEARL, "<light_purple>Teletransporte: Spawn</light_purple>",
                List.of("<gray>Click para ir al spawn</gray>")));

        ItemStack vanish = createItem(Material.ENDER_EYE, "<red><b>MODO VANISH</b></red>",
            List.of("<gray>Hazte invisible para otros.</gray>"));
        ItemStack freeze = createItem(Material.ICE, "<aqua><b>CONGELAR JUGADORES</b></aqua>",
            List.of("<gray>Usa /freeze <nombre> para bloquear.</gray>"));
        ItemStack spectate = createItem(Material.SPYGLASS, "<yellow><b>MODO ESPECTADOR</b></yellow>",
            List.of("<gray>Cambia rápido a espectador.</gray>"));

        inv.setItem(20, vanish);
        inv.setItem(22, freeze);
        inv.setItem(24, spectate);

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(miniMessage.deserialize("<!italic>" + name));
        meta.lore(lore.stream()
                .map(line -> miniMessage.deserialize("<!italic>" + line))
                .collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }
}