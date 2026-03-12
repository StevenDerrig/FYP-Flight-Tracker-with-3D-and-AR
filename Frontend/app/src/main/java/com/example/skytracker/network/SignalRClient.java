package com.example.skytracker.network;

import android.util.Log;

import com.example.skytracker.model.Flight;
import com.google.gson.reflect.TypeToken;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import io.reactivex.rxjava3.core.Completable;

import java.util.List;

public class SignalRClient {
    private static final String TAG = "SignalRClient";
    private HubConnection hubConnection;
    private FlightUpdateListener listener;

    public interface FlightUpdateListener {
        void onFlightsReceived(List<Flight> flights);
    }

    public void connect(String serverUrl, FlightUpdateListener flightListener) {
        this.listener = flightListener;
        Log.d(TAG, "Attempting to connect to: " + serverUrl + "/flightHub");

        try {
            // Build WITHOUT withAutomaticReconnect() to avoid the error
            hubConnection = HubConnectionBuilder
                    .create(serverUrl + "/flightHub")
                    .build();

            // 1. Manually handle disconnection to trigger a reconnect
            hubConnection.onClosed(ex -> {
                Log.e(TAG, "SignalR disconnected: " + (ex != null ? ex.getMessage() : "unknown reason"));
                Log.d(TAG, "SignalR disconnected. Retrying in 5 seconds...");
                // Use a handler to wait before retrying (prevents infinite fast loops)
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    // Check if listener still exists (hasn't been disconnected manually)
                    if (listener != null) {
                        connect(serverUrl, listener);
                    }
                }, 5000);
            });

            hubConnection.on("ReceiveFlightUpdate", (flights) -> {
                if (flights instanceof List) {
                    List<Flight> flightList = (List<Flight>) flights;
                    if (listener != null) {
                        listener.onFlightsReceived(flightList);
                    }
                }
            }, new TypeToken<List<Flight>>(){}.getType());

            // 2. Start using the RxJava subscribe method (more robust than new Thread)
            hubConnection.start().subscribe(
                    () -> Log.d(TAG, "SignalR connected successfully"),
                    throwable -> Log.e(TAG, "SignalR connection failed: " + throwable.getMessage())
            );

        } catch (Exception e) {
            Log.e(TAG, "Error building SignalR connection: " + e.getMessage(), e);
        }
    }

    public void disconnect() {
        if (hubConnection != null) {
            new Thread(() -> {
                try {
                    hubConnection.stop();
                    Log.d(TAG, "SignalR disconnected");
                } catch (Exception e) {
                    Log.e(TAG, "Error disconnecting: " + e.getMessage(), e);
                }
            }).start();
        }
    }

    public boolean isConnected() {
        if (hubConnection == null) return false;
        return hubConnection.getConnectionState() == com.microsoft.signalr.HubConnectionState.CONNECTED;
    }
}
