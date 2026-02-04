package com.smartgrid.models;

/**
 * Represents a load source (consumer or producer) in the grid.
 * Consumers have positive load, producers (e.g., solar panels) have negative load.
 */
public class LoadSource {
    private final String sourceId;
    private final String type; // "CONSUMER" or "PRODUCER"
    private volatile double baseLoad; // in MW
    private volatile double variabilityFactor; // 0.0 to 1.0

    public LoadSource(String sourceId, String type, double baseLoad, double variabilityFactor) {
        this.sourceId = sourceId;
        this.type = type;
        this.baseLoad = baseLoad;
        this.variabilityFactor = Math.min(1.0, Math.max(0.0, variabilityFactor));
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getType() {
        return type;
    }

    public double getBaseLoad() {
        return baseLoad;
    }

    public double getVariabilityFactor() {
        return variabilityFactor;
    }

    /**
     * Calculates current load with random fluctuation based on variability factor.
     * @return Current load in MW
     */
    public double getCurrentLoad() {
        double variation = (Math.random() - 0.5) * 2 * variabilityFactor;
        double load = baseLoad * (1 + variation);
        return type.equals("PRODUCER") ? -Math.abs(load) : Math.abs(load);
    }

    @Override
    public String toString() {
        return String.format("LoadSource[id=%s, type=%s, baseLoad=%.2f MW]",
                sourceId, type, baseLoad);
    }
}
