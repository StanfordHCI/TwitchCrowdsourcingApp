package com.twitch.homescreenlock;

import java.util.HashMap;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitchCensusActivity extends TwitchMicrotaskActivity implements View.OnClickListener {

	long endTime; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.twitch_census_activity);
		
		setAsHomeScreen();
		
		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		ImageButton imageButtonHome = (ImageButton) findViewById(R.id.imageButtonHome);
		ImageButton imageButtonWork = (ImageButton) findViewById(R.id.imageButtonWork);
		ImageButton imageButtonSocial = (ImageButton) findViewById(R.id.imageButtonSocial);		
		ImageButton imageButtonExcercise = (ImageButton) findViewById(R.id.imageButtonExcercise);
		ImageButton imageButtonFood = (ImageButton) findViewById(R.id.imageButtonFood);
		ImageButton imageButtonTransit = (ImageButton) findViewById(R.id.imageButtonTransit);
		imageButtonHome.setOnClickListener(this); 
		imageButtonWork.setOnClickListener(this); 
		imageButtonSocial.setOnClickListener(this); 		
		imageButtonExcercise.setOnClickListener(this);
		imageButtonFood.setOnClickListener(this); 	
		imageButtonTransit.setOnClickListener(this); 	

		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}

	public void onClick(View view) {
		endTime = System.currentTimeMillis();
		String typeOfActivity = null;
		boolean success = true; 
		String toastText = null;

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.twitch_census_toast,
		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));

		// Possibility to set an image
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);
		
		// Set image for toast while selecting value of response
		switch(view.getId()) {
			case R.id.imageButtonHome:
				Log.d("CLICK","imageButtonHome");			
				typeOfActivity = "home";		
				image.setImageResource(R.drawable.home);
				break;
			case R.id.imageButtonWork:
				Log.d("CLICK","imageButtonWork");			
				typeOfActivity = "work";
				image.setImageResource(R.drawable.work);
				break; 
			case R.id.imageButtonSocial:
				Log.d("CLICK","imageButtonSocial");			
				typeOfActivity = "social";		
				image.setImageResource(R.drawable.social);
				break; 	
			case R.id.imageButtonExcercise:
				Log.d("CLICK","imageButtonExcercise");
				typeOfActivity = "exercise";
				image.setImageResource(R.drawable.exercise);
				break; 
			case R.id.imageButtonFood:
				Log.d("CLICK","imageButtonFood");
				typeOfActivity = "food";
				image.setImageResource(R.drawable.eating);
				break;
			case R.id.imageButtonTransit:
				Log.d("CLICK","imageButtonTransit");
				typeOfActivity = "transit";
				image.setImageResource(R.drawable.transit);
				break;
		}

		try{
			long laterStartTime = getLaterStartTime(endTime, "TwitchCensusActivity");
			
			// Send POST to Twitch server
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("activity", typeOfActivity);
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.ACTIVITY, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(TwitchCensusActivity.this, cr);
				cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusActivity.this);
				entry.open();
				entry.createEntryActivityCensus(typeOfActivity, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime); 
				entry.close();
			}
		}
		catch(Exception e){
			success = false;
			e.printStackTrace();
		}
		finally{
			if(success){
				Log.d("DATABASE ENTRY DRESS CENSUS", "SUCCESS"); 
			}				
		}

		removeFromHomeScreen();

		// Set a message
		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusActivity.this);
		aggregate.open();
		int nextToastPercentage = aggregate.getPercentageForResponse(TwitchConstants.AGGREGATE_TYPE_NAMES[1], typeOfActivity);
		int nextToastPercentageOf = aggregate.getNumResponses(TwitchConstants.AGGREGATE_TYPE_NAMES[1], typeOfActivity);
		
		toastText = String.valueOf(nextToastPercentage);
		TextView text = (TextView) layout.findViewById(R.id.textToast);		
		text.setText(toastText + "% ");
		TextView textPercentOf = (TextView) layout.findViewById(R.id.textToastPercentOf);
		String respStr = nextToastPercentageOf == 1 ? " response " : " responses ";
		textPercentOf.setText(" of " + nextToastPercentageOf + respStr);
		
		TextView textLocation = (TextView) layout.findViewById(R.id.textToastLocation);
		if(TwitchUtils.getGeocodingStatus(this) && TwitchUtils.getGeocodingSuccess(this)) {
			// GeocodingStatus is true if the asynchronous task finished
			// GeocodingSuccess was true if we were able to get a valid address
			textLocation.setText("near " + TwitchUtils.getLocationText(this));
		} else if(TwitchUtils.canUsePrevLocation(this)) {
			// If current location
			textLocation.setText("near " + TwitchUtils.getLocationText(this));
		} else {
			textLocation.setText("near you");
		}

		// Display toast
		final Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toast.cancel();
			}
		}, TwitchConstants.TOAST_DURATION);
		
		aggregate.checkAggregates(this);
		finish();
	}
}
