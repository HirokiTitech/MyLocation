package jp.ac.titech.itpro.sdl.mylocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "MainActivity";
    private final static int ID = 0;

    // for GPS
    private TextView latLongView;
    private GoogleApiClient googleApiClient;
    private double latitude, longitude;
    private String nowTime;
    // for http connection
    private HttpResponsAsync httpResponsAsync;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final static int REQCODE_PERMISSIONS = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latLongView = (TextView) findViewById(R.id.latlong_view);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        httpResponsAsync = new HttpResponsAsync(this);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        googleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        showLastLocation(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspented");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
    }

    @Override
    public void onRequestPermissionsResult(int reqCode,
                                           @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (reqCode) {
        case REQCODE_PERMISSIONS:
            showLastLocation(false);
            break;
        }
    }

    private void showLastLocation(boolean reqPermission) {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                if (reqPermission)
                    ActivityCompat.requestPermissions(this, PERMISSIONS, REQCODE_PERMISSIONS);
                else
                    Toast.makeText(this, getString(R.string.toast_requires_permission, permission),
                            Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Location loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (loc != null) {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObjectChild = new JSONObject();
            try{
                jsonObjectChild.put("pole_id",ID);
                jsonObjectChild.put("longitude",longitude);
                jsonObjectChild.put("latitude",latitude);
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date date = new Date(System.currentTimeMillis());
                nowTime =  df.format(date);
                //jsonObjectChild.put("time",nowTime);
                //jsonObject.put("data",jsonObjectChild);
                Log.d(TAG,"JSON FINISH");

                httpResponsAsync.execute(jsonObjectChild);

                Log.d(TAG,jsonObject.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            displayLocation();
        }
    }

    private void displayLocation() {
        latLongView.setText(getString(R.string.latlong_format, latitude, longitude));
    }
}
