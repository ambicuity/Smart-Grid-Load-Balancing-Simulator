using Microsoft.EntityFrameworkCore;
using SmartGridAPI.Models;

namespace SmartGridAPI.Data;

public class SmartGridDbContext : DbContext
{
    public SmartGridDbContext(DbContextOptions<SmartGridDbContext> options) : base(options)
    {
    }

    public DbSet<GridNode> GridNodes { get; set; }
    public DbSet<SensorReading> SensorReadings { get; set; }
    public DbSet<LoadEvent> LoadEvents { get; set; }
    public DbSet<OptimizationAction> OptimizationActions { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<GridNode>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.NodeId).IsUnique();
            entity.Property(e => e.NodeId).IsRequired();
            entity.Property(e => e.Region).IsRequired();
        });

        modelBuilder.Entity<SensorReading>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => new { e.NodeId, e.Timestamp });
            entity.Property(e => e.SensorId).IsRequired();
            entity.Property(e => e.NodeId).IsRequired();
        });

        modelBuilder.Entity<LoadEvent>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => new { e.NodeId, e.Timestamp });
            entity.Property(e => e.NodeId).IsRequired();
            entity.Property(e => e.EventType).IsRequired();
        });

        modelBuilder.Entity<OptimizationAction>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.Timestamp);
            entity.Property(e => e.FromNodeId).IsRequired();
            entity.Property(e => e.ToNodeId).IsRequired();
            entity.Property(e => e.ActionType).IsRequired();
        });
    }
}
