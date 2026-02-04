namespace SmartGridAPI.Models;

public class LoadEvent
{
    public int Id { get; set; }
    public string NodeId { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public double LoadValue { get; set; }
    public double UtilizationPercent { get; set; }
    public string EventType { get; set; } = string.Empty; // "NORMAL", "OVERLOAD", "UNDERLOAD"
}
