using SkyTracker.API.Models;
using Microsoft.Extensions.Logging;
using SkyTracker.API.Data;
using System.Text.Json;

namespace SkyTracker.API.Services
{
    public class OpenSkyService : IOpenSkyService
    {
        private readonly HttpClient _httpClient;
        private readonly ILogger<OpenSkyService> _logger;
        private readonly AppDbContext _dbContext;
        private const string OpenSkyURL = "https://opensky-network.org/api/states/all";

        public OpenSkyService(HttpClient httpClient, ILogger<OpenSkyService> logger, AppDbContext dbContext)
        {
            _httpClient = httpClient;
            _logger = logger;
            _dbContext = dbContext;
        }

        public async Task<List<Flight>> GetFlightsAsync(
            double latMin, double latMax,
            double lonMin, double lonMax
        )
        {
            try
            {
                _logger.LogInformation("Fetching flights in bounds");

                var response = await _httpClient.GetAsync(OpenSkyURL);
                if (!response.IsSuccessStatusCode)
                {
                    _logger.LogError("OpenSky API call failed");
                    return new List<Flight>();
                }

                var content = await response.Content.ReadAsStringAsync();
                using var doc = JsonDocument.Parse(content);
                var root = doc.RootElement;

                var flights = new List<Flight>();
                var states = root.GetProperty("states");

                foreach (var state in states.EnumerateArray())
                {
                    try
                    {
                        var flight = new Flight
                        {
                            IcaoCode = state[0].GetString() ?? "UNKNOWN",
                            Callsign = state[1].GetString()?.Trim() ?? "N/A",
                            Country = state[2].GetString() ?? "",
                            LastPositionUpdate = DateTime.UtcNow,
                            Longitude = state[5].GetDouble(),
                            Latitude = state[6].GetDouble(),
                            Altitude = state[7].GetDouble(),
                            Velocity = state[9].GetDouble(),
                            Heading = state[10].GetDouble(),
                            OriginAirport = state[11].GetString() ?? "N/A",
                            DestinationAirport = state[12].GetString() ?? "N/A",
                            FirstSeen = DateTime.UtcNow,
                            LastSeen = DateTime.UtcNow
                        };

                        flights.Add(flight);
                    }

                    catch (Exception ex)
                    {
                        _logger.LogError($"Error fetching flights: {ex.Message}");
                    }
                }

                var limitedFlights = flights.Take(20).ToList();

                // Store in database
                foreach (var flight in limitedFlights)
                {
                    var existing = _dbContext.Flights.FirstOrDefault(f => f.IcaoCode == flight.IcaoCode);
                    if (existing != null)
                    {
                        existing.Latitude = flight.Latitude;
                        existing.Longitude = flight.Longitude;
                        existing.Altitude = flight.Altitude;
                        existing.Velocity = flight.Velocity;
                        existing.Heading = flight.Heading;
                        existing.LastPositionUpdate = DateTime.UtcNow;
                    }
                    else
                    {
                        _dbContext.Flights.Add(flight);
                    }
                }

                await _dbContext.SaveChangesAsync();
                _logger.LogInformation($"Stored {limitedFlights.Count} flights");
                return limitedFlights;
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error: {ex.Message}");
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