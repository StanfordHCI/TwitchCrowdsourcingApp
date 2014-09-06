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


public class TwitchCensusPeople extends TwitchMicrotaskActivity implements View.OnClickListener {

	long endTime; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.twitch_census_people);		

		setAsHomeScreen();

		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		// Register listeners for buttons
		int[] buttonIds = { R.id.imageButtonOne, R.id.imageButtonMoreThanOne,
				R.id.imageButtonGroup, R.id.imageButtonCrowd };
		for(int id : buttonIds) {
			ImageButton button = (ImageButton) findViewById(id);
			button.setOnClickListener(this);
		}
		
		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}

	public void onClick(View view) {
		endTime = System.currentTimeMillis();
		String numberOfPeople = null;

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.twitch_census_toast,
				(ViewGroup) findViewById(R.id.custom_toast_layout_id));
		
		// Set image for toast while selecting value of response
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);
		switch(view.getId()) {
			case R.id.imageButtonOne:
				Log.d("CLICK","imageButtonOne");			
				numberOfPeople = "alone";			
				image.setImageResource(R.drawable.oneperson);
				break;
			case R.id.imageButtonMoreThanOne:
				Log.d("CLICK","imageButtonMoreThanOne");			
				numberOfPeople = "smallGroup";		
				image.setImageResource(R.drawable.morethanone);
				break; 
			case R.id.imageButtonGroup:
				Log.d("CLICK","imageButtonGroup");			
				numberOfPeople = "largeGroup";
				image.setImageResource(R.drawable.group);
				break; 
			case R.id.imageButtonCrowd:
				Log.d("CLICK","imageButtonCrowd");
				numberOfPeople = "crowd";
				image.setImageResource(R.drawable.crowd);
				break;
		}

		try {			
			long laterStartTime = getLaterStartTime(endTime, "TwitchCensusPeople");
			
			// Send POST to Twitch server
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("numPeople", numberOfPeople);
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.PEOPLE, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(TwitchCensusPeople.this, cr);
				cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusPeople.this);
				entry.open();
				entry.createEntryPeopleCensus(numberOfPeople, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime);
				entry.close();
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		removeFromHomeScreen();
		
		// Set toast message
		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusPeople.this);
		aggregate.open();
		int nextToastPercentage = aggregate.getPercentageForResponse(TwitchConstants.AGGREGATE_TYPE_NAMES[0], numberOfPeople);
		int nextToastPercentageOf = aggregate.getNumResponses(TwitchConstants.AGGREGATE_TYPE_NAMES[0], numberOfPeople);
		
		String toastText = String.valueOf(nextToastPercentage);
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
		
		// Run toast
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
