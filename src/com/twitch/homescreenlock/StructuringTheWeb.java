package com.twitch.homescreenlock;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class StructuringTheWeb extends Activity implements View.OnClickListener, LocationListener {

	LocationManager locationManager; 
	String towers;
	static long startTime;
	long endTime;
	
	// Data structure to hold STW questions
	String[] extraction;
	String sourceSentence, topic;
	
	// Demo samples
	String[] demoSentences = {
		"<font color='#9AB5D9'>Stanford University</font> <font color='#ECE7F2'>was founded in</font> <font color='#3690C0'>1885</font> by Leland Stanford as a memorial to their son",
		"Signals are likely perceived and interpreted in <font color='#9AB5D9'>accordance</font> with what <font color='#ECE7F2'>is expected based on</font> <font color='#3690C0'>a user's</font> past experience",
		"<font color='#9AB5D9'>The principles</font> <font color='#ECE7F2'>may be tailored to</font> <font color='#3690C0'>a specific design or situation</font>"
	};
	String[] demoExtractions = {
		"<font color='#9AB5D9'>Stanford University</font> <font color='#ECE7F2'>was founded in</font> <font color='#3690C0'>1885</font>",
		"<font color='#9AB5D9'>accordance</font> <font color='#ECE7F2'>is expected based on</font> <font color='#3690C0'>a user's</font>",
		"<font color='#9AB5D9'>The principles</font> <font color='#ECE7F2'>may be tailored to</font> <font color='#3690C0'>a specific design or situation</font>"
	};
	
	// Layout items
	ImageButton imageButtonRight, imageButtonWrong, imageButtonInfo;
	TextView textSentence, textExtraction, header;
	final String COLOR_TOPIC = "#9AB5D9";
	final String COLOR_RELATION = "#ECE7F2";
	final String COLOR_OBJECT = "#3690C0";
	
	// Control flow between extraction and information pages
	ScrollView app_layout, explanation;
	Button doneButton;
	boolean showingExplanation;
	
	// Sets of icons for right and wrong
	final int[] UNFILLED_ICONS = { R.drawable.rightunfilled, R.drawable.wrongunfilled };
	final int[] FILLED_ICONS = { R.drawable.rightfilled, R.drawable.wrongfilled };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		//setContentView(R.layout.census_statistics); // For grabbing screenshots of stats app
		setContentView(R.layout.structuring_the_web);
		setupLocationListening();

		header = (TextView) findViewById(R.id.stw_main_header);
		
		app_layout = (ScrollView) findViewById(R.id.stw_app_layout);
		explanation = (ScrollView) findViewById(R.id.stw_explanation_scrollview);
		showingExplanation = false;
		
		imageButtonRight = (ImageButton) findViewById(R.id.imageButtonRight);
		imageButtonWrong = (ImageButton) findViewById(R.id.imageButtonWrong);
		imageButtonRight.setOnClickListener(this); 
		imageButtonWrong.setOnClickListener(this);
		
		imageButtonInfo = (ImageButton) findViewById(R.id.imageButtonInfo);
		imageButtonInfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Twitch_STW", "Clicked info button");
				if(showingExplanation) { doneShowingExplanation(); }
				else {
					showingExplanation = true;
					app_layout.setVisibility(View.GONE);
					explanation.setVisibility(View.VISIBLE);
				}
			}
		});
		
		textSentence = (TextView) findViewById(R.id.stw_sentence);
		textExtraction = (TextView) findViewById(R.id.stw_extraction);
		
		PackageManager pm = getPackageManager(); //Enables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		
		extraction = getTabSeparatedExtraction();
		if(extraction != null) {
			sourceSentence = extraction[11];
			topic = extraction[12];
			header.setText(extraction[12]);
			setSentenceTextFormatted(extraction);
			setExtractionTextFormatted(extraction);
		} // If error in file, will display noncolored Stanford founded extraction

		setupExplanation();
	}
	
	private void setupExplanation() {
		int[] demoSentenceIds = { R.id.stw_demo1_sentence, R.id.stw_demo2_sentence, R.id.stw_demo3_sentence };
		int[] demoExtractionIds = { R.id.stw_demo1_extraction, R.id.stw_demo2_extraction, R.id.stw_demo3_extraction };
		for(int i = 0; i < demoSentences.length; i++) {
			TextView sentenceView = (TextView) findViewById(demoSentenceIds[i]);
			TextView extractionView = (TextView) findViewById(demoExtractionIds[i]);
			sentenceView.setText(Html.fromHtml(demoSentences[i]));
			extractionView.setText(Html.fromHtml(demoExtractions[i]));
		}
		
		doneButton = (Button) findViewById(R.id.STW_explanation_done);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) { 
				Log.d("Twitch_STW", "Clicked done button");
				doneShowingExplanation();
			}
		});
	}
	
	private void doneShowingExplanation() {
		showingExplanation = false;
		explanation.setVisibility(View.GONE);
		app_layout.setVisibility(View.VISIBLE);
	}
	
	private String[] getTabSeparatedExtraction() {
		try {
			AssetManager assetManager = getAssets();
			String fileName = "StructuringTheWeb.txt";
			final int numLines = getNumExtractionLines(assetManager, fileName);
			InputStream is = assetManager.open(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			// Read desired line from file
			int row = TwitchUtils.getSTWrow(this);
			Log.d("Twitch_STW", "Grabbing extraction from row: " + row);
			for(int i = 0; i < row; i++) {
				br.readLine(); // Skip over previous rows
			}
			String line = br.readLine();
			br.close();
			
			// Update prefs and return line split around tab
			// Skip 1-100 rows ahead
			row += 1 + ((int) 100 * Math.random());
			if(row >= numLines) row = row % numLines;
			Log.d("Twitch_STW", "Skipped ahead to row: " + row);
			TwitchUtils.setSTWrow(this, row);
			String[] splitExtraction = line.split("\\t");
			return splitExtraction;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int getNumExtractionLines(AssetManager manager, String fileName) {
		try {
			InputStream is = manager.open(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			int lines = 0;
			while (reader.readLine() != null) lines++;
			reader.close();
			return lines;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	private void setSentenceTextFormatted(String[] values) {
		String sentence = values[11];
		String[] words = sentence.split(" ");
		int startTopic = Integer.parseInt(values[4]);
		int endTopic = Integer.parseInt(values[5]);
		int startRelation = Integer.parseInt(values[6]);
		int endRelation = Integer.parseInt(values[7]);
		int startObject = Integer.parseInt(values[8]);
		int endObject = Integer.parseInt(values[9]);
				
		StringBuilder formatted = new StringBuilder("");
		// Add words before topic
		addToFormatted(formatted, words, 0, startTopic);
		// Add topic
		formatted.append("<font color='" + COLOR_TOPIC + "'>");
		addToFormatted(formatted, words, startTopic, endTopic);
		formatted.append("</font>");
		// Add words between topic and relation
		addToFormatted(formatted, words, endTopic, startRelation);
		// Add relation
		formatted.append("<font color='" + COLOR_RELATION + "'>");
		addToFormatted(formatted, words, startRelation, endRelation);
		formatted.append("</font>");
		// Add words between relation and object
		addToFormatted(formatted, words, endRelation, startObject);
		// Add object
		formatted.append("<font color='" + COLOR_OBJECT + "'>");
		addToFormatted(formatted, words, startObject, endObject);
		formatted.append("</font>");
		// Add words after object
		addToFormatted(formatted, words, endObject, words.length);
		
		// Set formatted sentence text
		Log.d("Twitch_STW", "Setting sentence to: " + formatted.toString());
		textSentence.setText(Html.fromHtml(formatted.toString()));
	}
	
	private void addToFormatted(StringBuilder formatted, String[] words, int startIndex, int endIndex) {
		String punctuations = ".,:;?";
		for(int i = startIndex; i < endIndex; i++) {
			if(i == words.length - 1 || (words[i+1].length() == 1 && punctuations.contains(words[i+1]))) {
				formatted.append(StringEscapeUtils.escapeHtml4(words[i])); // Don't space around punctuation
			} else {
				formatted.append(StringEscapeUtils.escapeHtml4(words[i] + " "));
			}
		}
	}

	private void setExtractionTextFormatted(String[] values) {
		String formatted = "";
		formatted += "<font color='" + COLOR_TOPIC + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[1] + " ");
		formatted += "</font><font color='" + COLOR_RELATION + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[2] + " ");
		formatted += "</font><font color='" + COLOR_OBJECT + "'>";
		formatted += StringEscapeUtils.escapeHtml4(values[3]) + "</font>";
		Log.d("Twitch_STW", "Setting extraction to: " + formatted);
		textExtraction.setText(Html.fromHtml(formatted));
	}
	
	private void setImageButtons(boolean rightFilled, boolean wrongFilled) {
		if(rightFilled) imageButtonRight.setBackgroundResource(FILLED_ICONS[0]);
		if(wrongFilled) imageButtonWrong.setBackgroundResource(FILLED_ICONS[1]);
	}
	
	@Override
	public void onClick(View view) {
		endTime = System.currentTimeMillis();
		int id = view.getId();
		setImageButtons(id == R.id.imageButtonRight, id == R.id.imageButtonWrong);
		String userSelection = null;

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.twitch_census_toast,
				(ViewGroup) findViewById(R.id.custom_toast_layout_id));

		// Possibility to set an image
		ImageView image = (ImageView) layout.findViewById(R.id.imageToast);

		switch(id){
		case R.id.imageButtonRight:
			Log.d("CLICK","imageButtonRight");
			userSelection = "Right"; //Can also have chosen it to be 1 or 0. 			
			image.setImageResource(R.drawable.rightunfilled);
			break;
		case R.id.imageButtonWrong:
			Log.d("CLICK","imageButtonWrong");
			userSelection = "Wrong";		
			image.setImageResource(R.drawable.wrongunfilled);
			break; 

		}

		try{	
			//Calculating the duration of time spent on the lock screen. 		
			long durationInMiliSecondsViaFocus = endTime - startTime; 
			long durationInMiliSecondsViaScreenOn = endTime - LockScreenBroadcast.startTime;
			Log.d("DurationForPeople", "Duration in milisecs via Focus= " + durationInMiliSecondsViaFocus); 
			Log.d("DurationForPeople", "Duration in milisecs via ScreenOn= " + durationInMiliSecondsViaScreenOn);
			long durationInMiliSeconds = 0;
			if(durationInMiliSecondsViaFocus < durationInMiliSecondsViaScreenOn)
			{
				durationInMiliSeconds = durationInMiliSecondsViaFocus; 
			}
			else if(durationInMiliSecondsViaScreenOn < durationInMiliSecondsViaFocus)
			{
				durationInMiliSeconds = durationInMiliSecondsViaScreenOn; 
			}
			Log.d("Twitch_STW", "Duration in milisecs= " + durationInMiliSeconds);
			long laterStartTime = startTime > LockScreenBroadcast.startTime ? startTime : LockScreenBroadcast.startTime;

			
			if(TwitchUtils.isOnline(this)) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("latitude", Float.toString(TwitchUtils.getCurrLatitude(this)));
				params.put("longitude", Float.toString(TwitchUtils.getCurrLongitude(this)));
				params.put("clientLoadTime", Long.toString(laterStartTime));
				params.put("clientSubmitTime", Long.toString(endTime));
				params.put("sourceSentence", sourceSentence);
				params.put("feedback", userSelection);
				params.put("topic", TwitchConstants.DEFAULT_TOPIC); // Later: real topic
				CensusResponse cr = new CensusResponse(params, CensusResponse.CensusAppType.STRUCTURE_THE_WEB, this);
				
				Log.d("TwitchServer", "executing CensusPostTask");
				CensusPostTask cpt = new CensusPostTask();
				CensusPostTask.Param p = cpt.new Param(StructuringTheWeb.this, cr);
				cpt.execute(p);
			}
			else {
				Log.d("TwitchServer", "not online - can't push to Twitch server");
				Log.d("Caching", "Caching response locally");
				TwitchDatabase entry = new TwitchDatabase(StructuringTheWeb.this);
				entry.open();
				entry.createStructuringTheWeb(sourceSentence, userSelection, topic, TwitchUtils.getCurrLatitude(this), TwitchUtils.getCurrLongitude(this), laterStartTime, endTime);
				entry.close();
			}
		}
		catch(Exception e){
			e.printStackTrace(); 
		}

		PackageManager pm = getPackageManager(); //Disables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

		// Set Toast
		TwitchDatabase aggregate = new TwitchDatabase(StructuringTheWeb.this);
		aggregate.open();
		int[] percentAndTotal = aggregate.getPercentageForSentenceFeedback(sourceSentence, userSelection);
				
		TextView text = (TextView) layout.findViewById(R.id.textToast);
		TextView textPercentOf = (TextView) layout.findViewById(R.id.textToastPercentOf);
		
		text.setText(percentAndTotal[0] + "% ");
		String reviewPlurality = percentAndTotal[1] != 1 ? "reviews" : "review";
		textPercentOf.setText(" of " + percentAndTotal[1] + " " + reviewPlurality);

		// Toast
		final Toast toast = new Toast(getApplicationContext());
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();

		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				toast.cancel(); 
			}
		}, 1250);

		aggregate.checkAggregates(this);
		finish(); 
	}

	@Override
	public void onBackPressed() {
		//Handles no action upon back button press
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus)
		{
			Log.d("WindowFocus", "Focus on"); 
			startTime = System.currentTimeMillis();	
		}	    
	}
	
	private void setupLocationListening() {
		TwitchUtils.setGeocodingStatus(this, false);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		towers = locationManager.getBestProvider(new Criteria(), false);
		
		Log.d("Internet", "Online? " + TwitchUtils.isOnline(this));
	}
	
	@Override
	public void onUserLeaveHint() {
		Log.d("TwitchHome", "Detected home button press");
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)
			return true;
		else
			return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onPause() {		
		super.onPause();
		if(towers!=null && locationManager!=null)
			locationManager.removeUpdates(this);
	}
	//Register for the updates when Activity is in foreground
	@Override
	protected void onResume() {		
		super.onResume();
		if(towers!=null && locationManager!=null)
			locationManager.requestLocationUpdates(towers, 20000, 1, this);
	}

	//Menu stuff begins.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home_screen_lock, menu); 
		return true; 
	}

	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {		
		Intent intentSettings = new Intent(this, LockScreenSettings.class);  

		switch(item.getItemId()){		
		case R.id.settings:			
			startActivity(intentSettings); //starts the activity 
			return true; 
		case R.id.exit:
			PackageManager pm = getPackageManager(); //Disables the activity to be home.
			ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
			pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
			
			TwitchUtils.registerExit(this);
			finish(); 
			return true;		
		default: 
			return false; 
		}

	}
	//Menu stuff ends.
	@Override
	public void onLocationChanged(Location l) {
		TwitchUtils.setCurrLocation(this, (float) l.getLatitude(), (float) l.getLongitude());
	}
	@Override
	public void onProviderDisabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onProviderEnabled(String provider) {
		// Auto-generated method stub
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Auto-generated method stub
	}
}
