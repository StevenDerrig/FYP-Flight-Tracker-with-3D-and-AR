// Model for the Flight information
namespace SkyTracker.API.Models
{
    public class Flight
    {
        public int Id {get; set;}
        public string Callsign {get; set;}
        public string IcaoCode {get; set;}
        public string Country { get; set; }
        public double Latitude {get; set;}
        public double Longitude {get; set;}
        public double Altitude {get; set;}
        public double Velocity {get; set;}
        public double Heading {get; set;}
        public string? AircraftType {get; set;}
        public string? Airline {get; set;}
        public string OriginAirport {get; set;}
        public string DestinationAirport {get; set;}
        public DateTime FirstSeen { get; set; }
        public DateTime LastSeen { get; set; }
        public DateTime LastPositionUpdate { get; set; }
        public bool IsOnGround { get; set; }
        public int? AircraftId { get; set; }
        public Aircraft Aircraft { get; set; }
    }
}