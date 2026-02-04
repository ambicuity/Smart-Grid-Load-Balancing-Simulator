# Getting Started Guide

## Overview
This guide will walk you through setting up and running the Smart Grid Load Balancing Simulator.

## Setup Methods

### Option 1: Docker Compose (Recommended for Quick Start)

1. **Prerequisites**: Docker and Docker Compose installed

2. **Start the database and API**:
   ```bash
   docker-compose up -d
   ```

3. **Wait for services to be ready** (about 30 seconds):
   ```bash
   docker-compose logs -f
   ```

4. **Run the Java simulator**:
   ```bash
   cd java-simulator
   mvn clean package
   java -jar target/smart-grid-simulator-1.0.0.jar
   ```

5. **Access Swagger UI**: http://localhost:5000/swagger

### Option 2: Manual Setup

1. **Install Prerequisites**:
   - Java JDK 17+
   - .NET 8.0 SDK
   - PostgreSQL 12+
   - Maven 3.6+

2. **Setup Database**:
   ```bash
   createdb smartgrid
   psql -d smartgrid -f database/scripts/schema.sql
   psql -d smartgrid -f database/scripts/seed_data.sql
   ```

3. **Start C# API**:
   ```bash
   cd csharp-api/SmartGridAPI
   dotnet run
   ```

4. **Run Java Simulator**:
   ```bash
   cd java-simulator
   mvn clean package
   java -jar target/smart-grid-simulator-1.0.0.jar
   ```

## Verification

### Check API is Running
```bash
curl http://localhost:5000/api/grid-status
```

### Check Database Connection
```bash
psql -d smartgrid -c "SELECT COUNT(*) FROM grid_nodes;"
```

### Monitor Simulator Logs
The Java simulator will output logs showing:
- Grid initialization
- Load updates
- Optimization actions
- Status reports

Example output:
```
[INFO] === Smart Grid Load Balancing Simulator ===
[INFO] Initialized grid with 10 nodes and 50 load sources
[INFO] Starting simulation engine...
[INFO] === Grid Status Report ===
[INFO]   GridNode[id=NODE-1, region=North, load=75.23/120.50 MW, utilization=62.4%]
[INFO] Running optimization...
[INFO] Transferred 15.50 MW from NODE-1 to NODE-2
```

## Configuration Tips

### For Development
- Set `simulation.duration=60` for shorter test runs
- Set `simulation.reportingInterval=5` for more frequent updates
- Reduce `grid.nodes=5` and `grid.loadSources=20` for lighter load

### For Load Testing
- Increase `grid.nodes=50` and `grid.loadSources=200`
- Set `simulation.threadPoolSize=8` or higher
- Reduce `simulation.loadUpdateInterval=2` for faster simulation

## Troubleshooting

### Port 5000 Already in Use
Change the port in `appsettings.json`:
```json
"Urls": "http://localhost:5001"
```
And update `api.endpoint` in `simulation.properties`.

### Maven Build Fails
Clear Maven cache:
```bash
rm -rf ~/.m2/repository
mvn clean install
```

### Database Connection Timeout
Check PostgreSQL is running:
```bash
pg_isready -h localhost
```

## Next Steps

1. **Explore API**: Visit http://localhost:5000/swagger
2. **Run Analytics**: Execute queries from `database/queries/analytics.sql`
3. **Customize**: Modify simulation parameters in `simulation.properties`
4. **Extend**: Add new features to Java simulator or C# API

## Support

For issues or questions, please refer to the main README.md file.
