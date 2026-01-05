package com.goldbijoux.maboutique;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;
import android.view.View;

public class RingCircleView extends View {

    private Paint circlePaint;
    private Paint innerCirclePaint;
    private Paint shadowPaint;
    private Paint guidePaint;
    private float circleRadius = 200f;
    private float diameterMm = 16.6f;


    private static final float PIXELS_PER_MM = 12f;

    public RingCircleView(Context context) {
        super(context);
        init();
    }

    public RingCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(8f);
        circlePaint.setColor(0xFF8B4513); // Brown color
        circlePaint.setShadowLayer(10f, 0f, 0f, 0x40000000);

        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setStyle(Paint.Style.STROKE);
        innerCirclePaint.setStrokeWidth(2f);
        innerCirclePaint.setColor(0xFFD4AF37); // Gold color
        innerCirclePaint.setPathEffect(new DashPathEffect(new float[]{10, 5}, 0));


        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(20f);
        shadowPaint.setColor(0x208B4513); // Light brown shadow


        guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(1f);
        guidePaint.setColor(0x40000000);
        guidePaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));

        setLayerType(LAYER_TYPE_SOFTWARE, null); // Enable shadow
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        drawGrid(canvas, centerX, centerY);

        canvas.drawCircle(centerX, centerY, circleRadius, shadowPaint);


        canvas.drawCircle(centerX, centerY, circleRadius - 15, innerCirclePaint);

        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);

        drawCrosshair(canvas, centerX, centerY);

        drawDiameterLine(canvas, centerX, centerY);
    }

    private void drawGrid(Canvas canvas, float centerX, float centerY) {
        Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(0.5f);
        gridPaint.setColor(0x10000000);

        for (int i = 0; i < getWidth(); i += 50) {
            canvas.drawLine(i, 0, i, getHeight(), gridPaint);
        }

        for (int i = 0; i < getHeight(); i += 50) {
            canvas.drawLine(0, i, getWidth(), i, gridPaint);
        }
    }

    private void drawCrosshair(Canvas canvas, float centerX, float centerY) {
        canvas.drawLine(centerX - circleRadius - 30, centerY,
                centerX - circleRadius, centerY, guidePaint);
        canvas.drawLine(centerX + circleRadius, centerY,
                centerX + circleRadius + 30, centerY, guidePaint);

        canvas.drawLine(centerX, centerY - circleRadius - 30,
                centerX, centerY - circleRadius, guidePaint);
        canvas.drawLine(centerX, centerY + circleRadius,
                centerX, centerY + circleRadius + 30, guidePaint);
    }

    private void drawDiameterLine(Canvas canvas, float centerX, float centerY) {
        // Draw diameter measurement line
        Paint measurePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        measurePaint.setStyle(Paint.Style.STROKE);
        measurePaint.setStrokeWidth(2f);
        measurePaint.setColor(0xFFD4AF37); // Gold color

        canvas.drawLine(centerX - circleRadius, centerY,
                centerX + circleRadius, centerY, measurePaint);

        // Draw small arrows at ends
        drawArrow(canvas, centerX - circleRadius, centerY, true, measurePaint);
        drawArrow(canvas, centerX + circleRadius, centerY, false, measurePaint);
    }

    private void drawArrow(Canvas canvas, float x, float y, boolean pointLeft, Paint paint) {
        Path arrow = new Path();
        float arrowSize = 15f;

        if (pointLeft) {
            arrow.moveTo(x, y);
            arrow.lineTo(x + arrowSize, y - arrowSize / 2);
            arrow.lineTo(x + arrowSize, y + arrowSize / 2);
            arrow.close();
        } else {
            arrow.moveTo(x, y);
            arrow.lineTo(x - arrowSize, y - arrowSize / 2);
            arrow.lineTo(x - arrowSize, y + arrowSize / 2);
            arrow.close();
        }

        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(arrow, paint);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setDiameterMm(float diameterMm) {
        this.diameterMm = diameterMm;
        this.circleRadius = (diameterMm * PIXELS_PER_MM) / 2f;
        invalidate(); // Redraw
    }

    public float getDiameterMm() {
        return diameterMm;
    }

    public void increaseDiameter() {
        setDiameterMm(diameterMm + 0.3f); // Increase by 0.3mm
    }

    public void decreaseDiameter() {
        if (diameterMm > 10f) { // Minimum size
            setDiameterMm(diameterMm - 0.3f); // Decrease by 0.3mm
        }
    }
}