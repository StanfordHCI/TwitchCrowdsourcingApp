package com.twitch.homescreenlock;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

public class CogStudyPostTask extends AsyncTask<Activity, Void, Void> {

	private void dumpToFile(Activity a) {
		TwitchDatabase db = new TwitchDatabase(a);
		db.open();
		db.dumpCogStudyToFile(a);
		db.close();
	}
	
	@Override
	protected Void doInBackground(Activity... arg0) {
		Activity activity = arg0[0];
		Log.d("CogStudy", "Started doing post in background");
		dumpToFile(activity);
		
		if(TwitchUtils.isOnline(activity)) {
			Log.d("CogStudy", "is online, trying to push to server");
			
			/* The implementation here can vary depending on your specific
			 * server setup, the protocol you want to use to send the data,
			 * and the level of security you want.
			 */
			
			try {
				String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/CogStudyDumps";
				Log.d("CogStudy", "Looking for dumps in: " + dir_path);
				File dir = new File(dir_path);
				File[] dumps = dir.listFiles();
				
				for(File f : dumps) {
					FileInputStream fis = new FileInputStream(f);
					// Send contents to server
			        fis.close();
				}
		        
				// Close connection and mark upload flag as false
		        TwitchUtils.setUploadDBStatus(activity, false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d("CogStudy", "Couldn't upload DB file: hit exception");
			}		    
		} else {
			Log.d("CogStudy", "Couldn't upload DB file: no internet connection");
		}
		
		return null;
	}

}
