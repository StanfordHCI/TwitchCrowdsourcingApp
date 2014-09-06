package com.twitch.homescreenlock;

import java.util.Calendar;

public class TwitchConstants {
	private TwitchConstants() { } // Prevent instantiation
	
	/* Day of week to force slide-to-unlock. */
	public static final int BASELINE_DAY = Calendar.SUNDAY;
	
	/* Default app to run if user hasn't manually changed preferences. */
	public static final String DEFAULT_APP = "TwitchCensus";
	
	/* Length, in millis, of feedback Toast. */
	public static final int TOAST_DURATION = 1250;
	
	/* Put Twitch before or after any security unlocks? */
	public static final boolean TWITCH_BEFORE_SECURITY = false;
	
	/* URL identifying base Twitch EC2 server, including port. */
	public static final String SERVER_URL = "http://your.server.here";
	
	public static final double SAME_LOCATION_RADIUS = 5.0;
	
	/* Minimum milliseconds between polling server for aggregation updates
	 * and sending responses cached on the local database.
	 * Read calculation as (1000=ms) * (60=seconds) * (mins) */
	public static final int TIME_BETWEEN_UPDATES = 1000 * 60 * 10;
	
	public static final int TIME_BETWEEN_PHOTO_DUMPS = 1000 * 60 * 60 * 12;
	
	/* STW constants */
	public static final String DEFAULT_TOPIC = "default_STW_topic";
	
	
	/* ======== Useful naming schemes ======== */
	public static final String PREF_FILE = "TWITCH_PREFERENCES";
	
	public static final String PREF_GEOCODING_DONE = "geocodingDone";
	public static final String PREF_GEOCODING_SUCCESS = "geocodingSuccess";
	public static final String PREF_HAS_PREV_LOCATION = "hasPrevLocation";
	public static final String PREF_PREV_LAT = "prevLatitutde";
	public static final String PREF_PREV_LONG = "prevLongitude";
	public static final String PREF_CURR_LAT = "currLatitude";
	public static final String PREF_CURR_LONG = "currLongitude";
	public static final String PREF_LOCATION_TEXT = "locationText";
	public static final String PREF_SENT_EMAIL = "sentEmail";
	public static final String PREF_EMAIL_ADDRESS = "emailAddress";
	
	public static final String PREF_UPLOAD_DB = "uploadDB";
	public static final String PREF_LAST_DUMPED_PHOTOS = "lastDumpedPhotos";
	
	public static final String PREF_STW_ROW = "STW_row";
	public static final String PREF_SAVED_APP_PREF = "savedAppPref";
	
	public static final String[] AGGREGATE_TYPE_NAMES = {
		"peopleResponses",
		"activityResponses",
		"dressResponses",
		"energyResponses"
	};
	public static enum TwitchStats {
		SCREEN_ON,
		EXIT_BUTTON
	}
	
	/* ======== Debugging utility constants ======== */
	
	/* Force the census app to "Activity?" so new features can be tested
	 * in only one file - TwitchCensusActivity.java. */
	public static final boolean FORCE_ACTIVITY = false;
}
