package com.twitch.homescreenlock;

import java.util.HashMap;

import android.app.Activity;

/* Holds information about a user's response to a census app.
 * 
 * An object of this class can be passed into an AsyncTask as its single
 * parameter, and calls to getUrl()/getParams() will expose the necessary
 * components of an HttpPost request.
 */
public class CensusResponse {
	public enum CensusAppType {
		PEOPLE,
		ACTIVITY,
		DRESS,
		ENERGY,
		SLIDE_TO_UNLOCK,
		STRUCTURE_THE_WEB
	}
	
	private static final String[] POST_URLS = {
		"/census/newPeopleResponse",
		"/census/newActivityResponse",
		"/census/newDressResponse",
		"/census/newEnergyResponse",
		"/census/newSlideToUnlockResponse",
		"/census/newStructureTheWebResponse"
	};
	
	private static final String[] APP_NAMES = {
		"Census/People",
		"Census/Activity",
		"Census/Dress",
		"Census/Energy",
		"Slide to Unlock",
		"Structure the Web"
	};
	
	/* url is determined by using the CensusAppType enum passed to the constructor
	 * as an index into POST_URLS. */
	private String url;
	
	/* Params fields:
	 * 	clientLoadTime, clientSubmitTime: set by the classes for each census activity
	 * 	latitude, longitude: set by the classes for each census activity
	 * 	app: set here, based on APP_NAMES and the enum type.
	 * 	source: set here to "Android".
	 *  timezone: set here
	 * 
	 * There should also be a key-value pair, or pairs, unique to the specific
	 * census app (e.g., numPeople->crowd).
	 */
	private HashMap<String, String> params;
	
	public CensusAppType type;
	
	/* Accessors. */
	public String getUrl() { return url; }
	public HashMap<String, String> getParams() { return params; }
	
	/** Constructor: pass in an appropriate CensusAppType and a HashMap of parameters
	 * from the app. Params should include clientLoadTime, clientSubmitTime, latitude,
	 * longitude, and some other unique key/value pair(s).
	 */
	public CensusResponse(HashMap<String, String> params, CensusAppType app, Activity activity) {
		url = TwitchConstants.SERVER_URL + POST_URLS[app.ordinal()];
		if(params == null) params = new HashMap<String, String>();
		this.type = app;
		this.params = params;
		this.params.put("source", "Android");
		this.params.put("app", APP_NAMES[app.ordinal()]);
		this.params.put("phoneInfo", TwitchUtils.getDeviceName());
		this.params.put("deviceHash", TwitchUtils.getDeviceID(activity));
		this.params.put("timezone", TwitchUtils.getTimezone());
	}
}
