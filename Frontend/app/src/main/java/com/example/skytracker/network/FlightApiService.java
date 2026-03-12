package com.example.skytracker.network;

import com.example.skytracker.model.Flight;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FlightApiService {

    @GET("api/flights")
    Call<List<Flight>> getAllFlights();

    @GET("api/flights/{id}")
    Call<Flight> getFlightById(@Path("id") int id);

    @GET("api/flights/health")
    Call<Object> healthCheck();
}
