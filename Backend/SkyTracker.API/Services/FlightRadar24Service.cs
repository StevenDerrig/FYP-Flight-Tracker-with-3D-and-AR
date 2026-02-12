using SkyTracker.API.Models;
using System.Text.Json;
using Microsoft.AspNetCore.WebUtilities;

namespace SkyTracker.API.Services
{
    public class FlightRadar24Service : IOpenSkyService
    {
        private readonly IHttpClientFactory _httpClientFactory;
        private readonly IConfiguration _configuration;
        private readonly ILogger<FlightRadar24Service> _logger;

        public FlightRadar24Service(
            IHttpClientFactory httpClientFactory,
            IConfiguration configuration,
            ILogger<FlightRadar24Service> logger)
        {
            _httpClientFactory = httpClientFactory;
            _configuration = configuration;
            _logger = logger;
        }

        public async Task<List<Flight>> GetFlightsAsync(double latMin, double latMax, double lonMin, double lonMax)
        {
            try
            {
                var apiKey = _configuration["FlightRadar24:ApiKey"];
                var baseUrl = _configuration["FlightRadar24:BaseUrl"];

                var client = _httpClientFactory.CreateClient();
                client.DefaultRequestHeaders.Add("Authorization", $"Bearer {apiKey}");
                client.DefaultRequestHeaders.Add("Accept", "application/json");
                client.DefaultRequestHeaders.Add("Accept-Version", "v1");

                // FR24 API requires 'bounds' parameter: north,south,west,east
                var baseUrl2 = $"{baseUrl}/api/live/flight-positions/light";
                var boundsValue = $"{latMax:F4},{latMin:F4},{lonMin:F4},{lonMax:F4}";
                var url = QueryHelpers.AddQueryString(baseUrl2, "bounds", boundsValue);
                _logger.LogInformation("Fetching flights from FR24: {Url}", url);

                var response = await client.GetAsync(url);
                if (!response.IsSuccessStatusCode)
                {
                    var errorContent = await response.Content.ReadAsStringAsync();
                    _logger.LogError("FR24 API call failed: {Status} - Body: {Error}", response.StatusCode, errorContent);
                    return new List<Flight>();
                }

                var content = await response.Content.ReadAsStringAsync();
                _logger.LogInformation("FR24 raw response: {Content}", content);

                using var doc = JsonDocument.Parse(content);
                var root = doc.RootElement;

                var flights = new List<Flight>();

                if (!root.TryGetProperty("data", out var data))
                {
                    _logger.LogWarning("FR24 response has no 'data' property. Full response: {Content}", content);
                    return new List<Flight>();
                }

                var dataCount = data.GetArrayLength();
                _logger.LogInformation("FR24 returned {Count} flights in data array", dataCount);

                foreach (var item in data.EnumerateArray())
                {
                    try
                    {
                        var flight = new Flight
                        {
                            IcaoCode        = item.TryGetProperty("fr24_id", out var id) ? id.GetString() ?? "UNKNOWN" : "UNKNOWN",
                            Callsign        = item.TryGetProperty("callsign", out var cs) ? cs.GetString()?.Trim() ?? "N/A" : "N/A",
                            Latitude        = item.TryGetProperty("lat", out var lat) ? lat.GetDouble() : 0,
                            Longitude       = item.TryGetProperty("lon", out var lon) ? lon.GetDouble() : 0,
                            Altitude        = item.TryGetProperty("alt", out var alt) ? alt.GetDouble() : 0,
                            Velocity        = item.TryGetProperty("gspeed", out var spd) ? spd.GetDouble() : 0,
                            Heading         = item.TryGetProperty("track", out var trk) ? trk.GetDouble() : 0,
                            OriginAirport   = item.TryGetProperty("orig_iata", out var orig) ? orig.GetString() ?? "N/A" : "N/A",
                            DestinationAirport = item.TryGetProperty("dest_iata", out var dest) ? dest.GetString() ?? "N/A" : "N/A",
                            Airline         = item.TryGetProperty("airline_iata", out var airline) ? airline.GetString() : null,
                            AircraftType    = item.TryGetProperty("type", out var type) ? type.GetString() : null,
                            Country         = "",
                            IsOnGround      = item.TryGetProperty("alt", out var altGnd) && altGnd.GetDouble() == 0,
                            FirstSeen       = DateTime.UtcNow,
                            LastSeen        = DateTime.UtcNow,
                            LastPositionUpdate = DateTime.UtcNow,
                        };

                        flights.Add(flight);
                    }
                    catch (Exception ex)
                    {
                        _logger.LogError("Error parsing FR24 flight entry: {Message}", ex.Message);
                    }
                }

                _logger.LogInformation("Fetched {Count} flights from FR24", flights.Count);
                return flights;
            }
            catch (Exception ex)
            {
                _logger.LogError("FR24 GetFlightsAsync error: {Message}", ex.Message);
                return new List<Flight>();
            }
        }

        public async Task<bool> TestConnectionAsync()
        {
            try
            {
                var apiKey = _configuration["FlightRadar24:ApiKey"];
                var baseUrl = _configuration["FlightRadar24:BaseUrl"];

                var client = _httpClientFactory.CreateClient();
                client.DefaultRequestHeaders.Add("Authorization", $"Bearer {apiKey}");
                client.DefaultRequestHeaders.Add("Accept", "application/json");
                client.DefaultRequestHeaders.Add("Accept-Version", "v1");

                // Small bounding box over Ireland as a connection test
                // bounds format: north,south,west,east
                var baseUrl2 = $"{baseUrl}/api/live/flight-positions/light";
                var url = QueryHelpers.AddQueryString(baseUrl2, "bounds", "55.5,51.0,-10.5,-5.5");
                _logger.LogInformation("FR24 test connection URL: {Url}", url);
                var response = await client.GetAsync(url);
                _logger.LogInformation("FR24 test connection response: {Status}", response.StatusCode);
                if (!response.IsSuccessStatusCode)
                {
                    var errorBody = await response.Content.ReadAsStringAsync();
                    _logger.LogError("FR24 test failed - Body: {Error}", errorBody);
                }
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                _logger.LogError("FR24 connection test failed: {Message}", ex.Message);
                return false;
            }
        }
    }
}