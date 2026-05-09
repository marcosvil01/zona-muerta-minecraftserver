# Zona Muerta - Minecraft Apocalypse Server

Plugin de survival zombie para Minecraft 1.21.4 con sistema completo de progresion, clases, economia y eventos.

## Caracteristicas

### Sistemas Implementados

| Sistema | Descripcion | Estado |
|---------|-------------|--------|
| **MythicMobs Integration** | Bridge para spawns de mobs personalizados con escalado por dia y luna de sangre | ✅ |
| **Progression System** | Niveles 1-100 con 13 perks (vida extra, dano, drop chance, berserker, etc) | ✅ |
| **Class System** | 5 clases (Survivor/Medic/Soldier/Scavenger/Engineer) con habilidades activas | ✅ |
| **Specializations** | 4 specs nivel 50+ (Apocalypse/Plague/DarkArts/FieldMedic) | ✅ |
| **Economy** | Moneda ZP, 3 traders (Mercader/Arsenalero/Medico), sistema de subastas | ✅ |
| **Quest System** | Quests diarias/semanales/main con multiples tipos (kill/collect/cure/explore) | ✅ |
| **Auction House** | Sistema completo de subastas con pujas y compra inmediata | ✅ |
| **Stats & Scoreboard** | Tracking de kills/deaths/KD, scoreboard en tiempo real | ✅ |
| **AntiCheat** | Deteccion de speed hack, fast place/break, blocos illegales | ✅ |
| **Menu System** | GUI menus con /menu o Shift+Click | ✅ |
| **Monthly Events** | 6 tipos de eventos (Horde/BossRaid/DoubleLoot/Outbreak/Darkness/GoldenArmy) | ✅ |
| **Infection System** | Sistema de infeccion por zombies con cura | ✅ |
| **Structure Gen** | Generacion procedural de estructuras (ruined houses, hospitals) | ✅ |
| **Loot Tables** | Sistema configurabe de loot para zombies y estructuras | ✅ |

### Plugins Recomendados (Del docker-compose)

```
ViaVersion + ViaBackwards      - Compatibilidad de versiones
LuckPerms                       - Sistema de permisos
EssentialsX                     - Comandos basicos
GriefPrevention + CoreProtect   - Proteccion de terrain
WorldEdit + WorldGuard           - Edicion de mundos
DynMap                          - Mapa web en tiempo real
DiscordSRV                      - Bridge Discord <-> Minecraft
MythicMobs                      - Sistema de mobs avanzados
PlaceholderAPI                 - Placeholders para scores
GrapplingHook                  - Gancho grappling
```

## Comandos

| Comando | Descripcion |
|---------|-------------|
| `/zm` | Menu principal |
| `/zm level` | Ver nivel y XP |
| `/zm class` | Seleccionar clase |
| `/zm skill [target]` | Usar habilidad de clase |
| `/zm balance` | Ver balance ZP |
| `/zm trader <mercader/arsenalero/medico>` | Abrir tienda |
| `/zm sell` | Vender items |
| `/zm stats` | Ver estadisticas |
| `/zm scoreboard` | Toggle scoreboard |
| `/zm menu` | Abrir menu principal |
| `/zm spec <select/info/ability>` | Especializaciones nivel 50+ |
| `/ah` | Casa de subastas |
| `/zm zonasegura` | Sistema de zonas seguras |

## API del Plugin

```java
// Acceder managers desde cualquier plugin
ZonaMuerta zm = (ZonaMuerta) Bukkit.getPluginManager().getPlugin("ZonaMuerta");

ProgressionManager prog = zm.getProgressionManager();
ClassManager classMgr = zm.getClassManager();
EconomyManager econ = zm.getEconomyManager();
AuctionHouse auction = zm.getAuctionHouse();
StatsManager stats = zm.getStatsManager();
QuestManager quests = zm.getQuestManager();
SpecsManager specs = zm.getSpecsManager();
MonthlyEventManager events = zm.getMonthlyEventManager();
```

## Estructura del Proyecto

```
zona-muerta-plugin/
├── src/main/java/com/zonamuerta/plugin/
│   ├── ZonaMuerta.java          # Main plugin class
│   ├── CommandHandler.java      # All commands
│   ├── ProgressionManager.java  # Level/XP/perks
│   ├── ClassManager.java        # 5 classes + skills
│   ├── SpecsManager.java        # 4 specializations
│   ├── EconomyManager.java      # ZP currency + traders
│   ├── AuctionHouse.java        # Auction system
│   ├── QuestManager.java         # Quest system
│   ├── StatsManager.java         # Player statistics
│   ├── ScoreboardManager.java   # Live scoreboard
│   ├── MenuManager.java          # GUI menus
│   ├── AntiCheatListener.java    # Anti-cheat
│   ├── MonthlyEventManager.java  # Event system
│   ├── WeaponsManager.java       # Custom weapons
│   ├── MythicBridge.java         # MythicMobs bridge
│   ├── Infected.java             # Infection system
│   ├── SafezoneManager.java      # Safe zones
│   └── structures/               # Structure generation
├── src/main/resources/
│   ├── config.yml               # Main config
│   ├── progression.yml         # Level/perk config
│   ├── classes.yml            # Class configs
│   ├── economy.yml            # Trader configs
│   ├── quests.yml              # Quest definitions
│   └── loot_tables.yml         # Loot configurations
└── mythicmobs/
    ├── Mobs/                   # Mob definitions
    ├── Skills/                 # Skill definitions
    └── RandomSpawns/           # Spawn rules
```

## Configuracion de Lunas de Sangre

En `config.yml`:
```yaml
events:
  bloodmoon:
    default_spawn_multiplier: 2.0
    schedule_enabled: false
    schedule:
      - id: standard_night
        days: "*/5"
        start_tick: 13000
        end_tick: 23000
        spawn_multiplier: 2.0
        mutation_multiplier: 10.0
```

## DiscordSRV Config

DiscordSRV conecta tu server Minecraft con un canal de Discord. Configuracion basica en `config.yml` de DiscordSRV:

```yaml
Channels:
  global: "123456789012345678"
  alternate: "987654321098765432"

BotToken: "YOUR_BOT_TOKEN_HERE"

DiscordChatChannelBridgeToMinecraft: true
MinecraftChatChannelBridgeToDiscord: true
DiscordChatChannelPrefix: ""

NotifyJoinsAndLeaves: true
JoinMessage: "**{player}** se unio al apocalipsis!"
LeaveMessage: "**{player}** murio en el apocalipsis..."

GameStateSync:
  Enabled: true
  CheckInterval: 5

DeathMessages: true
DeathMessageFormat: "☠ **{player}** fue eliminado por {killer}"
```

## Compilacion

```bash
# Requiere JDK 21+
mvn clean package

# El JAR se genera en target/ZonaMuerta-{version}.jar
```

## Deployment

```yaml
# docker-compose.yml (excerpt)
services:
  minecraft:
    image: itzg/minecraft-server:latest
    environment:
      MODRINTH_PROJECTS: |
        mythicmobs
        placeholderapi
        worldedit
        worldguard
        discordsrv
        dynmap
        luckperms
        essentialsx
```

## Licencia

GPL-3.0 - Ver LICENSE

## Enlaces

- [DiscordSRV](https://github.com/DiscordSRV/DiscordSRV) - Bridge Discord
- [MythicMobs](https://github.com/Lumineers/MythicMobs) - Sistema de mobs
- [Paper API](https://papermc.io/) - Minecraft server software
