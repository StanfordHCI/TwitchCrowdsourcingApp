package com.twitch.homescreenlock;

import java.util.HashMap;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class SlideToUnlock extends TwitchMicrotaskActivity implements OnSeekBarChangeListener {
	
	long endTime;
	int progress = 0;
	
	final int[] images = {R.drawable.closedlock, R.drawable.openlock};
	ImageView lockImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.slide_to_unlock);
		setAsHomeScreen();
		
		SeekBar seekEnergy = (SeekBar) findViewById(R.id.seekBarUnlock);
		lockImage = (ImageView) findViewById(R.id.imageViewLocks);	
		
		int unlockLevelMax = 1000; 
		int unlockLevel = 50; // Start the control close to left side of bar. 
		seekEnergy.setMax(unlockLevelMax);
		seekEnergy.setProgress(unlockLevel); 
		seekEnergy.setOnSeekBarChangeListener(this);

		lockImage.setImageResource(R.drawable.closedlock); // Start with closed lock image
		Log.d("InternetConnection", String.valueOf(TwitchUtils.isOnline(this)));
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
		this.progress = progress;

		if(progress >= 0 && progress < 500) {
			Log.d("SlideToUnlockBar", "progress=" + progress + ", state=LOCKED");
			lockImage.setImageResource(images[0]);
		}
		else if(progress >= 500 && progress <= 1000) {
			// Visually unlock once slider passes halfway mark.
			Log.d("SlideToUnlockBar", "progress=" + progress + ", state=UNLOCKED");
			lockImage.setImageResource(images[1]);
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// Unlock the phone after any completed user interaction with the slider bar.
		endTime = System.currentTimeMillis();		

		double latitude = 0.0, longitude = 0.0;
		try {
			if(TwitchUtils.isOnline(this)) {
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
				Criteria criteria = new Criteria(); 
				towers = locationManager.getBestProvider(criteria, false); 
				Location locationFromGPS = locationManager.getLastKnownLocation(towers);		

				if(locationFromGPS != null) {
					latitude = (double) (locationFromGPS.getLatitude()); 
					longitude = (double) (locationFromGPS.getLongitude()); 
					Log.d("LatLong", "Lat= " + latitude + " Long= " + longitude); 
					// We don't need to display anything afterwards, so text location is unnecessary.			
				}
				else {
					Log.d("GPS", "Not found"); 
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace(); 
		}

		try {
			long laterStartTime = getLatestStartTime(endTime, "SlideToUnlock");
			
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Double.toString(latitude));
				params.put("longitude", Double.toString(longitude));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("progress", Integer.toString(progress));
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.SLIDE_TO_UNLOCK, this);
				
				Log.d("TwitchServer", "Starting CensusPostTask in SlideToUnlock");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(SlideToUnlock.this, cr);
				cpt.execute(p);
			}
			else {
				// If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(SlideToUnlock.this);
				entry.open();
				entry.createSlideToUnlock(progress, latitude, longitude, laterStartTime, endTime); 
				entry.close(); 
			}
			Log.d("DATABASE ENTRY UNLOCK", "SUCCESS"); 
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		removeFromHomeScreen();
		checkAggregates(SlideToUnlock.this);
	}

	// Required override
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}
}
