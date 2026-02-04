namespace SmartGridAPI.DTOs;

public class GridStatusDto
{
    public DateTime Timestamp { get; set; }
    public int TotalNodes { get; set; }
    public double TotalLoad { get; set; }
    public double TotalCapacity { get; set; }
    public double AverageUtilization { get; set; }
    public int OverloadedNodes { get; set; }
    public List<NodeStatusDto> Nodes { get; set; } = new();
}

public class NodeStatusDto
{
    public string NodeId { get; set; } = string.Empty;
    public string Region { get; set; } = string.Empty;
    public double CurrentLoad { get; set; }
    public double Capacity { get; set; }
    public double UtilizationPercent { get; set; }
    public DateTime LastUpdated { get; set; }
}
