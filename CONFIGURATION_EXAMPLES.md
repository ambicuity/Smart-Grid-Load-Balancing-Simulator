# Example Configuration Files for Smart Grid Simulator

## Configuration Files Location
All configuration files should be placed in `java-simulator/src/main/resources/`

## Default Configuration (simulation.properties)
This is the standard configuration used when no arguments are provided.

```properties
# Grid Configuration
grid.nodes=10
grid.loadSources=50
grid.nodeBaseCapacity=100.0
grid.overloadThreshold=85.0
grid.underloadThreshold=40.0

# Simulation Parameters
simulation.threadPoolSize=4
simulation.loadUpdateInterval=5
simulation.optimizationInterval=15
simulation.reportingInterval=10
simulation.duration=300

# API Configuration
api.endpoint=http://localhost:5000
```

## Quick Test Configuration
For rapid testing and development (short runtime, fewer nodes).

```properties
# Quick Test - 30 second run
grid.nodes=5
grid.loadSources=20
grid.nodeBaseCapacity=100.0
grid.overloadThreshold=85.0
grid.underloadThreshold=40.0

simulation.threadPoolSize=2
simulation.loadUpdateInterval=3
simulation.optimizationInterval=8
simulation.reportingInterval=5
simulation.duration=30

api.endpoint=http://localhost:5000
```

## High Load Scenario
For stress testing and performance evaluation.

```properties
# High Load - More nodes and sources
grid.nodes=50
grid.loadSources=200
grid.nodeBaseCapacity=100.0
grid.overloadThreshold=85.0
grid.underloadThreshold=40.0

simulation.threadPoolSize=8
simulation.loadUpdateInterval=5
simulation.optimizationInterval=15
simulation.reportingInterval=10
simulation.duration=300

api.endpoint=http://localhost:5000
```

## Peak Demand Simulation
Simulates peak demand with aggressive thresholds.

```properties
# Peak Demand - Aggressive thresholds
grid.nodes=20
grid.loadSources=100
grid.nodeBaseCapacity=80.0
grid.overloadThreshold=75.0
grid.underloadThreshold=30.0

simulation.threadPoolSize=6
simulation.loadUpdateInterval=3
simulation.optimizationInterval=10
simulation.reportingInterval=8
simulation.duration=600

api.endpoint=http://localhost:5000
```

## Long-term Monitoring
For extended simulation runs with less frequent updates.

```properties
# Long-term - 1 hour simulation
grid.nodes=15
grid.loadSources=75
grid.nodeBaseCapacity=120.0
grid.overloadThreshold=85.0
grid.underloadThreshold=40.0

simulation.threadPoolSize=4
simulation.loadUpdateInterval=10
simulation.optimizationInterval=30
simulation.reportingInterval=60
simulation.duration=3600

api.endpoint=http://localhost:5000
```

## Distributed Scenario
For testing multiple regions with different API endpoints.

```properties
# Distributed - Multiple regions
grid.nodes=25
grid.loadSources=125
grid.nodeBaseCapacity=100.0
grid.overloadThreshold=85.0
grid.underloadThreshold=40.0

simulation.threadPoolSize=8
simulation.loadUpdateInterval=5
simulation.optimizationInterval=15
simulation.reportingInterval=10
simulation.duration=300

# Point to different API instance
api.endpoint=http://api.smartgrid.local:5000
```

## Configuration Parameters Explained

### Grid Configuration
- `grid.nodes`: Number of grid nodes (substations) - Range: 1-100
- `grid.loadSources`: Number of load sources (consumers/producers) - Range: 1-500
- `grid.nodeBaseCapacity`: Base capacity per node in MW - Range: 50-200
- `grid.overloadThreshold`: Threshold for overload detection (%) - Range: 70-95
- `grid.underloadThreshold`: Threshold for underload (%) - Range: 20-50

### Simulation Parameters
- `simulation.threadPoolSize`: Number of worker threads - Range: 2-16
- `simulation.loadUpdateInterval`: Seconds between load updates - Range: 1-30
- `simulation.optimizationInterval`: Seconds between optimization runs - Range: 5-60
- `simulation.reportingInterval`: Seconds between status reports - Range: 5-120
- `simulation.duration`: Total simulation time in seconds - Range: 10-86400

### API Configuration
- `api.endpoint`: Base URL of the C# API - Format: http://host:port

## Using Custom Configuration

To use a custom configuration:

1. Create a `.properties` file with your settings
2. Place it in `java-simulator/src/main/resources/`
3. Rebuild the JAR: `mvn clean package`
4. The configuration is embedded in the JAR

Or, keep it external and use classpath:
```bash
java -cp target/smart-grid-simulator-1.0.0.jar:. com.smartgrid.SimulatorMain my-config.properties
```

## Performance Tuning

### For Better Performance
- Increase `simulation.threadPoolSize` to match CPU cores
- Reduce `simulation.loadUpdateInterval` for more frequent updates
- Decrease `grid.nodes` if running on constrained hardware

### For More Realistic Simulation
- Use realistic `grid.nodeBaseCapacity` values (typical: 100-150 MW)
- Set `grid.overloadThreshold` to 85% (industry standard)
- Balance `grid.loadSources` to about 4-5x the number of nodes

### For Testing Load Balancing
- Set `grid.overloadThreshold` lower (e.g., 70%)
- Increase variability by having more load sources
- Reduce `simulation.optimizationInterval` for faster response
