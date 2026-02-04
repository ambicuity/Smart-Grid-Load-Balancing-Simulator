# Smart Grid Load Balancing Simulator

A complete end-to-end distributed simulation system that models electrical load distribution across a regional smart grid. The system optimizes energy allocation during peak demand using concurrent processing and persists historical data for analysis.

## System Architecture

The simulator consists of three integrated components:

```
┌─────────────────────┐
│  Java Simulator     │ ─── JSON/HTTP ──→ ┌──────────────────┐
│  (Multi-threaded)   │                    │  C# REST API     │
│  - Grid Nodes       │                    │  (ASP.NET Core)  │
│  - Load Sources     │                    │  - Data Ingestion│
│  - Load Balancer    │                    │  - Status API    │
└─────────────────────┘                    └─────────┬────────┘
                                                     │
                                                     ↓
                                            ┌─────────────────┐
                                            │  PostgreSQL DB  │
                                            │  - Sensor Data  │
                                            │  - Analytics    │
                                            └─────────────────┘
```

### 1. Java Simulation Engine

Multi-threaded simulation engine built with Java 17+ that models:
- **GridNode**: Represents substations with capacity and load management
- **LoadSource**: Consumers and producers with dynamic load variations
- **Sensor**: Real-time monitoring with voltage, frequency, and load metrics
- **LoadBalancer**: Intelligent optimization logic for load redistribution

**Key Features:**
- Thread-safe concurrent operations using `ExecutorService`
- Automatic peak-load detection and redistribution
- Configurable simulation parameters
- Real-time JSON data streaming to API

### 2. C# REST API Layer

ASP.NET Core 8.0 Web API providing:
- **POST /api/SensorData**: Ingest real-time sensor readings
- **GET /api/GridStatus**: Retrieve aggregated grid metrics
- **POST /api/Control/optimize**: Record optimization actions

**Key Features:**
- Clean DTO-based API contracts
- Entity Framework Core with PostgreSQL
- Swagger/OpenAPI documentation
- Structured logging and error handling

### 3. PostgreSQL Database

Relational database schema designed for time-series analytics:
- `grid_nodes`: Substation information
- `sensor_readings`: Time-series sensor data
- `load_events`: Historical load events
- `optimization_actions`: Load balancing actions

**Key Features:**
- Optimized indexes for time-series queries
- Support for trend analysis
- Predictive maintenance queries

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (for Java build)
- **.NET**: .NET 8.0 SDK
- **PostgreSQL**: 12+ (optional, API works without database)

## Quick Start

### 1. Setup Database (Optional)

```bash
# Start PostgreSQL
# Create database
createdb smartgrid

# Run schema
psql -d smartgrid -f database/scripts/schema.sql

# Load seed data
psql -d smartgrid -f database/scripts/seed_data.sql
```

### 2. Start the C# API

```bash
cd csharp-api/SmartGridAPI

# Update connection string in appsettings.json if needed
# Default: Host=localhost;Database=smartgrid;Username=postgres;Password=postgres

# Run the API
dotnet run

# API will start on http://localhost:5000
# Swagger UI: http://localhost:5000/swagger
```

### 3. Run the Java Simulator

```bash
cd java-simulator

# Build the project
mvn clean package

# Run the simulator
java -jar target/smart-grid-simulator-1.0.0.jar

# Or with custom config
# java -cp target/smart-grid-simulator-1.0.0.jar com.smartgrid.SimulatorMain custom-config.properties
```

## Configuration

### Java Simulator Configuration

Edit `java-simulator/src/main/resources/simulation.properties`:

```properties
# Grid Configuration
grid.nodes=10                      # Number of grid nodes
grid.loadSources=50                # Number of load sources
grid.nodeBaseCapacity=100.0        # Base capacity per node (MW)
grid.overloadThreshold=85.0        # Overload threshold (%)
grid.underloadThreshold=40.0       # Underload threshold (%)

# Simulation Parameters
simulation.threadPoolSize=4        # Thread pool size
simulation.loadUpdateInterval=5    # Load update interval (seconds)
simulation.optimizationInterval=15 # Optimization interval (seconds)
simulation.reportingInterval=10    # Status reporting interval (seconds)
simulation.duration=300            # Total simulation duration (seconds)

# API Configuration
api.endpoint=http://localhost:5000 # C# API endpoint
```

### C# API Configuration

Edit `csharp-api/SmartGridAPI/appsettings.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Host=localhost;Database=smartgrid;Username=postgres;Password=postgres"
  },
  "Urls": "http://localhost:5000"
}
```

## Sample API Usage

### Send Sensor Data
```bash
curl -X POST http://localhost:5000/api/SensorData \
  -H "Content-Type: application/json" \
  -d '[{
    "sensorId": "SENSOR-1",
    "nodeId": "NODE-1",
    "timestamp": "2024-02-04T18:00:00",
    "loadReading": 85.5,
    "voltage": 410.2,
    "frequency": 60.1
  }]'
```

### Get Grid Status
```bash
curl http://localhost:5000/api/GridStatus
```

Response:
```json
{
  "timestamp": "2024-02-04T18:00:00Z",
  "totalNodes": 10,
  "totalLoad": 850.5,
  "totalCapacity": 1200.0,
  "averageUtilization": 70.88,
  "overloadedNodes": 2,
  "nodes": [...]
}
```

### Send Optimization Actions
```bash
curl -X POST http://localhost:5000/api/Control/optimize \
  -H "Content-Type: application/json" \
  -d '[{
    "fromNodeId": "NODE-1",
    "toNodeId": "NODE-2",
    "amount": 15.5,
    "actionType": "LOAD_TRANSFER",
    "timestamp": "2024-02-04T18:00:00"
  }]'
```

## Simulation Flow

1. **Initialization**: Simulator creates grid nodes and load sources based on configuration
2. **Load Updates**: Every 5 seconds, load sources generate fluctuating demand
3. **Monitoring**: Sensors collect voltage, frequency, and load data
4. **Optimization**: Every 15 seconds, load balancer detects overloaded nodes and redistributes load
5. **Reporting**: Every 10 seconds, grid status is logged and sent to API
6. **Persistence**: API stores all data in PostgreSQL for analytics

## Analytics Queries

The `database/queries/analytics.sql` file contains 10 pre-built queries for:
- Real-time grid health dashboard
- Historical load trends by region
- Peak load analysis
- Overload event frequency
- Optimization effectiveness
- Voltage stability monitoring
- Frequency deviation detection
- Predictive maintenance risk scoring
- Daily load profiles
- Regional load balance

Example:
```bash
psql -d smartgrid -f database/queries/analytics.sql
```

## Development

### Building from Source

**Java Simulator:**
```bash
cd java-simulator
mvn clean compile
mvn test  # Run tests (when available)
mvn package  # Create JAR
```

**C# API:**
```bash
cd csharp-api/SmartGridAPI
dotnet build
dotnet test  # Run tests (when available)
dotnet publish -c Release  # Create release build
```

## Architecture Highlights

### Thread Safety
- Java: Uses `ReentrantReadWriteLock` for GridNode state
- Java: `ExecutorService` for concurrent load updates
- C#: Async/await patterns throughout

### Design Patterns
- **Repository Pattern**: Database access layer
- **Service Layer**: Business logic separation
- **DTO Pattern**: Clean API contracts
- **Dependency Injection**: ASP.NET Core DI container

### Performance Optimizations
- Database indexes on time-series columns
- Batch processing of sensor data
- Connection pooling
- Efficient thread pool sizing

## Troubleshooting

### API Not Receiving Data
- Check API is running on correct port (default: 5000)
- Verify `api.endpoint` in Java configuration
- Check firewall/network settings

### Database Connection Issues
- Verify PostgreSQL is running
- Check connection string credentials
- API will start without database but with warnings

### High Memory Usage
- Reduce `grid.nodes` or `grid.loadSources` in configuration
- Adjust `simulation.threadPoolSize`
- Increase heap size: `java -Xmx2g -jar ...`

## License

MIT License - See LICENSE file for details

## Contributors

Built as a demonstration of distributed system design with Java, C#, and SQL integration.