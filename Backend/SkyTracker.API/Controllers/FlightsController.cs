using Microsoft.AspNetCore.Mvc;
using SkyTracker.API.Models;
using SkyTracker.API.Services;

namespace SkyTracker.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class FlightsController : ControllerBase
    {
        private readonly IFlightService _flightService;
        private readonly IOpenSkyService _openSkyService;
        private readonly ILogger<FlightsController> _logger;

        public FlightsController(
            IFlightService flightService,
            IOpenSkyService openSkyService,
            ILogger<FlightsController> logger
        )
        {
            _flightService = flightService;
            _openSkyService = openSkyService;
            _logger = logger;
        }

        [HttpGet]
        public async Task<ActionResult<List<Flight>>> GetAllFlights()
        {
            var flights = await _flightService.GetAllFlightsAsync();
            return Ok(flights);
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<Flight>> GetFlight(int id)
        {
            var flight = await _flightService.GetFlightByIdAsync(id);
            if (flight == null)
                return NotFound();
            return Ok(flight);
        }

        [HttpGet("health")]
        public async Task<ActionResult<object>> HealthCheck()
        {
            var openSkyConnected = await _openSkyService.TestConnectionAsync();
            return Ok(new
            {
                status = "healthy",
                openSkyConnected = openSkyConnected,
                timestamp = DateTime.UtcNow
            });
        }
    }
}