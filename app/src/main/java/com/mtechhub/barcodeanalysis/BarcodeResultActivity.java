package com.mtechhub.barcodeanalysis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

/**
 * Handles the Barcode data provided through the CaptureBarcodeActivity
 */
public class BarcodeResultActivity extends AppCompatActivity implements OnRetrievalCompleted{

    private static final String TAG = "BarcodeResult";

    private TextView tv;

    String setTextErr = "Could not retrieve JSON data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_result);

        // Urls sent over from the previous activity
        String[] results = getIntent().getStringArrayExtra("results");
        tv  = findViewById(R.id.barcodeResults);
        tv.setMovementMethod(new ScrollingMovementMethod());

        // Begins a retrieval of from webpages provided with the intent urls
        WebDataRetrieval retrieval = new WebDataRetrieval();
        retrieval.listener = this;
        retrieval.execute(results);
    }

    /**
     * Implementation of the OnRetrievalCompleted interface, used to retrieve async task results
     * @param webData Data scraped from the webpages
     */
    @Override
    public void onRetrievalCompleted (String webData) {
        if (!webData.isEmpty()) {
            // Currently displays results in a textview, can change as needed
            tv.setText(webData);
        } else {
            tv.setText(setTextErr);
        }
    }

}
