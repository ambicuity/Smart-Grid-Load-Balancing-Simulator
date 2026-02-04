using Microsoft.AspNetCore.Mvc;
using SmartGridAPI.DTOs;
using SmartGridAPI.Services;

namespace SmartGridAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class SensorDataController : ControllerBase
{
    private readonly GridService _gridService;
    private readonly ILogger<SensorDataController> _logger;

    public SensorDataController(GridService gridService, ILogger<SensorDataController> logger)
    {
        _gridService = gridService;
        _logger = logger;
    }

    /// <summary>
    /// Receives sensor data from the simulation engine
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> PostSensorData([FromBody] List<SensorDataDto> sensorData)
    {
        if (sensorData == null || sensorData.Count == 0)
        {
            return BadRequest("Sensor data cannot be empty");
        }

        try
        {
            await _gridService.ProcessSensorDataAsync(sensorData);
            _logger.LogInformation("Received and processed {Count} sensor readings", sensorData.Count);
            return Ok(new { message = $"Successfully processed {sensorData.Count} sensor readings" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing sensor data");
            return StatusCode(500, "Internal server error");
        }
    }
}
