package com.twitch.homescreenlock;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class HomeScreenLock extends Application {
	
	private static Context twitchContext;
	
	@Override
	public void onCreate() {		
		twitchContext = getApplicationContext();
		startService(new Intent(this, LockScreenService.class));
		super.onCreate();
		Log.d("LockScreenService", "Running onCreate of HomeScreenLock? " + LockScreenService.isRunning());
	}
	
	public static Context getContext() {
		return twitchContext;
	}
}
