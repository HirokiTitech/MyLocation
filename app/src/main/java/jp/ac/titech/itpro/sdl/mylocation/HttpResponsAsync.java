package jp.ac.titech.itpro.sdl.mylocation;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kayalibra on 2017/06/20.
 */

public class HttpResponsAsync extends AsyncTask <JSONObject, Void, Void>{


    private final static String TAG = "HttpResponsAsync";
    private static final String HOST_URL = "https://sodium-carver-170712.appspot.com/api/test";

    public HttpResponsAsync(MainActivity mainActivity) {

    }


    @Override
    protected Void doInBackground(JSONObject... params) {
        HttpURLConnection httpURLConnection = null;
        URL url = null;
        String urlString = HOST_URL;
        Log.d(TAG, "doInBackground is called");
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(100000);
            httpURLConnection.setReadTimeout(100000);

            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);

            httpURLConnection.setRequestProperty("Accept-Language", "jp");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            PrintStream printStreams = new PrintStream(httpURLConnection.getOutputStream());

            Log.d(TAG, params[0].toString());

            printStreams.print(params[0]);
            printStreams.close();

            outputStream.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
