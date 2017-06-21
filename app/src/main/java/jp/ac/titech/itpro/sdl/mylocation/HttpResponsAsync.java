package jp.ac.titech.itpro.sdl.mylocation;

import android.os.AsyncTask;

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

public class HttpResponsAsync extends AsyncTask {
    private static final String HOST_URL = "http://sample.jp";

    public HttpResponsAsync(MainActivity mainActivity) {

    }


    @Override
    protected Object doInBackground(Object[] params) {
        HttpURLConnection httpURLConnection = null;
        URL url = null;
        String urlString = HOST_URL;
        try {
            url = new URL(urlString);
            httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setConnectTimeout(100000);
            httpURLConnection.setReadTimeout(100000);

            httpURLConnection.setRequestMethod("Post");
            httpURLConnection.setDoOutput(true);

            httpURLConnection.setRequestProperty("Accept-Language", "jp");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            httpURLConnection.connect();

            OutputStream outputStream = httpURLConnection.getOutputStream();
            PrintStream printStreams = new PrintStream(httpURLConnection.getOutputStream());

            printStreams.print(params);
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
