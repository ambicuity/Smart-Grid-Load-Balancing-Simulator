using Microsoft.AspNetCore.Mvc;
using SmartGridAPI.DTOs;
using SmartGridAPI.Services;

namespace SmartGridAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ControlController : ControllerBase
{
    private readonly GridService _gridService;
    private readonly ILogger<ControlController> _logger;

    public ControlController(GridService gridService, ILogger<ControlController> logger)
    {
        _gridService = gridService;
        _logger = logger;
    }

    /// <summary>
    /// Receives optimization actions from the simulation engine
    /// </summary>
    [HttpPost("optimize")]
    public async Task<IActionResult> PostOptimizationActions([FromBody] List<OptimizationActionDto> actions)
    {
        if (actions == null || actions.Count == 0)
        {
            return BadRequest("Optimization actions cannot be empty");
        }

        try
        {
            await _gridService.ProcessOptimizationActionsAsync(actions);
            _logger.LogInformation("Received and processed {Count} optimization actions", actions.Count);
            return Ok(new { message = $"Successfully processed {actions.Count} optimization actions" });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error processing optimization actions");
            return StatusCode(500, "Internal server error");
        }
    }
}
