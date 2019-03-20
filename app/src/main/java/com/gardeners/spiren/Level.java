package com.gardeners.spiren;

public class Level {
    private final int maxWater;
    private final int maxBugs;

    public Level(int maxWater, int maxBugs) {
        this.maxWater = maxWater;
        this.maxBugs = maxBugs;
    }

    public int getMaxWater() {
        return maxWater;
    }

    public int getMaxBugs() {
        return maxBugs;
    }
}
