package com.zonamuerta.plugin;

import java.util.Set;

public class WorldConfig {
    public boolean enabled = true;
    public double mutationRateMultiplier = 1.0;
    public double spawnRateMultiplier = 1.0;
    public double damageMultiplier = 1.0;
    public double speedMultiplier = 1.0;
    public int maxZombies = 100;
    public boolean fireImmune = false;
    public boolean canTeleport = false;
    public Set<String> enabledMutations = null;
    public Set<String> disabledMutations = null;
}

