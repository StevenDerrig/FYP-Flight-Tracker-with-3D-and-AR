using SkyTracker.API.Models;
using SkyTracker.API.Data;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace SkyTracker.API.Services
{
    public class FlightService : IFlightService
    {
        private readonly AppDbContext _dbContext;
        private readonly ILogger<FlightService> _logger;

        public FlightService(AppDbContext dbContext, ILogger<FlightService> logger)
        {
            _dbContext = dbContext;
            _logger = logger;
        }

        public async Task<List<Flight>> GetAllFlightsAsync()
        {
            try
            {
                return await _dbContext.Flights.ToListAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error: {ex.Message}");
                return new List<Flight>();
            }
        }

        public async Task<Flight> GetFlightByIdAsync(int id)
        {
            try
            {
                return await _dbContext.Flights.FirstOrDefaultAsync(f => f.Id == id);
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error: {ex.Message}");
                return null;
            }
        }

        public async Task AddOrUpdateFlightAsync(Flight flight)
        {
            try
            {
                _dbContext.Flights.Add(flight);
                await _dbContext.SaveChangesAsync();
            }
            catch (Exception ex)
            {
                _logger.LogError($"Error: {ex.Message}");
            }
        }
    }
}