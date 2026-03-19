package com.example.skytracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.skytracker.model.Flight;
import com.example.skytracker.network.SignalRClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class FlightListActivity extends AppCompatActivity {

    private static final String TAG = "FlightListActivity";
    private GoogleMap googleMap;
    private SignalRClient signalRClient;
    private TextView tvStatus;
    private List<String> markerIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight_list);

        tvStatus = findViewById(R.id.tv_status);

        // Get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            Log.d(TAG, "Map fragment found, requesting async map");
            mapFragment.getMapAsync(map -> {
                Log.d(TAG, "Map async callback received");
                googleMap = map;
                // Center on Ireland/UK
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(53.0, -8.0), 6));
                Log.d(TAG, "Map initialized and centered");
            });
        } else {
            Log.e(TAG, "Map fragment is null!");
            tvStatus.setText("Error: Map fragment not found");
        }

        // Initialize SignalR
        signalRClient = new SignalRClient();
        connectToSignalR();
    }

    private void connectToSignalR() {
        signalRClient.connect("http://192.168.1.113:5136", flights -> {
            runOnUiThread(() -> updateMapWithFlights(flights));
        });
    }

    private void updateMapWithFlights(List<Flight> flights) {
        if (googleMap == null) return;

        // Clear old markers
        googleMap.clear();
        markerIds.clear();

        if (flights.isEmpty()) {
            tvStatus.setText("Waiting for flights...");
            tvStatus.setVisibility(android.view.View.VISIBLE);
            return;
        }

        tvStatus.setVisibility(android.view.View.GONE);

        // Add new markers
        for (Flight flight : flights) {
            LatLng position = new LatLng(flight.getLatitude(), flight.getLongitude());

            String title = flight.getCallsign() != null ? flight.getCallsign() : "Unknown";
            String snippet = String.format("Alt: %.0f ft | Spd: %.0f kts\n%s → %s",
                    flight.getAltitude(),
                    flight.getVelocity(),
                    flight.getOriginAirport() != null ? flight.getOriginAirport() : "N/A",
                    flight.getDestinationAirport() != null ? flight.getDestinationAirport() : "N/A");

            MarkerOptions marker = new MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet);

            googleMap.addMarker(marker);
            markerIds.add(flight.getId() + "");
        }

        Log.d(TAG, "Displayed " + flights.size() + " flights on map");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (signalRClient != null && !signalRClient.isConnected()) {
            connectToSignalR();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (signalRClient != null) {
            signalRClient.disconnect();
        }
    }
}
