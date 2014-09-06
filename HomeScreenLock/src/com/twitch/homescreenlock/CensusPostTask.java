package com.twitch.homescreenlock;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

/** AsyncTask subclass to send an HttpPost to the Twitch server.
 * Only doInBackground(CensusResponse) is implemented; no progress or return
 * values are used.
 * @author keithwyngarden
 */
public class CensusPostTask extends AsyncTask<CensusPostTask.Param, Void, Void> {

	public class Param {
		public Context context;
		public CensusResponse response;
		public Param(Context context, CensusResponse response) {
			this.context = context;
			this.response = response;
		}
	}
	
	public void makeLocalEntry(Context context, CensusResponse response) {
		Log.d("TwitchServer", "Couldn't reach TwitchServer, rerouting response to local db");
		TwitchDatabase db = new TwitchDatabase(context);
    	db.open();
    	db.addResponse(response);
    	db.close();
	}
	
	@Override
	protected Void doInBackground(Param... param) {
		Param p = param[0];
		if(!TwitchUtils.twitchServerOnline()) {
			if(p.context != null) { // if null, can't store locally (happens if lose connection during update)
		    	makeLocalEntry(p.context, p.response);
			}
	    	return null;
		}
		
		// Otherwise, server is online and we can send a request through the API
		try {
			HttpClient client = new DefaultHttpClient();  
	        HttpPost post = new HttpPost(p.response.getUrl());
	        JSONObject json = new JSONObject(p.response.getParams());

	        StringEntity se = new StringEntity(json.toString());
	        post.setEntity(se);
	        post.setHeader("Content-Type", "application/json");
	        Log.d("TwitchServer", "Sending post with JSON: " + json.toString());
	        HttpResponse httpResponse = client.execute(post);
	        Log.d("TwitchServer", "CensusPostTask responseCode: " + httpResponse.getStatusLine().getStatusCode());
	    } catch (Exception e) {
	        e.printStackTrace();
	        makeLocalEntry(p.context, p.response);
	    }
		Log.d("TwitchServer", "Done with CensusPostTask");
		return null;
	}
}