using SkyTracker.API.Models;

namespace SkyTracker.API.Services
{
    public interface IFlightService
    {
        Task<List<Flight>> GetAllFlightsAsync();
        Task<Flight> GetFlightByIdAsync(int id);
        Task AddOrUpdateFlightAsync(Flight flight);
    }
}