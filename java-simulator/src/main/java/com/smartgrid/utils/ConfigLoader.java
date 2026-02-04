package com.smartgrid.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration loader for simulation parameters.
 */
public class ConfigLoader {
    private final Properties properties;

    public ConfigLoader(String configFile) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFile)) {
            if (input == null) {
                throw new IOException("Unable to find " + configFile);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public int getNumberOfNodes() {
        return Integer.parseInt(properties.getProperty("grid.nodes", "10"));
    }

    public int getNumberOfLoadSources() {
        return Integer.parseInt(properties.getProperty("grid.loadSources", "50"));
    }

    public double getNodeBaseCapacity() {
        return Double.parseDouble(properties.getProperty("grid.nodeBaseCapacity", "100.0"));
    }

    public double getOverloadThreshold() {
        return Double.parseDouble(properties.getProperty("grid.overloadThreshold", "85.0"));
    }

    public double getUnderloadThreshold() {
        return Double.parseDouble(properties.getProperty("grid.underloadThreshold", "40.0"));
    }

    public int getThreadPoolSize() {
        return Integer.parseInt(properties.getProperty("simulation.threadPoolSize", "4"));
    }

    public int getLoadUpdateInterval() {
        return Integer.parseInt(properties.getProperty("simulation.loadUpdateInterval", "5"));
    }

    public int getOptimizationInterval() {
        return Integer.parseInt(properties.getProperty("simulation.optimizationInterval", "15"));
    }

    public int getReportingInterval() {
        return Integer.parseInt(properties.getProperty("simulation.reportingInterval", "10"));
    }

    public String getApiEndpoint() {
        return properties.getProperty("api.endpoint", "http://localhost:5000");
    }

    public int getSimulationDuration() {
        return Integer.parseInt(properties.getProperty("simulation.duration", "300"));
    }
}
