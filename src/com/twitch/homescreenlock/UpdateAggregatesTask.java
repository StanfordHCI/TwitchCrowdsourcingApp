package com.twitch.homescreenlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.AsyncTask;

public class UpdateAggregatesTask extends AsyncTask<UpdateAggregatesTask.Param, Void, ArrayList<CensusResponse>> {
	
	public class Param {
		public Activity activity;
		public double latitude;
		public double longitude;
		public TwitchDatabase db;
		public Param(Activity activity, double latitude, double longitude, TwitchDatabase db) {
			this.activity = activity;
			this.latitude = latitude;
			this.longitude = longitude;
			this.db = db;
		}
	}
	
	@Override
	protected ArrayList<CensusResponse> doInBackground(Param... param) {
		Param p = param[0];
		// Request aggregate JSON from the server, and update the local database.
		HttpClient client = new DefaultHttpClient();
		
		List<NameValuePair> params = new LinkedList<NameValuePair>();
        params.add(new BasicNameValuePair("lat", String.valueOf(p.latitude)));
        params.add(new BasicNameValuePair("long", String.valueOf(p.longitude)));
        params.add(new BasicNameValuePair("stwTopic", TwitchConstants.DEFAULT_TOPIC));
        params.add(new BasicNameValuePair("timezone", TwitchUtils.getTimezone()));
        params.add(new BasicNameValuePair("deviceHash", TwitchUtils.getDeviceID(p.activity)));
        params.add(new BasicNameValuePair("screenOn", Integer.toString(p.db.getStat(TwitchConstants.TwitchStats.SCREEN_ON))));
        params.add(new BasicNameValuePair("exitButton", Integer.toString(p.db.getStat(TwitchConstants.TwitchStats.EXIT_BUTTON))));
        
        if(!TwitchUtils.hasSentEmail(p.activity)) {
        	String email = TwitchUtils.getEmailAddress(p.activity);
        	if(email != null) {
        		params.add(new BasicNameValuePair("userEmail", email));
        	}
        	TwitchUtils.setEmailStatus(p.activity, true);
        }
        
	    String paramString = URLEncodedUtils.format(params, "utf-8");
	    String uri = TwitchConstants.SERVER_URL + "/twitch_responses/getAggregates?" + paramString;
		HttpGet get = new HttpGet(uri);
		
        Log.d("Aggregates", "Sending GET to: " + uri);
        HttpResponse httpResponse;
        String json = "";
		try {
			httpResponse = client.execute(get);
			json = EntityUtils.toString(httpResponse.getEntity());
		} catch (HttpHostConnectException e) {
			p.db.close();
			return null; // update failed
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// On success, can reset usage stats
		p.db.resetStat(TwitchConstants.TwitchStats.SCREEN_ON);
		p.db.resetStat(TwitchConstants.TwitchStats.EXIT_BUTTON);
		// Update local db with aggregates
        p.db.updateAggregates(json);
        ArrayList<CensusResponse> responses = p.db.getCachedResponses(p.activity);
        p.db.close();
        
        // Launch CogStudyPostTask if necessary (check flag)
        if(TwitchUtils.shouldUploadDB(p.activity)) {
        	new CogStudyPostTask().execute(p.activity);
        }
        
        // Launch PhotoRankingDumpTask if necessary
        if(TwitchUtils.shouldDumpPhotoRanking(p.activity)) {
        	new PhotoRankingDumpTask().execute(p.activity);
        }
        
        return responses;
	}
	
	@Override
	protected void onPostExecute(ArrayList<CensusResponse> responses) {
		if(responses == null) return; // Twitch server offline
		// Push responses cached locally to the server.
        for(CensusResponse response : responses) {
        	Log.d("Caching", "Sending cached response to server");
        	CensusPostTask cpt = new CensusPostTask();
        	CensusPostTask.Param p = cpt.new Param(null, response);
        	cpt.execute(p);
        }
	}
}
