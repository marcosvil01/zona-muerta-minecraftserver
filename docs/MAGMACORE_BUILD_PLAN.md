# ZonaMuerta - Análisis de Plugins de Mobs y Estructuras

## Estado Actual (Mayo 2026)

### Plugins Descargados y Listos para Deploy

| Plugin | Archivo | Versión | Tamaño | MagmaCore |
|--------|---------|---------|--------|-----------|
| MythicMobs | `MythicMobs-5.12.0.jar` | 5.12.0-SNAPSHOT-local | 19MB | NO (externo) |
| BetterStructures | `BetterStructures-2.3.0.jar` | 2.3.0 | 5.4MB | ✅ SHADED |
| EliteMobs | `EliteMobs-9.6.3.jar` | 9.6.3 | 9.7MB | ✅ SHADED |
| TAB | `TAB-6.0.2.jar` | 6.0.2 | 8MB | NO |
| ZonaMuerta | `ZonaMuerta-2.0.0.jar` | 2.0.0 | 269KB | NO |

---

## 1. MythicMobs (Plugin Comercial, Ya Comprado)

### Información General
- **Autor:** Lumine (MythicMobs Ltd.)
- **Tipo:** Comercial (no open source, pero ya lo tenés comprado)
- **API:** 1.13+ (incluye Folia support)
- **Repo Maven:** `https://mvn.lumine.io/repository/maven-public/`
- **Web:** https://mythicmobs.net/

### Características Principales
- **Spawn System:** RandomSpawns.yml configurable para spawns automaticos
- **Mob Types:** Mobs con skills, niveles, modifiers
- **Skills System:** YAML-based skills (damage, effects, summons, etc.)
- **AI Goals:** Configurable mob AI behavior
- **Damage System:** Integración con combat mechanics
- **Hooks:** WorldGuard, PlaceholderAPI, Vault, etc.

### Para ZonaMuerta (Ya Configurado)
```
mythicmobs/
├── Mobs/ZombiasZonaMuerta.yml    # 7 tipos de zombies
├── Skills/                       # Skills para mobs
├── RandomSpawns/ZombiasSpawns.yml # Spawn rules
└── Items/                        # Custom items
```

**Zombie Types ya configurados:**
- `ZombiCaminante` - Lento, básico
- `ZombiCorredor` - Rápido
- `ZombiSoldado` - Nivel medio, daño alto
- `ZombiMutante` - Grande, mucho HP
- `ZombiExplosivo` - Daño por explosión al morir
- `ZombiTrepador` - Escala paredes
- `ElPatron` - Boss final

### Integración con ZonaMuerta (MythicBridge.java)
- `spawnMythicMob()` - Spawn mobs programáticamente
- `setBloodMoonActive()` / `setCurrentDay()` - Scaling dinámico
- `refreshAllMobScaling()` - Aplica HP/speed multipliers
- Event listeners para tracking y XP rewards

---

## 2. BetterStructures (Open Source - GPL-3.0)

### Información General
- **Autor:** MagmaGuy
- **Tipo:** Open Source (GPL-3.0)
- **API:** 1.14+
- **Repo Maven:** `https://repo.magmaguy.com/releases`
- **GitHub:** https://github.com/MagmaGuy/BetterStructures
- **Discord:** https://discord.gg/QSA2wgh
- **Dependencies:** SHADED (MagmaCore 2.x inside JAR, no instalar aparte)

### Características Principales
- **Structure Generation:** Genera estructuras random en el mundo
- **Modular World Gen:** Sistema de world generation custom
- **Dungeon System:** Dungeons pre-configurados con spawns
- **Treasure Hunting:** Sistema de cofres y loot
- **Configurable:** Todo via YAML configs

### Comandos
- `/betterstructures` - Comando principal (alias: `/bs`)
- `/betterstructures setup` - Setup de configuraciones
- `/betterstructures initialize` - Inicializar estructuras
- `/betterstructures generateModules` - Generar módulos

### Permisos
- `betterstructures.setup` - Setup commands (op)
- `betterstructures.initialize` - Initialize (op)
- `betterstructures.generatemodules.*` - Generate modules

### Para ZonaMuerta - Uso Recomendado
BetterStructures puede agregar:
1. **Estructuras zombie tematizadas** ( RUINAS de edificios, bunkers)
2. **Dungeons spawn points** (areas especiales con más zombies)
3. **Treasure chests** ( loot zombie temático)
4. **Event structures** (estructuras que spawn durante blood moon)

**NO interfere con MythicMobs** - son sistemas complementarios.

---

## 3. EliteMobs (Open Source - GPL-3.0)

### Información General
- **Autor:** MagmaGuy
- **Tipo:** Open Source (GPL-3.0)
- **API:** 1.21
- **Versión descargada:** 9.6.3
- **Repo Maven:** `https://repo.magmaguy.com/releases`
- **GitHub:** https://github.com/MagmaGuy/EliteMobs
- **Discord:** https://discord.gg/QSA2wgh
- **Dependencies:** SHADED (MagmaCore 2.x inside JAR, no instalar aparte)

### ADVERTENCIA: Conflicto con MythicMobs
```
softdepend:
- MythicMobs    # Puede coexistir, pero:
- LevelledMobs
- InfernalMobs
```
**EliteMobs tiene su propio sistema de mobs elite y bosses.** Usar AMBOS puede causar:
- Doble spawn de mobs
- Conflicts en damage/kill events
- Confusión en configuración

### Características Principales
- **Boss System:** Bosses épicos con fases y habilidades
- **Quest System:** Sistema de quests integrado
- **Loot System:** Loot épico procedural con enchantments
- **Currency:** Sistema de economía propia (EMCoins)
- **Ranks:** Sistema de rangos para jugadores
- **Arena System:** Arenas para combate
- **Custom Enchants:** Enchantments propios
- **Scriptable Powers:** Powers basados en Lua scripts
- **Dungeons:** Instanced dungeons
- **Treasure Chests:** Cofres con loot

### Comandos Principales
- `/elitemobs` (alias: `/em`) - Comando principal
- `/adventurersguild` (alias: `/ag`) - Menú del guild
- Quest commands, loot commands, dungeon commands, etc.

### Permisos
~100 permisos configurados, incluyendo:
- `elitemobs.*` - Todos los permisos (op)
- `elitemobs.user` - Permisos básicos para jugadores (default: true)
- Sistema de permisos por rango

### Sistemas Internos
- **ElitePower system:** Powers para mobs (metior shower, ground pound, etc.)
- **Script system:** Scripts Lua para powers custom
- **MatchInstance system:** Sistema de instancias/arenasi
- **Dialog system:** Diálogos para NPCs y quests

### ADVERTENCIA CRÍTICA: EliteMobs vs MythicMobs para ZonaMuerta

**Problema:** Ambos plugins manejan mobs custom y tienen sistemas de spawn/skill independientes.

| Aspecto | MythicMobs (ZonaMuerta) | EliteMobs |
|---------|------------------------|-----------|
| Mobs zombie | ✅ 7 tipos configurados | Tiene sus propios elites |
| Sistema spawn | RandomSpawns.yml | Regional spawns |
| Skills | YAML skills | ElitePowers + Lua scripts |
| Blood moon | ✅ Integración hecha | Tiene eventos custom |
| XP system | ✅ MythicBridge集成 | Tiene su propio XP |

**Si usás AMBOS:** Potential conflicts en:
- Mob spawn events
- Damage calculation
- Kill rewards
- Loot drops

**Recomendación para ZonaMuerta:**
- **Mantener MythicMobs** para zombies (ya está integrado)
- **IGNORAR EliteMobs** a menos que quieras reemplazar completamente el sistema de mobs
- **BetterStructures** puede usarse para estructuras (no tiene mobs propios, solo spawn points que pueden usarMythicMobs)

---

## Comparación Directa

| Feature | MythicMobs | BetterStructures | EliteMobs |
|---------|------------|-----------------|----------|
| **Licencia** | Comercial | Open Source (GPL-3) | Open Source (GPL-3) |
| **Costo** | Ya comprado | Gratis | Gratis |
| **Mobs custom** | ✅ Excellent | No (solo spawn points) | ✅ Excellent |
| **Skills/Abilities** | YAML-based | N/A | Lua + ElitePowers |
| **Spawn system** | RandomSpawns.yml | Structures gen | Regional spawns |
| **Bosses** | Basic | No | ✅ Excellent |
| **Quests** | No | No | ✅ Excellent |
| **Loot custom** | Basic items | Treasure chests | ✅ Procedural épico |
| **Dungeons** | No | ✅ Yes | ✅ Yes |
| **Structures** | Schematic support | ✅ Excellent | No |
| **Integración ZM** | ✅ MythicBridge | ✅ Compatible | ❌ Conflict |

---

## Arquitectura de ZonaMuerta - Recomendación Final

### Configuración Recomendada

```
plugins/
├── MythicMobs-5.12.0.jar     # Sistema de mobs zombie (YA CONFIGURADO)
├── BetterStructures-2.3.0.jar # Estructuras y dungeons (OPCIONAL)
├── EliteMobs-9.6.3.jar       # NO USAR (conflicto con MythicMobs)
├── TAB-6.0.2.jar             # Tab list
├── ZonaMuerta-2.0.0.jar     # Plugin principal
└── [otros plugins]
```

### NO USAR EliteMobs Porque:
1. **Conflicto de sistema de mobs** - Ambos registran mobs custom
2. **MythicBridge ya hecho** - Perderías toda la integración sangre/día
3. **Dos sistemas de spawn** - RandomSpawns.yml vs Regional Spawns
4. **Redundancia** - EliteMobs tiene boss system, pero ya tenés ElPatron en MythicMobs

### SÍ USAR BetterStructures Porque:
1. **No tiene mobs propios** - Solo genera estructuras
2. **Spawn points configurables** - Puede usar MythicMobs para spawns dentro de dungeons
3. **Dungeon system** - Dungeons tematizados zombies
4. **Compatible** - No interfere con MythicMobs

---

## Plan de Deploy

### Paso 1: Copiar plugins
```powershell
# Plugins actuales en repo
Copy-Item "plugins/MythicMobs-5.12.0.jar" "server-data\plugins\"
Copy-Item "plugins/TAB-6.0.2.jar" "server-data\plugins\"
Copy-Item "plugins/ZonaMuerta-2.0.0.jar" "server-data\plugins\"
Copy-Item "plugins/BetterStructures-2.3.0.jar" "server-data\plugins\" # Opcional

# NO copiar EliteMobs-9.6.3.jar (conflicto)
```

### Paso 2: Configurar BetterStructures (si se usa)
1. Editar `plugins/BetterStructures/config.yml`
2. Configurar structure spawns en biomas relevantes
3. Opcional: Integrar con MythicMobs para spawns dentro de dungeons

### Paso 3: Testear
```bash
# En VPS
docker-compose up -d
docker exec -it [container] logs -f

# Verificar en juego
/plugins
/mm list
/bs info
```

---

## MythicMobs - Detalle Técnico

### Estructura de Archivos
```
mythicmobs/
├── Mobs/              # Definiciones de mobs
│   └── ZombiasZonaMuerta.yml
├── Skills/            # Skills para mobs
│   └── [skill files]
├── RandomSpawns/      # Reglas de spawn automático
│   └── ZombiasSpawns.yml
├── Items/             # Custom items
├── Drops/             # Loot drops
└── compatibility/    # Compatibilidad con otros plugins
```

### Zombie Types (Ya Configurados)
| Tipo | Nivel Base | HP | Speed | Daño | Special |
|------|------------|-----|-------|------|---------|
| ZombiCaminante | 1 | 30 | 0.2 | 3 | None |
| ZombiCorredor | 2 | 25 | 0.35 | 4 | Fast |
| ZombiSoldado | 3 | 45 | 0.25 | 6 | Armor |
| ZombiMutante | 5 | 80 | 0.15 | 10 | Big |
| ZombiExplosivo | 3 | 35 | 0.2 | 0 | Explodes |
| ZombiTrepador | 2 | 30 | 0.3 | 5 | Climbs |
| ElPatron | 10 | 500 | 0.2 | 15 | Boss |

### MythicBridge API (ZonaMuerta.java)
```java
// Blood moon activation
mythicBridge.setBloodMoonActive(true);
mythicBridge.refreshAllMobScaling();

// Day progression
mythicBridge.setCurrentDay(day);

// Spawn mobs
mythicBridge.spawnMythicMob("ElPatron", location, level);

// Track mobs
Set<UUID> tracked = mythicBridge.getTrackedMobs();
long count = mythicBridge.getZMZombieCount();
```

---

## BetterStructures - Detalle Técnico

### Estructura de Archivos
```
betterstructures/
├── config.yml              # Config general
├── structures/             # Estructuras custom
│   └── [structure files]
├── spawnpools/            # Spawn pools
├── modules/               # Módulos de generation
├── dungeons/              # Configuraciones de dungeon
└── treasures/             # Cofres y loot
```

### Uso para ZonaMuerta
BetterStructures puede generar:
1. **Ruins** - Estructuras rotas con spawns de zombies
2. **Bunkers** - Ambientes cerrados con múltiples zombies
3. **Monuments** - Estructuras grandes para boss fights
4. **Dungeons** - Instanced areas con dificultad escalada

---

## NO USAR: EliteMobs - Detalle de Conflictos

### Conflictos Específicos
1. **Mob spawn events** - Ambos escuchan `EntitySpawnEvent`
2. **Damage calculation** - Diferentes fórmulas de daño
3. **Kill rewards** - Doble XP/loot
4. **Boss mechanics** - ElPatron (MM) vs EliteBosses (EM)

### Si Realmente Quisieras EliteMobs
Habría que:
1. **REMOVER MythicMobs** completamente
2. **Reescribir MythicBridge** para usar EliteMobs API
3. **Convertir zombies** a EliteMobs format
4. **Perder** toda la integración actual de blood moon/day scaling

**NO RECOMENDADO** - Demasiado trabajo para algo que MythicMobs ya hace.

---

## Referencias

- MythicMobs: https://mythicmobs.net/
- BetterStructures: https://github.com/MagmaGuy/BetterStructures
- EliteMobs: https://github.com/MagmaGuy/EliteMobs
- Discord MagmaGuy: https://discord.gg/QSA2wgh
- MagmaCore Maven: https://repo.magmaguy.com/releases

---

## Checklist Final

- [x] MythicMobs-5.12.0.jar en repo (comprado, configurado)
- [x] BetterStructures-2.3.0.jar en repo (open source, listo)
- [x] EliteMobs-9.6.3.jar en repo (PERO NO USAR - conflicto)
- [x] MythicBridge integration hecho
- [x] Blood moon + day scaling funcionando
- [ ] Deploy a VPS
- [ ] Test BetterStructures (opcional)
- [ ] Documentar configs finales