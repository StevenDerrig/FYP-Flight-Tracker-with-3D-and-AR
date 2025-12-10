using SkyTracker.API.Models;
using Microsoft.Extensions.Logging;

namespace SkyTracker.API.Services
{
    public class OpenSkyService : IOpenSkyService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<OpenSkyService> _logger;

        public OpenSkyService(HttpClient httpClient, ILogger<OpenSkyService> logger)
        {
            _httpClient = httpClient;
            _logger = logger;
        }

        public async Task<List<Flight>> GetFlightsAsync(
            double latMin, double latMax,
            double lonMin, double lonMax
        )
        {
            try
            {
                _logger.LogInformation("Fetching flights in bounds");
                // Add OpenSky API here
                return new List<Flight>();
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error fetching flights: {ex.Message}");
                return new List<Flight>();
            }
        }

        public async Task<bool> TestConnectionAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync("https://opensky-network.org/api/states/all");
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                _logger.LogError($"OpenSky connection test failed: {ex.Message}");
                return false;
            }
        }
    }
}