package com.example.skytracker;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorFusionManager implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;

    private volatile float azimuthDegrees = 0f;
    private volatile float pitchDegrees   = 0f;

    public void start(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) return;

        float[] rotMatrix  = new float[9];
        float[] orientation = new float[3];

        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values);
        SensorManager.getOrientation(rotMatrix, orientation);

        // orientation[0] = azimuth (radians), orientation[1] = pitch (radians)
        // Azimuth: convert to 0-360 positive-north range
        azimuthDegrees = (float) ((Math.toDegrees(orientation[0]) + 360) % 360);
        // Pitch: Android raw pitch is negative when looking up — negate to make positive = looking up
        pitchDegrees   = (float) -Math.toDegrees(orientation[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public float getAzimuth() { return azimuthDegrees; }
    public float getPitch()   { return pitchDegrees; }
}
