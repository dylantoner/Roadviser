/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nuig.trafficapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.nuig.trafficapp.activities.MainActivity;
import com.nuig.trafficapp.fragments.TabFragment;

import java.util.List;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private  String accountName;
    private Location mLastLocation;
    private LocationManager mLocationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences settings = getSharedPreferences("TrafficApp", 0);
        accountName = settings.getString("accountName", null);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String email = data.getString("email");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        mLastLocation = getLastKnownLocation();
        if (from.startsWith("/topics/insert")) {
            // message received from some topic.

        } else {
            // normal downstream message.
        }

        Intent updateUI = new Intent(QuickstartPreferences.UPDATE_UI);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifications = sharedPreferences.getBoolean("notifications", true);
        double preferedDist  = sharedPreferences.getInt("radius", 50);

        if(message.equals("delete")){
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateUI);
        }
        else {
            float latit = Float.parseFloat(data.getString("lat"));
            float longit = Float.parseFloat(data.getString("long"));
            if( distFrom(latit,longit,
                    mLastLocation.getLatitude(),mLastLocation.getLongitude())<=preferedDist){

                //Check if notifications are enabled in preferences
                 if (notifications) {
                     //Check that message originates from another user
                    if(!(email.equals(accountName)))
                        sendNotification(message);
                }
                }
            LocalBroadcastManager.getInstance(this).sendBroadcast(updateUI);
        }
    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l=null;
            try {
                 l = mLocationManager.getLastKnownLocation(provider);
            }
            catch (SecurityException e){

            }

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {

                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setContentTitle("RoadViser")
                .setContentText("New Incident: " + message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }
}
