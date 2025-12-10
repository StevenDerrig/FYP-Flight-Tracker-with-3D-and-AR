namespace SkyTracker.API.Models
{
    public class Airport
    {
        public int Id { get; set; }
        public string IcaoCode { get; set; }
        public string IataCode { get; set; }
        public string Name { get; set; }
        public string City { get; set; }
        public string Country { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public int Elevation { get; set; }
        public string Timezone { get; set; }
    }
}