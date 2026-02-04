package com.smartgrid.models;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Represents a grid node (substation) in the smart grid network.
 * Thread-safe implementation using ReadWriteLock for concurrent access.
 */
public class GridNode {
    private final String nodeId;
    private final String region;
    private volatile double currentLoad; // in MW
    private volatile double capacity; // in MW
    private final ReentrantReadWriteLock lock;

    public GridNode(String nodeId, String region, double capacity) {
        this.nodeId = nodeId;
        this.region = region;
        this.capacity = capacity;
        this.currentLoad = 0.0;
        this.lock = new ReentrantReadWriteLock();
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getRegion() {
        return region;
    }

    public double getCurrentLoad() {
        lock.readLock().lock();
        try {
            return currentLoad;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setCurrentLoad(double currentLoad) {
        lock.writeLock().lock();
        try {
            this.currentLoad = currentLoad;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        lock.writeLock().lock();
        try {
            this.capacity = capacity;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @return Load utilization as a percentage (0-100+)
     */
    public double getUtilizationPercent() {
        lock.readLock().lock();
        try {
            return (currentLoad / capacity) * 100.0;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return Available capacity in MW
     */
    public double getAvailableCapacity() {
        lock.readLock().lock();
        try {
            return Math.max(0, capacity - currentLoad);
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isOverloaded(double threshold) {
        return getUtilizationPercent() > threshold;
    }

    @Override
    public String toString() {
        return String.format("GridNode[id=%s, region=%s, load=%.2f/%.2f MW, utilization=%.1f%%]",
                nodeId, region, getCurrentLoad(), capacity, getUtilizationPercent());
    }
}
