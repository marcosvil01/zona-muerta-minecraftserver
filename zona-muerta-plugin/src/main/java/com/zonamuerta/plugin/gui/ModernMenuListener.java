package com.zonamuerta.plugin.gui;

import com.zonamuerta.plugin.ZonaMuerta;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ModernMenuListener implements Listener {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().title();
        if (!title.contains("Panel de Control v2")) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        ZonaMuerta plugin = ZonaMuerta.getInstance();

        switch (item.getType()) {
            case FEATHER -> player.sendMessage(miniMessage.deserialize("<green>Doble Salto alternado!</green>"));
            case SUGAR -> player.sendMessage(miniMessage.deserialize("<aqua>Velocidad alternada!</aqua>"));
            case ENDER_PEARL -> {
                player.teleport(player.getWorld().getSpawnLocation());
                player.sendMessage(miniMessage.deserialize("<light_purple>Teletransportado al spawn!</light_purple>"));
                player.closeInventory();
            }
            case ENDER_EYE -> {
                player.performCommand("vanish");
                player.closeInventory();
            }
            case ICE -> {
                player.sendMessage(miniMessage.deserialize("<yellow>Usa /freeze <jugador> en el chat.</yellow>"));
                player.closeInventory();
            }
            case SPYGLASS -> {
                player.performCommand("spectate");
                player.closeInventory();
            }
        }
    }
}