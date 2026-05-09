package com.zonamuerta.plugin.structures;

import com.zonamuerta.plugin.ZonaMuerta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class RuinedHospital {
    private static final Random random = new Random();

    public static void generate(ZonaMuerta plugin, Location origin) {
        World world = origin.getWorld();
        int bx = origin.getBlockX();
        int by = origin.getBlockY();
        int bz = origin.getBlockZ();
        RuinedHospital.generateFoundation(world, bx, by, bz);
        RuinedHospital.generateOuterWalls(world, bx, by, bz);
        RuinedHospital.generateRoof(world, bx, by, bz);
        RuinedHospital.generateFloorSlab(world, bx, by, bz);
        RuinedHospital.carveInterior(world, bx, by, bz);
        RuinedHospital.generateInteriorWalls(world, bx, by, bz);
        RuinedHospital.generateDoorOpenings(world, bx, by, bz);
        RuinedHospital.generateStaircase(world, bx, by, bz);
        RuinedHospital.addBeds(world, bx, by, bz);
        RuinedHospital.addBarrels(world, bx, by, bz);
        RuinedHospital.addBlood(world, bx, by, bz);
        RuinedHospital.addSign(world, bx, by, bz);
        RuinedHospital.addSpawners(world, bx, by, bz);
        RuinedHospital.addCobwebs(world, bx, by, bz);
        RuinedHospital.addVines(world, bx, by, bz);
        RuinedHospital.addDamage(world, bx, by, bz);
    }

    private static void generateFoundation(World world, int bx, int by, int bz) {
        for (int x = 0; x <= 20; ++x) {
            for (int z = 0; z <= 14; ++z) {
                RuinedHospital.setBlock(world, bx + x, by - 1, bz + z, Material.COBBLESTONE);
                RuinedHospital.setBlock(world, bx + x, by, bz + z, Material.WHITE_CONCRETE);
            }
        }
    }

    private static void generateOuterWalls(World world, int bx, int by, int bz) {
        for (int y = 1; y <= 7; ++y) {
            if (y == 4) continue;
            for (int x = 0; x <= 20; ++x) {
                RuinedHospital.setBlock(world, bx + x, by + y, bz, Material.WHITE_CONCRETE);
                RuinedHospital.setBlock(world, bx + x, by + y, bz + 14, Material.WHITE_CONCRETE);
            }
            for (int z = 0; z <= 14; ++z) {
                RuinedHospital.setBlock(world, bx, by + y, bz + z, Material.WHITE_CONCRETE);
                RuinedHospital.setBlock(world, bx + 20, by + y, bz + z, Material.WHITE_CONCRETE);
            }
        }
        RuinedHospital.setBlock(world, bx + 10, by + 1, bz, Material.AIR);
        RuinedHospital.setBlock(world, bx + 10, by + 2, bz, Material.AIR);
    }

    private static void generateRoof(World world, int bx, int by, int bz) {
        for (int x = 0; x <= 20; ++x) {
            for (int z = 0; z <= 14; ++z) {
                RuinedHospital.setBlock(world, bx + x, by + 8, bz + z, Material.WHITE_CONCRETE);
            }
        }
    }

    private static void generateFloorSlab(World world, int bx, int by, int bz) {
        for (int x = 0; x <= 20; ++x) {
            for (int z = 0; z <= 14; ++z) {
                RuinedHospital.setBlock(world, bx + x, by + 4, bz + z, Material.WHITE_CONCRETE);
            }
        }
    }

    private static void carveInterior(World world, int bx, int by, int bz) {
        for (int x = 1; x <= 19; ++x) {
            for (int z = 1; z <= 13; ++z) {
                int y;
                for (y = 1; y <= 3; ++y) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + z, Material.AIR);
                }
                for (y = 5; y <= 7; ++y) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + z, Material.AIR);
                }
            }
        }
    }

    private static void generateInteriorWalls(World world, int bx, int by, int bz) {
        int[] nArray = new int[]{1, 5};
        int n = nArray.length;
        for (int i = 0; i < n; ++i) {
            int x;
            int floorBase;
            int y;
            for (y = floorBase = nArray[i]; y <= floorBase + 2; ++y) {
                for (int z = 1; z <= 13; ++z) {
                    RuinedHospital.setBlock(world, bx + 8, by + y, bz + z, Material.WHITE_CONCRETE);
                    RuinedHospital.setBlock(world, bx + 12, by + y, bz + z, Material.WHITE_CONCRETE);
                }
            }
            for (y = floorBase; y <= floorBase + 2; ++y) {
                for (x = 1; x <= 7; ++x) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + 5, Material.WHITE_CONCRETE);
                }
            }
            for (y = floorBase; y <= floorBase + 2; ++y) {
                for (x = 1; x <= 7; ++x) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + 10, Material.WHITE_CONCRETE);
                }
            }
            for (y = floorBase; y <= floorBase + 2; ++y) {
                for (x = 13; x <= 19; ++x) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + 5, Material.WHITE_CONCRETE);
                }
            }
            for (y = floorBase; y <= floorBase + 2; ++y) {
                for (x = 13; x <= 19; ++x) {
                    RuinedHospital.setBlock(world, bx + x, by + y, bz + 10, Material.WHITE_CONCRETE);
                }
            }
        }
    }

    private static void generateDoorOpenings(World world, int bx, int by, int bz) {
        for (int floorBase : new int[]{1, 5}) {
            for (int dz : new int[]{3, 8, 12}) {
                RuinedHospital.setBlock(world, bx + 8, by + floorBase, bz + dz, Material.AIR);
                RuinedHospital.setBlock(world, bx + 8, by + floorBase + 1, bz + dz, Material.AIR);
            }
            for (int dz : new int[]{3, 8, 12}) {
                RuinedHospital.setBlock(world, bx + 12, by + floorBase, bz + dz, Material.AIR);
                RuinedHospital.setBlock(world, bx + 12, by + floorBase + 1, bz + dz, Material.AIR);
            }
        }
    }

    private static void generateStaircase(World world, int bx, int by, int bz) {
        RuinedHospital.setBlock(world, bx + 9, by + 1, bz + 13, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 10, by + 1, bz + 13, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 9, by + 2, bz + 12, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 10, by + 2, bz + 12, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 9, by + 3, bz + 11, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 10, by + 3, bz + 11, Material.OAK_STAIRS);
        RuinedHospital.setBlock(world, bx + 9, by + 4, bz + 11, Material.AIR);
        RuinedHospital.setBlock(world, bx + 10, by + 4, bz + 11, Material.AIR);
        RuinedHospital.setBlock(world, bx + 9, by + 4, bz + 12, Material.AIR);
        RuinedHospital.setBlock(world, bx + 10, by + 4, bz + 12, Material.AIR);
        RuinedHospital.setBlock(world, bx + 9, by + 4, bz + 13, Material.AIR);
        RuinedHospital.setBlock(world, bx + 10, by + 4, bz + 13, Material.AIR);
        for (int z = 11; z <= 13; ++z) {
            for (int y = 2; y <= 3; ++y) {
                RuinedHospital.setBlock(world, bx + 9, by + y, bz + z, Material.AIR);
                RuinedHospital.setBlock(world, bx + 10, by + y, bz + z, Material.AIR);
            }
        }
        RuinedHospital.setBlock(world, bx + 9, by + 3, bz + 13, Material.AIR);
        RuinedHospital.setBlock(world, bx + 10, by + 3, bz + 13, Material.AIR);
    }

    private static void addBeds(World world, int bx, int by, int bz) {
        RuinedHospital.placeBed(world, bx + 2, by + 1, bz + 2, bz + 3);
        RuinedHospital.placeBed(world, bx + 2, by + 1, bz + 7, bz + 8);
        RuinedHospital.placeBed(world, bx + 17, by + 1, bz + 2, bz + 3);
        RuinedHospital.placeBed(world, bx + 17, by + 1, bz + 7, bz + 8);
        RuinedHospital.placeBed(world, bx + 2, by + 5, bz + 2, bz + 3);
        RuinedHospital.placeBed(world, bx + 2, by + 5, bz + 7, bz + 8);
        RuinedHospital.placeBed(world, bx + 2, by + 5, bz + 12, bz + 13);
        RuinedHospital.placeBed(world, bx + 17, by + 5, bz + 2, bz + 3);
        RuinedHospital.placeBed(world, bx + 17, by + 5, bz + 7, bz + 8);
        RuinedHospital.placeBed(world, bx + 17, by + 5, bz + 12, bz + 13);
    }

    private static void placeBed(World world, int x, int y, int footZ, int headZ) {
        RuinedHospital.setBlock(world, x, y, footZ, Material.WHITE_BED);
        RuinedHospital.setBlock(world, x, y, headZ, Material.WHITE_BED);
        Block footBlock = world.getBlockAt(x, y, footZ);
        Block headBlock = world.getBlockAt(x, y, headZ);
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

    private static void addBarrels(World world, int bx, int by, int bz) {
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 1, bz + 1, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 1, bz + 6, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 1, bz + 1, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 1, bz + 6, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 2, by + 1, bz + 11, true);
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 1, bz + 12, true);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 1, bz + 11, true);
        RuinedHospital.placeBarrelWithLoot(world, bx + 18, by + 1, bz + 12, true);
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 5, bz + 1, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 5, bz + 6, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 4, by + 5, bz + 11, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 5, bz + 1, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 5, bz + 6, false);
        RuinedHospital.placeBarrelWithLoot(world, bx + 16, by + 5, bz + 11, false);
    }

    private static void placeBarrelWithLoot(World world, int x, int y, int z, boolean isStorageRoom) {
        RuinedHospital.setBlock(world, x, y, z, Material.BARREL);
        Block barrelBlock = world.getBlockAt(x, y, z);
        if (barrelBlock.getState() instanceof Barrel) {
            Barrel barrel = (Barrel)barrelBlock.getState();
            Inventory inv = barrel.getSnapshotInventory();
            inv.clear();
            ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            if (isStorageRoom) {
                RuinedHospital.addRandomAmount(items, Material.IRON_INGOT, 0, 8);
                RuinedHospital.addRandomAmount(items, Material.DIAMOND, 0, 3);
                int mendingCount = random.nextInt(2);
                if (mendingCount > 0) {
                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta)book.getItemMeta();
                    meta.addStoredEnchant(Enchantment.MENDING, 1, true);
                    book.setItemMeta((ItemMeta)meta);
                    items.add(book);
                }
                RuinedHospital.addRandomAmount(items, Material.COBWEB, 0, 8);
            } else {
                RuinedHospital.addRandomAmount(items, Material.ROTTEN_FLESH, 0, 4);
                RuinedHospital.addRandomAmount(items, Material.COBWEB, 0, 5);
                RuinedHospital.addRandomAmount(items, Material.REDSTONE, 0, 3);
            }
            ArrayList<Integer> slots = new ArrayList<Integer>();
            for (int i = 0; i < 27; ++i) {
                slots.add(i);
            }
            Collections.shuffle(slots, random);
            int slotIndex = 0;
            for (ItemStack item : items) {
                if (slotIndex >= slots.size()) continue;
                inv.setItem(((Integer)slots.get(slotIndex)).intValue(), item);
                ++slotIndex;
            }
            barrel.update();
        }
    }

    private static void addRandomAmount(List<ItemStack> items, Material material, int min, int max) {
        int amount = random.nextInt(max - min + 1) + min;
        if (amount > 0) {
            items.add(new ItemStack(material, amount));
        }
    }

    private static void addBlood(World world, int bx, int by, int bz) {
        Block at;
        Block below;
        int x;
        boolean left;
        Block at2;
        int z;
        int x2;
        int i;
        for (i = 0; i < 8; ++i) {
            x2 = bx + 9 + random.nextInt(3);
            z = bz + 1 + random.nextInt(13);
            Block below2 = world.getBlockAt(x2, by, z);
            at2 = world.getBlockAt(x2, by + 1, z);
            if (below2.getType() != Material.WHITE_CONCRETE || at2.getType() != Material.AIR) continue;
            RuinedHospital.setBlock(world, x2, by + 1, z, Material.REDSTONE_WIRE);
        }
        for (i = 0; i < 6; ++i) {
            left = random.nextBoolean();
            x = left ? bx + 1 + random.nextInt(7) : bx + 13 + random.nextInt(7);
            int z2 = bz + 1 + random.nextInt(13);
            below = world.getBlockAt(x, by, z2);
            at = world.getBlockAt(x, by + 1, z2);
            if (below.getType() != Material.WHITE_CONCRETE || at.getType() != Material.AIR) continue;
            RuinedHospital.setBlock(world, x, by + 1, z2, Material.REDSTONE_WIRE);
        }
        for (i = 0; i < 6; ++i) {
            x2 = bx + 9 + random.nextInt(3);
            z = bz + 1 + random.nextInt(13);
            Block below3 = world.getBlockAt(x2, by + 4, z);
            at2 = world.getBlockAt(x2, by + 5, z);
            if (below3.getType() != Material.WHITE_CONCRETE || at2.getType() != Material.AIR) continue;
            RuinedHospital.setBlock(world, x2, by + 5, z, Material.REDSTONE_WIRE);
        }
        for (i = 0; i < 4; ++i) {
            left = random.nextBoolean();
            x = left ? bx + 1 + random.nextInt(7) : bx + 13 + random.nextInt(7);
            int z3 = bz + 1 + random.nextInt(13);
            below = world.getBlockAt(x, by + 4, z3);
            at = world.getBlockAt(x, by + 5, z3);
            if (below.getType() != Material.WHITE_CONCRETE || at.getType() != Material.AIR) continue;
            RuinedHospital.setBlock(world, x, by + 5, z3, Material.REDSTONE_WIRE);
        }
    }

    private static void addSign(World world, int bx, int by, int bz) {
        int x;
        int signY = by + 9;
        for (x = 4; x <= 16; ++x) {
            for (int dy = 0; dy <= 2; ++dy) {
                RuinedHospital.setBlock(world, bx + x, signY + dy, bz, Material.WHITE_CONCRETE);
            }
        }
        for (x = 4; x <= 16; ++x) {
            RuinedHospital.setBlock(world, bx + x, signY - 1, bz, Material.GRAY_CONCRETE);
            RuinedHospital.setBlock(world, bx + x, signY + 3, bz, Material.GRAY_CONCRETE);
        }
        for (int dy = -1; dy <= 3; ++dy) {
            RuinedHospital.setBlock(world, bx + 3, signY + dy, bz, Material.GRAY_CONCRETE);
            RuinedHospital.setBlock(world, bx + 17, signY + dy, bz, Material.GRAY_CONCRETE);
        }
        RuinedHospital.setRedLetterColumn(world, bx + 5, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 6, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 7, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 8, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 9, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 10, signY, bz);
        RuinedHospital.setRedLetterColumn(world, bx + 12, signY, bz);
        RuinedHospital.setBlock(world, bx + 15, signY + 1, bz, Material.RED_CONCRETE);
        RuinedHospital.setBlock(world, bx + 14, signY + 1, bz, Material.RED_CONCRETE);
        RuinedHospital.setBlock(world, bx + 16, signY + 1, bz, Material.RED_CONCRETE);
        RuinedHospital.setBlock(world, bx + 15, signY, bz, Material.RED_CONCRETE);
        RuinedHospital.setBlock(world, bx + 15, signY + 2, bz, Material.RED_CONCRETE);
    }

    private static void setRedLetterColumn(World world, int x, int signY, int z) {
        for (int dy = 0; dy <= 2; ++dy) {
            RuinedHospital.setBlock(world, x, signY + dy, z, Material.RED_CONCRETE);
        }
    }

    private static void addSpawners(World world, int bx, int by, int bz) {
        RuinedHospital.placeSpawner(world, bx + 4, by - 2, bz + 7);
        RuinedHospital.placeSpawner(world, bx + 16, by - 2, bz + 7);
    }

    private static void placeSpawner(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dy = -1; dy <= 1; ++dy) {
                    RuinedHospital.setBlock(world, x + dx, y + dy, z + dz, Material.MOSSY_COBBLESTONE);
                }
            }
        }
        RuinedHospital.setBlock(world, x, y, z, Material.SPAWNER);
        Block spawnerBlock = world.getBlockAt(x, y, z);
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

    private static void addCobwebs(World world, int bx, int by, int bz) {
        int[][] positions;
        for (int[] pos : positions = new int[][]{{1, 3, 1}, {7, 3, 1}, {1, 3, 4}, {7, 3, 4}, {1, 3, 9}, {7, 3, 9}, {13, 3, 1}, {19, 3, 1}, {13, 3, 4}, {19, 3, 9}, {10, 3, 5}, {1, 7, 1}, {7, 7, 1}, {1, 7, 9}, {19, 7, 1}, {13, 7, 4}, {19, 7, 9}, {10, 7, 7}}) {
            if (!(random.nextDouble() < 0.65)) continue;
            RuinedHospital.setBlock(world, bx + pos[0], by + pos[1], bz + pos[2], Material.COBWEB);
        }
    }

    private static void addVines(World world, int bx, int by, int bz) {
        for (int i = 0; i < 14; ++i) {
            int vx;
            int wz;
            int wx;
            int side = random.nextInt(4);
            int local = 1 + random.nextInt(13);
            int y = by + 2 + random.nextInt(6);
            int vz = switch (side) {
                case 0 -> {
                    wx = bx;
                    wz = bz + local;
                    vx = bx - 1;
                    yield wz;
                }
                case 1 -> {
                    wx = bx + 20;
                    wz = bz + local;
                    vx = bx + 21;
                    yield wz;
                }
                case 2 -> {
                    wx = bx + 1 + random.nextInt(19);
                    wz = bz;
                    vx = wx;
                    yield bz - 1;
                }
                default -> {
                    wx = bx + 1 + random.nextInt(19);
                    wz = bz + 14;
                    vx = wx;
                    yield bz + 15;
                }
            };
            if (world.getBlockAt(wx, y, wz).getType() != Material.WHITE_CONCRETE) continue;
            for (int len = 0; len < 3; ++len) {
                Block vineSpot;
                if (!(random.nextDouble() < 0.8) || (vineSpot = world.getBlockAt(vx, y - len, vz)).getType() != Material.AIR) continue;
                RuinedHospital.setBlock(world, vx, y - len, vz, Material.VINE);
            }
        }
    }

    private static void addDamage(World world, int bx, int by, int bz) {
        int wallHoles = 8 + random.nextInt(5);
        for (int i = 0; i < wallHoles; ++i) {
            int x;
            int side = random.nextInt(4);
            int y = by + 1 + random.nextInt(7);
            if (y == by + 4) continue;
            int z = switch (side) {
                case 0 -> {
                    x = bx;
                    yield bz + 2 + random.nextInt(11);
                }
                case 1 -> {
                    x = bx + 20;
                    yield bz + 2 + random.nextInt(11);
                }
                case 2 -> {
                    x = bx + 2 + random.nextInt(17);
                    yield bz;
                }
                default -> {
                    x = bx + 2 + random.nextInt(17);
                    yield bz + 14;
                }
            };
            if (x == bx + 10 && z == bz) continue;
            RuinedHospital.setBlock(world, x, y, z, Material.AIR);
        }
        int floorHoles = 3 + random.nextInt(3);
        for (int i = 0; i < floorHoles; ++i) {
            int y;
            int x = bx + 2 + random.nextInt(17);
            int z = bz + 2 + random.nextInt(11);
            boolean topFloor = random.nextBoolean();
            int n = y = topFloor ? by + 8 : by + 4;
            if (x >= bx + 9 && x <= bx + 10 && z >= bz + 11 && z <= bz + 13 && y == by + 4) continue;
            RuinedHospital.setBlock(world, x, y, z, Material.AIR);
        }
        if (random.nextDouble() < 0.4) {
            int x = bx + 3 + random.nextInt(15);
            int z = bz + 3 + random.nextInt(9);
            RuinedHospital.setBlock(world, x, by, z, Material.AIR);
        }
    }

    private static void setBlock(World world, int x, int y, int z, Material material) {
        world.getBlockAt(x, y, z).setType(material);
    }
}

