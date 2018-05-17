package com.mtechhub.barcodeanalysis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class BarcodeResultActivity extends AppCompatActivity implements OnRetrievalCompleted{

    private static final String TAG = "BarcodeResult";

    private TextView tv;

    String setTextErr = "Could not retrieve JSON data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_result);
        String[] results = getIntent().getStringArrayExtra("results");
        tv  = findViewById(R.id.barcodeResults);
        tv.setMovementMethod(new ScrollingMovementMethod());
        WebDataRetrieval retrieval = new WebDataRetrieval();
        retrieval.listener = this;
        retrieval.execute(results);
    }

    @Override
    public void onRetrievalCompleted (String webData) {
        if (!webData.isEmpty()) {
            tv.setText(webData);
        } else {
            tv.setText(setTextErr);
        }
    }

}
