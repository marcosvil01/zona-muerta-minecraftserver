# MEGA PLAN — Zona Muerta: Apocalipsis Zombie Survival

## FASE 1: CORE (1-2 semanas)
- Progression system (leveling + perks)
- Basic economy (ZP + traders)
- Class selection
- Improved loot tables

## FASE 2: CONTENT (2-3 semanas)
- Structures world gen (verify + fix)
- Quest system
- More traders + auction house
- Kits purchasables

## FASE 3: POLISH (1-2 semanas)
- UI menus polish
- Stats + leaderboards
- Backups automated
- Anti-cheat basic

## FASE 4: ADVANCED (2-4 semanas)
- Specs (sub-classes)
- Advanced quests
- Bunker/underground structures
- Monthly events

## SYSTEMS

### PROGRESSION
niveles: 1-100
xp_por_nivel: nivel * 10
xp_mobs:
  ZombiCaminante: 5
  ZombiCorredor: 10
  ZombiSoldado: 15
  ZombiMutante: 25
  ZombiExplosivo: 12
  ZombiTrepador: 10
  ElPatron: 500

Perks (cada 5 niveles):
- 5: zombie_slayer_1 (+15% dano zombies)
- 10: survivor_1 (+2 corazones)
- 15: zombie_slayer_2 (+25% dano)
- 20: survivor_2 (+2 corazones) + scavenger_1 (+20% drop)
- 25: zombie_slayer_3 (+35% dano)
- 30: survivor_3 (+2 corazones)
- 35: scavenger_2 (+30% drop)
- 40: berserker (speed+str on kill)
- 50: apocalypse_ready (+5 corazones, immunity first infection)
- 60:解毒剂增强 (50% more cure)
- 75: horde_destroyer (critico x2)
- 100:末日求生者 (+10 corazones, all perks)

### CLASSES
SURVIVOR (default): Machete + 3 torches + bread, +10% XP
MEDIC: 2 Cure Potions + bandages, +50% cure, /class skill primeros auxilios
SOLDIER: Iron Sword + Chainmail, +15% damage, /class skill rallying cry
SCAVENGER: 3 torches + rope, +25% drop, /class skill sense loot
ENGINEER: 5 iron ingots + torches, /class skill fortify

### ECONOMY
Moneda: ZP (Zombie Pesos)
3 Traders:
- MERCADER: comida, materials, tools
- ARSENALERO: weapons, armor
- MEDICO: potions, cures

### TRADERS LOCATIONS
Mercader: -10, 64, 200
Arsenalero: -25, 64, 180
Medico: -15, 64, 210

### KITS PURCHASABLES
STARKIT: 500 ZP - Diamond Sword, Diamond armor, 3 gapples, 16 torches
MEDICALKIT: 300 ZP - 3 cure potions, 5 bandages, 2 regen, 1 gapple
EXPLORERKIT: 400 ZP - Sword+shield, 32 torches, 3 bread, compass, map
ZOMBIEHUNTERKIT: 600 ZP - Crossbow+arrows, iron sword, 2 harm potions, 8 torches

### STRUCTURES
ruined_house_1: 10% chance, medical loot, 1-2 zombies
ruined_hospital: 3% chance, medical loot, 3-5 zombies (high density)
military_checkpoint: 5% chance, military loot, 2-4 ZombiSoldado
gas_station: 8% chance, fuel+snacks, 1-2 zombies
bunker_entrance: 2% chance, rare loot

### LOOT TABLES
common_loot: ROTLEN_FLESH, BONE, IRON_NUGGET, BREAD, TORCH
uncommon_loot: IRON_INGOT, CHAINMAIL, GOLD_INGOT, DIAMOND, zm_adrenalina
rare_loot: DIAMONDx1-3, EMERALD, ENCHANTED_BOOK, zm_fragmento_arma, zm_antidoto_fragmento
epic_loot: DIAMONDx3-5, ENCHANTED_GOLDEN_APPLE, zm_antidoto_completo, zm_arma_patron

### ACHIEVEMENTS
primer_zombie: +50 XP
mato_100_zombies: Machete mejorado
mato_mutante: +200 XP
mato_patron: ArmaLegendaria + 2000 ZP
dias_sobrevivido_30: KIT MEDICO
cures_10_infections: Titulo "Medico"

### QUESTS
DAILY:
- Cazador: 10 zombies → 100 ZP + 50 XP
- Coleccionista: 5 tipos loot → 150 ZP
- Medico: cura 1 jugador → Cure Potion gratis
- Limpieza: zona 30+ zombies → 200 ZP

WEEKLY:
- Horda: 100 zombies → 500 ZP + Rare loot
- Superviviente: 7 dias sin morir → +2 corazon permanente
- Arquitecto: construye base → 1000 ZP

### SPECS (nivel 50+)
APOCALYPSE SPECIALIST: +50% damage, counter-attack, immune knockback
PLAGUE CARRIER: Plague mode con毒雾 aura, puede curar
DARK ARTS: Raise zombies como slaves, 3 minions max, 50% XP bonus
FIELD MEDIC: -50% cure cost, AOE heal, bonus ZP

### IMPLEMENTATION ORDER
1. ProgressionManager + leveling + perks
2. ClassManager + PlayerClass
3. EconomyManager + traders
4. QuestManager + Quest
5. Structures verify
6. Anti-cheat
7. Stats + scoreboard
8. Backups
9. Advanced specs

### FILES TO CREATE
ProgressionManager.java
Perk.java
TraderNPC.java
QuestManager.java
Quest.java
ClassManager.java
PlayerClass.java
EconomyManager.java
ZPCurrency.java
AuctionHouse.java
AntiCheatListener.java
StatsManager.java
ScoreboardManager.java
MenuManager.java

### CONFIGS TO CREATE
progression.yml
quests.yml
traders.yml
classes.yml
perks.yml
loot_tables.yml

### EXISTING TO VERIFY
StructureManager.java (verify working)
ZonaMuerta.java (integrate all)
CommandHandler.java (add commands)
Infected.java (enhance)
MythicBridge.java (keep)