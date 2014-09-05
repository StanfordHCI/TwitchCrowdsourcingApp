package com.twitch.homescreenlock;

import java.util.HashMap;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TwitchCensusDress extends TwitchMicrotaskActivity implements View.OnClickListener {

	long endTime; 

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)
			return true;
		else
			return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.twitch_census_dress);
		
		setAsHomeScreen();
		
		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		ImageButton imageButtonCasual = (ImageButton) findViewById(R.id.imageButtonCasual);
		ImageButton imageButtonSemiFormal = (ImageButton) findViewById(R.id.imageButtonSemiFormal);
		ImageButton imageButtonFormal = (ImageButton) findViewById(R.id.imageButtonFormal);
		ImageButton imageButtonVeryFormal = (ImageButton) findViewById(R.id.imageButtonVeryFormal);
		imageButtonCasual.setOnClickListener(this); 
		imageButtonSemiFormal.setOnClickListener(this); 
		imageButtonFormal.setOnClickListener(this); 
		imageButtonVeryFormal.setOnClickListener(this); 		

		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}

	public void onClick(View view) {		
		endTime = System.currentTimeMillis();
		String typeOfDress = null;
		
		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.twitch_census_toast,
		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));

		// Possibility to set an image
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);

		// Set image for toast while selecting value of response
		switch(view.getId()){
			case R.id.imageButtonCasual:
				Log.d("CLICK","imageButtonCasual");			
				typeOfDress = "veryCasual";	
				image.setImageResource(R.drawable.casual);			
				break;
			case R.id.imageButtonSemiFormal:
				Log.d("CLICK","imageButtonSemiFormal");			
				typeOfDress = "casual";
				image.setImageResource(R.drawable.semiformal);		
				break; 
			case R.id.imageButtonFormal:
				Log.d("CLICK","imageButtonFormal");			
				typeOfDress = "formal";	
				image.setImageResource(R.drawable.formal);		
				break; 
			case R.id.imageButtonVeryFormal:
				Log.d("CLICK","imageButtonVeryFormal");
				typeOfDress = "veryFormal";
				image.setImageResource(R.drawable.veryformal);		
				break;
		}

		try{
			long laterStartTime = getLaterStartTime(endTime, "TwitchCensusDress");
			
			// Send POST to Twitch server
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("dressType", typeOfDress);
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.DRESS, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(TwitchCensusDress.this, cr);
				cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusDress.this);
				entry.open();
				entry.createEntryDressCensus(typeOfDress, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime); 
				entry.close(); 
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}

		removeFromHomeScreen();
		
		// Set a message
		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusDress.this);
		aggregate.open();
		int nextToastPercentage = aggregate.getPercentageForResponse(TwitchConstants.AGGREGATE_TYPE_NAMES[2], typeOfDress);
		int nextToastPercentageOf = aggregate.getNumResponses(TwitchConstants.AGGREGATE_TYPE_NAMES[2], typeOfDress);
		
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

		// Toast
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
