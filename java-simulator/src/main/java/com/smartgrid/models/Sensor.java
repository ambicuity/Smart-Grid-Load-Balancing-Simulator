package com.smartgrid.models;

import java.time.LocalDateTime;

/**
 * Represents a sensor reading from the grid.
 */
public class Sensor {
    private final String sensorId;
    private final String nodeId;
    private final LocalDateTime timestamp;
    private final double loadReading; // in MW
    private final double voltage; // in kV
    private final double frequency; // in Hz

    public Sensor(String sensorId, String nodeId, double loadReading, double voltage, double frequency) {
        this.sensorId = sensorId;
        this.nodeId = nodeId;
        this.timestamp = LocalDateTime.now();
        this.loadReading = loadReading;
        this.voltage = voltage;
        this.frequency = frequency;
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getLoadReading() {
        return loadReading;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return String.format("Sensor[id=%s, node=%s, load=%.2f MW, voltage=%.2f kV, freq=%.2f Hz]",
                sensorId, nodeId, loadReading, voltage, frequency);
    }
}
