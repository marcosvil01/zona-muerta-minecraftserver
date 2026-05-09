package com.zonamuerta.plugin;

import com.zonamuerta.plugin.Infected;
import com.zonamuerta.plugin.SafezoneManager;
import com.zonamuerta.plugin.ZonaMuerta;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler
implements CommandExecutor {
    private final ZonaMuerta plugin;
    private final Infected infected;
    private final SafezoneManager safezoneManager;
    private AuctionHouse auctionHouse;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;

    public CommandHandler(ZonaMuerta plugin, Infected infected, SafezoneManager safezoneManager) {
        this.plugin = plugin;
        this.infected = infected;
        this.safezoneManager = safezoneManager;
    }

    public void setManagers(AuctionHouse auctionHouse, StatsManager statsManager, ScoreboardManager scoreboardManager) {
        this.auctionHouse = auctionHouse;
        this.statsManager = statsManager;
        this.scoreboardManager = scoreboardManager;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(cmd.getName().equalsIgnoreCase("zonamuerta") || cmd.getName().equalsIgnoreCase("zm"))) {
            return false;
        }
        if (args.length == 0) {
            this.plugin.sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    this.plugin.reloadConfig();
                    this.infected.loadInfectionConfig();
                    this.plugin.loadMutationConfig();
                    this.plugin.loadLootDropsConfig();
                    this.plugin.loadAbilityConfig();
                    this.plugin.loadSpawnTimeModeConfig();
                    this.plugin.loadSmarterTargetingConfig();
                    this.plugin.loadGroupBehaviorConfig();
                    this.plugin.loadBlockBreakingConfig();
                    this.plugin.loadZombieStatCaps();
                    this.plugin.loadZombieLimitsConfig();
                    this.plugin.loadBloodmoonConfig();
                    this.plugin.loadEvolutionConfig();
                    this.plugin.loadPresetConfig();
                    this.plugin.loadWorldConfigs();
                    this.safezoneManager.loadSafezones();
                    this.plugin.getStructureManager().loadStructuresConfig();
                    sender.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Config recargada!");
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "info": {
                this.plugin.sendStatusInfo(sender);
                return true;
            }
            case "spawn": {
                if (sender instanceof Player && sender.hasPermission("zonamuerta.spawn")) {
                    this.plugin.handleZombieSpawn((Player)sender, args);
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "forcebloodmoon":
            case "forzarlunaroja": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    this.plugin.triggerBloodMoon();
                    sender.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Luna de sangre activada!");
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "forcestopbloodmoon":
            case "pararlunaroja": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    this.plugin.stopBloodMoon();
                    sender.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Luna de sangre desactivada!");
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "killzombies":
            case "matarzombies": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    this.plugin.killCustomZombies();
                    sender.sendMessage(String.valueOf(ChatColor.GREEN) + "Todos los zombies personalizados han sido eliminados.");
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "clearzombies":
            case "limpiarzombies": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    if (sender instanceof Player) {
                        Player player = (Player)sender;
                        if (args.length < 2) {
                            player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm limpiarzombies <radio>");
                            return true;
                        }
                        try {
                            int radius = Integer.parseInt(args[1]);
                            if (radius < 1) {
                                player.sendMessage(String.valueOf(ChatColor.RED) + "El radio debe ser un numero positivo.");
                                return true;
                            }
                            this.plugin.clearZombiesInRadius(player, radius);
                            player.sendMessage(String.valueOf(ChatColor.GREEN) + "Zombies eliminados en un radio de " + radius + " bloques.");
                        }
                        catch (NumberFormatException e) {
                            player.sendMessage(String.valueOf(ChatColor.RED) + "Proporciona un numero valido para el radio.");
                        }
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "Este comando solo puede ser usado por jugadores.");
                    }
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "preset":
            case "dificultad": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    if (args.length < 2) {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm dificultad <casual|normal|hardcore|pesadilla>");
                        sender.sendMessage(String.valueOf(ChatColor.YELLOW) + "Dificultad actual: " + String.valueOf(ChatColor.WHITE) + this.plugin.getCurrentPreset());
                        return true;
                    }
                    String presetName = args[1].toLowerCase();
                    this.plugin.applyPreset(presetName);
                    sender.sendMessage(String.valueOf(ChatColor.GREEN) + "Dificultad cambiada a: " + presetName);
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "safezone":
            case "zonasegura": {
                return this.handleSafezoneCommand(sender, args);
            }
            case "event":
            case "evento": {
                if (sender.hasPermission("zonamuerta.admin")) {
                    if (args.length < 3) {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm evento <iniciar/parar> <lunaroja>");
                        return true;
                    }
                    String action = args[1].toLowerCase();
                    String eventType = args[2].toLowerCase();
                    if (action.equals("start") || action.equals("iniciar")) {
                        if (eventType.equals("bloodmoon") || eventType.equals("lunaroja")) {
                            this.plugin.triggerBloodMoon();
                            sender.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Luna de sangre activada!");
                        } else {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "Tipo de evento desconocido. Disponibles: lunaroja");
                        }
                    } else if (action.equals("stop") || action.equals("parar")) {
                        if (eventType.equals("bloodmoon") || eventType.equals("lunaroja")) {
                            this.plugin.stopBloodMoon();
                            sender.sendMessage(String.valueOf(ChatColor.GREEN) + "\u00a1Luna de sangre desactivada!");
                        } else {
                            sender.sendMessage(String.valueOf(ChatColor.RED) + "Tipo de evento desconocido. Disponibles: lunaroja");
                        }
                    } else {
                        sender.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm evento <iniciar/parar> <lunaroja>");
                    }
                } else {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                }
                return true;
            }
            case "cure":
            case "cura": {
                int amount;
                if (!sender.hasPermission("zonamuerta.admin")) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
                    return true;
                }
                if (args.length < 4 || !args[1].equalsIgnoreCase("give") && !args[1].equalsIgnoreCase("dar")) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm cura dar <jugador> <cantidad>");
                    return true;
                }
                try {
                    amount = Integer.parseInt(args[3]);
                }
                catch (NumberFormatException e) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "La cantidad debe ser un numero positivo.");
                    return true;
                }
                if (amount <= 0) {
                    sender.sendMessage(String.valueOf(ChatColor.RED) + "La cantidad debe ser mayor que 0.");
                    return true;
                }
                this.plugin.giveCurePotions(sender, args[2], amount);
                return true;
            }
            case "crafting":
            case "crafteo": {
                if (sender instanceof Player) {
                    Player player = (Player)sender;
                    this.plugin.openCraftingMenuGUI(player);
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                }
                return true;
            }
            case "level":
            case "nivel": {
                if (sender instanceof Player player) {
                    ProgressionManager.ProgressionResult result = this.plugin.getProgressionManager().getPlayerLevelInfo(player);
                    player.sendMessage(ChatColor.GOLD + "==========");
                    player.sendMessage(ChatColor.YELLOW + "Nivel: " + ChatColor.WHITE + result.level);
                    player.sendMessage(ChatColor.YELLOW + "XP: " + ChatColor.WHITE + result.xp + " / " + result.xpToNext);
                    player.sendMessage(ChatColor.GRAY + "Perks: " + ChatColor.DARK_PURPLE + result.perks);
                    player.sendMessage(ChatColor.GOLD + "==========");
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                }
                return true;
            }
            case "class":
            case "clase": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (args.length < 2) {
                    ClassManager.PlayerClass pClass = this.plugin.getClassManager().getPlayerClass(player);
                    player.sendMessage(ChatColor.GOLD + "Tu clase: " + ChatColor.WHITE + pClass.getDisplayName());
                    player.sendMessage(ChatColor.GRAY + "Descripcion: " + pClass.getDescription());
                    player.sendMessage(ChatColor.GRAY + "Usa /zm class <clase> para cambiar o seleccionar una clase.");
                    player.sendMessage(ChatColor.YELLOW + "Clases disponibles: Survivor, Medic, Soldier, Scavenger, Engineer");
                    return true;
                }
                String className = args[1].toLowerCase();
                ClassManager.PlayerClass newClass = switch (className) {
                    case "survivor" -> ClassManager.PlayerClass.SURVIVOR;
                    case "medic" -> ClassManager.PlayerClass.MEDIC;
                    case "soldier" -> ClassManager.PlayerClass.SOLDIER;
                    case "scavenger" -> ClassManager.PlayerClass.SCAVENGER;
                    case "engineer" -> ClassManager.PlayerClass.ENGINEER;
                    default -> null;
                };
                if (newClass == null) {
                    player.sendMessage(ChatColor.RED + "Clase desconocida. Disponibles: survivor, medic, soldier, scavenger, engineer");
                    return true;
                }
                if (this.plugin.getClassManager().hasClass(player)) {
                    this.plugin.getClassManager().changeClass(player, newClass);
                } else {
                    this.plugin.getClassManager().selectClass(player, newClass);
                }
                return true;
            }
            case "skill": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("target")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usa: /zm skill target <jugador>");
                        return true;
                    }
                    Player target = this.plugin.getServer().getPlayer(args[2]);
                    if (target == null) {
                        player.sendMessage(ChatColor.RED + "Jugador no encontrado: " + args[2]);
                        return true;
                    }
                    this.plugin.getClassManager().setSkillTarget(player, target);
                    return true;
                }
                boolean success = this.plugin.getClassManager().useSkill(player);
                if (!success) {
                    player.sendMessage(ChatColor.RED + "No puedes usar habilidad.");
                }
                return true;
            }
            case "balance":
            case "bal":
            case "zp": {
                if (sender instanceof Player player) {
                    this.plugin.getEconomyManager().showBalance(player);
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                }
                return true;
            }
            case "trader":
            case "tienda": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Tiendas disponibles:");
                    player.sendMessage(ChatColor.WHITE + "/zm trader mercader - Comida y materiales");
                    player.sendMessage(ChatColor.WHITE + "/zm trader arsenalero - Armas y armaduras");
                    player.sendMessage(ChatColor.WHITE + "/zm trader medico - Pociones y curas");
                    player.sendMessage(ChatColor.WHITE + "/zm sell - Vender items");
                    return true;
                }
                String trader = args[1].toLowerCase();
                this.plugin.getEconomyManager().openTraderMenu(player, trader);
                return true;
            }
            case "sell":
            case "vender": {
                if (sender instanceof Player player) {
                    this.plugin.getEconomyManager().openSellMenu(player);
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                }
                return true;
            }
            case "kit":
            case "kits": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Kits disponibles (compra en /zm tienda):");
                    player.sendMessage(ChatColor.WHITE + "STARKIT - 500 ZP");
                    player.sendMessage(ChatColor.WHITE + "MEDICALKIT - 300 ZP");
                    player.sendMessage(ChatColor.WHITE + "EXPLORERKIT - 400 ZP");
                    player.sendMessage(ChatColor.WHITE + "ZOMBIEHUNTERKIT - 600 ZP");
                    player.sendMessage(ChatColor.GRAY + "Usa /zm buy <kitname> para comprar.");
                    return true;
                }
                String kitName = args[1].toUpperCase();
                player.sendMessage(ChatColor.YELLOW + "Usa /zm buy " + kitName + " en la tienda.");
                return true;
            }
            case "ah":
            case "auction":
            case "subasta": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (this.auctionHouse == null) {
                    player.sendMessage(ChatColor.RED + "Casa de subastas no disponible.");
                    return true;
                }
                if (args.length < 2) {
                    this.auctionHouse.openAuctionHouse(player);
                } else if (args[1].equalsIgnoreCase("create") || args[1].equalsIgnoreCase("crear")) {
                    player.sendMessage(ChatColor.YELLOW + "Usa /ah create <precio> <compra> <horas> con el item en la mano.");
                } else if (args[1].equalsIgnoreCase("mis") || args[1].equalsIgnoreCase("my")) {
                    this.auctionHouse.openMyAuctions(player);
                } else if (args[1].equalsIgnoreCase("ayuda") || args[1].equalsIgnoreCase("help")) {
                    player.sendMessage(ChatColor.GOLD + "=== Casa de Subastas ===");
                    player.sendMessage(ChatColor.GRAY + "/ah - Abrir casa de subastas");
                    player.sendMessage(ChatColor.GRAY + "/ah create <precio> <compra> <horas> - Crear auction");
                    player.sendMessage(ChatColor.GRAY + "/ah mis - Ver mis subastas");
                    player.sendMessage(ChatColor.GRAY + "/ah bid <id> <monto> - Pujar");
                    player.sendMessage(ChatColor.GRAY + "/ah buyout <id> - Compra inmediata");
                } else {
                    player.sendMessage(ChatColor.RED + "Uso: /ah [create/mis/bid/buyout]");
                }
                return true;
            }
            case "stats":
            case "estadisticas": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (this.statsManager == null) {
                    player.sendMessage(ChatColor.RED + "Sistema de estadisticas no disponible.");
                    return true;
                }
                this.statsManager.showStats(player);
                return true;
            }
            case "scoreboard":
            case "sb": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (this.scoreboardManager == null) {
                    player.sendMessage(ChatColor.RED + "Scoreboard no disponible.");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("off")) {
                    this.scoreboardManager.hideScoreboard(player);
                    player.sendMessage(ChatColor.YELLOW + "Scoreboard ocultado.");
                } else {
                    this.scoreboardManager.showScoreboard(player);
                    player.sendMessage(ChatColor.GREEN + "Scoreboard activado.");
                }
                return true;
            }
            case "menu":
            case "m": {
                if (sender instanceof Player player) {
                    this.plugin.getMenuManager().openMainMenu(player);
                } else {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                }
                return true;
            }
            case "spec": {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Este comando solo puede ser usado por jugadores.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ChatColor.GOLD + "=== Especializaciones ===");
                    player.sendMessage(ChatColor.GRAY + "/spec info - Ver tu especializacion");
                    player.sendMessage(ChatColor.GRAY + "/spec select <tipo> - Seleccionar especializacion");
                    player.sendMessage(ChatColor.GRAY + "/spec ability - Usar habilidad especial");
                    player.sendMessage(ChatColor.GRAY + "Tipos: apocalypse, plague, darkarts, fieldmedic");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "info" -> {
                        var spec = this.plugin.getSpecsManager().getPlayerSpec(player);
                        if (spec == null) {
                            player.sendMessage(ChatColor.RED + "No tienes especializacion.");
                        } else {
                            player.sendMessage(ChatColor.GOLD + "Tu especializacion: " + ChatColor.WHITE + spec.type.getDisplayName());
                            player.sendMessage(ChatColor.GRAY + spec.type.getDescription());
                        }
                    }
                    case "select" -> {
                        if (args.length < 3) {
                            player.sendMessage(ChatColor.RED + "Uso: /spec select <apocalypse|plague|darkarts|fieldmedic>");
                            return true;
                        }
                        SpecsManager.SpecType specType = switch (args[2].toLowerCase()) {
                            case "apocalypse" -> SpecsManager.SpecType.APOCALYPSE_SPECIALIST;
                            case "plague" -> SpecsManager.SpecType.PLAGUE_CARRIER;
                            case "darkarts" -> SpecsManager.SpecType.DARK_ARTS;
                            case "fieldmedic" -> SpecsManager.SpecType.FIELD_MEDIC;
                            default -> null;
                        };
                        if (specType == null) {
                            player.sendMessage(ChatColor.RED + "Tipo invalido: " + args[2]);
                        } else {
                            this.plugin.getSpecsManager().selectSpec(player, specType);
                        }
                    }
                    case "ability" -> {
                        this.plugin.getSpecsManager().useSpecAbility(player);
                    }
                    default -> {
                        player.sendMessage(ChatColor.RED + "Subcomando desconocido: " + args[1]);
                    }
                }
                return true;
            }
        }
        this.plugin.sendHelp(sender);
        return true;
    }

    private boolean handleSafezoneCommand(CommandSender sender, String[] args) {
        String subCommand;
        if (!(sender instanceof Player)) {
            sender.sendMessage(String.valueOf(ChatColor.RED) + "Este comando solo puede ser usado por jugadores.");
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("zonamuerta.safezone") && !player.hasPermission("zonamuerta.admin")) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "\u00a1Sin permisos!");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm zonasegura <crear|eliminar|lista|expandir> [nombre] [cantidad]");
            return true;
        }
        switch (subCommand = args[1].toLowerCase()) {
            case "create":
            case "crear": {
                if (args.length < 3) {
                    player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm zonasegura crear <nombre>");
                    return true;
                }
                this.safezoneManager.createSafezone(player, args[2]);
                return true;
            }
            case "remove":
            case "eliminar": {
                if (args.length < 3) {
                    player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm zonasegura eliminar <nombre>");
                    return true;
                }
                this.safezoneManager.removeSafezone(player, args[2]);
                return true;
            }
            case "list":
            case "lista": {
                this.safezoneManager.listSafezones(player);
                return true;
            }
            case "expand":
            case "expandir": {
                if (args.length < 3) {
                    player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm zonasegura expandir <nombre> [cantidad]");
                    return true;
                }
                int expandAmount = 5;
                if (args.length >= 4) {
                    try {
                        expandAmount = Integer.parseInt(args[3]);
                    }
                    catch (NumberFormatException e) {
                        player.sendMessage(String.valueOf(ChatColor.RED) + "Cantidad invalida. Usando por defecto: 5");
                    }
                }
                this.safezoneManager.expandSafezone(player, args[2], expandAmount);
                return true;
            }
        }
        player.sendMessage(String.valueOf(ChatColor.RED) + "Uso: /zm zonasegura <crear|eliminar|lista|expandir> [nombre] [cantidad]");
        return true;
    }
}

