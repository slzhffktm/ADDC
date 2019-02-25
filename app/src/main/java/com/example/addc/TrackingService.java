package com.example.addc;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class TrackingService extends Service {
    private static final String TAG = TrackingService.class.getSimpleName();

    public TrackingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        requestLocationUpdates();
    }

    //Create the persistent notification//

    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification//
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))

        //Make this notification ongoing so it can’t be dismissed by the user//
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.tracking_enabled);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground(broadcastIntent);
        else
            startForeground(1, builder.build());
    }

    private void startMyOwnForeground(PendingIntent broadcastIntent){
        String NOTIFICATION_CHANNEL_ID = "com.example.addc";
        String channelName = "My Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.tracking_enabled)
                .setContentTitle("ADDC")
                .setContentText(getString(R.string.tracking_enabled_notif))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .build();
        startForeground(2, notification);
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Unregister the BroadcastReceiver when the notification is tapped//
            unregisterReceiver(stopReceiver);

            //Stop the Service//
            stopSelf();
        }
    };

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        //Specify how often your app should request the device’s location//
        request.setInterval(10000);

        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //...then request location updates//
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    //Get a reference to the database, so your app can perform read and write operations//
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(TrackingService.this);
                        Log.d("FirebaseUser", "onLocationResult: "+acct);
                        String personId = acct.getId();
                        Log.d("FirebaseUser", "ID: "+personId);

//                        Save the location data to the database//
                        ref.child("users").child(personId).child("latitude").setValue(location.getLatitude());
                        ref.child("users").child(personId).child("longitude").setValue(location.getLongitude());
                    }
                }
            }, null);
        }
    }
}
