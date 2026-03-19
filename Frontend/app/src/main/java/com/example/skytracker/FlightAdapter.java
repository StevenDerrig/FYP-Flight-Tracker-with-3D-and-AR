package com.example.skytracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.skytracker.model.Flight;

import java.util.List;
import java.util.Locale;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {

    private final List<Flight> flights;

    public FlightAdapter(List<Flight> flights) {
        this.flights = flights;
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flight, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flights.get(position);

        String callsign = (flight.getCallsign() != null && !flight.getCallsign().trim().isEmpty())
                ? flight.getCallsign().trim()
                : flight.getIcaoCode();
        holder.tvCallsign.setText(callsign);

        holder.tvStatus.setText(flight.isOnGround() ? "ON GROUND" : "AIRBORNE");

        String origin = sanitiseAirport(flight.getOriginAirport());
        String dest   = sanitiseAirport(flight.getDestinationAirport());
        holder.tvRoute.setText(origin + " → " + dest);

        holder.tvAltitude.setText(
                String.format(Locale.getDefault(), "Alt: %.0f m", flight.getAltitude()));

        holder.tvSpeed.setText(
                String.format(Locale.getDefault(), "Speed: %.1f m/s", flight.getVelocity()));

        holder.tvCountry.setText(flight.getCountry() != null ? flight.getCountry() : "—");
    }

    @Override
    public int getItemCount() {
        return flights.size();
    }

    private String sanitiseAirport(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("Unknown")) {
            return "—";
        }
        return value.trim();
    }

    static class FlightViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCallsign;
        final TextView tvStatus;
        final TextView tvRoute;
        final TextView tvAltitude;
        final TextView tvSpeed;
        final TextView tvCountry;

        FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCallsign = itemView.findViewById(R.id.tv_callsign);
            tvStatus   = itemView.findViewById(R.id.tv_status);
            tvRoute    = itemView.findViewById(R.id.tv_route);
            tvAltitude = itemView.findViewById(R.id.tv_altitude);
            tvSpeed    = itemView.findViewById(R.id.tv_speed);
            tvCountry  = itemView.findViewById(R.id.tv_country);
        }
    }
}
