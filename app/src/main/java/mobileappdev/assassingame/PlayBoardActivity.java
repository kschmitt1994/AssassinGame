package mobileappdev.assassingame;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.support.v7.view.ContextThemeWrapper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author: Ajit Ku. Sahoo
 * @Date: 3/15/2017
 */

public class PlayBoardActivity extends AppCompatActivity implements LocationListener,
        GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_ID = 123;
    private static final String TAG = "PBActivity";
    private LocationManager mLocationManager;
    private Location mLocation = new Location("PlayBoard");
    private GoogleMap mGoogleMap;
    private String mMyself = "ajitPBA";
    private String mGameName;
    private String mTarget;
    private boolean mGameStarted = false;
    private boolean mGameFinished = false;
    private boolean mGameClosedByUser = false;
    private boolean mIsAdminOfGame = false;
    private boolean mInitialized = false;
    private boolean amIAlive = true;

    private static final long LOCATION_REFRESH_DISTANCE = 5; // in meters
    private static final long LOCATION_REFRESH_TIME = 5000; // in msec
    private static final double KILL_DISTANCE = 50; //in meters

    private Spinner mSpinner;
    private MyReceiver mMyReceiver;
    private Set<String> mPlayerNames;
    private Map<String, Marker> mMarkerMap;
    private Map<String, Player> mPlayersMap;
    private Map<String, MarkerOptions> mMarkerOptionsMap;

    private boolean mGoogleCameraUpdateDone;
    private GoogleMap.OnMarkerClickListener _this;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_board);

        Intent intent = getIntent();
        mIsAdminOfGame = intent.getBooleanExtra(BroadcastHelper.AM_I_ADMIN, false);
        if(!mIsAdminOfGame) {
            try {
                Thread.sleep(500); //in order to give some time to admin to assign characters and info to be updated in firebase
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        _this = this;
        mMyReceiver = new MyReceiver();
        mMarkerMap = new HashMap<>();
        mPlayersMap = new HashMap<>();
        mPlayerNames = new HashSet<>();
        mMarkerOptionsMap = new HashMap<>();
        mGameName = intent.getStringExtra(BroadcastHelper.GAME_NAME);

        mSpinner = new Spinner(this);
        mSpinner.show("Hang On!", "Doing initial game set up for you...", false);
        updateUserName(this);

        mGameStarted = intent.getBooleanExtra(BroadcastHelper.GAME_STARTED, false);
        if (mGameStarted) {
            fetchAllPlayer(mGameName); //mSpinner is being dismissed and initializeMap() is called within the method
        } else {
            mSpinner.dismiss();
            initializeMap();
        }

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
                            new Player(snapshot.getKey(), "test@doWeNeedThisInfo.com", //no, we don't need it
                            GameCharacter.getCharacterFrom(snapshot.child("role").getValue().toString()),
                            PlayerStatus.ALIVE.equals(PlayerStatus.getPlayerStatus(snapshot.child("status").getValue().toString()))));
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
        mPlayerNames = playersMap.keySet();

        if (mIsAdminOfGame) {
            assignCharacters(mGameName, new ArrayList<>(mPlayerNames));
        }
        initializeMap();
        addLocationListener(mPlayerNames);
        addListenerForGameStatus(mGameName);


        if (GameCharacter.ASSASSIN.equals(mPlayersMap.get(mMyself).getGameCharacterType())){
            getTarget();
        } else {
            Toast.makeText(getBaseContext(), "Your character role is " + mPlayersMap.get(mMyself).getGameCharacterType() + ".", Toast.LENGTH_SHORT).show();
        }

        mSpinner.dismiss();
    }

    private void addLocationListener(Set<String> playerNames) {
        for (final String playerName : playerNames) {
            addListenerForLocation(playerName);
            addListenerForPlayerStatusChange(playerName);
        }
    }

    private void addListenerForGameStatus(final String gameName) {
        String gameStatusUrl = "games/" + mGameName + "/status";
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameStatusRef = database.getReference(gameStatusUrl).getRef();
        gameStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w("God", "Game finished notification.");
                if (dataSnapshot.getValue() != null) {
                    String status = dataSnapshot.getValue().toString();
                    if (GameStatus.FINISHED.equals(GameStatus.getGameStatusFrom(status))) {
                        Log.w("God", "Game finished!");
                        handleFinishGameStatus(gameName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "playerLocationRef:onCancelled");
            }
        });

    }

    private void handleFinishGameStatus(String gameName) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference gameStatusRef = database.getReference("games/" + gameName + "/assassinWon").getRef();
        gameStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    boolean assassinWon = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                    handlePostGameFinishTasks(assassinWon);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "game_assassinWonStatus:onCancelled");
            }
        });

    }

    private void addListenerForPlayerStatusChange(final String playerName) {
        String playerStatusRef = "games/" + mGameName + "/players/" + playerName + "/status";
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference playerLocationRef = database.getReference(playerStatusRef).getRef();
        playerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String status = dataSnapshot.getValue().toString();
                    Log.w("God", "Got a player change status for " + playerName + ". Status=" + status);
                    if (PlayerStatus.LEFT.equals(PlayerStatus.getPlayerStatus(status))) {

                        mMarkerMap.get(playerName).setVisible(false);
//                        mMarkerMap.remove(playerName);
//                        mMarkerOptionsMap.remove(playerName);
                        mPlayersMap.get(playerName).setAlive(false);
//                        Toast.makeText(PlayBoardActivity.this, "The assassin left the game.", Toast.LENGTH_LONG).show();

                    } else if (PlayerStatus.DEAD.equals(PlayerStatus.getPlayerStatus(status))) {
                        Toast.makeText(PlayBoardActivity.this, playerName + " is dead. ", Toast.LENGTH_SHORT).show();
                        mPlayersMap.get(playerName).setAlive(false);
                        Marker marker = mMarkerMap.get(playerName);
                        marker.setVisible(false);
                        marker.remove();
                        mMarkerMap.put(playerName, null);
                        mMarkerOptionsMap.put(playerName, null);
                        updateMarker(playerName, marker.getPosition());

                    } else if (PlayerStatus.ALIVE.equals(PlayerStatus.getPlayerStatus(status))) {
//                        Toast.makeText(PlayBoardActivity.this, playerName + " has been revived. ", Toast.LENGTH_SHORT).show();
                        Marker marker = mMarkerMap.get(playerName);
                        if (marker ==  null) {
                            return;
                        }
                        mPlayersMap.get(playerName).setAlive(true);
                        marker.setVisible(false);
                        marker.remove();
                        mMarkerMap.put(playerName, null);
                        mMarkerOptionsMap.put(playerName, null);
                        updateMarker(playerName, marker.getPosition());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "playerStatusRef:onCancelled");
            }
        });

    }

    private void addListenerForLocation(final String mPlayerName) {
        String playerRef = "users/" + mPlayerName;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference playerLocationRef = database.getReference(playerRef).getRef();
        playerLocationRef.child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("lat").getValue() != null) {
                    Double userLat = Double.parseDouble(dataSnapshot.child("lat").getValue().toString());
                    Double userLng = Double.parseDouble(dataSnapshot.child("lng").getValue().toString());
                    if (userLat.equals(0.0) && userLng.equals(0.0)) {
                        Log.w(PlayBoardActivity.class.getSimpleName(), "Lat Lng is 0/0 for " + mPlayerName);
                        return;
                    }
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
        mPlayersMap.get(assassin).setGameCharacterType(GameCharacter.ASSASSIN);

        String detective = "";
        String doctor = "";

        // Doing some magic to handle < 4 player games

        if (playerNames.size() > 1) {
            int detectiveIndex = random.nextInt(playerNames.size());
            detective = playerNames.get(detectiveIndex);
            playerNames.remove(detectiveIndex);
            mPlayersMap.get(detective).setGameCharacterType(GameCharacter.DETECTIVE);
        }

        if (playerNames.size() > 1) {
            int doctorIndex = random.nextInt(playerNames.size());
            doctor = playerNames.get(doctorIndex);
            playerNames.remove(doctorIndex);
            mPlayersMap.get(doctor).setGameCharacterType(GameCharacter.DOCTOR);

        }

        for (String player : playerNames) {
            mPlayersMap.get(player).setGameCharacterType(GameCharacter.CITIZEN);
        }

        //could be done in an AsyncTask
        FirebaseHelper.updateCharactersOfPlayers(gameName, assassin, detective, doctor, playerNames);
        FirebaseHelper.initializeNoOfAliveCivilians(mGameName, playerNames.size());

    }

    private void addMarker(String userName, LatLng itemPoint, Player player) {
        if (player == null) {
            Log.e("PlayBoardActivity: ", "player is null in addMarker() for userName:" + userName);
        }
        GameCharacter roleForColor;
        String actualCharacter = "";
        if (player == null) {
            roleForColor = GameCharacter.UNDEFINED;
        } else if (!player.isAlive()) {
            roleForColor = GameCharacter.UNDEFINED;
            actualCharacter = player.getGameCharacterType().toString();
        } else {
            roleForColor = player.getGameCharacterType();
            actualCharacter = roleForColor.toString();
        }
        MarkerOptions markerOption = new MarkerOptions().position(itemPoint)
                                                  .title(userName)
                                                  .snippet(actualCharacter)
                                                  .icon(getBitmapDescriptor(roleForColor));
        mMarkerOptionsMap.put(userName, markerOption);
        if (mGoogleMap == null) {
            Log.w("Ajit", "mGoogleMap is null");
            return;
        }
        Marker marker1 = mGoogleMap.addMarker(markerOption);
        mMarkerMap.put(userName, marker1);
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
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);

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

    private void initializeMap() {
        Log.d("Ajit", "Inside initializeMap().");

        if (mInitialized) return;

        checkForLocationServices(PlayBoardActivity.this);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocation = getCurrentLocation(PlayBoardActivity.this);

        if (mLocation == null)
            Log.d("Ajit", "Inside initializeMap(). Location in null");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, this);


        Log.d("Ajit", "Inside initializeMap(). Calling Map fragment.");
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        final PlayBoardActivity playBoardActivity = this;
        fragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                try {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    playBoardActivity , R.raw.style_json));

                    if (!success) {
                        Log.e(PlayBoardActivity.class.getSimpleName(), "Style parsing failed.");
                    }
                } catch (Resources.NotFoundException e) {
                    Log.e(TAG, "Can't find style. Error: ", e);
                }
                Log.d("Ajit", "Inside onMapReady(). Checking if location is null.");
                if (mLocation != null) {
                    Log.d("Ajit", "Inside onMapReady(). Location is NOT null. Calling initialGoogleMapCameraUpdate()");
                    initialGoogleMapCameraUpdate();
//                    updateMarker(mMyself, new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
                }

            }
        });

        mInitialized = true;
    }

    private void initialGoogleMapCameraUpdate() {
        if (mGoogleCameraUpdateDone) return;

        if (mLocation == null || mGoogleMap == null) return;
        LatLng itemPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLng myPoint = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder().include(itemPoint).include(myPoint).build();
        final int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mGoogleMap.animateCamera(update);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 17));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.setOnMarkerClickListener(_this);
        mGoogleCameraUpdateDone = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleMap = null;

        if (mLocationManager != null) {
            mLocationManager.removeUpdates(PlayBoardActivity.this);
        }

        if (mGameFinished)
            return;

        if (mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.ASSASSIN)) {
            gameFinished(false, "Assassin left the game");
            return;
        }

        boolean amIDetective = mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.DETECTIVE);
        FirebaseHelper.updatePlayerStatus(mGameName, mMyself, PlayerStatus.LEFT, !amIDetective, !amIDetective);

//        Player player = mPlayersMap.get(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
//        String gamePlayerReference = "games/" + mGameName + "/players/" + player.getName() + "/status";
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference ref = database.getReference(gamePlayerReference);
//        ref.setValue(PlayerStatus.LEFT.toString());
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
            case R.id.info:
                AlertDialog.Builder alertDialog = getInfoDialog();
                alertDialog.show();
                return true;

            case R.id.chat:

                String statusReference = "games/" + mGameName + "/players/" + mMyself + "/status";
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference statusRef = database.getReference(statusReference);

                statusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String status = dataSnapshot.getValue().toString();
                        amIAlive = PlayerStatus.ALIVE.equals(PlayerStatus.getPlayerStatus(status));
                        Intent intent = new Intent(PlayBoardActivity.this, ChatActivity.class);
                        intent.putExtra("AM_I_ALIVE", amIAlive);
                        intent.putExtra("GAME", mGameName);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("FirebaseHelper", "getPlayerStatus:onCancelled");
                    }
                });
                return true;

            case R.id.exit_game:
                mGameClosedByUser = true;
                startActivity(new Intent(PlayBoardActivity.this, MainActivity.class));
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @NonNull
    private AlertDialog.Builder getInfoDialog() {
        ContextThemeWrapper ctw = new ContextThemeWrapper(this, R.style.Theme_AppCompat_Light);

        SpannableStringBuilder ssBuilderRed = new SpannableStringBuilder(   "  Red        - Assassin");
        SpannableStringBuilder ssBuilderGreen = new SpannableStringBuilder( "  Green    - Citizen");
        SpannableStringBuilder ssBuilderBlue = new SpannableStringBuilder(  "  Blue       - Doctor");
        SpannableStringBuilder ssBuilderCyan = new SpannableStringBuilder(  "  Cyan      - Detective");
        SpannableStringBuilder ssBuilderYellow = new SpannableStringBuilder("  Yellow   - Dead");

        ssBuilderRed.setSpan(new ForegroundColorSpan(Color.RED), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilderGreen.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilderBlue.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilderCyan.setSpan(new ForegroundColorSpan(Color.CYAN), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssBuilderYellow.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctw);
        alertDialog.setTitle("Player's Color Info");
        alertDialog.setMessage(ssBuilderBlue.append("\n").append(ssBuilderCyan).append("\n")
                .append(ssBuilderGreen).append("\n").append(ssBuilderRed).append("\n").append(ssBuilderYellow));
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        return alertDialog;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Ajit", "I am moving....");
        mLocation = location;

        if (!mGameStarted) //I am forcing it to choose the other way to initialize the Google Map before I even do any update in the next step
            return;

        initialGoogleMapCameraUpdate();

//        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
//                location.getLongitude()), 17));

        /*if (mGameStarted) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mGoogleMap.setMyLocationEnabled(false);*/
            updateMarker(mMyself, new LatLng(location.getLatitude(), location.getLongitude()));
            FirebaseHelper.sendLocation(mLocation, mGameName, mMyself);
        //}
    }

    private void updateMarker(String userName, LatLng latLng) {

        MarkerOptions markerOptions = mMarkerOptionsMap.get(userName);

        if (markerOptions == null) {
            initialGoogleMapCameraUpdate();
            addMarker(userName, latLng, mPlayersMap.get(userName));
            markerOptions = mMarkerOptionsMap.get(userName);
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

        Toast.makeText(this, "Location Service is disabled. You are no longer part of the game.", Toast.LENGTH_LONG).show();

        if (mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.ASSASSIN)) {
            gameFinished(false, "Assassin left the game");
            return;
        }

        boolean amIDetective = mPlayersMap.get(mMyself).getGameCharacterType().equals(GameCharacter.DETECTIVE);
        FirebaseHelper.updatePlayerStatus(mGameName, mMyself, PlayerStatus.LEFT, !amIDetective, !amIDetective);
        checkIfGameIsOver(mGameName, mMyself);

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
        alertDialog.setTitle("GPS is settings");
        alertDialog.setMessage("Location is not enabled. Please enable location services. Press Cancel to exit the game");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                context.startActivity(new Intent(context, MainActivity.class));
            }
        });

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
//                    initializeMap();
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
        /*if (!mGameStarted) {
            Toast.makeText(this, "The game has not yet started", Toast.LENGTH_SHORT).show();
            return false;
        }*/

        Player myself = mPlayersMap.get(mMyself);

        String targetPlayerName = marker.getTitle();
        String targetPlayerCharType = marker.getSnippet();

        MarkerOptions targetMarkerOption = mMarkerOptionsMap.get(targetPlayerName);

        switch (myself.getGameCharacterType()) {
            case ASSASSIN:
                double assassinDistance = getDistance(marker, myself);
                if (!mPlayersMap.get(targetPlayerName).isAlive()) {
                    Toast.makeText(getBaseContext(), "This player is already dead.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (assassinDistance > KILL_DISTANCE) {
                    Toast.makeText(getBaseContext(), "You can't kill a player " + "if you are not within " + KILL_DISTANCE + "m of his proximity. " +
                                    "Current distance is "+ assassinDistance + " meters.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!mPlayersMap.get(targetPlayerName).getName().equals(mTarget)){
                    Toast.makeText(getBaseContext(), "This player is not your target.  Keep searching.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Toast.makeText(this, "You have killed " + targetPlayerName, Toast.LENGTH_SHORT).show();
                marker.setVisible(false);
                mPlayersMap.get(targetPlayerName).setAlive(false);
                mMarkerOptionsMap.put(targetPlayerName, null);
                mMarkerMap.put(targetPlayerName, null);
                updateMarker(targetPlayerName, targetMarkerOption.getPosition());
                //update player status is being done inside the checkIfGameIsOver() after updating the alive citizens count
                checkIfGameIsOver(mGameName, marker.getTitle());
                getTarget();

                break;

            case DOCTOR:
                if (!myself.isAlive()) {
                    Toast.makeText(getBaseContext(), "Doctor, you are dead. So you cannot revive another dead player.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (mPlayersMap.get(targetPlayerName).isAlive()) {
                    Toast.makeText(getBaseContext(), "This player is already alive. Try your magic on a dead player.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

                double distance = getDistance(marker, myself);
                if (distance > KILL_DISTANCE) {
                    Toast.makeText(getBaseContext(), "You can't revive a player " +
                                    "if you are not within " + KILL_DISTANCE + "m of his proximity. " +
                                    "Current distance is " + distance + " meters.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                Toast.makeText(this, "You have revived " + targetPlayerName, Toast.LENGTH_SHORT).show();
                marker.setVisible(false);
                mPlayersMap.get(targetPlayerName).setAlive(true);
                mMarkerOptionsMap.put(targetPlayerName, null); //resetting so that a new marker can be added in updateMarker()
                mMarkerMap.put(targetPlayerName, null); //resetting so that a new marker can be added in updateMarker()
                updateMarker(targetPlayerName, targetMarkerOption.getPosition());
                FirebaseHelper.updatePlayerStatus(mGameName, targetPlayerName, PlayerStatus.ALIVE, true, true);

                break;

            case DETECTIVE:

                double detectiveDistance = getDistance(marker, myself);
                if (detectiveDistance > KILL_DISTANCE) {
                    Toast.makeText(getBaseContext(), "You can't arrest the assassin " +
                                    "if you are not within " + KILL_DISTANCE + "m of his proximity. " +
                                    "Current distance from " + targetPlayerName + " is " + detectiveDistance + " meters.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!GameCharacter.ASSASSIN.equals(GameCharacter.getCharacterFrom(targetPlayerCharType))) {

                    Toast.makeText(getBaseContext(), "This player is not the assassin.  Keep searching.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (GameCharacter.ASSASSIN.equals(GameCharacter.getCharacterFrom(targetPlayerCharType))) {
                    FirebaseHelper.updatePlayerStatus(mGameName, targetPlayerName, PlayerStatus.DEAD, false, false);
                    gameFinished(false, "Detective arrested the Assassin.");

                }
                break;
        }
        return false;
    }

    private void checkIfGameIsOver(String gameName, final String userName) {
            String gameReference = "games/" + gameName;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference aliveRef = database.getReference(gameReference + "/citizens_alive");

            aliveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int aliveCivilians = Integer.parseInt(dataSnapshot.getValue().toString());
                    if (aliveCivilians <= 0)
                        gameFinished(true, "Assassin killed all Civilians");
                    //this is patchy, but we need to do inside this call in order to make sure that there is no read of invalid count of alive players
                    FirebaseHelper.updatePlayerStatus(mGameName, userName, PlayerStatus.DEAD, true, false);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("FirebaseHelper", "getNoOfAliveCivilians:onCancelled");
                }
            });



    }

    private double getDistance(Marker marker, Player myself) {
        Location startPoint = new Location("mySelf");
        LatLng markerPosition1 = mMarkerOptionsMap.get(myself.getName()).getPosition();
        startPoint.setLatitude(markerPosition1.latitude);
        startPoint.setLongitude(markerPosition1.longitude);

        Location endPoint = new Location("target");
        LatLng markerPosition2 = mMarkerOptionsMap.get(marker.getTitle()).getPosition();
        endPoint.setLatitude(markerPosition2.latitude);
        endPoint.setLongitude(markerPosition2.longitude);

        return (double) startPoint.distanceTo(endPoint);
    }

    private void gameFinished(boolean assassinWon, String description) {
        mGameFinished = true;
        FirebaseHelper.updateGameStatus(mGameName, assassinWon, description);
//        handlePostGameFinishTasks(assassinWon);
    }

    private void handlePostGameFinishTasks(boolean assassinWon) {
        Player myself = mPlayersMap.get(mMyself);
        GameCharacter role = myself.getGameCharacterType();
        if (role.toString().equals("ASSASSIN")){
            if (assassinWon){
                FirebaseHelper.increaseNoOfWinsBy1(mMyself);
            } else {
                FirebaseHelper.increaseNoOfLossesBy1(mMyself);
            }

        } else {
            if (!assassinWon){
                FirebaseHelper.increaseNoOfWinsBy1(mMyself);
            } else {
                FirebaseHelper.increaseNoOfLossesBy1(mMyself);
            }
        }
        if (mGameClosedByUser)
            return;
        Intent intent = new Intent(PlayBoardActivity.this, PostgameActivity.class);
        if (mIsAdminOfGame) {
            intent.putExtra(BroadcastHelper.AM_I_ADMIN, true);
        }
        intent.putExtra(BroadcastHelper.ASSASSIN_WON, assassinWon);
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

            //todo:ajit: need to check if invite response is "Accepted" type. If decline, just ignore.
            if (mIsAdminOfGame && action.equals(BroadcastHelper.INVITE_RESPONSE)) {
                String playerName = intent.getExtras().getString(BroadcastHelper.PLAYER_NAME);
                mPlayerNames.add(playerName);
                addListenerForLocation(playerName);
                FirebaseHelper.increaseNoOfAliveCiviliansBy1(mGameName);
                FirebaseHelper.newPlayerAddedUp(playerName, mGameName);
                //rather than adding marker here, we will add it while attaching the location listener
                //addMarker(userName, new LatLng(latlng[0], latlng[1]), GameCharacter.CITIZEN);

            } else if (!mIsAdminOfGame && action.equals(BroadcastHelper.NEW_PLAYER_JOINED)) {
                String userName = intent.getExtras().getString(BroadcastHelper.PLAYER_NAME);
                handleNewPlayer(userName);

            } else if (action.equals(BroadcastHelper.GAME_ENDS)) {
                /*String msg = intent.getStringExtra(BroadcastHelper.RESULT_MESSAGE);
                boolean assassinWon = intent.getBooleanExtra(BroadcastHelper.WINNING_TEAM, false);
                gameFinished(assassinWon, msg);*/
            }
        }
    }

    private void handleNewPlayer(final String userName) {
        String gameTypeReference = "games/" + mGameName + "/status";
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(gameTypeReference);

        // Listen for single value then destroy listener
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String queriedGameStatus = (String) dataSnapshot.getValue();
                if (GameStatus.STARTED.equals(GameStatus.getGameStatusFrom(queriedGameStatus))) {
                    mPlayerNames.add(userName);
                    addListenerForLocation(userName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("PlayBoardActivity", "Failed to read value.", databaseError.toException());
            }
        });

    }

    private void getTarget(){
        String target;
        Random rand = new Random();

        int randomNum = rand.nextInt(mPlayerNames.size());
        target = mPlayersMap.keySet().toArray()[randomNum].toString();

        while (target.equals(mMyself) || !mPlayersMap.get(target).isAlive() ||
                mPlayersMap.get(target).getGameCharacterType().equals(GameCharacter.getCharacterFrom("Assassin"))){

            randomNum = rand.nextInt(mPlayerNames.size());
            target = mPlayerNames.toArray()[randomNum].toString();
        }

        mTarget = target;

        Toast.makeText(getBaseContext(), "Your new target is " + mTarget + ".", Toast.LENGTH_SHORT).show();
    }

}