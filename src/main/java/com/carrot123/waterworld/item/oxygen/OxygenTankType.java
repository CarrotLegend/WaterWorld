package com.carrot123.waterworld.item.oxygen;

public enum OxygenTankType {
    SMALL("small_compressed_air_tank", 1200, 2.0D, true),
    MIXED("high_pressure_mixed_gas_tank", 2400, 2.0D, true),
    DEEP_SEA("deep_sea_life_support_tank", 2400, 1.5D, false);

    private final String id;
    private final int capacity;
    private final double perSecond;
    private final boolean pressureDependent;

    OxygenTankType(String id, int capacity, double perSecond, boolean pressureDependent) {
        this.id = id;
        this.capacity = capacity;
        this.perSecond = perSecond;
        this.pressureDependent = pressureDependent;
    }

    public String id() {
        return id;
    }

    public int capacity() {
        return capacity;
    }

    public double perSecond() {
        return perSecond;
    }

    public boolean pressureDependent() {
        return pressureDependent;
    }

    public double perTick(double pressure) {
        return perSecond * (pressureDependent ? pressure : 1.0D) / 20.0D;
    }
}
