package com.example.skytracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.skytracker.model.Flight;

public class TargetingOverlayView extends View {
    private Paint paint;
    private Paint textPaint;
    private Paint subtextPaint;
    private RectF targetRect;
    private boolean isTargeting = false;
    private Flight targetedFlight = null;

    public TargetingOverlayView(Context context) {
        super(context);
        init();
    }

    public TargetingOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TargetingOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.CYAN);
        textPaint.setTextSize(52f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtextPaint.setColor(Color.WHITE);
        subtextPaint.setTextSize(38f);
        subtextPaint.setTextAlign(Paint.Align.CENTER);

        targetRect = new RectF();
    }

    public void setFlightInfo(Flight flight) {
        targetedFlight = flight;
        isTargeting = (flight != null);
        if (isTargeting) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float size = 150f;
            targetRect.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        }
        invalidate();
    }

    public void setTarget(float x, float y, float size) {
        targetRect.set(x - size / 2, y - size / 2, x + size / 2, y + size / 2);
        isTargeting = true;
        invalidate();
    }

    public void clearTarget() {
        targetedFlight = null;
        isTargeting = false;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isTargeting) return;

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Crosshair box
        canvas.drawRect(targetRect, paint);

        // Corner ticks
        float length = targetRect.width() / 4;
        canvas.drawLine(targetRect.left,  targetRect.top,    targetRect.left + length, targetRect.top,    paint);
        canvas.drawLine(targetRect.left,  targetRect.top,    targetRect.left,  targetRect.top + length,    paint);
        canvas.drawLine(targetRect.right, targetRect.top,    targetRect.right - length, targetRect.top,   paint);
        canvas.drawLine(targetRect.right, targetRect.top,    targetRect.right, targetRect.top + length,   paint);
        canvas.drawLine(targetRect.left,  targetRect.bottom, targetRect.left + length, targetRect.bottom, paint);
        canvas.drawLine(targetRect.left,  targetRect.bottom, targetRect.left,  targetRect.bottom - length, paint);
        canvas.drawLine(targetRect.right, targetRect.bottom, targetRect.right - length, targetRect.bottom, paint);
        canvas.drawLine(targetRect.right, targetRect.bottom, targetRect.right, targetRect.bottom - length, paint);

        // Flight info text above the crosshair
        if (targetedFlight != null) {
            String callsign = targetedFlight.getCallsign() != null ? targetedFlight.getCallsign() : "UNKNOWN";
            String stats    = String.format("Alt: %.0f ft   Spd: %.0f kts",
                    targetedFlight.getAltitude(), targetedFlight.getVelocity());
            String route    = (targetedFlight.getOriginAirport() != null ? targetedFlight.getOriginAirport() : "?")
                            + "  →  "
                            + (targetedFlight.getDestinationAirport() != null ? targetedFlight.getDestinationAirport() : "?");

            canvas.drawText(callsign, cx, cy - 210, textPaint);
            canvas.drawText(stats,    cx, cy - 155, subtextPaint);
            canvas.drawText(route,    cx, cy - 105, subtextPaint);
        }
    }
}