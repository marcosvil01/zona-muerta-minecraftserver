package com.zonamuerta.plugin.portals;

import com.zonamuerta.plugin.ZonaMuerta;
import com.zonamuerta.plugin.selection.SelectionManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;

public class ModernPortalManager implements Listener {
    private final ZonaMuerta plugin;
    private final SelectionManager selectionManager;
    private final Map<String, PortalRegion> regions = new HashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ModernPortalManager(ZonaMuerta plugin, SelectionManager selectionManager) {
        this.plugin = plugin;
        this.selectionManager = selectionManager;
    }

    public void createPortal(String name, Location p1, Location p2, String targetServer) {
        regions.put(name.toLowerCase(), new PortalRegion(p1, p2, targetServer));
    }

    public void removePortal(String name) {
        regions.remove(name.toLowerCase());
    }

    public boolean portalExists(String name) {
        return regions.containsKey(name.toLowerCase());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        for (Map.Entry<String, PortalRegion> entry : regions.entrySet()) {
            if (entry.getValue().contains(to)) {
                player.sendMessage(miniMessage.deserialize("<b>Portal detectado! Teletransportando a " +
                    entry.getValue().targetServer + "</b>"));
            }
        }
    }

    private static class PortalRegion {
        private final Location p1, p2;
        public final String targetServer;

        public PortalRegion(Location p1, Location p2, String targetServer) {
            this.p1 = p1;
            this.p2 = p2;
            this.targetServer = targetServer;
        }

        public boolean contains(Location loc) {
            double minX = Math.min(p1.getX(), p2.getX());
            double maxX = Math.max(p1.getX(), p2.getX());
            double minY = Math.min(p1.getY(), p2.getY());
            double maxY = Math.max(p1.getY(), p2.getY());
            double minZ = Math.min(p1.getZ(), p2.getZ());
            double maxZ = Math.max(p1.getZ(), p2.getZ());
            return loc.getX() >= minX && loc.getX() <= maxX &&
                   loc.getY() >= minY && loc.getY() <= maxY &&
                   loc.getZ() >= minZ && loc.getZ() <= maxZ &&
                   loc.getWorld().equals(p1.getWorld());
        }
    }
}