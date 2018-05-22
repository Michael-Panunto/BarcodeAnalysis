package com.mtechhub.barcodeanalysis;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WebDataRetrieval extends AsyncTask<String, Void, String> {

    private static final int READ_TIMEOUT = 15000;

    private static final String TAG = "BarcodeResult";

    public OnRetrievalCompleted listener = null;

    /**
     * Scrapes data from the webpages provided by each url.
     * Results are handled in BarcodeResultActivity
     * @param urls List of Urls given from the barcode capture
     */
    @Override
    protected String doInBackground(String... urls) {
        StringBuilder stringBuilder;
        URL url;
        for (String s : urls) {
            url = null;
            BufferedReader br = null;
            try{
                url = new URL(s);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setReadTimeout(READ_TIMEOUT);
                connection.connect();

                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                return stringBuilder.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "Malformed URL: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "Connection Error: " + e.getMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing reader: " + e.getMessage());
                    }
                }
            }
        }
        return "";
    }

    @Override
    protected void onPostExecute(String webData) {
        listener.onRetrievalCompleted(webData);
    }
}
