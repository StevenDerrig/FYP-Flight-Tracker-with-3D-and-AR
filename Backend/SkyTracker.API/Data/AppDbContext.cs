using Microsoft.EntityFrameworkCore;
using SkyTracker.API.Models;
// Data context for the application database

namespace SkyTracker.API.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options)
            : base(options)
        {
            
        }

        public DbSet<Flight> Flights { get; set; }
        public DbSet<Aircraft> Aircraft { get; set; }
        public DbSet<Airport> Airports { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
        }
    }
}