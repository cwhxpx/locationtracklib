/**
 * Copyright (C) 2014-2015 Imtoy Technologies. All rights reserved.
 * @charset UTF-8
 * @author xiong_it
 */
package co.devhub.locationtracklib;

import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

import co.devhub.locationtracklib.utils.Common;

/**
 * @description 
 * @charset UTF-8
 * @author xiong_it
 * @date 2015-7-20上午10:31:39
 * @version 
 */
public class LocationTrackService extends Service
		implements GoogleApiClient.ConnectionCallbacks,
		           GoogleApiClient.OnConnectionFailedListener {
	private static final String TAG = "LocationService";
	private static final String SERVICE_NAME = "LocationService";
	
	private static final long MIN_TIME = 0l;
	private static final float MIN_DISTANCE = 0f;

	public static final String BROADCAST_KEY_LATITUDE = "latitude";
	public static final String BROADCAST_KEY_LONGITUDE = "longitude";
	public static String INTENT_EXTRA_KEY_TRACKFREQUENCY = "track_frequency";
	private int location_track_freqency = 3000;
	
	private LocationManager locationManager;
	Location location;
	private boolean gpsEnabled = false;
	private int locationCounts = 0;
	GoogleApiClient mGoogleApiClient;
	

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Create an instance of GoogleAPIClient.
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		location_track_freqency = intent.getIntExtra(INTENT_EXTRA_KEY_TRACKFREQUENCY,
				                                     location_track_freqency);
		mGoogleApiClient.connect();

		return super.onStartCommand(intent, flags, startId);
	}

	private void informForegroundLocation() {
		// 通知Activity
		Intent intent = new Intent();
		intent.setAction(Common.LOCATION_ACTION);
		intent.putExtra(BROADCAST_KEY_LATITUDE, location.getLatitude());
		intent.putExtra(BROADCAST_KEY_LONGITUDE, location.getLongitude());
		sendBroadcast(intent);
	}

	private void setupLocationTrackTimer() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
				if (location != null) {
					informForegroundLocation();
				}
			}
		};

		timer.schedule(task, 0, location_track_freqency);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		setupLocationTrackTimer();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onDestroy() {
		mGoogleApiClient.disconnect();
		super.onDestroy();
	}
}
