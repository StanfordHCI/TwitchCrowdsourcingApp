package com.twitch.homescreenlock;

import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;

public class GeocodingTask extends AsyncTask<GeocodingTask.Param, Void, Void> {
	
	class Param {
		LocationManager locationManager; 
		String towers;
		Activity activity;
		public Param(Activity activity, LocationManager locationManager, String towers) {
			this.activity = activity;
			this.locationManager = locationManager;
			this.towers = towers;
		}
	}
	
	// The geocoding contract: try to finish geocoding (which involves
	// a networking component) before the user presses a button. On success,
	// PREF_GEOCODING_DONE is set to true (to avoid inaccuracies, this is the
	// last thing done) and a location will be available in PREF_LOCATION_TEXT.
	// If not, we will try to use the value in PREF_LOCATION_TEXT if the latlong
	// hash not changed much from the last update. The last resort is "near you".
	
	@Override
	protected Void doInBackground(Param... args) {
		Param param = args[0];
		boolean success = false;
		try {
			Location locationFromGPS = param.locationManager.getLastKnownLocation(param.towers);		
			double newLatitude = 0.0;
			double newLongitude = 0.0;
			boolean gotLocation = false;
			if(locationFromGPS != null) {
				gotLocation = true;
				newLatitude = locationFromGPS.getLatitude(); 
				newLongitude = locationFromGPS.getLongitude();
				TwitchUtils.setCurrLocation(param.activity, (float) newLatitude, (float) newLongitude);
				Log.d("Geocoding", "Pulled loc: Lat= " + newLatitude + " Long= " + newLongitude); 
			} else {
				Log.d("Geocoding", "Couldn't get location using cell towers");
			}
			
			if(gotLocation && TwitchUtils.isOnline(param.activity) && Geocoder.isPresent())
			{ //Prevents geocoder from crashing if no internet connection is available.
				Geocoder geocoder = new Geocoder(param.activity, Locale.getDefault());
				List<Address> addresses = geocoder.getFromLocation(
					TwitchUtils.getCurrLatitude(param.activity),
					TwitchUtils.getCurrLongitude(param.activity),
					1
				);
				
				if(addresses != null && addresses.size() > 0) {
					Address address = addresses.get(0);
					String locality = address.getLocality();
					String state = address.getAdminArea();
					
					if(locality != null) {
						String location = locality;
						if(state != null) location += ", " + TwitchUtils.getStateCode(state);
						TwitchUtils.setLocationText(param.activity, location);
						TwitchUtils.setPrevLocationStatus(param.activity, true);
						TwitchUtils.setPrevLocation(param.activity, (float) newLatitude, (float) newLongitude);
						Log.d("Geocoding", "Success. Setting location [" + location + "] for latlong (" + newLatitude + ", " + newLongitude + ")");
						success = true;
					}
					Log.d("Geocoding", "Locality found: " + address.getAddressLine(1) + "; state: " + state);
				} else {
					Log.d("Geocoding", "No matching addresses found.");
				}
			}
			else
			{
				Log.d("Geocoding", "Device not online or Geocoder not present.");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
		
		TwitchUtils.setGeocodingSuccess(param.activity, success);
		TwitchUtils.setGeocodingStatus(param.activity, true);
		return null;
	}
}
