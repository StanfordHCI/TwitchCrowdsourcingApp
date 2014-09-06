package com.twitch.homescreenlock;

/**
 * Log: Wrapper class around android.util.Log
 * @author keithwyngarden
 * Wraps log calls so debugging messages may be easily turned off
 * in final .apk (requirement for Google Play).
 */
public class Log {
	
	// For production .apk: set to android.util.Log.WARN
	public static final int LEVEL = android.util.Log.DEBUG;
	
	/**
	 * Wrapper around android.util.Log.d(tag, msg) that only executes
	 * if Log.LEVEL is no higher than android.uti.log.DEBUG.
	 * @param tag Tag of debugging output
	 * @param msg Message of debugging output
	 */
	public static void d(String tag, String msg) {
		if(LEVEL <= android.util.Log.DEBUG) {
			android.util.Log.d(tag, msg);
		}
	}
}
