// API Configuration
const API_BASE_URL = 'http://localhost:5136/api/flights';

// Initialize map centered on world view
const map = L.map('map').setView([20, 0], 2);

// Add tile layer (OpenStreetMap)
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 19,
    minZoom: 2
}).addTo(map);

// Store flight markers for easy updates
const flightMarkers = {};

// Fetch and display flights
async function loadFlights() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }
        
        const flights = await response.json();
        
        // Limit to 20 flights
        const displayFlights = flights.slice(0, 20);
        
        // Clear existing markers
        Object.values(flightMarkers).forEach(marker => map.removeLayer(marker));
        Object.keys(flightMarkers).forEach(key => delete flightMarkers[key]);
        
        // Display flights in table
        displayFlightsInTable(displayFlights);
        
        // Add markers to map
        displayFlights.forEach(flight => {
            if (flight.latitude && flight.longitude) {
                addFlightMarker(flight);
            }
        });
        
        console.log(`Loaded ${displayFlights.length} flights`);
    } catch (error) {
        console.error('Error loading flights:', error);
        const tbody = document.getElementById('flightsBody');
        tbody.innerHTML = `<tr><td colspan="7" class="loading">Error loading flights: ${error.message}</td></tr>`;
    }
}

// Add flight marker to map
function addFlightMarker(flight) {
    const lat = flight.latitude;
    const lon = flight.longitude;
    const flightNum = flight.callsign || 'Unknown';
    
    // Create a simple marker
    const marker = L.circleMarker([lat, lon], {
        radius: 8,
        fillColor: '#667eea',
        color: '#764ba2',
        weight: 2,
        opacity: 1,
        fillOpacity: 0.8
    }).bindPopup(`
        <strong>${flightNum}</strong><br>
        From: ${flight.originAirport || 'N/A'}<br>
        To: ${flight.destinationAirport || 'N/A'}<br>
        Alt: ${flight.altitude || 'N/A'} ft<br>
        Speed: ${flight.velocity || 'N/A'} kts
    `).addTo(map);
    
    flightMarkers[flight.id] = marker;
}

// Display flights in table
function displayFlightsInTable(flights) {
    const tbody = document.getElementById('flightsBody');
    
    if (flights.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No flights available</td></tr>';
        return;
    }
    
    tbody.innerHTML = flights.map(flight => {
        const status = flight.altitude < 500 && flight.velocity < 50 ? 'Landing/Landed' : 'In Air';
        return `
        <tr onclick="map.setView([${flight.latitude || 0}, ${flight.longitude || 0}], 5)">
            <td>${flight.callsign || 'N/A'}</td>
            <td>${flight.originAirport || 'N/A'}</td>
            <td>${flight.destinationAirport || 'N/A'}</td>
            <td>${flight.aircraftType || 'N/A'}</td>
            <td>${status}</td>
            <td>${flight.altitude ? flight.altitude.toLocaleString() : 'N/A'}</td>
            <td>${flight.velocity ? flight.velocity.toFixed(2) : 'N/A'}</td>
        </tr>
    `;
    }).join('');
}

// Load flights on page load
document.addEventListener('DOMContentLoaded', loadFlights);

// Refresh flights every 10 seconds
setInterval(loadFlights, 10000);
