namespace SmartGridAPI.Models;

public class GridNode
{
    public int Id { get; set; }
    public string NodeId { get; set; } = string.Empty;
    public string Region { get; set; } = string.Empty;
    public double Capacity { get; set; }
    public DateTime LastUpdated { get; set; }
}
