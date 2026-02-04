using Microsoft.EntityFrameworkCore;
using SmartGridAPI.Data;
using SmartGridAPI.DTOs;
using SmartGridAPI.Models;

namespace SmartGridAPI.Services;

public class GridService
{
    private readonly SmartGridDbContext _context;
    private readonly ILogger<GridService> _logger;

    public GridService(SmartGridDbContext context, ILogger<GridService> logger)
    {
        _context = context;
        _logger = logger;
    }

    public async Task ProcessSensorDataAsync(List<SensorDataDto> sensorData)
    {
        foreach (var data in sensorData)
        {
            try
            {
                // Parse timestamp
                var timestamp = DateTime.Parse(data.Timestamp);

                // Ensure grid node exists
                await EnsureGridNodeExistsAsync(data.NodeId);

                // Save sensor reading
                var reading = new SensorReading
                {
                    SensorId = data.SensorId,
                    NodeId = data.NodeId,
                    Timestamp = timestamp,
                    LoadReading = data.LoadReading,
                    Voltage = data.Voltage,
                    Frequency = data.Frequency
                };

                _context.SensorReadings.Add(reading);

                // Create load event
                var loadEvent = new LoadEvent
                {
                    NodeId = data.NodeId,
                    Timestamp = timestamp,
                    LoadValue = data.LoadReading,
                    UtilizationPercent = 0, // Will be calculated
                    EventType = "NORMAL"
                };

                _context.LoadEvents.Add(loadEvent);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing sensor data for sensor {SensorId}", data.SensorId);
            }
        }

        await _context.SaveChangesAsync();
        _logger.LogInformation("Processed {Count} sensor readings", sensorData.Count);
    }

    public async Task ProcessOptimizationActionsAsync(List<OptimizationActionDto> actions)
    {
        foreach (var action in actions)
        {
            try
            {
                var timestamp = DateTime.Parse(action.Timestamp);

                var optimizationAction = new OptimizationAction
                {
                    FromNodeId = action.FromNodeId,
                    ToNodeId = action.ToNodeId,
                    Amount = action.Amount,
                    ActionType = action.ActionType,
                    Timestamp = timestamp
                };

                _context.OptimizationActions.Add(optimizationAction);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error processing optimization action");
            }
        }

        await _context.SaveChangesAsync();
        _logger.LogInformation("Processed {Count} optimization actions", actions.Count);
    }

    public async Task<GridStatusDto> GetGridStatusAsync()
    {
        var nodes = await _context.GridNodes.ToListAsync();
        
        // Get latest sensor readings for each node
        var latestReadings = await _context.SensorReadings
            .GroupBy(sr => sr.NodeId)
            .Select(g => g.OrderByDescending(sr => sr.Timestamp).FirstOrDefault())
            .ToListAsync();

        var nodeStatuses = new List<NodeStatusDto>();
        double totalLoad = 0;
        double totalCapacity = 0;
        int overloadedNodes = 0;

        foreach (var node in nodes)
        {
            var reading = latestReadings.FirstOrDefault(r => r != null && r.NodeId == node.NodeId);
            var currentLoad = reading?.LoadReading ?? 0;
            var utilization = node.Capacity > 0 ? (currentLoad / node.Capacity) * 100 : 0;

            if (utilization > 85)
            {
                overloadedNodes++;
            }

            nodeStatuses.Add(new NodeStatusDto
            {
                NodeId = node.NodeId,
                Region = node.Region,
                CurrentLoad = currentLoad,
                Capacity = node.Capacity,
                UtilizationPercent = utilization,
                LastUpdated = reading?.Timestamp ?? node.LastUpdated
            });

            totalLoad += currentLoad;
            totalCapacity += node.Capacity;
        }

        return new GridStatusDto
        {
            Timestamp = DateTime.UtcNow,
            TotalNodes = nodes.Count,
            TotalLoad = totalLoad,
            TotalCapacity = totalCapacity,
            AverageUtilization = totalCapacity > 0 ? (totalLoad / totalCapacity) * 100 : 0,
            OverloadedNodes = overloadedNodes,
            Nodes = nodeStatuses
        };
    }

    private async Task EnsureGridNodeExistsAsync(string nodeId)
    {
        var exists = await _context.GridNodes.AnyAsync(n => n.NodeId == nodeId);
        if (!exists)
        {
            var node = new GridNode
            {
                NodeId = nodeId,
                Region = "Unknown",
                Capacity = 100.0,
                LastUpdated = DateTime.UtcNow
            };

            _context.GridNodes.Add(node);
            await _context.SaveChangesAsync();
        }
    }
}
