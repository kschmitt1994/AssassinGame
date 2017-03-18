package mobileappdev.assassingame;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class PlayBoardActivity extends AppCompatActivity implements LocationListener,
        GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_ID = 123;
    private LocationManager mLocationManager;
    private Location mLocation;
    private GoogleMap mGoogleMap;
    private Player mMyself;
    private boolean isAdminOfGame = false;


    private static final long LOCATION_REFRESH_DISTANCE = 1; // in meters
    private static final long LOCATION_REFRESH_TIME = 500; // .5 sec

    private MyReceiver mMyReceiver;
    private List<String> mPlayers;
    private Map<String, MarkerOptions> mMarkerMap = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_board);
        mMyReceiver = new MyReceiver();
        if (getIntent().getBooleanExtra("admin", false))
            isAdminOfGame = true;

        if (getIntent().getBooleanExtra("gamestarted", false)) {
            mPlayers = FirebaseHelper.getAllPlayerNames();
        }

        checkForLocationServices(PlayBoardActivity.this);
        Log.d("Ajit", "!@(!*$^!(#^(!*#&$(!*#^$(!&#$&!#$^");
        initialize();
    }


    private Location getLocation() {

        Location loc = null;
        try {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean checkGPS = mLocationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean checkNetwork = mLocationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(this, "No Service Provider Available", Toast.LENGTH_SHORT).show();
            } else {
                // First get location from Network Provider
                if (checkNetwork) {
                    Toast.makeText(this, "Network", Toast.LENGTH_SHORT).show();

                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                LOCATION_REFRESH_TIME,
                                LOCATION_REFRESH_DISTANCE, this);
                        Log.d("Network", "Network");
                        if (mLocationManager != null) {
                            loc = mLocationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }

                    } catch (SecurityException e) {

                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (checkGPS) {
                Toast.makeText(this, "GPS", Toast.LENGTH_SHORT).show();
                if (loc == null) {
                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                LOCATION_REFRESH_TIME,
                                LOCATION_REFRESH_DISTANCE, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mLocationManager != null) {
                            loc = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    } catch (SecurityException e) {

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return loc;
    }

    private void initialize() {
        Log.d("Ajit", "Inside initialize().");

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = getCurrentLocation(PlayBoardActivity.this);

        if (mLocation == null)
            Log.d("Ajit", "Location in null");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);

        Log.d("Ajit", "Inside initialize(). Calling fragment.");
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                Log.d("Ajit", "Inside onMapReady(). Checking if location is null -- 1");
                if (mLocation != null) {
                    Log.d("Ajit", "Inside onMapReady(). Calling addMarkers() -- 1");
                    addMarkers(googleMap);
                }

            }
        });
    }

    private void addMarkers(GoogleMap googleMap) {
        LatLng itemPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLng myPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                .include(myPoint)
                .build();
        final int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        googleMap.animateCamera(update);

        // TODO: 3/18/2017 get all the locations from the game from firebase and show markers
        MarkerOptions marker = new MarkerOptions().position(itemPoint).title("Smith").snippet("Assassin");
        mMarkerMap.put("Smith", marker);
        googleMap.addMarker(marker);
        LatLng secMarker = new LatLng(itemPoint.latitude + 0.00004, itemPoint.longitude + 0.00003);
        MarkerOptions marker1 = new MarkerOptions().position(secMarker).title("Kenny")
                .snippet("Doctor").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(marker1);
        mMarkerMap.put("Kenny", marker1);
        googleMap.setOnMarkerClickListener(this);
    }

    private void updateMarkers(String userName, Location location) {
        Log.d("Ajit", "I am moving....");
        MarkerOptions markerOptions = mMarkerMap.get(userName);
        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleMap = null;
        Log.d("ajit", "Ondestroy called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fragment_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat:
                startActivity(new Intent(PlayBoardActivity.this, ChatActivity.class));
                return true;
            case R.id.exit_game:
                startActivity(new Intent(PlayBoardActivity.this, MainActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO: 3/15/2017 broadcast the location to all the players
        updateMarkers(null, location);
        Criteria criteria = new Criteria();
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        String provider = mLocationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = mLocationManager.getLastKnownLocation(provider);
        FirebaseHelper.sendLocation(mLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO: 3/15/2017 show alarm dialog that you would be dead if you turn location off.
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mMyReceiver, new IntentFilter(BroadcastHelper.LOCATION_RECEIVED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mMyReceiver);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mLocationManager.removeUpdates(PlayBoardActivity.this);
    }


    public LocationManager getLocationManager() {
        return mLocationManager;
    }


    public static void checkForLocationServices(Context context) {

        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            showSettingsAlert(context);
        }
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public static void showSettingsAlert(final Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

//        alertDialog.setCancelable(false);
        // Setting Dialog Message
        alertDialog.setMessage("Location is not enabled. Please enable location services. Press Cancel to exit the game");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                context.startActivity(new Intent(context, MainActivity.class));
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    public Location getCurrentLocation(final Context context) {
        Criteria criteria = new Criteria();
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = mLocationManager.getBestProvider(criteria, false);
        Location location = null;
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not yet available and needs to be asked for
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    && ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // We provide an additional rationale to the user if the permission was not granted
                // and the user would benefit from additional context for the use of the permission.
                // For example if the user has previously denied the permission.
                Log.d("Ajit", "Inside checkPermission block # IFBLOCK. Requesting Runtime permission");

                new AlertDialog.Builder(context)
                        .setMessage("Permission is needed to access Location!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();

            } else {
                Log.d("Ajit", "Inside checkPermission # IFELSEBLOCK. Requesting Runtime permission");

                // Permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(PlayBoardActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_ID);
            }
        } else {
            Log.d("Ajit", "Inside checkPermission#LastElseBlock. Requesting Runtime permission");
            location = mLocationManager.getLastKnownLocation(provider);
        }

        return location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_ID: {
                Log.d("Ajit", "Inside onRequestPermissionsResult");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Ajit", "Inside onRequestPermissionsResult#beforeInitialize");
                    initialize();
                } else {
                    Toast.makeText(this, "Permissions denied. Exiting Game...", Toast.LENGTH_LONG).show();
                    // TODO: 3/16/2017 exit the game
                }
                break;
            }

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (marker.getSnippet().equals("Citizen") || marker.getSnippet().equals("Doctor")) {
            marker.setVisible(false);
            doesGameEnd();
        }
        return false;
    }

    private void doesGameEnd() {
        // TODO: 3/17/2017 check if Assassin wins
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BroadcastHelper.LOCATION_RECEIVED)) {
//                updateMarkers();

            } else if (action.equals(BroadcastHelper.INVITE_RESPONSE)) {
                String userName = intent.getExtras().getString(BroadcastHelper.USER_NAME);
                if (FirebaseHelper.isGameStarted(Game.getInstance().getGameName()))
                    mPlayers.add(userName);
            }
        }
    }
}