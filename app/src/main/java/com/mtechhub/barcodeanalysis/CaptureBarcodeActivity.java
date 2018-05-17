package com.mtechhub.barcodeanalysis;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.mtechhub.barcodeanalysis.camera.CameraSource;
import com.mtechhub.barcodeanalysis.camera.CameraSourcePreview;
import com.mtechhub.barcodeanalysis.camera.GraphicOverlay;

import java.io.IOException;

public class CaptureBarcodeActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {

    private static final String TAG = "CaptureFragment";

    private static final int CAMERA_PERMISSIONS = 0;

    private static final String ACTION_BAR_TITLE = "Capture";

    private static final int GOOGLE_AVAIL = 1;


    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<BarcodeGraphic> graphicOverlay;
    private boolean flash = false;
    private boolean autoFocus = true;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    /*
     *  TODO: App using html
     *  TODO: Mobile Databases
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_barcode);
        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);

        SpannableString ss = new SpannableString(ACTION_BAR_TITLE);
        ss.setSpan(new TypefaceSpan("Kavivanar-Regular.ttf"), 0, ss.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(ss);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, flash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(graphicOverlay, "Tap to capture. Pinch to zoom.",
                Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(getResources().getColor(R.color.navbarColor))
                .setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Snackbar will auto dismiss on click
                    }
                })
                .show();

    }

    private void requestCameraPermission() {
        Log.w(TAG, "Requesting camera permission");
        final String[] cameraPermissions = {Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_PERMISSIONS);
            return;
        }

        final Activity activity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                ActivityCompat.requestPermissions(activity, cameraPermissions, CAMERA_PERMISSIONS);
            }
        };

        findViewById(R.id.layout_top).setOnClickListener(listener);
        Snackbar.make(graphicOverlay, R.string.camera_permission_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != CAMERA_PERMISSIONS) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource(autoFocus, flash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.camera_denied)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent (MotionEvent ev) {
        boolean s = scaleGestureDetector.onTouchEvent(ev);
        boolean t = gestureDetector.onTouchEvent(ev);
        return t || s || super.onTouchEvent(ev);
    }

    private void createCameraSource (boolean autoFocus, boolean flash) {
        Context context = getApplicationContext();

        BarcodeDetector detector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();
        BarcodeTrackerFactory tracker = new BarcodeTrackerFactory(graphicOverlay, this);
        detector.setProcessor(
                new MultiProcessor.Builder<>(tracker).build()
        );

        if (!detector.isOperational()) {
            Log.w(TAG, "Dependencies not yet available");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_err, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_err));
            }
        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;

        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(width, height)
                .setRequestedFps(15.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null
            );
        }

        cameraSource = builder.setFlashMode(
            flash ? Camera.Parameters.FLASH_MODE_TORCH : null
        ).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    private void startCameraSource() throws SecurityException {
        int avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext()
        );
        if (avail != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(
                    this, avail, GOOGLE_AVAIL);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private String[] captureImage() {
        String[] results = new String[graphicOverlay.getGraphics().size()];
        int index = 0;
        for (BarcodeGraphic graphic : graphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            results[index] = barcode.rawValue;
            index++;
        }
        return results;
    }

    private boolean onTap() {
        System.out.println("============ Capturing Image =============");
        if (!graphicOverlay.getGraphics().isEmpty()) {
            String[] results = captureImage();
            for (String s : results) {
                System.out.println(s);
            }

            Intent intent = new Intent(CaptureBarcodeActivity.this,
                    BarcodeResultActivity.class);
            intent.putExtra("results", results);
            startActivity(intent);
            return true;
        }
        return false;
    }
    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e) {
            return onTap() || super.onSingleTapConfirmed(e);
        }

    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector d) { return false; }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector d) { return true; }

        @Override
        public void onScaleEnd(ScaleGestureDetector d) {
            cameraSource.doZoom(d.getScaleFactor());
        }
    }
    @Override
    public void onBarcodeDetected(Barcode barcode) {
        // Can do stuff here?
    }
}