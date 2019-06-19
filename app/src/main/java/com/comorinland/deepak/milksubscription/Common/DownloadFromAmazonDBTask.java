package com.comorinland.deepak.milksubscription.Common;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by deepak on 15/1/18.
 */

public class DownloadFromAmazonDBTask extends AsyncTask<JsonObject, Void, String>
{
    String mServerFileName;
    public ResponseHandler mResponseHandler = null;
    ProgressDialog mProgressDialog;

    public DownloadFromAmazonDBTask(ResponseHandler callback, String fileName, ProgressDialog progressDialog)
    {
        mResponseHandler = callback;
        mServerFileName = fileName;
        mProgressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute()
    {
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(JsonObject... params)
    {

        final String urlName = "http://192.168.43.81/MilkMan/VendorAppPhp/";

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String JsonResponse;
        JsonObject jsonObject = params[0];
        String JSONData = jsonObject.toString();

        try
        {

            URL url = new URL(urlName + mServerFileName);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
            writer.write(JSONData);
            writer.close();

            urlConnection.connect();
            int status = urlConnection.getResponseCode();

            InputStream inputStream = urlConnection.getInputStream();

            // Input Stream
            StringBuffer buffer = new StringBuffer();

            if (inputStream == null)
            {
                // Nothing to do
                return null;
            }
            
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;

            while ((inputLine = reader.readLine()) != null)
                buffer.append(inputLine);

            if (buffer.length() == 0)
            {
                // Stream was empty. No point in parsing.
                return null;
            }

            JsonResponse = buffer.toString();
            urlConnection.disconnect();
            return JsonResponse;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    protected void onPostExecute(String response)
    {
        String strReturnCode;

        mProgressDialog.dismiss();
        strReturnCode = mResponseHandler.HandleJsonResponse(response);
        mResponseHandler.UpdateMilkInfoDisplay(strReturnCode);
    }
}
