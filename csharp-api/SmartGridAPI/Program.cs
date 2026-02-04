using Microsoft.EntityFrameworkCore;
using SmartGridAPI.Data;
using SmartGridAPI.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new() { 
        Title = "Smart Grid API", 
        Version = "v1",
        Description = "REST API for Smart Grid Load Balancing Simulator"
    });
});

// Configure database
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection") 
    ?? "Host=localhost;Database=smartgrid;Username=postgres;Password=postgres";
builder.Services.AddDbContext<SmartGridDbContext>(options =>
    options.UseNpgsql(connectionString));

// Add services
builder.Services.AddScoped<GridService>();

// Add CORS for local development
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "Smart Grid API v1");
    });
}

app.UseCors("AllowAll");
app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Database initialization
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<SmartGridDbContext>();
    try
    {
        context.Database.EnsureCreated();
        app.Logger.LogInformation("Database initialized successfully");
    }
    catch (Exception ex)
    {
        app.Logger.LogWarning(ex, "Database initialization failed - will continue without database");
    }
}

app.Logger.LogInformation("Smart Grid API started successfully");
app.Run();
