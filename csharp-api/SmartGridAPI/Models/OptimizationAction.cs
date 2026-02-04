namespace SmartGridAPI.Models;

public class OptimizationAction
{
    public int Id { get; set; }
    public string FromNodeId { get; set; } = string.Empty;
    public string ToNodeId { get; set; } = string.Empty;
    public double Amount { get; set; }
    public string ActionType { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
}
