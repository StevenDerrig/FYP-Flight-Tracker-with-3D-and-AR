package com.example.skytracker.network;

public class ApiConfig {

    // Emulator (AVD): host machine is reachable via 10.0.2.2
    // Physical device on same WiFi: replace with your PC's LAN IP, e.g. "http://192.168.1.45:5136/"
    //   (run `ipconfig` on Windows → "Wireless LAN adapter Wi-Fi" → IPv4 Address)
    // Physical device also requires the backend to bind to all interfaces:
    //   dotnet run --urls "http://0.0.0.0:5136"
    public static final String BASE_URL = "http://10.64.108.241:5136/";

    private ApiConfig() {}
}
