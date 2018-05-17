package com.mtechhub.barcodeanalysis;

import android.content.Context;
import android.support.annotation.UiThread;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.mtechhub.barcodeanalysis.camera.GraphicOverlay;

public class BarcodeGraphicTracker extends Tracker<Barcode> {

    private GraphicOverlay<BarcodeGraphic> overlay;

    private BarcodeGraphic graphic;

    private BarcodeUpdateListener barcodeUpdateListener;

    public interface BarcodeUpdateListener {
        @UiThread
        void onBarcodeDetected(Barcode barcode);
    }

    BarcodeGraphicTracker (GraphicOverlay<BarcodeGraphic> overlay, BarcodeGraphic graphic,
                           Context context) {
        this.overlay = overlay;
        this.graphic = graphic;

        if (context instanceof BarcodeUpdateListener) {
            this.barcodeUpdateListener = (BarcodeUpdateListener) context;
        } else {
            throw new RuntimeException("Hosting activity must implement BarcodeUpdateListener");
        }
    }

    @Override
    public void onNewItem (int id, Barcode item) {
        graphic.setId(id);
        barcodeUpdateListener.onBarcodeDetected(item);
    }

    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode item) {
        overlay.add(graphic);
        graphic.updateItem(item);
    }

    @Override
    public void onMissing(Detector.Detections<Barcode> detectionResults) {
        overlay.remove(graphic);
    }

    @Override
    public void onDone() { overlay.remove(graphic); }
}
