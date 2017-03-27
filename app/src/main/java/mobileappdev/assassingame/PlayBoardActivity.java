package mobileappdev.assassingame;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class PlayBoardActivity extends AppCompatActivity implements LocationListener,
        GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_ID = 123;
    private LocationManager mLocationManager;
    private Location mLocation = new Location("PlayBoard");
    private GoogleMap mGoogleMap;
    private String mMyself = "ajit0"; //TODO:ajit: revert
    private String mGameName;
    private boolean mGameStarted;
    private boolean mIsAdminOfGame = false;

    private static final long LOCATION_REFRESH_DISTANCE = 1; // in meters
    private static final long LOCATION_REFRESH_TIME = 50; // .5 sec
    private static final double KILL_DISTANCE = 5;

    private Spinner mSpinner;
    private MyReceiver mMyReceiver;
    private List<String> mPlayerNames = new ArrayList<>();
    private Map<String, Player> mPlayersMap;
    private Map<String, MarkerOptions> mMarkerMap = new HashMap<>();

    private GoogleMap.OnMarkerClickListener _this;
    private boolean mGoogleCameraUpdateDone;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_board);


        Intent intent = getIntent();
        if (intent.getBooleanExtra(BroadcastHelper.AM_I_ADMIN, false)) {
            mIsAdminOfGame = true;
            mGameName = intent.getStringExtra(BroadcastHelper.GAME_NAME);
        } else if (intent.getBooleanExtra(BroadcastHelper.ON_GAME_REQUEST, false)) {
            String admin = intent.getStringExtra(BroadcastHelper.ADMIN);
            String player = intent.getStringExtra(BroadcastHelper.PLAYER_NAME);
            mGameName = intent.getStringExtra(BroadcastHelper.GAME_NAME);
            String gameReqResponse = intent.getStringExtra(BroadcastHelper.INVITATION_RESPONSE);
            if (InvitationStatus.ACCEPTED.equals(InvitationStatus.getStatusFrom(gameReqResponse))) {
                FirebaseHelper.sendAcceptResponse(player, mGameName);
            } else {
                FirebaseHelper.sendRejectionResponse(player, mGameName);
                //TODO:Ajit: do I need to call finish()?

            }
//            finishAffinity();
//            return;
        }

        mMyReceiver = new MyReceiver();
        _this = this;
        mSpinner = new Spinner(this);
        mSpinner.show("Hang On!", "Doing initial game set up for you...", false);
//        mLocation.setLatitude(0.0);
//        mLocation.setLongitude(0.0);

        updateUserName(this);
        mGameStarted = intent.getBooleanExtra(BroadcastHelper.GAME_STARTED, false);
        if (mGameStarted) {
            mGameName = intent.getStringExtra(BroadcastHelper.GAME_NAME);
            fetchAllPlayerNames(mGameName); //mSpinner is being dismissed and initialize() is called within the method
        } else {
            mSpinner.dismiss();
            initialize();
        }

    }

    private void attachLocationListener() {
    /*
         * Begin Firebase location synchronization stuff
         * ---------------------------------------------
         * We have a single listener that is updated any time any of our users change their location.
         * When that listener detects a change we will query the location for each of said users
         * and update the appropriate markers.
         */

        String gameReference = "games/" + mGameName + "/";
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference locationRef = database.getReference(gameReference + "/location_monitor");

        locationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final String mPlayerName : mPlayerNames) {
                    addListenerForLocation(mPlayerName, database);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "locationRef:onCancelled");
            }
        });
    }

    private void addListenerForLocation(final String mPlayerName, FirebaseDatabase database) {
        String playerRef = "users/" + mPlayerName;
        DatabaseReference playerLocationRef = database.getReference(playerRef);
        playerLocationRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("lat").getValue() != null) {
                    Double userLat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    Double userLng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                    if (userLat.equals(0.0) && userLng.equals(0.0))
                        return;
                    LatLng userLocation = new LatLng(userLat, userLng);
                    updateMarker(mPlayerName, userLocation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "playerLocationRef:onCancelled");
            }
        });
    }

    public void fetchAllPlayerNames(final String gameName) {
        final List<String> playerNames = new ArrayList<>();
        String playersReference = "games/" + gameName + "/players";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(playersReference);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Push the names to our result List.
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    playerNames.add(snapshot.getKey());
                }

                updatePlayerNamesListAndGetFurtherData(gameName, playerNames);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("FirebaseHelper", "getAllPlayerNames:onCancelled");
            }
        });

    }

    private void updatePlayerNamesListAndGetFurtherData(String gameName, List<String> playerNames) {
        mPlayerNames = playerNames;
        fetchAllPlayer(gameName);

    }

    private void fetchAllPlayer(String gameName) {

        final Map<String, Player> playerMap = new HashMap<>();

        String gamePlayerReference = "games/" + gameName + "/players";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(gamePlayerReference);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    playerMap.put(snapshot.getKey(),
                            new Player(snapshot.getKey(), "test@doWeNeedThisInfo.com", //todo:SAM: fix email
                                    GameCharacter.getCharacterFrom(snapshot.child("role").getValue().toString()),
                                    /*Boolean.parseBoolean(snapshot.child("isAlive").getValue().toString())*/false));
                }
                updateData(playerMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FirebaseHelper", "getAllPlayers:onCancelled");
            }
        });

    }

    private void updateData(Map<String, Player> playersMap) {
        mPlayersMap = playersMap;

        if (mIsAdminOfGame) {
            assignCharacters(mGameName, mPlayerNames);
            FirebaseHelper.initializeNoOfAliveCivilians(mGameName);
        }
        initialize();
        attachLocationListener();
        mSpinner.dismiss();
    }


    private void updateUserName(PlayBoardActivity playBoardActivity) {
        SharedPreferences sharedPreferences = playBoardActivity.getSharedPreferences(LogInActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString(LogInActivity.USER_NAME, null);
        if (userName != null) {
            mMyself = userName;
            Log.d(PlayBoardActivity.class.getSimpleName(), "PBA#UpdateUserName()= I got a name. I am " + userName);
        }
    }

    private void assignCharacters(String gameName, List<String> playerNames) {
        Random random = new Random();

        int assassinIndex = random.nextInt(playerNames.size());
        String assassin = playerNames.get(assassinIndex);
        playerNames.remove(assassinIndex);

        String detective = "";
        String doctor = "";

        // Doing some magic to handle < 4 player games

        if (playerNames.size() > 1) {
            int detectiveIndex = random.nextInt(playerNames.size());
            detective = playerNames.get(detectiveIndex);
            playerNames.remove(detectiveIndex);
        }

        if (playerNames.size() > 1) {
            int doctorIndex = random.nextInt(playerNames.size());
            doctor = playerNames.get(doctorIndex);
            playerNames.remove(doctorIndex);
        }

        FirebaseHelper.updateCharactersOfPlayers(gameName, assassin, detective, doctor, playerNames);

    }

    private void addMarker(String userName, LatLng itemPoint, Player player) {
        GameCharacter role = player == null ? GameCharacter.UNDEFINED : player.getGameCharacterType();
        MarkerOptions marker = new MarkerOptions().position(itemPoint).title(userName)
                .snippet(role.toString()).icon(getBitmapDescriptor(role));
        mMarkerMap.put(userName, marker);
        mGoogleMap.addMarker(marker);
    }

    @NonNull //TODO:Ajit: need a legend info panel as a menu item to describe the colors
    private BitmapDescriptor getBitmapDescriptor(GameCharacter character) {
        switch (character) {
            case ASSASSIN:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            case DETECTIVE:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
            case DOCTOR:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            case CITIZEN:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            default:
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);

        }
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

        checkForLocationServices(PlayBoardActivity.this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = getCurrentLocation(PlayBoardActivity.this);

        if (mLocation == null)
            Log.d("Ajit", "Location in null");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);


        Log.d("Ajit", "Inside initialize(). Calling Map fragment.");
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                Log.d("Ajit", "Inside onMapReady(). Checking if location is null.");
                if (mLocation != null) {
                    Log.d("Ajit", "Inside onMapReady(). Location is NOT null. Calling initialGoogleMapCameraUpdate()");
                    initialGoogleMapCameraUpdate();
                }

            }
        });

    }

    private void initialGoogleMapCameraUpdate() {
        if (mGoogleCameraUpdateDone) return;

        if (mLocation == null) return;
        LatLng itemPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLng myPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder().include(itemPoint).include(myPoint).build();
        final int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(update);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMarkerClickListener(_this);
        mGoogleCameraUpdateDone = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleMap = null;
        //TODO: Ajit: check if something else needs to be cleaned up, like listener
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
        Log.d("Ajit", "I am moving....");
        mLocation = location;
        initialGoogleMapCameraUpdate();



        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 14));
//        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(mGoogleMap.getCameraPosition().zoom - 7f));

        if (mGameStarted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(false);
            updateMarker(mMyself,
                    new LatLng(location.getLatitude(), location.getLongitude()));
            FirebaseHelper.sendLocation(mLocation, mGameName, mMyself);
        }
    }

    private void updateMarker(String userName, LatLng latLng) {


        MarkerOptions markerOptions = mMarkerMap.get(userName);

        if (markerOptions == null ) {
            initialGoogleMapCameraUpdate();
            addMarker(userName, latLng, mPlayersMap.get(userName));
            markerOptions = mMarkerMap.get(userName);
        }
        markerOptions.position(latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!mGameStarted)
            Toast.makeText(this, "Location services is needed for you to be part of the game.", Toast.LENGTH_SHORT).show();

        if (mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.ASSASSIN)) {
            FirebaseHelper.updateGameStatus(mGameName, false, "Assassin left the game.");
            return;
        }

        boolean amIDetective = mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.DETECTIVE);
        FirebaseHelper.updatePlayerStatus(mGameName, mMyself, PlayerStatus.LEFT, !amIDetective);
        Toast.makeText(this, "You are no more part of this game.", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
//        filter.addAction(BroadcastHelper.LOCATION_RECEIVED);
        filter.addAction(BroadcastHelper.INVITE_RESPONSE);
        filter.addAction(BroadcastHelper.NEW_PLAYER_JOINED);
        registerReceiver(mMyReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMyReceiver != null) {
            unregisterReceiver(mMyReceiver);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
//        mLocationManager.removeUpdates(PlayBoardActivity.this);
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
        String provider = mLocationManager.getBestProvider(criteria, true);
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
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_ID:
                Log.d("Ajit", "Inside onRequestPermissionsResult");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Ajit", "Inside onRequestPermissionsResult#beforeInitialize");
//                    initialize();
                } else {
                    Toast.makeText(this, "Permissions denied. Exiting Game...", Toast.LENGTH_LONG).show();
                    // TODO: 3/16/2017 exit the game
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        if (!marker.isInfoWindowShown()) { //TODO:Ajit:fix: Marker info not shown upon click
//            marker.showInfoWindow();
//            marker.set
//            marker.hideInfoWindow();
//        }
//        else {
//            Log.d("marker", "is marker shown?" + marker.isInfoWindowShown());
//            marker.hideInfoWindow();
//        }

        if (!mGameStarted) {
            Toast.makeText(this, "The game has not yet started", Toast.LENGTH_SHORT).show();
            return false;
        }

        Player myself = mPlayersMap.get(mMyself);

        switch (myself.getGameCharacterType()) {
            case ASSASSIN:
                if (marker.getSnippet().equals(GameCharacter.CITIZEN.toString())
                        || marker.getSnippet().equals(GameCharacter.DOCTOR.toString())) {
                    //marker.setVisible(false);
                    double distance = getDistance(marker, myself);
                    if (distance > KILL_DISTANCE) {
                        //todo:Ajit: check if getBaseContext() works??
                        Toast.makeText(getBaseContext(), "You can't kill a civilian (doctor included) " +
                                "if you are not within " + KILL_DISTANCE + "of his proximity. " +
                                "Current distance from " + marker.getTitle() + " is "+ distance + " meters.",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    mMarkerMap.get(marker.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    FirebaseHelper.updatePlayerStatus(mGameName, marker.getTitle(), PlayerStatus.DEAD, true);
                    checkIfGameIsOver(mGameName);
                }
                break;

            case DETECTIVE:
                if (marker.getSnippet().equals(GameCharacter.ASSASSIN.toString())) {
                    mMarkerMap.get(marker.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    gameFinished(false, "Detective arrested the Assassin.");
                }
                break;
        }
        return false;
    }

    private void checkIfGameIsOver(String gameName) {
            String gameReference = "games/" + gameName;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference aliveRef = database.getReference(gameReference + "/alive");

            final StringBuffer aliveCivilians = new StringBuffer();

            aliveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    aliveCivilians.append(dataSnapshot.getValue());
                    int aliveCivilans = Integer.parseInt(aliveCivilians.toString());
                    if (aliveCivilans == 0)
                        gameFinished(true, "Assassin killed all Civilians & Doctor.");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("FirebaseHelper", "getNoOfAliveCivilians:onCancelled");
                }
            });



    }

    private void gameFinished(int noOfAliveCivilians) {

    }

    private double getDistance(Marker marker, Player myself) {
        Location startPoint = new Location("mySelf");
        LatLng markerPosition1 = mMarkerMap.get(myself.getName()).getPosition();
        startPoint.setLatitude(markerPosition1.latitude);
        startPoint.setLongitude(markerPosition1.longitude);

        Location endPoint = new Location("target");
        LatLng markerPosition2 = mMarkerMap.get(marker.getTitle()).getPosition();
        endPoint.setLatitude(markerPosition2.latitude);
        endPoint.setLongitude(markerPosition2.longitude);

        return (double) startPoint.distanceTo(endPoint);
    }

    private void gameFinished(boolean assassinWon, String description) {
        FirebaseHelper.updateGameStatus(mGameName, assassinWon, description);
        //TODO:Ajit: show dialog for replay
        /*boolean shouldReplay = false;
        if (shouldReplay) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            PlayBoardActivity.this.finish();
                            startActivity(new Intent(PlayBoardActivity.this, MainActivity.class));
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }*/

        Intent intent = new Intent(PlayBoardActivity.this, PostgameActivity.class);
        if (mIsAdminOfGame) {
            intent.putExtra("IS_ADMIN", true);
        }
        startActivity(intent);
    }

    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            /*if (action.equals(BroadcastHelper.LOCATION_RECEIVED)) { //done by listeners
                double[] latlng = intent.getDoubleArrayExtra(BroadcastHelper.LOCATION);
                String playerName = intent.getStringExtra(BroadcastHelper.PLAYER_NAME);
                updateMarker(playerName, new LatLng(latlng[0], latlng[1]));

            } else */
            if (mIsAdminOfGame && action.equals(BroadcastHelper.INVITE_RESPONSE)) {
                String playerName = intent.getExtras().getString(BroadcastHelper.PLAYER_NAME);
                mPlayerNames.add(playerName); //todo:Ajit: add listener for location on this player
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                addListenerForLocation(playerName, database);
                FirebaseHelper.increaseNoOfAliveCiviliansBy1(mGameName);
                FirebaseHelper.newPlayerAddedUp(playerName, mGameName);
                //rather than adding marker here, we will add it while receiving updated location from the user
                //addMarker(userName, new LatLng(latlng[0], latlng[1]), GameCharacter.CITIZEN);

            } else if (!mIsAdminOfGame && action.equals(BroadcastHelper.NEW_PLAYER_JOINED)) {
                String userName = intent.getExtras().getString(BroadcastHelper.PLAYER_NAME);
//                double[] latlng = intent.getDoubleArrayExtra(BroadcastHelper.LOCATION);
                if (FirebaseHelper.isGameStarted(mGameName)) {
                    mPlayerNames.add(userName);
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    addListenerForLocation(userName, database);
//                    addMarker(userName, new LatLng(latlng[0], latlng[1]), mPlayersMap.get(userName));
                }
            }
        }
    }



}