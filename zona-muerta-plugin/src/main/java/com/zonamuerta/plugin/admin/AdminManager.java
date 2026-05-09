package com.zonamuerta.plugin.admin;

import com.zonamuerta.plugin.ZonaMuerta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdminManager implements Listener {
    private final ZonaMuerta plugin;
    private final Set<UUID> vanished = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdminManager(ZonaMuerta plugin) {
        this.plugin = plugin;
    }

    public void toggleVanish(Player player) {
        if (vanished.contains(player.getUniqueId())) {
            vanished.remove(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, player);
            }
            player.sendMessage(miniMessage.deserialize("<green>Vanish desactivado.</green>"));
        } else {
            vanished.add(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("zonamuerta.admin")) {
                    p.hidePlayer(plugin, player);
                }
            }
            player.sendMessage(miniMessage.deserialize("<red>Vanish activado.</red>"));
        }
    }

    public void toggleFreeze(Player target, Player admin) {
        if (frozen.contains(target.getUniqueId())) {
            frozen.remove(target.getUniqueId());
            admin.sendMessage(miniMessage.deserialize("<green>Has descongelado a " + target.getName() + ".</green>"));
            target.sendMessage(miniMessage.deserialize("<green>Has sido descongelado.</green>"));
        } else {
            frozen.add(target.getUniqueId());
            admin.sendMessage(miniMessage.deserialize("<red>Has congelado a " + target.getName() + ".</red>"));
            target.sendMessage(miniMessage.deserialize("<red>HAS SIDO CONGELADO PARA REVISIÓN.</red>"));
        }
    }

    public void toggleSpectate(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.sendMessage(miniMessage.deserialize("<yellow>Modo Espectador: OFF</yellow>"));
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(miniMessage.deserialize("<yellow>Modo Espectador: ON</yellow>"));
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (frozen.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    public boolean isVanished(UUID uuid) {
        return vanished.contains(uuid);
    }

    public boolean isFrozen(UUID uuid) {
        return frozen.contains(uuid);
    }
}