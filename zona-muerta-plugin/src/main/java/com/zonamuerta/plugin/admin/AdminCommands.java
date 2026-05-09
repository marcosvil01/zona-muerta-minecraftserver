package com.zonamuerta.plugin.admin;

import com.zonamuerta.plugin.ZonaMuerta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AdminCommands implements CommandExecutor {
    private final AdminManager adminManager;
    private final ZonaMuerta plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public AdminCommands(AdminManager adminManager, ZonaMuerta plugin) {
        this.adminManager = adminManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>Este comando solo puede ser usado por jugadores.</red>"));
            return true;
        }

        if (!player.hasPermission("zonamuerta.admin")) {
            player.sendMessage(miniMessage.deserialize("<red>Sin permisos.</red>"));
            return true;
        }

        switch (label.toLowerCase()) {
            case "vanish", "v" -> {
                adminManager.toggleVanish(player);
            }
            case "freeze" -> {
                if (args.length < 1) {
                    player.sendMessage(miniMessage.deserialize("<yellow>Uso: /freeze <jugador></yellow>"));
                } else {
                    Player target = plugin.getServer().getPlayer(args[0]);
                    if (target == null) {
                        player.sendMessage(miniMessage.deserialize("<red>Jugador no encontrado: " + args[0] + "</red>"));
                    } else {
                        adminManager.toggleFreeze(target, player);
                    }
                }
            }
            case "spectate", "spec" -> {
                adminManager.toggleSpectate(player);
            }
            default -> {
                player.sendMessage(miniMessage.deserialize("<red>Comando desconocido.</red>"));
            }
        }
        return true;
    }
}