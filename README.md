# Sky Tracker: Real-Time Aviation Tracking and AR Identification

**Steven Derrig — B.Sc. (Hons) Software Development, ATU Galway**
*Final Year Project — Applied Project & Minor Dissertation*

---

### Link to Screencast
[Final Year Project Demo](https://youtu.be/W-7YeE6EylQ)

## Overview

Sky Tracker is a mobile application for tracking and identifying aircraft in real time using Augmented Reality. The system is designed for aviation enthusiasts, pilots in training, and anyone curious about the air traffic around them.

Users can point their phone at the sky and receive real-time information about aircraft overhead, similar in concept to how 'Shazam' identifies music. The app also provides a traditional map-based view of nearby flights.

The system consists of two components:

- **Android Mobile App** — AR camera view, map display of nearby flights, real-time data via SignalR
- **ASP.NET Core Backend API** — aggregates live flight data from FlightRadar24, serves it to the mobile client via REST and SignalR WebSocket

### Technology Stack

| Layer | Technology |
|---|---|
| Mobile (Frontend) | Android (Java), Google ARCore, Google Maps SDK |
| Backend | ASP.NET Core, SignalR, Swagger/OpenAPI |
| Database | PostgreSQL with PostGIS (schema defined) |
| Flight Data | FlightRadar24 API (primary), OpenSky Network API (prototyping) |
| Real-time | SignalR WebSocket hub (`/flightHub`) |

---

## Project Structure

```
FYP Work/
├── Backend/
│   └── SkyTracker.API/         # ASP.NET Core REST API + SignalR hub
├── Frontend/                   # Android Studio project (Java) and prototype
```

---

## Running the Project

### Prerequisites

- [.NET 8 SDK](https://dotnet.microsoft.com/download)
- [Android Studio](https://developer.android.com/studio) (Hedgehog or later)
- A physical Android device with ARCore support (Android 7.0+)
- PostgreSQL with PostGIS installed (optional — app runs without it in stateless mode)
- A FlightRadar24 API key (for live flight data)

---

### 1. Backend (ASP.NET Core API)

```bash
cd Backend/SkyTracker.API
```

Copy the settings template and fill in your credentials:

```bash
cp appsettings.template.json appsettings.json
```

Edit `appsettings.json` and set:
- `ConnectionStrings:DefaultConnection` — your PostgreSQL connection string (leave empty to skip database persistence)
- `FlightRadar24:ApiKey` — your FlightRadar24 API key

Run the API:

```bash
dotnet run
```

The API starts on `http://localhost:5000` by default. Swagger UI is available at `http://localhost:5000/api/swagger`.

The backend will begin polling FlightRadar24 every 30 seconds and broadcasting flight data to connected clients via the SignalR hub at `/flightHub`.

#### API Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/flights/nearby` | GET | Flights within a geographic bounding box |
| `/api/flights/{id}` | GET | Specific flight details |
| `/api/health` | GET | System health check |
| `/api/swagger` | GET | Interactive API documentation |

---

### 2. Android App (Frontend)

Open the `Frontend/` folder in **Android Studio**.

Before building, update the backend URL in the SignalR client configuration to point to your running backend (e.g. `http://10.0.2.2:5000/flightHub` for an emulator, or your machine's local IP for a physical device).

Connect a physical Android device with USB debugging enabled and click **Run**. The emulator has limited AR support — a physical device is required for the AR camera view.

The app has three screens accessible from the home screen:

- **AR Camera** — live camera feed with ARCore targeting overlay
- **Flight Tracker** — Google Maps view showing nearby aircraft markers with callsign, altitude, speed, and route
- **Settings** — placeholder for future configuration

---

## Features

### Implemented
- Real-time flight data from the FlightRadar24 API (15–20 flights per query)
- Google Maps view with live aircraft markers
- AR camera feed using Google ARCore and OpenGL ES 2.0
- Targeting reticle overlay in AR view
- SignalR real-time data streaming from backend to mobile
- RESTful backend API with Swagger documentation
- PostgreSQL + PostGIS database schema (schema defined; persistence is a planned enhancement)

### Partially Implemented
- GPS continuous location updates (permissions and LocationManager initialised; listener not yet wired)
- Sensor fusion for aircraft identification (bearing calculation framework in place; matching algorithm in progress)
- AR flight information overlay (UI framework built; auto-population pending sensor fusion completion)

---

## Architecture

The backend runs a `FlightBroadcastService` background thread that polls FlightRadar24 every 30 seconds over a bounding box covering the Atlantic and Europe (35N–65N, 30W–20E). Results are broadcast to all connected Android clients via SignalR. The Android app maintains a local snapshot of nearby flights that the AR detection logic reads each render frame.

---

## Notes

- The app was developed and tested on Samsung Android phones.
- The FlightRadar24 API requires a paid subscription; credit tokens are consumed per call. The app limits queries to 20 flights per request.
- The `Practice AR/` folder contains the initial Unity AR Foundation prototype, which was superseded by the native Android implementation following the January 2026 scope refinement.
