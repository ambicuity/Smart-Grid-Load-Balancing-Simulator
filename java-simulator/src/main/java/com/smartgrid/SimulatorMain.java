package com.smartgrid;

import com.smartgrid.services.SimulationEngine;
import com.smartgrid.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Smart Grid Load Balancing Simulator.
 */
public class SimulatorMain {
    private static final Logger logger = LoggerFactory.getLogger(SimulatorMain.class);

    public static void main(String[] args) {
        logger.info("=== Smart Grid Load Balancing Simulator ===");
        
        try {
            // Load configuration
            String configFile = (args.length > 0) ? args[0] : "simulation.properties";
            ConfigLoader config = new ConfigLoader(configFile);
            
            // Create and start simulation engine
            SimulationEngine engine = new SimulationEngine(config);
            
            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received");
                engine.stop();
            }));
            
            // Start simulation
            engine.start();
            
            // Run for configured duration
            int duration = config.getSimulationDuration();
            logger.info("Simulation will run for {} seconds", duration);
            
            Thread.sleep(duration * 1000L);
            
            // Stop simulation
            engine.stop();
            logger.info("Simulation completed successfully");
            
        } catch (Exception e) {
            logger.error("Fatal error in simulation", e);
            System.exit(1);
        }
    }
}
