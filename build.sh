#!/bin/bash
# Quick Start Script for Smart Grid Load Balancing Simulator

set -e

echo "==================================="
echo "Smart Grid Simulator - Quick Start"
echo "==================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Please install Java 17 or higher."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi

if ! command -v dotnet &> /dev/null; then
    echo "ERROR: .NET SDK not found. Please install .NET 8.0 SDK."
    exit 1
fi

echo "✓ All prerequisites found"
echo ""

# Build Java simulator
echo "Building Java simulator..."
cd java-simulator
mvn clean package -q
if [ $? -eq 0 ]; then
    echo "✓ Java simulator built successfully"
else
    echo "ERROR: Java build failed"
    exit 1
fi
cd ..
echo ""

# Build C# API
echo "Building C# API..."
cd csharp-api/SmartGridAPI
dotnet build --verbosity quiet
if [ $? -eq 0 ]; then
    echo "✓ C# API built successfully"
else
    echo "ERROR: C# API build failed"
    exit 1
fi
cd ../..
echo ""

echo "==================================="
echo "Build completed successfully!"
echo "==================================="
echo ""
echo "To start the system:"
echo ""
echo "1. Start the API:"
echo "   cd csharp-api/SmartGridAPI"
echo "   dotnet run"
echo ""
echo "2. In a new terminal, run the simulator:"
echo "   cd java-simulator"
echo "   java -jar target/smart-grid-simulator-1.0.0.jar"
echo ""
echo "3. Access Swagger UI:"
echo "   http://localhost:5000/swagger"
echo ""
echo "Note: The API will work without a database, but for full functionality,"
echo "      set up PostgreSQL as described in README.md"
echo ""
