package com.twitch.homescreenlock;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;

public class LockScreenBroadcast extends BroadcastReceiver {

	static long startTime;

	@Override
	public void onReceive(final Context context, Intent intent) {
		if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			Log.d("LockScreenBroadcast", "Action: screen on");
			try {
				TwitchDatabase entry = new TwitchDatabase(context);
				entry.open();
				entry.incrementTwitchStats(TwitchConstants.TwitchStats.SCREEN_ON); 
				entry.close();
			} catch(Exception e) {
				e.printStackTrace(); 
			}
			startTime = System.currentTimeMillis();	
		} else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			boolean isCallActive = isCallActive(context);
			boolean isTwitchRunning = isTwitchRunning(context);
			Log.d("LockScreenBroadcast", "Screen off. Twitch running? " + isTwitchRunning + " Phonecall status: " + isCallActive);
			if(!isCallActive && !isTwitchRunning) {
				launchTwitchActivity(context);
			}
		} else {
			Log.d("LockScreenBroadcast", "Received intent: " + intent.getAction());
			boolean serviceRunning = LockScreenService.isRunning();
			Log.d("LockScreenService", "Running after that intent? " + serviceRunning);
			if(!serviceRunning) {
				context.startService(new Intent(context, LockScreenService.class));
			}
		}
	}
	
	private void launchTwitchActivity(Context context) {
		SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(context); 
		String lockOption = getPrefs.getString("keyOfLocksForPrefs", TwitchConstants.DEFAULT_APP);
		if(!lockOption.equals("TwitchCognitiveLoad")) {
			TwitchUtils.setPrevAppPref(HomeScreenLock.getContext(), lockOption);
		}
		boolean forceSlideToUnlock = forceSlideToUnlock(context);
		Log.d("AppChoice", "Lock option: " + lockOption + "; forcing slide-to-unlock? " + 
				forceSlideToUnlock + "; forced day=" + TwitchConstants.BASELINE_DAY);
		if(TwitchConstants.FORCE_ACTIVITY) {
			forceSlideToUnlock = false;
		}
		
		// Start new intent based on slide-to-unlock day and user preference.
		Intent intent = new Intent();
		if(!TwitchConstants.FORCE_ACTIVITY && lockOption.equals("TwitchCognitiveLoad")) {
			intent.setClass(context, TwitchCognitiveLoad.class);
		}
		else if(forceSlideToUnlock || lockOption.equals("TwitchSlideToUnlock")) {
			intent.setClass(context, SlideToUnlock.class);
		}
		else if(lockOption.equals("TwitchImages")) {
			intent.setClass(context, HomeScreenLockMain.class);
		}
		else if(lockOption.equals("TwitchStructureTheWeb")) {
			intent.setClass(context, StructuringTheWeb.class);
		}
		else if(TwitchConstants.FORCE_ACTIVITY || lockOption.equals("TwitchCensus")) {
			// Randomly pick a census app.
			Random rng = new Random();
			int censusAppNum = rng.nextInt(4);
			if(TwitchConstants.FORCE_ACTIVITY) {
				censusAppNum = 0; // Debugging: manually set a single Twitch app
			}
			Log.d("AppChoice", "Census number: " + censusAppNum);
			switch(censusAppNum) {
			case 0:
				intent.setClass(context, TwitchCensusPeople.class);
				break;
			case 1:
				intent.setClass(context, TwitchCensusDress.class);
				break;
			case 2:
				intent.setClass(context, TwitchCensusEnergy.class);
				break;
			case 3:
				intent.setClass(context, TwitchCensusActivity.class);
				break;
			}
		} else {
			Log.d("AppChoice", "Unrecognized preference: " + lockOption);
		}
		
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		context.startActivity(intent);
	}
	
	// Return whether or not we should set all applications to Slide-To-Unlock today.
	private boolean forceSlideToUnlock(Context context) {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		return day == TwitchConstants.BASELINE_DAY;
	}
	
	// Is the user in a phone call right now?
	public static boolean isCallActive(Context context){
	   AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	   if(manager.getMode()==AudioManager.MODE_IN_CALL){
		   return true;
	   }
	   else{
	       return false;
	   }
	}
	
	// Are there any Twitch-related Activities running right now?
	public static boolean isTwitchRunning(Context context) {
		if(twitchActivities == null) initTwitchActivities();
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> activities = activityManager.getRunningTasks(Integer.MAX_VALUE);
		for(RunningTaskInfo info : activities) {
			String name = info.topActivity.toString();
			if(twitchActivities.contains(name)) {
				Log.d("AppChoice", "Found running app: " + name);
				return true; // We don't care which activity is running - we won't launch a new one
			}
		}
		return false;
	}

	static HashSet<String> twitchActivities = null;
	private static void initTwitchActivities() {
		twitchActivities = new HashSet<String>();
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.TwitchCensusDress}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.TwitchCensusPeople}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.TwitchCensusEnergy}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.TwitchCensusActivity}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.SlideToUnlock}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.HomeScreenLockActivity}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.HomeScreenLockMain}");
		twitchActivities.add("ComponentInfo{com.twitch.homescreenlock/com.twitch.homescreenlock.StructuringTheWeb}");
	}
}