package com.twitch.homescreenlock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeScreenLockActivity extends Activity implements View.OnClickListener {

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;

	// Flow of control
	boolean showEmailDialog;
	boolean shownHomeLauncher;
	int currScreen;
	int NUM_SCREENS;
	LinearLayout emailLayout, homeLauncherLayout, securityLayout;
	TextView header;
	
	@Override
	public void onBackPressed() {
		//Handles no action upon back button press
	}
	
	@Override
	public void onUserLeaveHint() {
		Log.d("TwitchHome", "Detected home button press");
	}
	
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
		setContentView(R.layout.home_launcher);

		// Flow of control
		showEmailDialog = !TwitchUtils.hasSentEmail(this);
		NUM_SCREENS = showEmailDialog ? 3 : 2;
		currScreen = 1;
		header = (TextView) findViewById(R.id.setupHeader);
		emailLayout = (LinearLayout) findViewById(R.id.submitEmailLayout);
		homeLauncherLayout = (LinearLayout) findViewById(R.id.setHomeLauncherLayout);
		securityLayout = (LinearLayout) findViewById(R.id.finishInstallLayout);
		
		// Email submission setup
		Button emailSubmit = (Button) findViewById(R.id.submitEmailButton);
		Button emailSkip = (Button) findViewById(R.id.emailSkipButton);
		final Activity a = this;
		emailSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText emailText = (EditText) findViewById(R.id.editText);
				String email = emailText.getText().toString();;
				if(email.length() > 0) {
					TwitchUtils.setEmail(a, email);
					TwitchUtils.setEmailStatus(a, false);
					emailText.setText("Thanks!");
				}
				// Hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(
					      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(emailText.getWindowToken(), 0);
				changeDisplay();
			}
		});
		emailSkip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeDisplay();
			}
		});
		
		// Home launcher setup
		shownHomeLauncher = false;
		Button homeLaunch = (Button) findViewById(R.id.button_setTwitch);
		Button homeLaunchSkip = (Button) findViewById(R.id.homeLauncherSkipButton);
		homeLaunch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);
				Log.d("TwitchHome", "Pressed home-starting button");
			}
		});
		homeLaunchSkip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeDisplay();
			}
		});
		
		// Security buttons
		Button disableSecurity = (Button) findViewById(R.id.button_disableSecurity);
		Button skipSecurity = (Button) findViewById(R.id.button_skipSecurity);
		skipSecurity.setOnClickListener(this);
		disableSecurity.setOnClickListener(this);
		
		// Set up local storage used for asynchronous geocoding and email sending.
		SharedPreferences.Editor editor = getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putFloat(TwitchConstants.PREF_PREV_LAT, 0.0f);
		editor.putFloat(TwitchConstants.PREF_PREV_LONG, 0.0f);
		editor.putBoolean(TwitchConstants.PREF_HAS_PREV_LOCATION, false);
		editor.putFloat(TwitchConstants.PREF_CURR_LAT, 0.0f);
		editor.putFloat(TwitchConstants.PREF_CURR_LONG, 0.0f);
		editor.putString(TwitchConstants.PREF_LOCATION_TEXT, "you");
		editor.putBoolean(TwitchConstants.PREF_GEOCODING_DONE, false);
		editor.putBoolean(TwitchConstants.PREF_GEOCODING_SUCCESS, false);
		editor.putBoolean(TwitchConstants.PREF_SENT_EMAIL, false);
		editor.putBoolean(TwitchConstants.PREF_UPLOAD_DB, false);
		editor.putInt(TwitchConstants.PREF_STW_ROW, 0);
		editor.apply();
		
		//Enables the activity to be home.
		TwitchUtils.disableUserLockScreen(this);
		PackageManager pm = getPackageManager();
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		
		changeDisplay();
	}
	
	private boolean didPartialInstall() {
		SharedPreferences prefs = getSharedPreferences(TwitchConstants.PREF_FILE, 0);
		return prefs.getBoolean("PARTIAL_INSTALL", false);
	}
	
	private void setPartialInstall() {
		SharedPreferences.Editor editor = getSharedPreferences(TwitchConstants.PREF_FILE, 0).edit();
		editor.putBoolean("PARTIAL_INSTALL", true);
		editor.apply();
	}
	
	public void changeDisplay() {
		boolean showEmail = currScreen == 1 && showEmailDialog;
		boolean showHomeLauncher = (currScreen==2 && showEmailDialog) || (currScreen == 1 && !showEmailDialog);
		if(didPartialInstall()) {
			// Already got to home launcher setting once, skip to last settings
			showEmail = false;
			showHomeLauncher = false;
			currScreen = showEmailDialog ? 3 : 2;
			Log.d("TwitchHome", "Detected previous install");
		}
		
		header.setText("Twitch Setup (" + currScreen + "/" + NUM_SCREENS + ")");
		Log.d("TwitchHome", "curr screen: " + currScreen + "; showing email? " + showEmailDialog);
		emailLayout.setVisibility(View.GONE);
		homeLauncherLayout.setVisibility(View.GONE);
		securityLayout.setVisibility(View.GONE);
		
		if(showEmail) {
			emailLayout.setVisibility(View.VISIBLE);
		}
		else if(showHomeLauncher) {
			homeLauncherLayout.setVisibility(View.VISIBLE);
			setPartialInstall();
		}
		else {
			securityLayout.setVisibility(View.VISIBLE);
		}
		currScreen++;
	}

	@Override
	// Handle security button clicks
	public void onClick(View v) {
		PackageManager pm = getPackageManager(); //Disables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
		
		// Code to start security settings menu
		if(v.getId() == R.id.button_disableSecurity) {
			Intent securitySettingsMenu = new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS);
			securitySettingsMenu.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			try {
				Log.d("TwitchInstall", "Launching security settings");
				startActivity(securitySettingsMenu);
			} catch (Exception e) {} // Security settings action is invalid for user's phone model.
		}
		
		finish();
	}
	
	//Menu stuff begins.
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
			startActivity(intentSettings); //starts the activity 
			return true; 
		case R.id.exit:
			PackageManager pm = getPackageManager(); //Disables the activity to be home.
			ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
			pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

			TwitchUtils.registerExit(this);
			finish(); 
			return true;		
		default: 
			return false; 
		}

	}
	//Menu stuff ends.
}
