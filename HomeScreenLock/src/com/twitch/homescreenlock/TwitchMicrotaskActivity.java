package com.twitch.homescreenlock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class TwitchMicrotaskActivity extends Activity implements LocationListener {
	
	LocationManager locationManager;
	String towers;
	
	long startTime;
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus) {
	    	Log.d("WindowFocus", "Focus on");
	    	// Reset start time if user returns to Twitch after another window
	    	// is active.
	    	startTime = System.currentTimeMillis();	
	    }
	}
	
	public void setAsHomeScreen() {
		PackageManager pm = getPackageManager();
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);

		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
	}
	
	public void removeFromHomeScreen() {
		PackageManager pm = getPackageManager();
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
	}
	
	public void checkAggregates(Context context) {
		TwitchDatabase aggregate = new TwitchDatabase(context);
		aggregate.open();
		aggregate.checkAggregates(this);
		finish();
	}
	
	protected long getLaterStartTime(long endTime, String activityName) {
		long durationViaFocus = endTime - startTime;
		long durationViaScreenOn = endTime - LockScreenBroadcast.startTime;
		Log.d("DurationFor" + activityName, durationViaFocus + " ms via focus, " + durationViaScreenOn + " ms via screen on event");
		return startTime > LockScreenBroadcast.startTime ? startTime : LockScreenBroadcast.startTime;
	}
	
	// Menu stuff begins.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_screen_lock, menu); 
		return true; 
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intentSettings = new Intent(this, LockScreenSettings.class);  

		switch(item.getItemId()){		
		case R.id.settings:			
			startActivity(intentSettings);
			return true; 
		case R.id.exit:
			// Remove census activity from the home screen.
			PackageManager pm = getPackageManager();
			ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
			pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

			TwitchUtils.registerExit(this);
			finish(); 
			return true;		
		default: 
			return false; 
		}
	}
	// Menu stuff ends.
	
	@Override
	public void onBackPressed() {
		// Do nothing when the back button is pressed.
	}
	
	@Override
	public void onUserLeaveHint() {
		Log.d("TwitchHome", "Detected home button press");
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)
			return true;
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		if(towers != null && locationManager != null)
			locationManager.removeUpdates(this);
	}
	
	//Register for location updates when the activity is in foreground
	@Override
	protected void onResume() {		
		super.onResume();
		if(towers != null && locationManager != null)
			locationManager.requestLocationUpdates(towers, 20000, 1, this);
		
		// Fire off a geocoding task whenever the screen is turned on or
		// the phone returns to Twitch from a higher priority app.
		TwitchUtils.setGeocodingStatus(this, false);
		GeocodingTask task = new GeocodingTask();
		GeocodingTask.Param param = task.new Param(this, locationManager, towers);
		task.execute(param);
		Log.d("Geocoding", "Launched asynchronous geocoding task");
	}
	
	// LocationListener methods
	@Override
	public void onLocationChanged(Location l) {
		TwitchUtils.setCurrLocation(this, (float) l.getLatitude(), (float) l.getLongitude()); 
	}
	@Override
	public void onProviderDisabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onProviderEnabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Auto-generated method stub
	}
}
