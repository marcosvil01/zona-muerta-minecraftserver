package com.zonamuerta.plugin.structures;

import com.zonamuerta.plugin.ZonaMuerta;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

public class RuinedHouse1 {
    private static final Random random = new Random();

    public static void generate(ZonaMuerta plugin, Location origin) {
        World world = origin.getWorld();
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();
        RuinedHouse1.generateFoundation(world, baseX, baseY, baseZ);
        RuinedHouse1.generateWalls(world, baseX, baseY, baseZ);
        RuinedHouse1.generateRoof(world, baseX, baseY, baseZ);
        RuinedHouse1.generateInterior(world, baseX, baseY, baseZ);
        RuinedHouse1.generateSpawner(world, baseX, baseY, baseZ);
        RuinedHouse1.addCobwebs(world, baseX, baseY, baseZ);
        RuinedHouse1.addDamage(world, baseX, baseY, baseZ);
    }

    private static void generateFoundation(World world, int baseX, int baseY, int baseZ) {
        for (int x = 0; x < 7; ++x) {
            for (int z = 0; z < 7; ++z) {
                RuinedHouse1.setBlock(world, baseX + x, baseY - 1, baseZ + z, Material.COBBLESTONE);
                RuinedHouse1.setBlock(world, baseX + x, baseY, baseZ + z, Material.OAK_PLANKS);
            }
        }
    }

    private static void generateWalls(World world, int baseX, int baseY, int baseZ) {
        for (int y = 1; y <= 3; ++y) {
            for (int x = 0; x < 7; ++x) {
                RuinedHouse1.setBlock(world, baseX + x, baseY + y, baseZ, Material.OAK_PLANKS);
                RuinedHouse1.setBlock(world, baseX + x, baseY + y, baseZ + 6, Material.OAK_PLANKS);
            }
            for (int z = 0; z < 7; ++z) {
                RuinedHouse1.setBlock(world, baseX, baseY + y, baseZ + z, Material.OAK_PLANKS);
                RuinedHouse1.setBlock(world, baseX + 6, baseY + y, baseZ + z, Material.OAK_PLANKS);
            }
        }
        RuinedHouse1.setBlock(world, baseX, baseY + 1, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 1, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX, baseY + 1, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 1, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX, baseY + 2, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 2, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX, baseY + 2, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 2, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX, baseY + 3, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 3, baseZ, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX, baseY + 3, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 3, baseZ + 6, Material.OAK_LOG);
        RuinedHouse1.setBlock(world, baseX + 3, baseY + 1, baseZ, Material.AIR);
        RuinedHouse1.setBlock(world, baseX + 3, baseY + 2, baseZ, Material.AIR);
        RuinedHouse1.setBlock(world, baseX + 2, baseY + 2, baseZ + 6, Material.GLASS_PANE);
        RuinedHouse1.setBlock(world, baseX + 4, baseY + 2, baseZ + 6, Material.GLASS_PANE);
        RuinedHouse1.setBlock(world, baseX, baseY + 2, baseZ + 3, Material.GLASS_PANE);
        RuinedHouse1.setBlock(world, baseX + 6, baseY + 2, baseZ + 3, Material.GLASS_PANE);
    }

    private static void generateRoof(World world, int baseX, int baseY, int baseZ) {
        int x;
        for (x = 0; x < 7; ++x) {
            for (int z = 0; z < 7; ++z) {
                RuinedHouse1.setBlock(world, baseX + x, baseY + 4, baseZ + z, Material.OAK_PLANKS);
            }
        }
        for (x = -1; x < 8; ++x) {
            RuinedHouse1.setBlock(world, baseX + x, baseY + 4, baseZ - 1, Material.OAK_STAIRS);
            RuinedHouse1.setBlock(world, baseX + x, baseY + 4, baseZ + 7, Material.OAK_STAIRS);
        }
    }

    private static void generateInterior(World world, int baseX, int baseY, int baseZ) {
        for (int x = 1; x < 6; ++x) {
            for (int z = 1; z < 6; ++z) {
                for (int y = 1; y <= 3; ++y) {
                    RuinedHouse1.setBlock(world, baseX + x, baseY + y, baseZ + z, Material.AIR);
                }
            }
        }
        RuinedHouse1.setBlock(world, baseX + 5, baseY + 1, baseZ + 5, Material.CRAFTING_TABLE);
        RuinedHouse1.setBlock(world, baseX + 5, baseY + 1, baseZ + 4, Material.CHEST);
        RuinedHouse1.setBlock(world, baseX + 1, baseY + 1, baseZ + 5, Material.OAK_PLANKS);
        RuinedHouse1.setBlock(world, baseX + 1, baseY + 2, baseZ + 5, Material.TORCH);
    }

    private static void generateSpawner(World world, int baseX, int baseY, int baseZ) {
        int spawnerX = baseX + 3;
        int spawnerY = baseY - 2;
        int spawnerZ = baseZ + 3;
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                for (int y = -1; y <= 1; ++y) {
                    RuinedHouse1.setBlock(world, spawnerX + x, spawnerY + y, spawnerZ + z, Material.MOSSY_COBBLESTONE);
                }
            }
        }
        RuinedHouse1.setBlock(world, spawnerX, spawnerY, spawnerZ, Material.SPAWNER);
        Block spawnerBlock = world.getBlockAt(spawnerX, spawnerY, spawnerZ);
        if (spawnerBlock.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner)spawnerBlock.getState();
            spawner.setSpawnedType(EntityType.ZOMBIE);
            spawner.setDelay(200);
            spawner.setMinSpawnDelay(200);
            spawner.setMaxSpawnDelay(800);
            spawner.setSpawnCount(2);
            spawner.setMaxNearbyEntities(6);
            spawner.setRequiredPlayerRange(16);
            spawner.setSpawnRange(4);
            spawner.update();
        }
    }

    private static void addCobwebs(World world, int baseX, int baseY, int baseZ) {
        int[][] cobwebPositions;
        for (int[] pos : cobwebPositions = new int[][]{{1, 3, 1}, {5, 3, 1}, {1, 3, 5}, {5, 3, 5}, {3, 3, 3}, {2, 2, 2}, {4, 2, 4}}) {
            if (!(random.nextDouble() < 0.7)) continue;
            RuinedHouse1.setBlock(world, baseX + pos[0], baseY + pos[1], baseZ + pos[2], Material.COBWEB);
        }
    }

    private static void addDamage(World world, int baseX, int baseY, int baseZ) {
        int holeZ;
        int holeX;
        if (random.nextDouble() < 0.6) {
            holeX = 2 + random.nextInt(3);
            holeZ = random.nextBoolean() ? 0 : 6;
            RuinedHouse1.setBlock(world, baseX + holeX, baseY + 2, baseZ + holeZ, Material.AIR);
        }
        if (random.nextDouble() < 0.6) {
            int holeZ2 = 2 + random.nextInt(3);
            int holeX2 = random.nextBoolean() ? 0 : 6;
            RuinedHouse1.setBlock(world, baseX + holeX2, baseY + 2, baseZ + holeZ2, Material.AIR);
        }
        if (random.nextDouble() < 0.5) {
            holeX = 1 + random.nextInt(5);
            holeZ = 1 + random.nextInt(5);
            RuinedHouse1.setBlock(world, baseX + holeX, baseY + 4, baseZ + holeZ, Material.AIR);
        }
        if (random.nextDouble() < 0.4) {
            holeX = 1 + random.nextInt(5);
            holeZ = 1 + random.nextInt(5);
            RuinedHouse1.setBlock(world, baseX + holeX, baseY + 4, baseZ + holeZ, Material.AIR);
            RuinedHouse1.setBlock(world, baseX + holeX + 1, baseY + 4, baseZ + holeZ, Material.AIR);
        }
        if (random.nextDouble() < 0.3) {
            RuinedHouse1.setBlock(world, baseX + 1, baseY + 3, baseZ, Material.AIR);
        }
        if (random.nextDouble() < 0.3) {
            RuinedHouse1.setBlock(world, baseX + 5, baseY + 3, baseZ + 6, Material.AIR);
        }
        if (random.nextDouble() < 0.5) {
            int vineX = random.nextBoolean() ? 0 : 6;
            int vineZ = 1 + random.nextInt(5);
            for (int y = 1; y <= 2; ++y) {
                Block adjacent;
                if (!(random.nextDouble() < 0.7) || (adjacent = world.getBlockAt(baseX + vineX, baseY + y, baseZ + vineZ)).getType() != Material.OAK_PLANKS) continue;
                int checkX = vineX == 0 ? -1 : 1;
                RuinedHouse1.setBlock(world, baseX + vineX + checkX, baseY + y, baseZ + vineZ, Material.VINE);
            }
        }
        if (random.nextDouble() < 0.4) {
            int mossX = 1 + random.nextInt(5);
            int mossZ = 1 + random.nextInt(5);
            RuinedHouse1.setBlock(world, baseX + mossX, baseY, baseZ + mossZ, Material.MOSSY_COBBLESTONE);
        }
    }

    private static void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }
}

