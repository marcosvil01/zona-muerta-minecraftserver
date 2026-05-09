package com.zonamuerta.plugin.structures;

import com.zonamuerta.plugin.ZonaMuerta;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.EntityType;

public class RuinedHouse2 {
    private static final Random random = new Random();

    public static void generate(ZonaMuerta plugin, Location origin) {
        World world = origin.getWorld();
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();
        RuinedHouse2.generateFoundation(world, baseX, baseY, baseZ);
        RuinedHouse2.generateShell(world, baseX, baseY, baseZ);
        RuinedHouse2.carveInterior(world, baseX, baseY, baseZ);
        RuinedHouse2.generateSecondFloor(world, baseX, baseY, baseZ);
        RuinedHouse2.generateRoof(world, baseX, baseY, baseZ);
        RuinedHouse2.addBedOnTopFloor(world, baseX, baseY, baseZ);
        RuinedHouse2.addCobwebs(world, baseX, baseY, baseZ);
        RuinedHouse2.addVines(world, baseX, baseY, baseZ);
        RuinedHouse2.addDamage(world, baseX, baseY, baseZ);
        RuinedHouse2.generateGroundSpawner(world, baseX, baseY, baseZ);
    }

    private static void generateFoundation(World world, int baseX, int baseY, int baseZ) {
        for (int x = 0; x < 9; ++x) {
            for (int z = 0; z < 9; ++z) {
                RuinedHouse2.setBlock(world, baseX + x, baseY - 1, baseZ + z, Material.COBBLESTONE);
                RuinedHouse2.setBlock(world, baseX + x, baseY, baseZ + z, Material.OAK_PLANKS);
            }
        }
    }

    private static void generateShell(World world, int baseX, int baseY, int baseZ) {
        int y;
        for (y = 1; y <= 7; ++y) {
            for (int x = 0; x < 9; ++x) {
                RuinedHouse2.setBlock(world, baseX + x, baseY + y, baseZ, Material.OAK_PLANKS);
                RuinedHouse2.setBlock(world, baseX + x, baseY + y, baseZ + 8, Material.OAK_PLANKS);
            }
            for (int z = 0; z < 9; ++z) {
                RuinedHouse2.setBlock(world, baseX, baseY + y, baseZ + z, Material.OAK_PLANKS);
                RuinedHouse2.setBlock(world, baseX + 8, baseY + y, baseZ + z, Material.OAK_PLANKS);
            }
        }
        for (y = 1; y <= 7; ++y) {
            RuinedHouse2.setBlock(world, baseX, baseY + y, baseZ, Material.OAK_LOG);
            RuinedHouse2.setBlock(world, baseX + 8, baseY + y, baseZ, Material.OAK_LOG);
            RuinedHouse2.setBlock(world, baseX, baseY + y, baseZ + 8, Material.OAK_LOG);
            RuinedHouse2.setBlock(world, baseX + 8, baseY + y, baseZ + 8, Material.OAK_LOG);
        }
        RuinedHouse2.setBlock(world, baseX + 4, baseY + 1, baseZ, Material.AIR);
        RuinedHouse2.setBlock(world, baseX + 4, baseY + 2, baseZ, Material.AIR);
    }

    private static void carveInterior(World world, int baseX, int baseY, int baseZ) {
        for (int x = 1; x < 8; ++x) {
            for (int z = 1; z < 8; ++z) {
                for (int y = 1; y <= 7; ++y) {
                    RuinedHouse2.setBlock(world, baseX + x, baseY + y, baseZ + z, Material.AIR);
                }
            }
        }
    }

    private static void generateSecondFloor(World world, int baseX, int baseY, int baseZ) {
        for (int x = 1; x < 8; ++x) {
            for (int z = 1; z < 8; ++z) {
                RuinedHouse2.setBlock(world, baseX + x, baseY + 4, baseZ + z, Material.OAK_PLANKS);
            }
        }
        for (int y = 1; y <= 4; ++y) {
            RuinedHouse2.setBlock(world, baseX + 2, baseY + y, baseZ + 2, Material.OAK_STAIRS);
        }
        RuinedHouse2.setBlock(world, baseX + 2, baseY + 4, baseZ + 2, Material.AIR);
        RuinedHouse2.setBlock(world, baseX + 2, baseY + 4, baseZ + 3, Material.AIR);
        RuinedHouse2.setBlock(world, baseX + 2, baseY + 2, baseZ + 8, Material.GLASS_PANE);
        RuinedHouse2.setBlock(world, baseX + 6, baseY + 2, baseZ + 8, Material.GLASS_PANE);
        RuinedHouse2.setBlock(world, baseX + 2, baseY + 6, baseZ + 8, Material.GLASS_PANE);
        RuinedHouse2.setBlock(world, baseX + 6, baseY + 6, baseZ + 8, Material.GLASS_PANE);
    }

    private static void generateRoof(World world, int baseX, int baseY, int baseZ) {
        for (int x = 0; x < 9; ++x) {
            for (int z = 0; z < 9; ++z) {
                RuinedHouse2.setBlock(world, baseX + x, baseY + 8, baseZ + z, Material.OAK_PLANKS);
            }
        }
    }

    private static void addBedOnTopFloor(World world, int baseX, int baseY, int baseZ) {
        RuinedHouse2.setBlock(world, baseX + 6, baseY + 5, baseZ + 2, Material.RED_BED);
        RuinedHouse2.setBlock(world, baseX + 6, baseY + 5, baseZ + 3, Material.RED_BED);
        Block footBlock = world.getBlockAt(baseX + 6, baseY + 5, baseZ + 2);
        Block headBlock = world.getBlockAt(baseX + 6, baseY + 5, baseZ + 3);
        BlockData footData = footBlock.getBlockData();
        BlockData headData = headBlock.getBlockData();
        if (footData instanceof Bed && headData instanceof Bed) {
            Bed foot = (Bed)footData;
            Bed head = (Bed)headData;
            foot.setPart(Bed.Part.FOOT);
            head.setPart(Bed.Part.HEAD);
            footBlock.setBlockData((BlockData)foot);
            headBlock.setBlockData((BlockData)head);
        }
    }

    private static void addCobwebs(World world, int baseX, int baseY, int baseZ) {
        int[][] cobwebPositions;
        for (int[] pos : cobwebPositions = new int[][]{{1, 3, 1}, {7, 3, 1}, {1, 3, 7}, {7, 3, 7}, {2, 6, 2}, {6, 6, 2}, {2, 6, 6}, {6, 6, 6}, {4, 6, 4}, {3, 2, 5}, {5, 2, 3}}) {
            if (!(random.nextDouble() < 0.75)) continue;
            RuinedHouse2.setBlock(world, baseX + pos[0], baseY + pos[1], baseZ + pos[2], Material.COBWEB);
        }
    }

    private static void addVines(World world, int baseX, int baseY, int baseZ) {
        for (int i = 0; i < 10; ++i) {
            int side = random.nextInt(4);
            int local = 1 + random.nextInt(7);
            int y = baseY + 2 + random.nextInt(5);
            int x = baseX;
            int z = baseZ;
            switch (side) {
                case 0: {
                    x = baseX;
                    z = baseZ + local;
                    break;
                }
                case 1: {
                    x = baseX + 8;
                    z = baseZ + local;
                    break;
                }
                case 2: {
                    x = baseX + local;
                    z = baseZ;
                    break;
                }
                case 3: {
                    x = baseX + local;
                    z = baseZ + 8;
                }
            }
            if (world.getBlockAt(x, y, z).getType() != Material.OAK_PLANKS) continue;
            int vx = x;
            int vz = z;
            if (side == 0) {
                vx = x - 1;
            } else if (side == 1) {
                vx = x + 1;
            } else {
                vz = side == 2 ? z - 1 : z + 1;
            }
            for (int len = 0; len < 3; ++len) {
                if (!(random.nextDouble() < 0.85)) continue;
                RuinedHouse2.setBlock(world, vx, y - len, vz, Material.VINE);
            }
        }
    }

    private static void addDamage(World world, int baseX, int baseY, int baseZ) {
        int i;
        for (i = 0; i < 14; ++i) {
            int wallSide = random.nextInt(4);
            int y = baseY + 1 + random.nextInt(7);
            int local = 1 + random.nextInt(7);
            int x = baseX;
            int z = baseZ;
            switch (wallSide) {
                case 0: {
                    x = baseX;
                    z = baseZ + local;
                    break;
                }
                case 1: {
                    x = baseX + 8;
                    z = baseZ + local;
                    break;
                }
                case 2: {
                    x = baseX + local;
                    z = baseZ;
                    break;
                }
                case 3: {
                    x = baseX + local;
                    z = baseZ + 8;
                }
            }
            RuinedHouse2.setBlock(world, x, y, z, Material.AIR);
        }
        for (i = 0; i < 6; ++i) {
            int x = baseX + 1 + random.nextInt(7);
            int z = baseZ + 1 + random.nextInt(7);
            RuinedHouse2.setBlock(world, x, baseY + 8, z, Material.AIR);
        }
    }

    private static void generateGroundSpawner(World world, int baseX, int baseY, int baseZ) {
        int spawnerX = baseX + 4;
        int spawnerY = baseY - 2;
        int spawnerZ = baseZ + 4;
        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                for (int y = -1; y <= 1; ++y) {
                    RuinedHouse2.setBlock(world, spawnerX + x, spawnerY + y, spawnerZ + z, Material.MOSSY_COBBLESTONE);
                }
            }
        }
        RuinedHouse2.setBlock(world, spawnerX, spawnerY, spawnerZ, Material.SPAWNER);
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

    private static void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }
}

