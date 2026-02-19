using Microsoft.EntityFrameworkCore;
using Serilog;
using SkyTracker.API.Data;
using SkyTracker.API.Services;
using SkyTracker.API.Hubs;

var builder = WebApplication.CreateBuilder(args);

// Add services
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Logging
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Debug()
    .WriteTo.Console()
    .CreateLogger();

builder.Host.UseSerilog();

// Database (disabled for now - streaming live data only)
// var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");
// builder.Services.AddDbContext<AppDbContext>(options =>
// {
//     options.UseNpgsql(connectionString);
// });

// Services
builder.Services.AddScoped<IOpenSkyService, FlightRadar24Service>();
// builder.Services.AddScoped<IFlightService, FlightService>(); // Disabled - database disabled
builder.Services.AddHttpClient();
builder.Services.AddHostedService<FlightBroadcastService>();

// SignalR & CORS
builder.Services.AddSignalR();
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", builder =>
    {
        builder.AllowAnyOrigin()
               .AllowAnyMethod()
               .AllowAnyHeader();
    });
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

// Disable HTTPS redirect in development to allow HTTP connections from Android phone
if (!app.Environment.IsDevelopment())
{
    app.UseHttpsRedirection();
}
app.UseRouting();
app.UseCors("AllowAll");

app.MapHub<FlightHub>("/flightHub");
app.MapControllers();

app.Run();