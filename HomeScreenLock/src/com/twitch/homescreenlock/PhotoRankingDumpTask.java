package com.twitch.homescreenlock;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;

public class PhotoRankingDumpTask extends AsyncTask<Activity, Void, Void> {

	private void dumpToFile(Activity activity) {
		TwitchDatabase db = new TwitchDatabase(activity);
		db.open();
		db.dumpPhotoRankingToFile(activity);
		db.close();
	}
	
	@Override
	protected Void doInBackground(Activity... arg0) {
		Activity activity = arg0[0];
		Log.d("PhotoDump", "Started doing post in background");
		dumpToFile(activity);
		
		/* The implementation here can vary depending on your specific
		 * server setup, the protocol you want to use to send the data,
		 * and the level of security you want.
		 */
		
		if(TwitchUtils.isOnline(activity)) {
			Log.d("PhotoDump", "is online, trying to push to server");

			try {
				String dir_path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/PhotoRankingDumps";
				Log.d("PhotoDump", "Looking for dumps in: " + dir_path);
				File dir = new File(dir_path);
				File[] dumps = dir.listFiles();
				
				for(File f : dumps) {
					FileInputStream fis = new FileInputStream(f);
					// Send to server
			        fis.close();
				}
		        
				// Do any cleanup and note update time
		        TwitchUtils.setLastDumpedPhotos(activity);
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
