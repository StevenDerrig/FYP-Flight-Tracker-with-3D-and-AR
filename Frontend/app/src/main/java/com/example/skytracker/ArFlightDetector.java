package com.example.skytracker;

import com.example.skytracker.model.Flight;

import java.util.ArrayList;
import java.util.List;

public class ArFlightDetector {

    /**
     * Great-circle bearing from device GPS position to flight GPS position.
     * Returns degrees 0–360, where 0 = North.
     */
    public static double calculateBearing(double devLat, double devLon,
                                          double fltLat, double fltLon) {
        double dLon = Math.toRadians(fltLon - devLon);
        double x = Math.sin(dLon) * Math.cos(Math.toRadians(fltLat));
        double y = Math.cos(Math.toRadians(devLat)) * Math.sin(Math.toRadians(fltLat))
                 - Math.sin(Math.toRadians(devLat)) * Math.cos(Math.toRadians(fltLat)) * Math.cos(dLon);
        return (Math.toDegrees(Math.atan2(x, y)) + 360) % 360;
    }

    /**
     * Elevation angle above the horizon to the flight.
     * altFeet is the flight altitude in feet (as reported by FlightRadar24).
     * Returns degrees — positive means above the horizon.
     */
    public static double calculateElevation(double devLat, double devLon,
                                            double fltLat, double fltLon,
                                            double altFeet) {
        double altMetres = altFeet * 0.3048;
        double R = 6_371_000.0;

        double dLat = Math.toRadians(fltLat - devLat);
        double dLon = Math.toRadians(fltLon - devLon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(devLat)) * Math.cos(Math.toRadians(fltLat))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double dist = R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        if (dist < 1.0) return 90.0; // directly overhead
        return Math.toDegrees(Math.atan2(altMetres, dist));
    }

    /**
     * Shortest angular difference between two angles (handles 0/360 wrap).
     */
    public static double angleDiff(double a, double b) {
        double d = Math.abs(a - b) % 360;
        return d > 180 ? 360 - d : d;
    }

    /**
     * Returns flights whose computed bearing and elevation fall within the given
     * tolerances of the device's current azimuth and pitch.
     *
     * azTol and pitchTol are in degrees. Start loose (20°/15°) for demo reliability.
     */
    public static List<Flight> getFlightsInFrame(double devLat, double devLon,
                                                  float azimuth, float pitch,
                                                  List<Flight> all,
                                                  float azTol, float pitchTol) {
        List<Flight> result = new ArrayList<>();
        for (Flight f : all) {
            if (f.isOnGround() || f.getAltitude() <= 0) continue;

            double bearing   = calculateBearing(devLat, devLon, f.getLatitude(), f.getLongitude());
            double elevation = calculateElevation(devLat, devLon, f.getLatitude(), f.getLongitude(), f.getAltitude());

            if (angleDiff(bearing, azimuth) <= azTol && Math.abs(elevation - pitch) <= pitchTol) {
                result.add(f);
            }
        }
        return result;
    }
}
