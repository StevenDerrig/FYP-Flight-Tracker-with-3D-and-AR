package com.example.skytracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the buttons by their IDs from the layout
        Button btnArCamera = findViewById(R.id.btn_ar_camera);
        Button btnFlightTracker = findViewById(R.id.btn_flight_tracker);
        Button btnSettings = findViewById(R.id.btn_settings);

        // AR Camera button - launches your existing MainActivity (the AR view)
        btnArCamera.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnFlightTracker.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FlightListActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}