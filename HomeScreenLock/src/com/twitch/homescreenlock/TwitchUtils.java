package com.twitch.homescreenlock;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

public class TwitchUtils {
	// Note in the local database that a user took the 'Exit' escape
	public static void registerExit(Activity a) {
		TwitchDatabase entry = new TwitchDatabase(a);
		entry.open();
		entry.incrementTwitchStats(TwitchConstants.TwitchStats.EXIT_BUTTON); 
		entry.close(); 
	}
	
	// Check for Internet connection
	public static boolean isOnline(Activity a)	{
		ConnectivityManager cm = (ConnectivityManager)a.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}
	
	public static void disableUserLockScreen(Activity a) {
		// Controls for showing a Twitch activity before or after a security unlock screen.
		/*
		Window w = a.getWindow();
		w.addFlags(LayoutParams.FLAG_DISMISS_KEYGUARD); // Dismiss non-secure unlock screens by default
		if(TwitchConstants.TWITCH_BEFORE_SECURITY) {
			Log.d("SecurityPreferences", "Running Twitch while phone is locked");
			w.addFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED); // Display Twitch before phone is unlocked
		} else {
			Log.d("SecurityPreferences", "Running Twitch after phone is unlocked");
		}
		*/
		
		// This code will forcefully disable security locks, but they will
		// simply re-enable after the Twitch activity calls finish().
		/*
		if(TwitchUtils.getUserSecurityPreference(a)) {
			// Disable security lock screens (PIN, swipe pattern, password)
			Log.d("SecurityPreferences", "Disabling a secure lock screen");
			KeyguardManager keyguardManager = (KeyguardManager) a.getSystemService(Activity.KEYGUARD_SERVICE);
			KeyguardLock lock = keyguardManager.newKeyguardLock(a.KEYGUARD_SERVICE);
			lock.disableKeyguard();
		} else {
			Log.d("SecurityPreferences", "Maintaining lock screen.");
		}
		*/
	}
	
	/* ===== Preferences convenience methods ===== */
	
	// Return true if user wants to disable security locks; false otherwise
	public static boolean getUserSecurityPreference(Activity a) {
		SharedPreferences settings = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
	    return settings.getBoolean("disableUserLock", false);
	}
	
	public static float getCurrLatitude(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getFloat(TwitchConstants.PREF_CURR_LAT, 0.0f);
	}
	
	public static float getCurrLongitude(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getFloat(TwitchConstants.PREF_CURR_LONG, 0.0f);
	}
	
	public static void setCurrLocation(Activity a, float latitude, float longitude) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putFloat(TwitchConstants.PREF_CURR_LAT, latitude);
		editor.putFloat(TwitchConstants.PREF_CURR_LONG, longitude);
		editor.apply();
	}
	
	public static float getPrevLatitude(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getFloat(TwitchConstants.PREF_PREV_LAT, 0.0f);
	}
	
	public static float getPrevLongitude(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getFloat(TwitchConstants.PREF_PREV_LONG, 0.0f);
	}
	
	public static void setPrevLocation(Activity a, float latitude, float longitude) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putFloat(TwitchConstants.PREF_PREV_LAT, latitude);
		editor.putFloat(TwitchConstants.PREF_PREV_LONG, longitude);
		editor.apply();
	}
	
	public static void setGeocodingStatus(Activity a, boolean status) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean(TwitchConstants.PREF_GEOCODING_DONE, status);
		editor.apply();
	}
	
	public static void setGeocodingSuccess(Activity a, boolean success) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean(TwitchConstants.PREF_GEOCODING_SUCCESS, success);
		editor.apply();
	}
	
	public static boolean getGeocodingStatus(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean(TwitchConstants.PREF_GEOCODING_DONE, false);
	}
	
	public static boolean getGeocodingSuccess(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean(TwitchConstants.PREF_GEOCODING_SUCCESS, false);
	}
	
	public static String getLocationText(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getString(TwitchConstants.PREF_LOCATION_TEXT, "you");
	}
	
	public static void setLocationText(Activity a, String text) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putString(TwitchConstants.PREF_LOCATION_TEXT, text);
		editor.apply();
	}
	
	public static boolean hasSentEmail(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean(TwitchConstants.PREF_SENT_EMAIL, false);
	}
	
	public static boolean hasPrevLocation(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean(TwitchConstants.PREF_HAS_PREV_LOCATION, false);
	}
	
	public static void setPrevLocationStatus(Activity a, boolean status) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean(TwitchConstants.PREF_HAS_PREV_LOCATION, status);
		editor.apply();
	}
	
	public static String getEmailAddress(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getString(TwitchConstants.PREF_EMAIL_ADDRESS, null);
	}
	
	public static void setEmail(Activity a, String email) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putString(TwitchConstants.PREF_EMAIL_ADDRESS, email);
		editor.apply();
	}
	
	public static boolean shouldUploadDB(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean(TwitchConstants.PREF_UPLOAD_DB, false);
	}
	
	public static void setUploadDBStatus(Activity a, boolean status) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean(TwitchConstants.PREF_UPLOAD_DB, status);
		editor.apply();
	}
	
	public static boolean shouldDumpPhotoRanking(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		long lastDumped = prefs.getLong(TwitchConstants.PREF_LAST_DUMPED_PHOTOS, 0);
		long timeSinceLastDumped = System.currentTimeMillis() - lastDumped;
		Log.d("PhotoDump", "Time since last dumped; " + timeSinceLastDumped + "; comparing to " + TwitchConstants.TIME_BETWEEN_PHOTO_DUMPS);
		return timeSinceLastDumped > TwitchConstants.TIME_BETWEEN_PHOTO_DUMPS;
	}
	
	public static void setLastDumpedPhotos(Activity a) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putLong(TwitchConstants.PREF_LAST_DUMPED_PHOTOS, System.currentTimeMillis());
		editor.apply();
	}
	
	public static void setEmailStatus(Activity a, boolean status) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean(TwitchConstants.PREF_SENT_EMAIL, status);
		editor.apply();
	}
	
	public static String getPrevAppPref(Context c) {
		SharedPreferences prefs = c.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getString(TwitchConstants.PREF_SAVED_APP_PREF, "TwitchCensus");
	}
	
	public static void setPrevAppPref(Context c, String pref) {
		SharedPreferences.Editor editor = c.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putString(TwitchConstants.PREF_SAVED_APP_PREF, pref);
		editor.apply();
	}
	
	public static int getSTWrow(Activity a) {
		SharedPreferences prefs = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getInt(TwitchConstants.PREF_STW_ROW, 0);
	}
	
	public static void setSTWrow(Activity a, int row) {
		SharedPreferences.Editor editor = a.getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putInt(TwitchConstants.PREF_STW_ROW, row);
		editor.apply();
	}
	
	// Check if remote twitch server is online. Don't call from main UI thread
	public static boolean twitchServerOnline() {
		boolean success = false;
	    HttpURLConnection urlConnection = null;
	    Log.d("TwitchServer", "Checking if the server is online...");
 
	    try {
	    	// Load any webpage on the server to ensure connectivity.
	    	URL url = new URL(TwitchConstants.SERVER_URL);
	        urlConnection = (HttpURLConnection) url.openConnection();
	        // Tell the connection attempt to close immediately and to timeout after
	        // 2000 milliseconds. Note that these calls should fail quickly, i.e.,
	        // an exception should be quickly raised if the server is offline.
	        urlConnection.setRequestProperty("Connection", "close");
	        urlConnection.setConnectTimeout(2000);
	        urlConnection.connect();
	        success = urlConnection.getResponseCode() == 200;
	    }catch(Exception e){
	        Log.d("TwitchServer", "Could not reach server because of exception; resource:" + TwitchConstants.SERVER_URL);
	    }
	    Log.d("TwitchServer", "Server connection status: " + success);
	    return success;
	}
	
	public static boolean canUsePrevLocation(Activity a) {
		float lat1 = TwitchUtils.getCurrLatitude(a);
		float lng1 = TwitchUtils.getCurrLongitude(a);
		float lat2 = TwitchUtils.getPrevLatitude(a);
		float lng2 = TwitchUtils.getPrevLongitude(a);
		if((lat1 == 0.0 && lng1 == 0.0) || (lat2 == 0.0 && lng2 == 0.0)) {
			Log.d("Geocoding", "Can't use previous location due to bad latlongs: " + TwitchUtils.getLocationText(a));
			return false; // Bad location reading, won't be able to calculate distance
		}
		if(!TwitchUtils.hasPrevLocation(a)) {
			Log.d("Geocoding", "No previous geocoded location exists.");
			return false; // Never pulled a valid location
		}
		return TwitchConstants.SAME_LOCATION_RADIUS >= distanceFrom(lat1, lng1, lat2, lng2);
	}
	
	public static int getPixelsFromDp(Context c, int dp) {
		final float scale = c.getResources().getDisplayMetrics().density;
		int pixels = (int) (dp * scale + 0.5f);
		return pixels;
	}
	
	public static double distanceFrom(double lat1, double lng1, double lat2, double lng2) {
		// Implementation of the Haversine distance formula
	    lat1 = Math.toRadians(lat1);
	    lng1 = Math.toRadians(lng1);
	    lat2 = Math.toRadians(lat2);
	    lng2 = Math.toRadians(lng2);

	    double dlon = lng2 - lng1;
	    double dlat = lat2 - lat1;

	    double a = Math.pow((Math.sin(dlat/2)),2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon/2),2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    return 3958.75 * c; // 3958: Earth radius in miles
	}
	
	// Return hash of phone's unique id
	public static String getDeviceID(Activity a) {
		TelephonyManager tm = (TelephonyManager) a.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String id = tm.getDeviceId();
		if(id == null) id = "DefaultTwitchUser";
		return Integer.toHexString(id.hashCode());
	}
	
	// Naming mechanism: "Manufacturer Model", adapted from StackOverflow
	public static String getDeviceName() {
	  String manufacturer = Build.MANUFACTURER;
	  String model = Build.MODEL;
	  return model.startsWith(manufacturer) ? model : manufacturer + " " + model;
	}
	
	// Get user-set timezone, formatted something like 'US/Pacific'
	public static String getTimezone() {
		return Calendar.getInstance().getTimeZone().getID();
	}
	
	public static String getStateCode(String state) {
		if(states == null) initStates();
		if(states.containsKey(state)) return states.get(state);
		return state;
	}
	
	static HashMap<String, String> states = null;
	private static void initStates() {
		states = new HashMap<String, String>();
		states.put("Alabama","AL");
		states.put("Alaska","AK");
		states.put("Alberta","AB");
		states.put("American Samoa","AS");
		states.put("Arizona","AZ");
		states.put("Arkansas","AR");
		states.put("Armed Forces (AE)","AE");
		states.put("Armed Forces Americas","AA");
		states.put("Armed Forces Pacific","AP");
		states.put("British Columbia","BC");
		states.put("California","CA");
		states.put("Colorado","CO");
		states.put("Connecticut","CT");
		states.put("Delaware","DE");
		states.put("District Of Columbia","DC");
		states.put("Florida","FL");
		states.put("Georgia","GA");
		states.put("Guam","GU");
		states.put("Hawaii","HI");
		states.put("Idaho","ID");
		states.put("Illinois","IL");
		states.put("Indiana","IN");
		states.put("Iowa","IA");
		states.put("Kansas","KS");
		states.put("Kentucky","KY");
		states.put("Louisiana","LA");
		states.put("Maine","ME");
		states.put("Manitoba","MB");
		states.put("Maryland","MD");
		states.put("Massachusetts","MA");
		states.put("Michigan","MI");
		states.put("Minnesota","MN");
		states.put("Mississippi","MS");
		states.put("Missouri","MO");
		states.put("Montana","MT");
		states.put("Nebraska","NE");
		states.put("Nevada","NV");
		states.put("New Brunswick","NB");
		states.put("New Hampshire","NH");
		states.put("New Jersey","NJ");
		states.put("New Mexico","NM");
		states.put("New York","NY");
		states.put("Newfoundland","NF");
		states.put("North Carolina","NC");
		states.put("North Dakota","ND");
		states.put("Northwest Territories","NT");
		states.put("Nova Scotia","NS");
		states.put("Nunavut","NU");
		states.put("Ohio","OH");
		states.put("Oklahoma","OK");
		states.put("Ontario","ON");
		states.put("Oregon","OR");
		states.put("Pennsylvania","PA");
		states.put("Prince Edward Island","PE");
		states.put("Puerto Rico","PR");
		states.put("Quebec","PQ");
		states.put("Rhode Island","RI");
		states.put("Saskatchewan","SK");
		states.put("South Carolina","SC");
		states.put("South Dakota","SD");
		states.put("Tennessee","TN");
		states.put("Texas","TX");
		states.put("Utah","UT");
		states.put("Vermont","VT");
		states.put("Virgin Islands","VI");
		states.put("Virginia","VA");
		states.put("Washington","WA");
		states.put("West Virginia","WV");
		states.put("Wisconsin","WI");
		states.put("Wyoming","WY");
		states.put("Yukon Territory","YT");
	}
}
