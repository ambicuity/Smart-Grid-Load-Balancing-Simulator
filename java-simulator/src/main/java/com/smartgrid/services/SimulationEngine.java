package com.smartgrid.services;

import com.smartgrid.models.GridNode;
import com.smartgrid.models.LoadSource;
import com.smartgrid.models.Sensor;
import com.smartgrid.utils.ApiClient;
import com.smartgrid.utils.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Multi-threaded simulation engine for the smart grid.
 * Uses ExecutorService for concurrent load updates and monitoring.
 */
public class SimulationEngine {
    private static final Logger logger = LoggerFactory.getLogger(SimulationEngine.class);
    
    private final List<GridNode> gridNodes;
    private final List<LoadSource> loadSources;
    private final LoadBalancer loadBalancer;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final ApiClient apiClient;
    private final ConfigLoader config;
    private volatile boolean running;

    public SimulationEngine(ConfigLoader config) {
        this.config = config;
        this.gridNodes = new ArrayList<>();
        this.loadSources = new ArrayList<>();
        this.loadBalancer = new LoadBalancer(
            config.getOverloadThreshold(),
            config.getUnderloadThreshold()
        );
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.apiClient = new ApiClient(config.getApiEndpoint());
        this.running = false;
        
        initializeGrid();
    }

    private void initializeGrid() {
        // Create grid nodes
        String[] regions = {"North", "South", "East", "West", "Central"};
        for (int i = 0; i < config.getNumberOfNodes(); i++) {
            String region = regions[i % regions.length];
            GridNode node = new GridNode(
                "NODE-" + (i + 1),
                region,
                config.getNodeBaseCapacity() + (Math.random() * 50)
            );
            gridNodes.add(node);
        }

        // Create load sources
        for (int i = 0; i < config.getNumberOfLoadSources(); i++) {
            String type = (i % 5 == 0) ? "PRODUCER" : "CONSUMER";
            LoadSource source = new LoadSource(
                "SOURCE-" + (i + 1),
                type,
                10 + (Math.random() * 30),
                0.3 + (Math.random() * 0.4)
            );
            loadSources.add(source);
        }

        logger.info("Initialized grid with {} nodes and {} load sources",
                gridNodes.size(), loadSources.size());
    }

    public void start() {
        running = true;
        logger.info("Starting simulation engine...");

        // Schedule periodic load updates
        scheduler.scheduleAtFixedRate(
            this::updateLoads,
            0,
            config.getLoadUpdateInterval(),
            TimeUnit.SECONDS
        );

        // Schedule periodic optimization
        scheduler.scheduleAtFixedRate(
            this::runOptimization,
            config.getOptimizationInterval(),
            config.getOptimizationInterval(),
            TimeUnit.SECONDS
        );

        // Schedule periodic status reporting
        scheduler.scheduleAtFixedRate(
            this::reportStatus,
            config.getReportingInterval(),
            config.getReportingInterval(),
            TimeUnit.SECONDS
        );
    }

    private void updateLoads() {
        List<Future<?>> futures = new ArrayList<>();
        
        for (GridNode node : gridNodes) {
            Future<?> future = executorService.submit(() -> {
                double totalLoad = 0.0;
                
                // Simulate load from sources assigned to this node
                int sourcesPerNode = loadSources.size() / gridNodes.size();
                int startIdx = gridNodes.indexOf(node) * sourcesPerNode;
                int endIdx = Math.min(startIdx + sourcesPerNode, loadSources.size());
                
                for (int i = startIdx; i < endIdx; i++) {
                    totalLoad += loadSources.get(i).getCurrentLoad();
                }
                
                node.setCurrentLoad(Math.max(0, totalLoad));
            });
            futures.add(future);
        }

        // Wait for all updates to complete
        for (Future<?> future : futures) {
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Error updating loads", e);
            }
        }
    }

    private void runOptimization() {
        logger.info("Running optimization...");
        
        List<GridNode> overloaded = loadBalancer.detectOverloadedNodes(gridNodes);
        if (!overloaded.isEmpty()) {
            logger.warn("Detected {} overloaded nodes", overloaded.size());
            for (GridNode node : overloaded) {
                logger.warn("  - {}", node);
            }
        }

        List<LoadBalancer.OptimizationAction> actions = loadBalancer.optimize(gridNodes);
        if (!actions.isEmpty()) {
            logger.info("Applied {} optimization actions", actions.size());
            
            // Send optimization data to API
            try {
                apiClient.sendOptimizationActions(actions);
            } catch (Exception e) {
                logger.error("Failed to send optimization data to API", e);
            }
        }
    }

    private void reportStatus() {
        logger.info("=== Grid Status Report ===");
        
        double totalLoad = 0;
        double totalCapacity = 0;
        
        for (GridNode node : gridNodes) {
            totalLoad += node.getCurrentLoad();
            totalCapacity += node.getCapacity();
            logger.info("  {}", node);
        }
        
        logger.info("Total Load: {} MW / {} MW ({}% utilization)",
                String.format("%.2f", totalLoad), 
                String.format("%.2f", totalCapacity), 
                String.format("%.1f", (totalLoad / totalCapacity) * 100));
        
        // Create and send sensor data to API
        List<Sensor> sensors = new ArrayList<>();
        for (GridNode node : gridNodes) {
            Sensor sensor = new Sensor(
                "SENSOR-" + node.getNodeId(),
                node.getNodeId(),
                node.getCurrentLoad(),
                400 + (Math.random() * 20), // Voltage
                60 + (Math.random() * 0.5)  // Frequency
            );
            sensors.add(sensor);
        }
        
        try {
            apiClient.sendSensorData(sensors);
        } catch (Exception e) {
            logger.error("Failed to send sensor data to API", e);
        }
    }

    public void stop() {
        running = false;
        logger.info("Stopping simulation engine...");
        
        scheduler.shutdown();
        executorService.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Simulation engine stopped");
    }

    public List<GridNode> getGridNodes() {
        return new ArrayList<>(gridNodes);
    }

    public boolean isRunning() {
        return running;
    }
}
