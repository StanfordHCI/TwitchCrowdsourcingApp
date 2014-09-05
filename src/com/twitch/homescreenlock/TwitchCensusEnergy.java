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

public class TwitchCensusEnergy extends TwitchMicrotaskActivity implements View.OnClickListener {

	String energyLevel = "None";
	long endTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.twitch_census_energy);

		setAsHomeScreen();
		
		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		ImageButton imageButtonLvl1 = (ImageButton) findViewById(R.id.imageButtonEnergyLevel1);
		ImageButton imageButtonLvl2 = (ImageButton) findViewById(R.id.imageButtonEnergyLevel2);
		ImageButton imageButtonLvl3 = (ImageButton) findViewById(R.id.imageButtonEnergyLevel3);
		ImageButton imageButtonLvl4 = (ImageButton) findViewById(R.id.imageButtonEnergyLevel4);
		imageButtonLvl1.setOnClickListener(this);
		imageButtonLvl2.setOnClickListener(this);
		imageButtonLvl3.setOnClickListener(this);
		imageButtonLvl4.setOnClickListener(this);
		
		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}
	
	@Override
	public void onClick(View view) {
		endTime = System.currentTimeMillis();

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.twitch_census_toast,
		  (ViewGroup) findViewById(R.id.custom_toast_layout_id));
		
		// Possibility to set an image
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);
		
		// Set image for toast while selecting value of response
		switch(view.getId()) {
			case R.id.imageButtonEnergyLevel1:
				Log.d("CLICK", "imageButtonEnergyLevel1");
				energyLevel = "level1";
				image.setImageResource(R.drawable.energylevel1);
				break;
			case R.id.imageButtonEnergyLevel2:
				Log.d("CLICK", "imageButtonEnergyLevel2");
				energyLevel = "level2";
				image.setImageResource(R.drawable.energylevel2);
				break;
			case R.id.imageButtonEnergyLevel3:
				Log.d("CLICK", "imageButtonEnergyLevel3");
				energyLevel = "level3";
				image.setImageResource(R.drawable.energylevel3);
				break;
			case R.id.imageButtonEnergyLevel4:
				Log.d("CLICK", "imageButtonEnergyLevel4");
				energyLevel = "level4";
				image.setImageResource(R.drawable.energylevel4);
				break;
		}

		try {			 
			long laterStartTime = this.getLaterStartTime(endTime, "TwitchCensusEnergy");
			
			// Send POST to Twitch server
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("energyLevel", energyLevel);
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.ENERGY, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(TwitchCensusEnergy.this, cr);
				cpt.execute(p);
			}
			else { // If the phone isn't online, log entry locally
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(TwitchCensusEnergy.this);
				entry.open();
				entry.createEntryEnergyCensus(energyLevel, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime); 
				entry.close();
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}

		removeFromHomeScreen();
		
		//Toast begin
		// Set a message
		TwitchDatabase aggregate = new TwitchDatabase(TwitchCensusEnergy.this);
		aggregate.open();
		int nextToastPercentage = aggregate.getPercentageForResponse(TwitchConstants.AGGREGATE_TYPE_NAMES[3], energyLevel);
		int nextToastPercentageOf = aggregate.getNumResponses(TwitchConstants.AGGREGATE_TYPE_NAMES[3], energyLevel);
			
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
