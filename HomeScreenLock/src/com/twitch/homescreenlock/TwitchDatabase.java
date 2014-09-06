package com.twitch.homescreenlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class TwitchDatabase {
	
	//To find out whether USB/External storage is accessible.
	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	String state = Environment.getExternalStorageState(); 

	//Variables for stored aggregation tables
	public static final String KEY_LAST_CHECKED = "checkedTime";
	public static final String KEY_RESPONSE_TYPE = "response_type";
	public static final String KEY_RESPONSE_VALUE = "response_value";
	public static final String KEY_RESPONSE_COUNT = "num_responses";
	
	// Variables for STW aggregation table
	public static final String KEY_SOURCE_SENTENCE = "sourceSentence";
	public static final String KEY_RIGHT = "feedbackRight";
	public static final String KEY_WRONG = "feedbackWrong";
	public static final String KEY_STW_TOTAL = "feedbackTotal";
	
	//Variables for Twitch Statistics Table
	public static final String KEY_ROWID_STATS = "_id";
	public static final String KEY_STAT_TYPE = "stat";
	public static final String KEY_STAT_COUNT = "count";
	public static final String VAL_NUM_SCREEN_ON = "number_of_screen_on";
	public static final String VAL_NUM_EXIT_BUTTON = "number_of_exits";
	
	// Variables for Twitch Cognitive Load Study
	public static final String KEY_ROWID_COGSTUDY = "_id";
	public static final String KEY_PHONE_ID = "phoneID";
	public static final String KEY_ENTRY_NUMBER = "number";
	public static final String KEY_CONDITION = "condition";
	public static final String KEY_TASK = "task";
	public static final String KEY_PREV_TASK = "previousTask";
	public static final String KEY_DURATION_COGSTUDY = "duration";
	public static final String KEY_USER_GUESS = "userGuess";
	public static final String KEY_CORRECT_ANSWER = "correctAnswer";
	
	// Shared Census keys
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	public static final String KEY_LOAD_TIME = "clientLoadTime";
	public static final String KEY_SUBMIT_TIME = "clientSubmitTime";
	
	//Variables for Census People Table
	public static final String KEY_ROWID_PEOPLE = "_id";
	public static final String KEY_NUMBEROFPEOPLE = "number_of_people";

	//Variables for Census Dress Table
	public static final String KEY_ROWID_DRESS = "_id";
	public static final String KEY_TYPEOFDRESS = "type_of_dress";

	//Variables for Census Energy Table
	public static final String KEY_ROWID_ENERGY = "_id";
	public static final String KEY_ENERGYLEVEL = "energy_level";

	//Variables for Census Activity Table
	public static final String KEY_ROWID_ACTIVITY = "_id";
	public static final String KEY_ACTIVITYLEVEL = "current_activity";

	//Variables for Images Table 
	public static final String KEY_ROWID_FORIMAGES = "_id";
	public static final String IMAGEVIEW_CHOOSEN = "imageView1or2";
	public static final String IMAGEVIEW_TOPIMAGE = "top_image";
	public static final String IMAGEVIEW_BOTTOMIMAGE = "bottom_image";
	public static final String IMAGEVIEW_TOPIMAGE_NAME = "top_image_name";
	public static final String IMAGEVIEW_BOTTOMIMAGE_NAME = "bottom_image_name";
	public static final String IMAGEVIEW_DURATION = "duration";

	//Variables for Images URL Table
	public static final String KEY_ROWID_FORIMAGESURL = "_id";
	public static final String IMAGES_URLS = "urls";
	public static final String IMAGES_URLS_FLAG = "flag"; //Selected randomly/to Download	
	public static final String IMAGES_HEIGHT = "height";
	public static final String IMAGES_WIDTH = "width";
	public static final String IMAGES_URLS_DELETEFLAG = "delete_flag";

	//Variables for Slide to Unlock Table
	public static final String KEY_ROWID_UNLOCK = "_id";	
	public static final String KEY_PROGRESS = "progress";

	//Variables for StructureTheWeb Table
	public static final String KEY_ROWID_WEB = "_id";	
	public static final String KEY_SENTENCE = "sourceSentence";
	public static final String KEY_FEEDBACK = "feedback";
	public static final String KEY_TOPIC = "topic";

	//Variable for Database which would contain tables 
	private static final String DATABASE_NAME = "Twitch_db";
	private static final int DATABASE_VERSION = 1;

	//Variable for the table (Names of tables)
	private static final String DATABASE_TABLE_PEOPLECENSUS = "PeopleCensus_tb"; //Name of table
	private static final String DATABASE_TABLE_IMAGES = "Images_tb"; //Name of table
	private static final String DATABASE_TABLE_DRESSCENSUS = "Dress_tb"; //Name of table
	private static final String DATABASE_TABLE_ENERGYCENSUS = "Energy_tb"; //Name of table
	private static final String DATABASE_TABLE_ACTIVITYCENSUS = "Activity_tb"; //Name of table
	private static final String DATABASE_TABLE_TWITCHSTATISTICS = "TwitchStats_tb"; //Name of table
	private static final String DATABASE_TABLE_IMAGEURLS = "ImageURLs_tb"; //Name of table
	private static final String DATABASE_TABLE_UNLOCK = "SlideToUnlock_tb"; //Name of table
	private static final String DATABASE_TABLE_WEB = "StructureTheWeb_tb"; //Name of table
	private static final String DATABASE_TABLE_LAST_CHECKED = "LastChecked_tb";
	private static final String DATABASE_TABLE_CENSUS_RESPONSES = "CensusResponses_tb";
	private static final String DATABASE_TABLE_COG_STUDY = "CogStudy_tb";
	private static final String DATABASE_TABLE_STW_AGGREGATES = "STW_Aggregates_tb";

	private DbHelper dbHelperTwitch; 
	private final Context contextTwitch;
	private SQLiteDatabase databaseTwitch; 

	//Subclass to create a database 
	private static class DbHelper extends SQLiteOpenHelper{

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// Auto-generated constructor stub
		}

		@Override //Is called once when we create the database. 
		public void onCreate(SQLiteDatabase db) {
			// Creating table with columns
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_PEOPLECENSUS + " (" +
					KEY_ROWID_PEOPLE + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					KEY_NUMBEROFPEOPLE + " TEXT NOT NULL, " +
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"
					); 
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_IMAGES + " (" +
					KEY_ROWID_FORIMAGES + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					IMAGEVIEW_CHOOSEN + " TEXT NOT NULL, " + 
					IMAGEVIEW_TOPIMAGE + " TEXT NOT NULL, " +
					IMAGEVIEW_BOTTOMIMAGE + " TEXT NOT NULL, " +
					IMAGEVIEW_TOPIMAGE_NAME + " TEXT NOT NULL, " +
					IMAGEVIEW_BOTTOMIMAGE_NAME + " TEXT NOT NULL, " +
					IMAGEVIEW_DURATION + " INTEGER NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_DRESSCENSUS + " (" +
					KEY_ROWID_DRESS + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					KEY_TYPEOFDRESS + " TEXT NOT NULL, " + 
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"
					); 
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_ENERGYCENSUS + " (" +
					KEY_ROWID_ENERGY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					KEY_ENERGYLEVEL + " TEXT NOT NULL, " + 
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_ACTIVITYCENSUS + " (" +
					KEY_ROWID_ACTIVITY + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					KEY_ACTIVITYLEVEL + " TEXT NOT NULL, " + 
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_IMAGEURLS + " (" +
					KEY_ROWID_FORIMAGESURL + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					IMAGES_URLS + " TEXT NOT NULL, " +
					IMAGES_URLS_FLAG + " TEXT NOT NULL, " +					
					IMAGES_HEIGHT + " INTEGER NOT NULL, " +
					IMAGES_WIDTH + " INTEGER NOT NULL, " +
					IMAGES_URLS_DELETEFLAG + " TEXT NOT NULL);"					
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_UNLOCK + " (" +
					KEY_ROWID_UNLOCK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_PROGRESS + " INTEGER NOT NULL, " +
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_WEB + " (" +
					KEY_ROWID_WEB + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_SENTENCE + " TEXT NOT NULL, " +
					KEY_FEEDBACK + " TEXT NOT NULL, " +
					KEY_TOPIC + " TEXT NOT NULL, " +
					KEY_LATITUDE + " FLOAT NOT NULL, " +
					KEY_LONGITUDE + " FLOAT NOT NULL, " +
					KEY_LOAD_TIME + " INTEGER NOT NULL, " +
					KEY_SUBMIT_TIME + " INTEGER NOT NULL);"				
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_COG_STUDY + " (" +
					KEY_ROWID_COGSTUDY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_PHONE_ID + " TEXT NOT NULL, " +
					KEY_ENTRY_NUMBER + " INTEGER NOT NULL, " +
					KEY_CONDITION + " TEXT NOT NULL, " +
					KEY_TASK + " TEXT NOT NULL, " +
					KEY_PREV_TASK + " TEXT NOT NULL, " +
					KEY_DURATION_COGSTUDY + " INTEGER NOT NULL, " +
					KEY_USER_GUESS + " INTEGER, " +
					KEY_CORRECT_ANSWER + " INTEGER);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_TWITCHSTATISTICS + " (" +
					KEY_ROWID_STATS + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_STAT_TYPE + " TEXT NOT NULL, " +
					KEY_STAT_COUNT + " INTEGER NOT NULL);"
					);
			// Initially 0 screen on, exits
			ContentValues cv = new ContentValues();
			cv.put(KEY_STAT_TYPE, VAL_NUM_SCREEN_ON);
			cv.put(KEY_STAT_COUNT, 0);
			db.insert(DATABASE_TABLE_TWITCHSTATISTICS, null, cv);
			cv = new ContentValues();
			cv.put(KEY_STAT_TYPE, VAL_NUM_EXIT_BUTTON);
			cv.put(KEY_STAT_COUNT, 0);
			db.insert(DATABASE_TABLE_TWITCHSTATISTICS, null, cv);
			
			// Aggregation
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_LAST_CHECKED + " (" +
					KEY_LAST_CHECKED + " INTEGER NOT NULL);"
					);
			cv = new ContentValues();
			cv.put(KEY_LAST_CHECKED, 0); // Never checked
			db.insert(DATABASE_TABLE_LAST_CHECKED, null, cv);
			
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_CENSUS_RESPONSES + " (" +
					KEY_RESPONSE_TYPE + " TEXT NOT NULL, " +
					KEY_RESPONSE_VALUE + " TEXT NOT NULL, " +
					KEY_RESPONSE_COUNT + " INTEGER NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + DATABASE_TABLE_STW_AGGREGATES + " (" +
					KEY_SOURCE_SENTENCE + " TEXT NOT NULL, " +
					KEY_RIGHT + " INTEGER NOT NULL, " +
					KEY_WRONG + " INTEGER NOT NULL, " +
					KEY_STW_TOTAL + " INTEGER NOT NULL);"
					);
			
			Log.d("TwitchDB", "Tables created.");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//If the table exists, then drop it and call OnCreate method. 
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PEOPLECENSUS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_IMAGES);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_DRESSCENSUS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ENERGYCENSUS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ACTIVITYCENSUS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_TWITCHSTATISTICS);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_IMAGEURLS);	
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_UNLOCK);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_WEB);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_LAST_CHECKED);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CENSUS_RESPONSES);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_COG_STUDY);
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STW_AGGREGATES);
			onCreate(db);
		}
	}

	public TwitchDatabase(Context c)
	{
		contextTwitch = c; 
	}

	public TwitchDatabase open() throws SQLException // Open the database to write 
	{
		dbHelperTwitch = new DbHelper(contextTwitch);
		databaseTwitch = dbHelperTwitch.getWritableDatabase(); 
		return this; 
	}

	public void close()
	{
		dbHelperTwitch.close(); 
		//Copy the database file AFTER closing the execution, to the SDCARD. This helps in analysis.
		//By default db file is saved internally/locally, but not on SDCARD. 
		if(Environment.MEDIA_MOUNTED.equals(state)) //Checking if SDCARD is accessible. 
		{
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			try {
				File sd = Environment.getExternalStorageDirectory();
				File data = Environment.getDataDirectory();

				if (sd.canWrite()) {
					String currentDBPath = "//data//com.twitch.homescreenlock//databases//Twitch_db";
					String backupDBPath = "Twitch_db"; //Default 
					File currentDB = new File(data, currentDBPath);
					File backupDB = new File(sd, backupDBPath);

					if (currentDB.exists()) {
						FileInputStream fi = new FileInputStream(currentDB);
						FileOutputStream fo = new FileOutputStream(backupDB);
						FileChannel src = fi.getChannel();
						FileChannel dst = fo.getChannel();
						dst.transferFrom(src, 0, src.size());
						src.close();
						dst.close();
						fi.close();
						fo.close();
					}
				}
			} catch (Exception e) {
				e.printStackTrace(); 
			}
		}
	}
	
	// === Cognitive Load Study ===
	public long createCogStudyEntry(String phoneID, int entryNum,
			String condition, String task, String prevTask, int duration,
			Boolean userGuess, Boolean correctAnswer) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_PHONE_ID, phoneID);
		cv.put(KEY_ENTRY_NUMBER, entryNum);
		cv.put(KEY_CONDITION, condition);
		cv.put(KEY_TASK, task);
		cv.put(KEY_PREV_TASK, prevTask);
		cv.put(KEY_DURATION_COGSTUDY, duration);
		// If userGuess/correctAnswer are null, don't explicitly insert
		if(userGuess != null) {
			cv.put(KEY_USER_GUESS, userGuess.booleanValue());
		}
		if(correctAnswer != null) {
			cv.put(KEY_CORRECT_ANSWER, correctAnswer.booleanValue());
		}
		return databaseTwitch.insert(DATABASE_TABLE_COG_STUDY, null, cv); 
	}
	
	public void clearCogStudyTable() {
		databaseTwitch.delete(DATABASE_TABLE_COG_STUDY, null, null);
	}
	
	public void dumpCogStudyToFile(Activity a) {
		Cursor c = databaseTwitch.query(DATABASE_TABLE_COG_STUDY, null, null, null, null, null, null);
		try {
			c.moveToFirst();
			if(c.getCount() > 0) {
				StringBuilder sb = new StringBuilder();
				DatabaseUtils.dumpCursor(c, sb);
				String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
				File folder = new File(path + "/CogStudyDumps");
				if(!folder.exists()) {
					folder.mkdir();
				}
				path += "/CogStudyDumps/" + System.currentTimeMillis() + ".dmp";
				File file = new File(path);
				FileWriter fw = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(sb.toString());
				out.close();
				// Once we've dumped it, clear the table
				clearCogStudyTable();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("CogStudy", "Exception occurred, failed to dump sqlite file");
		}
	}
	
	// Photo ranking dumping (DATABASE_TABLE_IMAGES)
	public void clearPhotoRankingTable() {
		databaseTwitch.delete(DATABASE_TABLE_IMAGES, null, null);
	}
	
	public void dumpPhotoRankingToFile(Activity a) {
		Cursor c = databaseTwitch.query(DATABASE_TABLE_IMAGES, null, null, null, null, null, null);
		try {
			c.moveToFirst();
			if(c.getCount() > 0) {
				StringBuilder sb = new StringBuilder();
				DatabaseUtils.dumpCursor(c, sb);
				String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
				File folder = new File(path + "/PhotoRankingDumps");
				if(!folder.exists()) {
					Log.d("PhotoDump", "Making directory: " + folder.getAbsolutePath());
					folder.mkdir();
				}
				path += "/PhotoRankingDumps/" + System.currentTimeMillis() + ".dmp";
				File file = new File(path);
				FileWriter fw = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(sb.toString());
				out.close();
				// Once we've dumped it, clear the table
				clearPhotoRankingTable();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("PhotoDump", "Exception occurred, failed to dump sqlite file");
		}
	}
	
	
	// === Aggregation and Caching ===
	
	// If we haven't updated aggregates recently and the phone has internet connection,
	// grab the most recent aggregates from the server.
	public void checkAggregates(Activity a) {
		double latitude = TwitchUtils.getCurrLatitude(a);
		double longitude = TwitchUtils.getCurrLongitude(a);
		if(!TwitchUtils.isOnline(a)) return;
		Cursor c = databaseTwitch.query(DATABASE_TABLE_LAST_CHECKED, new String[]{ KEY_LAST_CHECKED }, null, null, null, null, null);
		if(c != null) {
			try {
				c.moveToFirst();
				long timeLastUpdated = c.getLong(0);
				Log.d("Aggregates", "Last updated: " + timeLastUpdated);
				if((System.currentTimeMillis() - timeLastUpdated) > TwitchConstants.TIME_BETWEEN_UPDATES) {
					UpdateAggregatesTask task = new UpdateAggregatesTask();
					UpdateAggregatesTask.Param p = task.new Param(a, latitude, longitude, this);
					task.execute(p);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				c.close();
			}
		}
	}

	// Update the CensusResponses table with aggregation information in the given json.
	public void updateAggregates(String json) {
		Log.d("Aggregates", "Got aggregate JSON: " + json);
		try {
			JSONObject jo = new JSONObject(json);
			// Clear old aggregates
			databaseTwitch.delete(DATABASE_TABLE_CENSUS_RESPONSES, null, null);
			databaseTwitch.delete(DATABASE_TABLE_STW_AGGREGATES, null, null);
			
			for(int i = 0; i < TwitchConstants.AGGREGATE_TYPE_NAMES.length; i++) { // iterate type names, eg "peopleResponses"
				String typeName = TwitchConstants.AGGREGATE_TYPE_NAMES[i];
				JSONArray values = jo.getJSONArray(typeName);
				for(int j = 0; j < values.length(); j++) { // iterate values, eg ["crowd",33]
					JSONArray valuePair = values.getJSONArray(j);
					ContentValues cv = new ContentValues();
					cv.put(KEY_RESPONSE_TYPE, typeName);
					cv.put(KEY_RESPONSE_VALUE, valuePair.getString(0));
					cv.put(KEY_RESPONSE_COUNT, valuePair.getInt(1));
					Log.d("Aggregates", "Putting {" + valuePair.getString(0) + ", "
							+ valuePair.getInt(1) + "} in aggregates db for type " + typeName);
					long success = databaseTwitch.insert(DATABASE_TABLE_CENSUS_RESPONSES, null, cv);
					if(success == -1) {
						Log.d("Aggregates", "Error putting {" + valuePair.getString(0) + ", "
								+ valuePair.getInt(1) + "} in local db");
					}
				}
			}
			
			JSONObject STW_aggregates = jo.getJSONObject("STWresponses");
			updateSTWaggregates(STW_aggregates);		
			
			updateLastChecked();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void updateSTWaggregates(JSONObject STW_aggregates) throws JSONException {
		Iterator<String> iter = STW_aggregates.keys();
		while(iter.hasNext()) { // Iterate over sentences
			String sentenceKey = iter.next();
			JSONObject sentenceCounts = STW_aggregates.getJSONObject(sentenceKey);
			Iterator<String> countsIter = sentenceCounts.keys();
			
			HashMap<String, Integer> counts = new HashMap<String, Integer>();
			counts.put("Right", 0);
			counts.put("Wrong", 0);
			counts.put("total", 0);
			
			while(countsIter.hasNext()) { // Iterate over feedback fields ("Right", "Wrong", "total")
				String feedback = countsIter.next();
				int count = sentenceCounts.getInt(feedback);
				counts.put(feedback, count);
				Log.d("Twitch_STW", "For sentence [" + sentenceKey + "], found count " + 
						count + " for feedback --" + feedback + "--");
			}
			
			ContentValues cv = new ContentValues();
			cv.put(KEY_SOURCE_SENTENCE, sentenceKey);
			cv.put(KEY_RIGHT, counts.get("Right"));
			cv.put(KEY_WRONG, counts.get("Wrong"));
			cv.put(KEY_STW_TOTAL, counts.get("total"));
			databaseTwitch.insert(DATABASE_TABLE_STW_AGGREGATES, null, cv);
		}
	}
	
	public int[] getPercentageForSentenceFeedback(String sentence, String feedback) {
		String selection = KEY_SOURCE_SENTENCE + "=?";
		int count = 1, total = 1;
		Log.d("Twitch_STW", "Retrieving aggregates for feedback --" + feedback + "--" +
				" on sentence: " + sentence);
		try {
			Cursor c = databaseTwitch.query(DATABASE_TABLE_STW_AGGREGATES, null, selection,
					new String[] { sentence }, null, null, null);
			if(c.getCount() == 0) return new int[] { 100, 1 }; // No previous aggregates for that sentence
			c.moveToFirst();
			// Otherwise, calculate percentage of total, including this response
			total += c.getInt(3);
			if(feedback.equals("Right")) {
				count += c.getInt(1);
			} else if(feedback.equals("Wrong")) {
				count += c.getInt(2);
			} else {
				Log.d("Twitch_STW", "Invalid feedback: " + feedback);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		double percentage = (100.0 * count) / total;
		return new int[] { (int) Math.round(percentage), total };
	}
	
	// Update the LastChecked table to note that we just updated aggregates.
	private long updateLastChecked() {
		databaseTwitch.delete(DATABASE_TABLE_LAST_CHECKED, null, null); // clear previous check time
		ContentValues cv = new ContentValues();
		cv.put(KEY_LAST_CHECKED, System.currentTimeMillis());
		return databaseTwitch.insert(DATABASE_TABLE_LAST_CHECKED, null, cv);
	}
	
	public int getPercentageForResponse(String type, String value) {
		if(!Arrays.asList(TwitchConstants.AGGREGATE_TYPE_NAMES).contains(type)) {
			Log.d("Aggregates", "asked for invalid type: " + type);
			return 0;
		}
		String selection = KEY_RESPONSE_TYPE + "='" + type + "' and " + KEY_RESPONSE_VALUE
				+ "='" + value + "'";
		Cursor c = databaseTwitch.query(DATABASE_TABLE_CENSUS_RESPONSES,
				new String[]{KEY_RESPONSE_COUNT}, selection, null, null, null, null);
		try {
			int numSameResponse = 1;
			if(c.getCount() > 0) {
				c.moveToFirst();
				numSameResponse += c.getInt(0);
			} else {
				Log.d("Aggregates", "Didn't find entry for " + selection);
			}
			
			int totalResponses = getNumResponses(type, value);
			double percentage = (100.0 * numSameResponse) / totalResponses;
			return (int) Math.round(percentage);
		} catch(Exception e) {
			e.printStackTrace();
			Log.d("Aggregates", "Returning 100 after local query exception.");
			return 100;
		} finally {
			c.close();
		}
	}
	
	public int getNumResponses(String type, String value) {
		String totalSelection = KEY_RESPONSE_TYPE + "='" + type + "' and " + KEY_RESPONSE_VALUE
				+ "='total'";
		Cursor c = databaseTwitch.query(DATABASE_TABLE_CENSUS_RESPONSES,
				new String[]{KEY_RESPONSE_COUNT}, totalSelection, null, null, null, null);
		int totalResponses = 1;
		try {
			if(c.getCount() > 0) {
				c.moveToFirst();
				totalResponses += c.getInt(0);
			} else {
				Log.d("Aggregates", "Didn't find entry for total");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		
		return totalResponses;
	}
	
	// Caching
	
	public void addResponse(CensusResponse response) {
		Log.d("Caching", "Adding response to db after failure to connect to Twitch server");
		HashMap<String, String> params = response.getParams();
		switch(response.type) {
			case PEOPLE:
				createEntryPeopleCensus(params.get("numPeople"),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
			case ACTIVITY:
				createEntryActivityCensus(params.get("activity"),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
			case DRESS:
				createEntryDressCensus(params.get("dressType"),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
			case ENERGY:
				createEntryEnergyCensus(params.get("energyLevel"),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
			case SLIDE_TO_UNLOCK:
				createSlideToUnlock(Integer.parseInt(params.get("progress")),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
			case STRUCTURE_THE_WEB:
				createStructuringTheWeb(params.get("sourceSentence"),
						params.get("feedback"),
						params.get("topic"),
						Double.parseDouble(params.get("latitude")),
						Double.parseDouble(params.get("longitude")),
						Long.parseLong(params.get("clientLoadTime")),
						Long.parseLong(params.get("clientSubmitTime")));
				break;
		default:
			break;
		}
	}
	
	public ArrayList<CensusResponse> getCachedResponses(Activity a) {
		if(!TwitchUtils.twitchServerOnline()) {
			return null;
		}
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		responses.addAll(getCensusActivityResponses(a));
		responses.addAll(getCensusPeopleResponses(a));
		responses.addAll(getCensusDressResponses(a));
		responses.addAll(getCensusEnergyResponses(a));
		responses.addAll(getSlideToUnlockResponses(a));
		responses.addAll(getSTWResponses(a));
		// Clear cached responses
		databaseTwitch.delete(DATABASE_TABLE_PEOPLECENSUS, null, null);
		databaseTwitch.delete(DATABASE_TABLE_ACTIVITYCENSUS, null, null);
		databaseTwitch.delete(DATABASE_TABLE_DRESSCENSUS, null, null);
		databaseTwitch.delete(DATABASE_TABLE_ENERGYCENSUS, null, null);
		databaseTwitch.delete(DATABASE_TABLE_UNLOCK, null, null);
		databaseTwitch.delete(DATABASE_TABLE_WEB, null, null);
		return responses;
	}
	
	public ArrayList<CensusResponse> getSlideToUnlockResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_PROGRESS, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_UNLOCK,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("progress", Integer.toString(c.getInt(0)));
				params.put("latitude", Double.toString(c.getDouble(1)));
				params.put("longitude", Double.toString(c.getDouble(2)));
				params.put("clientLoadTime", Long.toString(c.getLong(3)));
				params.put("clientSubmitTime", Long.toString(c.getLong(4)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.STRUCTURE_THE_WEB, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return responses;
	}
	
	public ArrayList<CensusResponse> getCensusActivityResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_ACTIVITYLEVEL, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_ACTIVITYCENSUS,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("activity", c.getString(0));
				params.put("latitude", Double.toString(c.getDouble(1)));
				params.put("longitude", Double.toString(c.getDouble(2)));
				params.put("clientLoadTime", Long.toString(c.getLong(3)));
				params.put("clientSubmitTime", Long.toString(c.getLong(4)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.ACTIVITY, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return responses;
	}
	
	public ArrayList<CensusResponse> getCensusPeopleResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_NUMBEROFPEOPLE, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_PEOPLECENSUS,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("numPeople", c.getString(0));
				params.put("latitude", Double.toString(c.getDouble(1)));
				params.put("longitude", Double.toString(c.getDouble(2)));
				params.put("clientLoadTime", Long.toString(c.getLong(3)));
				params.put("clientSubmitTime", Long.toString(c.getLong(4)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.PEOPLE, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		
		return responses;
	}
	
	public ArrayList<CensusResponse> getCensusDressResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_TYPEOFDRESS, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_DRESSCENSUS,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("dressType", c.getString(0));
				params.put("latitude", Double.toString(c.getDouble(1)));
				params.put("longitude", Double.toString(c.getDouble(2)));
				params.put("clientLoadTime", Long.toString(c.getLong(3)));
				params.put("clientSubmitTime", Long.toString(c.getLong(4)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.DRESS, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return responses;
	}
	
	public ArrayList<CensusResponse> getCensusEnergyResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_ENERGYLEVEL, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_ENERGYCENSUS,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("energyLevel", c.getString(0));
				params.put("latitude", Double.toString(c.getDouble(1)));
				params.put("longitude", Double.toString(c.getDouble(2)));
				params.put("clientLoadTime", Long.toString(c.getLong(3)));
				params.put("clientSubmitTime", Long.toString(c.getLong(4)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.ENERGY, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return responses;
	}
	
	public ArrayList<CensusResponse> getSTWResponses(Activity a) {
		ArrayList<CensusResponse> responses = new ArrayList<CensusResponse>();
		String[] columns = {KEY_SENTENCE, KEY_FEEDBACK, KEY_TOPIC, KEY_LATITUDE, KEY_LONGITUDE,
				KEY_LOAD_TIME, KEY_SUBMIT_TIME };
		Cursor c = databaseTwitch.query(DATABASE_TABLE_WEB,
				columns, null, null, null, null, null);
		try {
			for(int i = 0; i < c.getCount(); i++) {
				c.moveToPosition(i);
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("sourceSentence", c.getString(0));
				params.put("feedback", c.getString(1));
				params.put("topic", c.getString(2));
				params.put("latitude", Double.toString(c.getDouble(3)));
				params.put("longitude", Double.toString(c.getDouble(4)));
				params.put("clientLoadTime", Long.toString(c.getLong(5)));
				params.put("clientSubmitTime", Long.toString(c.getLong(6)));
				CensusResponse response = new CensusResponse(params, CensusResponse.CensusAppType.ENERGY, a);
				responses.add(response);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return responses;
	}
	
	//Writing in the database. 
	public long createEntryPeopleCensus(String numberOfPeople, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues(); 
		cv.put(KEY_NUMBEROFPEOPLE, numberOfPeople);
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_PEOPLECENSUS, null, cv); 
	}

	//Writing in the database. 
	public long createEntryImages(String whichImageView, String topImageURL, String bottomImageURL, String topImageName, String bottomImageName, long duration) {
		ContentValues cv = new ContentValues(); 
		cv.put(IMAGEVIEW_CHOOSEN, whichImageView);
		cv.put(IMAGEVIEW_TOPIMAGE, topImageURL);
		cv.put(IMAGEVIEW_BOTTOMIMAGE, bottomImageURL);
		cv.put(IMAGEVIEW_TOPIMAGE_NAME, topImageName);
		cv.put(IMAGEVIEW_BOTTOMIMAGE_NAME, bottomImageName);
		cv.put(IMAGEVIEW_DURATION, duration);
		return databaseTwitch.insert(DATABASE_TABLE_IMAGES, null, cv); 
	}

	//Writing in the database. 
	public long createEntryDressCensus(String typeOfDress, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues(); 
		cv.put(KEY_TYPEOFDRESS, typeOfDress); 
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_DRESSCENSUS, null, cv); 
	}

	//Writing in the database. 
	public long createSlideToUnlock(int progress, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues();		
		cv.put(KEY_PROGRESS, progress);
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_UNLOCK, null, cv);
	}

	//Writing in the database. 
	public long createStructuringTheWeb(String sentence, String feedback, String topic, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_SENTENCE, sentence);
		cv.put(KEY_FEEDBACK, feedback);
		cv.put(KEY_TOPIC, topic);
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_WEB, null, cv); 
	}

	//Writing in the database. 
	public long createEntryEnergyCensus(String energyLevel, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues(); 
		cv.put(KEY_ENERGYLEVEL, energyLevel); 
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_ENERGYCENSUS, null, cv);
	}

	//Writing in the database. 
	public long createEntryActivityCensus(String activity, double lat, double lng, long loadTime, long submitTime) {
		ContentValues cv = new ContentValues(); 
		cv.put(KEY_ACTIVITYLEVEL, activity); 
		cv.put(KEY_LATITUDE, lat);
		cv.put(KEY_LONGITUDE, lng);
		cv.put(KEY_LOAD_TIME, loadTime);
		cv.put(KEY_SUBMIT_TIME, submitTime);
		return databaseTwitch.insert(DATABASE_TABLE_ACTIVITYCENSUS, null, cv); 
	}
	
	private String getStatType(TwitchConstants.TwitchStats stat) {
		String statType = "";
		switch(stat) {
			case SCREEN_ON:
				statType = VAL_NUM_SCREEN_ON;
				break;
			case EXIT_BUTTON:
				statType = VAL_NUM_EXIT_BUTTON;
				break;
		}
		return statType;
	}
	
	//Writing in the database. 
	public void incrementTwitchStats(TwitchConstants.TwitchStats stat) {
		String statType = getStatType(stat);
		ContentValues cv = new ContentValues();
		// Unfortunate construct: for this to work, I had to do 2 queries:
		cv.put(KEY_STAT_COUNT, getStat(stat) + 1);
		String where = KEY_STAT_TYPE + "='" + statType + "'";
		Log.d("TwitchStats", "Increment stat: " + statType);
		databaseTwitch.update(DATABASE_TABLE_TWITCHSTATISTICS, cv, where, null);
	}
	
	public int getStat(TwitchConstants.TwitchStats stat) {
		String[] columns = new String[]{KEY_STAT_COUNT};
		String statType = getStatType(stat);
		String selection = KEY_STAT_TYPE + "='" + statType + "'";
		Cursor c = databaseTwitch.query(DATABASE_TABLE_TWITCHSTATISTICS, columns, selection, null, null, null, null);
		int count = 0;
		try {
			if(c != null && c.moveToFirst()) {
				count = c.getInt(0);
			} else {
				Log.d("TwitchStats", "Empty result for stat " + statType);
			}
		} catch (Exception e) { e.printStackTrace(); }
		Log.d("TwitchStats", "Retrieved value " + count + " for stat " + statType);
		return count;
	}
	
	public void resetStat(TwitchConstants.TwitchStats stat) {
		String statType = getStatType(stat);
		ContentValues cv = new ContentValues();
		cv.put(KEY_STAT_COUNT, 0);
		String where = KEY_STAT_TYPE + "='" + statType + "'";
		Log.d("TwitchStats", "Reset stat: " + statType);
		databaseTwitch.update(DATABASE_TABLE_TWITCHSTATISTICS, cv, where, null);
	}
	
	//Writing in the database. 
	public long createEntryURLS(String urls, String flag, Integer height, Integer width, String deleteflag) {
		ContentValues cv = new ContentValues(); 
		cv.put(IMAGES_URLS, urls); 
		cv.put(IMAGES_URLS_FLAG, flag);		
		cv.put(IMAGES_HEIGHT, height);
		cv.put(IMAGES_WIDTH, width);
		cv.put(IMAGES_URLS_DELETEFLAG, deleteflag);
		return databaseTwitch.insert(DATABASE_TABLE_IMAGEURLS, null, cv);
	}

	//Methods to get data or information from tables. 
	public String getImageURL(int randomRow) {
		String[] columns = new String[]{KEY_ROWID_FORIMAGESURL, IMAGES_URLS, IMAGES_URLS_FLAG, IMAGES_HEIGHT, IMAGES_WIDTH, IMAGES_URLS_DELETEFLAG}; 
		Cursor c = databaseTwitch.query( DATABASE_TABLE_IMAGEURLS, columns, KEY_ROWID_FORIMAGESURL + "=" + randomRow, null, null, null, null); 
		try
		{
			if(c!=null && c.moveToFirst())
			{
				c.moveToFirst(); 
				String url = c.getString(1); //1 is for IMAGE_URLS.
				return url; 
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
		finally
		{
			c.close(); 
		}
		return null;
	}

	//Methods to get data or information from tables. 
	public List<Integer> getListOfSelectedRows() {
		String flagSelected = "Selected";	
		String[] columns = new String[]{KEY_ROWID_FORIMAGESURL, IMAGES_URLS, IMAGES_URLS_FLAG, IMAGES_HEIGHT, IMAGES_WIDTH, IMAGES_URLS_DELETEFLAG}; 
		Cursor c = databaseTwitch.query( DATABASE_TABLE_IMAGEURLS, columns, IMAGES_URLS_FLAG + "='" + flagSelected + "'", null, null, null, null); 
		List<Integer> listToPreventDublicateRandomNumber = new ArrayList<Integer>();
		try
		{
			if(c!=null && c.moveToFirst())
			{			
				for(c.moveToFirst();!c.isAfterLast();c.moveToNext())
				{
					listToPreventDublicateRandomNumber.add(Integer.parseInt(c.getString(0))); //0 is for row ids.  
				}				
				return listToPreventDublicateRandomNumber;	
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}
		finally
		{
			c.close(); 
		}
		return listToPreventDublicateRandomNumber;	
	}

	//Methods to get data or information from tables. 
	public int getNumberOfRows(String nameOfTable) {		
		Cursor c = databaseTwitch.rawQuery("select * from " + nameOfTable ,null);
		try
		{
			if(c!=null && c.moveToFirst())
			{
				return c.getCount();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(); 
		}
		finally
		{
			c.close(); 
		}
		return 0; 
	}

	//Methods to get data or information from tables. 
	public int getNumberOfSelectedRows(String nameOfTable) {

		String flagSelected = "Selected";		
		Cursor c = databaseTwitch.rawQuery("select * from " + nameOfTable + " where flag='" + flagSelected + "'",null);			
		try
		{
			if(c!=null && c.moveToFirst())
			{
				return c.getCount();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			c.close(); 
		}
		return 0; 
	}

	//Method to update the flag from NotSelected to Selected. 
	public void updateURLSelectedFlag(int randomRow) {
		ContentValues cvUpdate = new ContentValues(); 
		cvUpdate.put(IMAGES_URLS_FLAG, "Selected"); 
		databaseTwitch.update(DATABASE_TABLE_IMAGEURLS, cvUpdate, KEY_ROWID_FORIMAGESURL + "=" + randomRow, null);			
	}

	//Method to update the flag from Selected to NotSelected. For all, to loop the table.  
	public void updateURLNotSelectedFlag() {
		ContentValues cvUpdate = new ContentValues(); 
		cvUpdate.put(IMAGES_URLS_FLAG, "NotSelected"); 
		databaseTwitch.update(DATABASE_TABLE_IMAGEURLS, cvUpdate, null, null);			
	}

	//Method to update the choice of whether top image was selected or bottom. 
	public void updateTopBottom(String whichImageView, String topImageName, String bottomImageName, long duration) {
		ContentValues cvUpdate = new ContentValues(); 
		cvUpdate.put(IMAGEVIEW_CHOOSEN, whichImageView);		
		databaseTwitch.update(DATABASE_TABLE_IMAGES, cvUpdate, IMAGEVIEW_TOPIMAGE_NAME + "=" + topImageName + " AND " + IMAGEVIEW_BOTTOMIMAGE_NAME + "=" + bottomImageName, null);	

		ContentValues cvUpdateDuration = new ContentValues(); 
		cvUpdateDuration.put(IMAGEVIEW_DURATION, duration);		
		databaseTwitch.update(DATABASE_TABLE_IMAGES, cvUpdateDuration, IMAGEVIEW_TOPIMAGE_NAME + "=" + topImageName + " AND " + IMAGEVIEW_BOTTOMIMAGE_NAME + "=" + bottomImageName, null);	

	}

}