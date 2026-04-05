package com.example.skytracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.skytracker.model.Flight;
import com.example.skytracker.network.ApiConfig;
import com.example.skytracker.network.SignalRClient;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableException;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {
    private static final String TAG = "ARActivity";
    private static final int CAMERA_PERMISSION_CODE   = 0;
    private static final int LOCATION_PERMISSION_CODE = 1;

    // AR / OpenGL
    private GLSurfaceView surfaceView;
    private Session session;
    private TargetingOverlayView targetingOverlay;
    private boolean installRequested;
    private int textureId = -1;
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

    // Live flight data (updated by SignalR on a background thread)
    private SignalRClient signalRClient;
    private volatile List<Flight> latestFlights = new ArrayList<>();

    // Sensors
    private SensorFusionManager sensorFusion;

    // GPS
    private LocationManager locationManager;
    private volatile double deviceLat  = 0.0;
    private volatile double deviceLon  = 0.0;
    private volatile boolean hasLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView     = findViewById(R.id.surfaceview);
        targetingOverlay = findViewById(R.id.targeting_overlay);

        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        signalRClient  = new SignalRClient();
        sensorFusion   = new SensorFusionManager();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasCameraPermission()) {
            requestCameraPermission();
            return;
        }

        // --- ARCore session setup ---
        if (session == null) {
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }
                session = new Session(this);
            } catch (UnavailableException e) {
                Log.e(TAG, "ARCore not available", e);
                Toast.makeText(this, "ARCore not available", Toast.LENGTH_LONG).show();
                return;
            } catch (SecurityException e) {
                Log.e(TAG, "Camera permission not granted", e);
                return;
            }
        }
        try {
            session.resume();
        } catch (Exception e) {
            Log.e(TAG, "Session resume failed", e);
            session = null;
            return;
        }
        surfaceView.onResume();

        // --- GPS ---
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }

        // --- Sensors ---
        sensorFusion.start(this);

        // --- SignalR ---
        if (!signalRClient.isConnected()) {
            signalRClient.connect(ApiConfig.BASE_URL, flights -> {
                latestFlights = new ArrayList<>(flights);
                Log.d(TAG, "Updated flights: " + flights.size());
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (session != null) {
            surfaceView.onPause();
            session.pause();
        }
        sensorFusion.stop();
        signalRClient.disconnect();
    }

    // -------------------------------------------------------------------------
    // Permissions
    // -------------------------------------------------------------------------

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera granted — onResume will handle it next cycle
            } else {
                Toast.makeText(this, "Camera permission is needed to run this application",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (requestCode == LOCATION_PERMISSION_CODE) {
            if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.w(TAG, "Location permission denied — AR detection will be limited");
                Toast.makeText(this, "Location needed for plane identification", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // -------------------------------------------------------------------------
    // GPS helpers
    // -------------------------------------------------------------------------

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 3000, 5f, location -> {
                        deviceLat   = location.getLatitude();
                        deviceLon   = location.getLongitude();
                        hasLocation = true;
                        Log.d(TAG, "GPS update: " + deviceLat + ", " + deviceLon);
                    });

            // Use last known location immediately while waiting for a fresh fix
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                deviceLat   = last.getLatitude();
                deviceLon   = last.getLongitude();
                hasLocation = true;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission missing", e);
        }
    }

    // -------------------------------------------------------------------------
    // OpenGL / ARCore render loop
    // -------------------------------------------------------------------------

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        backgroundRenderer.createOnGlThread();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        if (session != null) {
            Display display = getWindowManager().getDefaultDisplay();
            session.setDisplayGeometry(display.getRotation(), width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null || textureId == -1) return;

        try {
            session.setCameraTextureName(textureId);
            Frame frame = session.update();
            backgroundRenderer.draw(frame, textureId);

            Camera camera = frame.getCamera();
            if (camera.getTrackingState() == TrackingState.TRACKING && hasLocation) {
                List<Flight> inFrame = ArFlightDetector.getFlightsInFrame(
                        deviceLat, deviceLon,
                        sensorFusion.getAzimuth(), sensorFusion.getPitch(),
                        latestFlights, 20f, 15f);

                if (!inFrame.isEmpty()) {
                    Flight best = inFrame.get(0);
                    Log.d(TAG, "In frame: " + best.getCallsign()
                            + " az=" + sensorFusion.getAzimuth()
                            + " pitch=" + sensorFusion.getPitch());
                }

                final Flight toShow = inFrame.isEmpty() ? null : inFrame.get(0);
                runOnUiThread(() -> targetingOverlay.setFlightInfo(toShow));
            } else {
                runOnUiThread(() -> targetingOverlay.setFlightInfo(null));
            }

        } catch (Exception e) {
            Log.e(TAG, "Draw frame failed", e);
        }
    }
}
