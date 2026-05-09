package com.zonamuerta.plugin.selection;

import com.zonamuerta.plugin.ZonaMuerta;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;

public class SelectionListener implements Listener {
    private final ZonaMuerta plugin;
    private final SelectionManager selectionManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public SelectionListener(ZonaMuerta plugin, SelectionManager selectionManager) {
        this.plugin = plugin;
        this.selectionManager = selectionManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("zonamuerta.admin")) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Location clicked = event.getClickedBlock().getLocation();
            selectionManager.setPos1(player, clicked);
            player.sendMessage(miniMessage.deserialize("<yellow>Posicion 1 establecida en " +
                formatLocation(clicked) + "</yellow>"));
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location clicked = event.getClickedBlock().getLocation();
            selectionManager.setPos2(player, clicked);
            player.sendMessage(miniMessage.deserialize("<aqua>Posicion 2 establecida en " +
                formatLocation(clicked) + "</aqua>"));
            event.setCancelled(true);
        }
    }

    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}