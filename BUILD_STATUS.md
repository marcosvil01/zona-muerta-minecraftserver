# Zona Muerta - Project Status

## Build Status: BLOCKED - No JDK 21 Compiler Available

**Last Updated:** 2026-05-10

## What's Done (FASE 1 COMPLETE)

### Core Systems Implemented
1. **ProgressionManager.java** - Levels 1-100, 13 perks, XP rewards per mob, achievements, berserker effect, scoreboard integration
2. **ClassManager.java** - 5 classes (Survivor/Medic/Soldier/Scavenger/Engineer), active skills with cooldowns, starting kits
3. **EconomyManager.java** - ZP currency, 3 traders (Mercader/Arsenalero/Medico), buy/sell GUI menus
4. **WeaponsManager.java** - Added `createMacheteEnriched()` (Sharpness 4, Fire Aspect 1)
5. **AuctionHouse.java** - Full auction system with bidding, buyout, listing fees
6. **StatsManager.java** - Player statistics tracking (kills, deaths, K/D, damage, playtime, etc.)
7. **ScoreboardManager.java** - Live scoreboard with day, level, XP, kills, class, balance, health, infection
8. **AntiCheatListener.java** - Speed hack, fast place/break, illegal blocks, AFK detection
9. **QuestManager.java** - Complete with QuestType (8 types), QuestCategory, daily/weekly quests

### Fixes Applied
- MythicBridge: `onVanillaZombieDeath` guard, NPE fix in `spawnMythicMob`, `applyStatScaling()` with day+bloodmoon multipliers
- ZonaMuerta: `loadLootDropsConfig()` implemented, blood moon day cycle logic fixed, `startSpawningSystem()` removed (MM-only spawn)
- SafezoneManager: `loadConfig()` called before `loadSafezones()`
- MythicMobs: All 11 skills match mob calls, all 6 mobs match spawn configs verified

### Configuration Files
- `progression.yml` - Level/perk definitions, XP rewards per mob
- `classes.yml` - Class configs
- `economy.yml` - Trader configs
- `quests.yml` - Quest definitions
- `config.yml` - Updated with new sections (progression, classes, economy, quests)
- `plugin.yml` - Updated to v2.2.0 with auction/stats/scoreboard permissions

### Files Created/Modified
```
zona-muerta-plugin/src/main/java/com/zonamuerta/plugin/
├── AuctionHouse.java (NEW)
├── StatsManager.java (NEW)
├── ScoreboardManager.java (NEW)
├── AntiCheatListener.java (NEW)
├── QuestManager.java (NEW - from previous session)
├── ProgressionManager.java (UPDATED)
├── ClassManager.java (UPDATED)
├── EconomyManager.java (UPDATED)
├── CommandHandler.java (UPDATED - new commands)
├── ZonaMuerta.java (UPDATED - new managers wired)
└── WeaponsManager.java (UPDATED)
```

## Compilation Blocker

**Issue:** JDK 21 required but only JRE 21 (no compiler) is available locally.
- Local machine has JRE 17 and JRE 21 (no javac)
- Maven cannot compile without JDK 21
- No chocolatey/scoop/winget available

## Next Steps

1. **Install JDK 21** - Download from https://adoptium.net and install manually
2. **Run compilation:** `mvn package` in zona-muerta-plugin/
3. **Deploy** to VPS and test

## New Commands Added
- `/ah` or `/subasta` - Auction house
- `/zm stats` or `/estadisticas` - View player statistics
- `/zm scoreboard` or `/sb` - Toggle scoreboard

## Key Technical Details
- Spawn system unified: ZonaMuerta internal spawner removed, MythicMobs RandomSpawns handles all spawns
- Day-based stat scaling via MythicBridge: +5% HP/damage per day, +50% HP +30% speed on blood moon
- Economy uses YAML files (no MySQL) - fine for <50 players
- QuestManager tracks daily/weekly/main quests separately with completion tracking
