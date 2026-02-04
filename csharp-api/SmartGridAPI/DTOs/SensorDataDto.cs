namespace SmartGridAPI.DTOs;

public class SensorDataDto
{
    public string SensorId { get; set; } = string.Empty;
    public string NodeId { get; set; } = string.Empty;
    public string Timestamp { get; set; } = string.Empty;
    public double LoadReading { get; set; }
    public double Voltage { get; set; }
    public double Frequency { get; set; }
}
