using SkyTracker.API.Models;

namespace SkyTracker.API.Services
{
    public interface IOpenSkyService
    {
        Task<List<Flight>> GetFlightsAsync(double latMin, double latMax, double lonMin, double lonMax);
        Task<bool> TestConnectionAsync();
    }
}