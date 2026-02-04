namespace SmartGridAPI.DTOs;

public class OptimizationActionDto
{
    public string FromNodeId { get; set; } = string.Empty;
    public string ToNodeId { get; set; } = string.Empty;
    public double Amount { get; set; }
    public string ActionType { get; set; } = string.Empty;
    public string Timestamp { get; set; } = string.Empty;
}
