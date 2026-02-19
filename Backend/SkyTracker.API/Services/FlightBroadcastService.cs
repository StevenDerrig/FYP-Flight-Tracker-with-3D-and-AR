using Microsoft.AspNetCore.SignalR;
using SkyTracker.API.Hubs;
using SkyTracker.API.Models;

namespace SkyTracker.API.Services
{
    public class FlightBroadcastService : BackgroundService
    {
        private readonly IServiceProvider _services;
        private readonly IHubContext<FlightHub> _hubContext;
        private readonly ILogger<FlightBroadcastService> _logger;

        public FlightBroadcastService(
            IServiceProvider services,
            IHubContext<FlightHub> hubContext,
            ILogger<FlightBroadcastService> logger)
        {
            _services = services;
            _hubContext = hubContext;
            _logger = logger;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("FlightBroadcastService started");

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    // Create a new scope for the scoped IOpenSkyService
                    using var scope = _services.CreateScope();
                    var fr24Service = scope.ServiceProvider.GetRequiredService<IOpenSkyService>();

                    // Fetch flights over Atlantic + Europe (major route corridor with more traffic)
                    // bounds: south,north,west,east
                    // Using: 35°N to 65°N latitude, -30°W to 20°E longitude
                    var flights = await fr24Service.GetFlightsAsync(35.0, 65.0, -30.0, 20.0);

                    // Broadcast to all connected clients
                    if (flights.Count > 0)
                    {
                        _logger.LogInformation("Broadcasting {Count} flights to clients", flights.Count);
                        await _hubContext.Clients.All.SendAsync("ReceiveFlightUpdate", flights, stoppingToken);
                    }
                    else
                    {
                        _logger.LogWarning("No flights received from FR24");
                    }

                    // Wait 30 seconds before next poll (reduces API credit usage)
                    await Task.Delay(TimeSpan.FromSeconds(30), stoppingToken);
                }
                catch (OperationCanceledException)
                {
                    _logger.LogInformation("FlightBroadcastService cancellation requested");
                    break;
                }
                catch (Exception ex)
                {
                    _logger.LogError("Error in FlightBroadcastService: {Message}", ex.Message);
                    // Wait a bit before retrying on error
                    await Task.Delay(TimeSpan.FromSeconds(5), stoppingToken);
                }
            }

            _logger.LogInformation("FlightBroadcastService stopped");
        }
    }
}
