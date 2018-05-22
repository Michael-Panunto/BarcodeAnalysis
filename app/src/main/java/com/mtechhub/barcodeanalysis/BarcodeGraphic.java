package com.mtechhub.barcodeanalysis;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.android.gms.vision.barcode.Barcode;
import com.mtechhub.barcodeanalysis.camera.GraphicOverlay;

/**
 * Draws a rectangle overlay for a given barcode
 */
class BarcodeGraphic extends GraphicOverlay.Graphic {

    private int id;
    /* [0] => Green, [1] => Blue, [2] => Yellow */
    private static final int COLOUR_CHOICES[] = {
            Color.rgb(115, 173, 67),
            Color.rgb(68, 191, 170),
            Color.rgb(255,255,0)
    };

    private static int currCoulourIndex = 0;

    private Paint rectPaint;
    private Paint textPaint;
    private volatile Barcode barcode;

    BarcodeGraphic (GraphicOverlay overlay) {
        super(overlay);

        currCoulourIndex = (currCoulourIndex + 1) % COLOUR_CHOICES.length;
        final int selectedColour = COLOUR_CHOICES[currCoulourIndex];

        rectPaint = new Paint();
        rectPaint.setColor(selectedColour);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4.5f);

        textPaint = new Paint();
        textPaint.setColor(selectedColour);
        textPaint.setTextSize(36.0f);
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public Barcode getBarcode() { return barcode; }

    /**
     * Redraws the graphic for a changed barcode
     * @param barcode Barcode to redraw the overlay for
     */
    void updateItem (Barcode barcode) {
        this.barcode = barcode;
        postInvalidate();
    }

    /**
     * Draws the rectangle overlay
     */
    @Override
    public void draw(Canvas canvas) {
        Barcode b = barcode;
        if (b == null) {
            return;
        }

        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);

        canvas.drawRect(rect, rectPaint);
        canvas.drawText(barcode.rawValue, rect.left, rect.bottom, textPaint);

    }
}
