namespace SkyTracker.API.Models
{
    public class Aircraft
    {
        public int Id { get; set; }
        public string IcaoCode { get; set; }
        public string Registration { get; set; }
        public string Model { get; set; }
        public string Airline { get; set; }
        public DateTime FirstSeen { get; set; }
        public DateTime LastSeen { get; set; }
        public ICollection<Flight> Flights { get; set; } = new List<Flight>();
    }
}