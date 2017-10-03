package jp.ac.titech.itpro.sdl.mylocation;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final static String TAG = "MainActivity";
    private int id = 0;

    // for GPS
    private TextView latLongView;
    private TextView idView, jsonView;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double latitude, longitude;
    private String nowTime;
    private enum UpdatingState {STOPPED, REQUESTING, STARTED}
    private UpdatingState state = UpdatingState.STOPPED;

    // for http connection
    private HttpResponsAsync httpResponsAsync;

    private final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private final static int REQCODE_PERMISSIONS = 1111;
    private int firstFlag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latLongView = (TextView) findViewById(R.id.latlong_view);
        idView = (TextView) findViewById(R.id.idText);
        jsonView = (TextView) findViewById(R.id.jsonView);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        httpResponsAsync = new HttpResponsAsync(this);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        googleApiClient.connect();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (state != UpdatingState.STARTED && googleApiClient.isConnected()) {
            startLocationUpdate(true);
        } else {
            state = UpdatingState.REQUESTING;
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (state == UpdatingState.STARTED) {
            stopLocationUpdate();
        }
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
        if (state == UpdatingState.REQUESTING) {
            startLocationUpdate(true);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspented");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onRequestPermissionsResult(int reqCode,
                                           @NonNull String[] permissions, @NonNull int[] grants) {
        Log.d(TAG, "onRequestPermissionsResult");
        switch (reqCode) {
        case REQCODE_PERMISSIONS:
            startLocationUpdate(false);
            break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location is changed");
        id = Integer.valueOf(idView.getText().toString());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if(httpResponsAsync.getStatus() == AsyncTask.Status.FINISHED || firstFlag == 1) {
            firstFlag = 0;

            JSONObject jsonObject = new JSONObject();
            JSONObject jsonObjectChild = new JSONObject();
            try{
                jsonObjectChild.put("pole_id",id);
                jsonObjectChild.put("longitude",longitude);
                jsonObjectChild.put("latitude",latitude);
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                final Date date = new Date(System.currentTimeMillis());
                nowTime =  df.format(date);
                //jsonObjectChild.put("time",nowTime);
                //jsonObject.put("data",jsonObjectChild);
                Log.d(TAG,"JSON FINISH");
                httpResponsAsync = new HttpResponsAsync(this);
                httpResponsAsync.execute(jsonObjectChild);

                Log.d(TAG,jsonObjectChild.toString());
                jsonView.setText(jsonObjectChild.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            latLongView.setText(getString(R.string.latlong_format, latitude, longitude));
        }

    }

    private void startLocationUpdate(boolean reqPermission) {
        Log.d(TAG, "startLocationUpdate: " + reqPermission);
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
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        state = UpdatingState.STARTED;
    }

    private void stopLocationUpdate() {
        Log.d(TAG, "stopLocationUpdate");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        state = UpdatingState.STOPPED;
    }
}
