using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Logging;

namespace SkyTracker.API.Hubs
{
    public class FlightHub : Hub
    {
        private readonly ILogger<FlightHub> _logger;

        public FlightHub(ILogger<FlightHub> logger)
        {
            _logger = logger;
        }

        public override async Task OnConnectedAsync()
        {
            _logger.LogInformation($"Client connected: {Context.ConnectionId}");
            await base.OnConnectedAsync();
        }

        public override async Task OnDisconnectedAsync(Exception exception)
        {
            _logger.LogInformation($"Client disconnected: {Context.ConnectionId}");
            await base.OnDisconnectedAsync(exception);
        }

        public async Task SubscribeToRegion(double latitude, double longitude, double radiusKm)
        {
            var groupName = $"region_{latitude}_{longitude}_{radiusKm}";
            await Groups.AddToGroupAsync(Context.ConnectionId, groupName);
            _logger.LogInformation($"Client subscribed to region: {groupName}");
        }
    }
}