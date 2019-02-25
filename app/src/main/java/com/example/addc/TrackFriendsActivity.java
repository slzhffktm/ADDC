package com.example.addc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TrackFriendsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = TrackFriendsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Firebase instantiation
    private DatabaseReference mDatabase;

    private String todoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            todoId = extras.getString("todo_id");
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigate_nearest_workshop);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d("SUCCESS", "Created map");

        mDatabase =
                FirebaseDatabase.getInstance().getReference();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("SUCCESS", "Adapter");

        // Prompt the user for permission.
        getLocationPermission();
        Log.d("SUCCESS", "Permission granted");

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        Log.d("SUCCESS", "Update Location UI");

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        Log.d("SUCCESS", "Get current location");

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(TrackFriendsActivity.this);
        Log.d("FirebaseUser", "onLocationResult: "+acct);
        final String personId = acct.getId();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("Tes", "Todo id: "+todoId);
                if (dataSnapshot.child("todo_users").child(todoId).getChildrenCount() != 0) {
                    for (DataSnapshot users : dataSnapshot.child("todo_users").child(todoId).getChildren()) {
                        Log.d("Tes", "here");
                        Log.d("Tes", "Key: " + users.getKey());
                        if (users.getKey() != personId) {
                            User user;
                            user = dataSnapshot.child("users").child(users.getKey()).getValue(User.class);
                            Log.d("Name", user.getName());
                            LatLng location = new LatLng(user.getLatitude(), user.getLongitude());
                            Log.d("Tes",String.valueOf(dataSnapshot.child("todos").child(todoId).child("name")));
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(user.getName())
                                    .snippet(String.valueOf(dataSnapshot.child("todos").child(todoId).child("name")))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    }
                }
//
//                  for (DataSnapshot projects : dataSnapshot.child("user_todos").getChildren()) {
//                    Log.d("Tes", "here");
//                    Log.d("Tes", projects.getKey());
//                    for (DataSnapshot users : dataSnapshot.child("todo_users").getChildren()) {
//                        if (users.getKey() != personId) {
//                            User user;
//                            user = dataSnapshot.child("users").child(users.getKey()).getValue(User.class);
//                            Log.d("Name", user.getName());
//                            LatLng location = new LatLng(user.getLatitude(), user.getLongitude());
//                            Log.d("Tes","Snippet "+String.valueOf(dataSnapshot.child("todos").child(projects.getKey()).child("name")));
//                            Marker m = mMap.addMarker(new MarkerOptions()
//                                    .position(location)
//                                    .title(user.getName())
//                                    .snippet(String.valueOf(dataSnapshot.child("todos").child(projects.getKey()).child("name")))
//                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//                        }
//                    }
//                }

//                for (DataSnapshot users : dataSnapshot.getChildren()) {
//                    Log.d("Tes", "here");
//                    Log.d("Tes", users.getKey());
//                    if (users.getKey() == personId) {
//                        User user;
//                        user = dataSnapshot.child("users").child(users.getKey()).getValue(User.class);
//                        Log.d("Name", user.getName());
//                        LatLng location = new LatLng(user.getLatitude(), user.getLongitude());
//                        Marker m = mMap.addMarker(new MarkerOptions()
//                                .position(location)
//                                .title(user.getName())
//                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
//                    }
//                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public double originLatitude;
            public double originLongitude;
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng destination = marker.getPosition();
                originLatitude = mLastKnownLocation.getLatitude();
                originLongitude = mLastKnownLocation.getLongitude();
                LatLng origin = new LatLng(originLatitude, originLongitude);

                Log.d("origin", String.valueOf(origin));
                Log.d("destination", String.valueOf(destination));

                String serverKey = "AIzaSyBoKM22Gt7W3vtvLIy9vzj0LlDdKnuOl-Q";
                GoogleDirection.withServerKey(serverKey)
                        .from(origin)
                        .to(destination)
                        .transportMode(TransportMode.DRIVING)
                        .alternativeRoute(true)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                // Do something here
                                if (direction.isOK()) {
                                    String status = direction.getStatus();
                                    Log.d("STATUS", status);

                                    Route route = direction.getRouteList().get(0);
                                    Leg leg = route.getLegList().get(0);

                                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                } else {

                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {
                                // Do something here
                            }
                        });

                return false;
            }
        });
    }
}
