package com.example.skytracker.model;

import com.google.gson.annotations.SerializedName;

public class Flight {

    @SerializedName("id")
    private int id;

    @SerializedName("callsign")
    private String callsign;

    @SerializedName("icaoCode")
    private String icaoCode;

    @SerializedName("country")
    private String country;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("altitude")
    private double altitude;

    @SerializedName("velocity")
    private double velocity;

    @SerializedName("heading")
    private double heading;

    @SerializedName("aircraftType")
    private String aircraftType;

    @SerializedName("airline")
    private String airline;

    @SerializedName("originAirport")
    private String originAirport;

    @SerializedName("destinationAirport")
    private String destinationAirport;

    @SerializedName("isOnGround")
    private boolean isOnGround;

    public int getId()                    { return id; }
    public String getCallsign()           { return callsign; }
    public String getIcaoCode()           { return icaoCode; }
    public String getCountry()            { return country; }
    public double getLatitude()           { return latitude; }
    public double getLongitude()          { return longitude; }
    public double getAltitude()           { return altitude; }
    public double getVelocity()           { return velocity; }
    public double getHeading()            { return heading; }
    public String getAircraftType()       { return aircraftType; }
    public String getAirline()            { return airline; }
    public String getOriginAirport()      { return originAirport; }
    public String getDestinationAirport() { return destinationAirport; }
    public boolean isOnGround()           { return isOnGround; }
}
