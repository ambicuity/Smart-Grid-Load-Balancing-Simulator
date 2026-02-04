namespace SmartGridAPI.Models;

public class SensorReading
{
    public int Id { get; set; }
    public string SensorId { get; set; } = string.Empty;
    public string NodeId { get; set; } = string.Empty;
    public DateTime Timestamp { get; set; }
    public double LoadReading { get; set; }
    public double Voltage { get; set; }
    public double Frequency { get; set; }
}
