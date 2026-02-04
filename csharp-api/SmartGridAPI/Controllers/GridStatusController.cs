using Microsoft.AspNetCore.Mvc;
using SmartGridAPI.Services;

namespace SmartGridAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class GridStatusController : ControllerBase
{
    private readonly GridService _gridService;
    private readonly ILogger<GridStatusController> _logger;

    public GridStatusController(GridService gridService, ILogger<GridStatusController> logger)
    {
        _gridService = gridService;
        _logger = logger;
    }

    /// <summary>
    /// Gets current grid status with aggregated metrics
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetGridStatus()
    {
        try
        {
            var status = await _gridService.GetGridStatusAsync();
            return Ok(status);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error retrieving grid status");
            return StatusCode(500, "Internal server error");
        }
    }
}
