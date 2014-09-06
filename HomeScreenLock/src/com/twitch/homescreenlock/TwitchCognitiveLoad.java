package com.twitch.homescreenlock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class TwitchCognitiveLoad extends Activity implements OnClickListener, OnSeekBarChangeListener {

	// General app
	final double TWITCH_CHANCE = 1.0 / 7.0;
	long startTime, endTime;
	String currTask, prevTask;
	int iterations;
	TextView header, roundCounter;
	
	// n-back letter cognitive loading
	final double SAME_LETTER_CHANCE = 1.0 / 7.0;
	final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	int N_BACK; // How many back should we be comparing?
	Queue<Integer> prevLetterIndices;
	int letterIndex;
	TextView bigLetter;
	Button nextLetterButton, yesButton, noButton;
	LinearLayout letterLayout, nextButtonLayout, yesNoButtonsLayout;
	
	// Round-robin handling
	final int ROUNDS_TO_DO = 6;
	final int TASKS_AFTER_ROUND = 5;
	ArrayList<String> twitchTaskRound;
	int currTwitchTaskIndex;
	int currTasksAfterRound;
	boolean twitchRoundDone;
	int roundsCompleted;
	int iterationsThisRound;
	boolean prevWasTwitch;
	
	// Twitch handling
	SeekBar seekEnergy;
	ImageView lockImage;
	LinearLayout slideLayout;
	LinearLayout picRow1;
	LinearLayout picRow2;
	LinearLayout picRow3;
	ImageButton[] imgButtons; // 6 of them, indexed row-major
	
	// Photo ranking
	ImageView pic1, pic2;
	LinearLayout layoutPhotoRanking;
	
	// IDs of Twitch Census image resources
	int[] peopleImgIDs = { R.drawable.oneperson, R.drawable.morethanone,
						   R.drawable.group, R.drawable.crowd };
	int[] activityImgIDs = { R.drawable.work, R.drawable.home, R.drawable.eating,
							 R.drawable.transit, R.drawable.social, R.drawable.exercise };
	int[] dressImgIDs = { R.drawable.casual, R.drawable.semiformal,
						  R.drawable.formal, R.drawable.veryformal };
	int[] energyImgIDs = { R.drawable.energylevel1, R.drawable.energylevel2,
						   R.drawable.energylevel3, R.drawable.energylevel4 };
	final Integer[] photoIDs = { R.drawable.nature1, R.drawable.nature2, R.drawable.nature3,
					   R.drawable.nature4, R.drawable.nature5, R.drawable.nature6,
					   R.drawable.nature7, R.drawable.nature8, R.drawable.nature9,
					   R.drawable.nature10, R.drawable.nature11, R.drawable.nature12 };
	Queue<Integer> photoIDqueue;
	
	@Override
	public void onUserLeaveHint() {
		Log.d("TwitchHome", "Detected home button press");
		cogStudyDone();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.twitch_cognitive_load);		

		// Set activity to start on home screen turn on
		PackageManager pm = getPackageManager();
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
		
		// Disable existing lock screen
		TwitchUtils.disableUserLockScreen(this);
		
		// Is first unlock 2back or 3back? Alternate after this
		if(Math.random() < 0.5) N_BACK = 2;
		else N_BACK = 3;
		Log.d("CogStudy", "Starting n: " + N_BACK);
		
		// General/n-back initialization
		iterations = 0;
		currTask = "intro" + N_BACK + "back";
		prevTask = "none";
		letterLayout = (LinearLayout) findViewById(R.id.bigLetterRow);
		bigLetter = (TextView) findViewById(R.id.bigLetter);
		prevLetterIndices = new LinkedList<Integer>();
		header = (TextView) findViewById(R.id.headerText);
		roundCounter = (TextView) findViewById(R.id.study_header);
		
		// Twitch task initialization
		iterationsThisRound = 1;
		currTwitchTaskIndex = 0;
		currTasksAfterRound = 0;
		twitchRoundDone = false;
		roundsCompleted = 0;
		prevWasTwitch = false;
		twitchTaskRound = new ArrayList<String>();
		resetTwitchTasks();
		
		// n-back button initialization
		nextButtonLayout = (LinearLayout) findViewById(R.id.layoutNextButton);
		yesNoButtonsLayout = (LinearLayout) findViewById(R.id.layoutYesNoButtons);
		nextLetterButton = (Button) findViewById(R.id.nextButton);
		yesButton = (Button) findViewById(R.id.yesButton);
		noButton = (Button) findViewById(R.id.noButton);
		nextLetterButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				endTime = System.currentTimeMillis();
				logEntry();
				changeDisplay();
			}
		});
		yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleNBackAnswer(true);
			}
		});
		noButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				handleNBackAnswer(false);
			}
		});
		
		// Initialize Slide-To-Unlock
		seekEnergy = (SeekBar) findViewById(R.id.seekBarUnlock);
		lockImage = (ImageView)findViewById(R.id.imageViewLocks); 
		seekEnergy.setMax(1000);
		seekEnergy.setProgress(50);
		seekEnergy.setOnSeekBarChangeListener(this);
		lockImage.setImageResource(R.drawable.closedlock);
		
		// Set on click listener for census app
		imgButtons = new ImageButton[6];
		int[] buttonIds = { R.id.imageButton1, R.id.imageButton2,
							R.id.imageButton3, R.id.imageButton4,
							R.id.imageButton5, R.id.imageButton6 };
		for(int i = 0; i < imgButtons.length; i++) {
			imgButtons[i] = (ImageButton) findViewById(buttonIds[i]);
			imgButtons[i].setOnClickListener(this);
		}
		
		// Other Twitch setup
		slideLayout = (LinearLayout) findViewById(R.id.layoutSlideToUnlock);
		picRow1 = (LinearLayout) findViewById(R.id.layoutRow1);
		picRow2 = (LinearLayout) findViewById(R.id.layoutRow2);
		picRow3 = (LinearLayout) findViewById(R.id.layoutRow3);
		pic1 = (ImageView) findViewById(R.id.imageView1);
		pic2 = (ImageView) findViewById(R.id.imageView2);
		pic1.setOnClickListener(this);
		pic2.setOnClickListener(this);
		layoutPhotoRanking = (LinearLayout) findViewById(R.id.layoutPhotoRanking);
		
		// Initialize image pairings
		photoIDqueue = new LinkedList<Integer>();
		populatePhotoIDqueue();
		
		// Kick off: display intro
		displayIntro();
	}
	
	private void cogStudyDone() {
		// Set app preference to previous choice
		String pref = TwitchUtils.getPrevAppPref(this);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString("keyOfLocksForPrefs", pref);
		editor.apply();
		
		Log.d("CogStudy", "CogStudy done, starting post task");
		TwitchUtils.setUploadDBStatus(this, true);
		new CogStudyPostTask().execute(this);
		finish();
	}
	
	private void populatePhotoIDqueue() {
		photoIDqueue.clear();
		List<Integer> ids = Arrays.asList(photoIDs);
		ArrayList<Integer> modifiableIDs = new ArrayList<Integer>(ids);
		Collections.shuffle(modifiableIDs);
		for(Integer id : modifiableIDs) {
			photoIDqueue.add(id);
		}
	}
	
	public void changeDisplay() {
		iterations++;
		prevTask = currTask;
		
		boolean extraLetters = twitchRoundDone && currTasksAfterRound < TASKS_AFTER_ROUND;
		if(twitchRoundDone && !extraLetters) {
			// Done with extra letters
			twitchRoundDone = false;
			iterationsThisRound = 0;
			currTasksAfterRound = 0;
		}
		
		Log.d("CogStudy", "rounds completed: " + roundsCompleted + "; iterations: " + iterations +
				"; this round: " + iterationsThisRound + "; last task: " + prevTask +
				"; extra letters? " + extraLetters);
		if(roundsCompleted == ROUNDS_TO_DO && !extraLetters) {
			cogStudyDone();
			return;
		}
		
		startTime = System.currentTimeMillis();
		if(iterationsThisRound == 0) {
			// Alternate the working memory load factor (N)
			// Note was originally set 50-50 chance in onCreate, so still 50-50
			N_BACK = (N_BACK == 2) ? 3 : 2;
			prevLetterIndices.clear();
			Log.d("CogStudy", "Set N_BACK to " + N_BACK);
			prevWasTwitch = false;
			iterationsThisRound++;
			displayIntro();
		}
		else if(extraLetters || iterationsThisRound <= N_BACK + 1 || prevWasTwitch || Math.random() > TWITCH_CHANCE) {
			if(extraLetters) currTasksAfterRound++;
			prevWasTwitch = false;
			// iterationsThisRound incremented in displayLetter()
			displayLetter();
		}
		else {
			prevWasTwitch = true; // Don't allow back-to-back Twitch
			iterationsThisRound++;
			displayTwitch();
		}
	}
	
	// Turn visibility to 'gone' for letter-related layouts
	private void hideLetterStuff() {
		letterLayout.setVisibility(View.GONE);
		nextButtonLayout.setVisibility(View.GONE);
		yesNoButtonsLayout.setVisibility(View.GONE);
	}
	
	// Turn visibility to 'gone' for Twitch-related layouts
	private void hideTwitchStuff() {
		picRow1.setVisibility(View.GONE);
		picRow2.setVisibility(View.GONE);
		picRow3.setVisibility(View.GONE);
		slideLayout.setVisibility(View.GONE);
		layoutPhotoRanking.setVisibility(View.GONE);
	}
	
	public void displayIntro() {
		hideLetterStuff();
		hideTwitchStuff();
		roundCounter.setText("Twitch Study (round " + (roundsCompleted+1) + "/" + ROUNDS_TO_DO + ")");
		currTask = "intro" + N_BACK + "back";
		nextButtonLayout.setVisibility(View.VISIBLE);
		header.setText(Html.fromHtml("New task: remember the letter that was " +
				"<b>" + N_BACK + " letters ago</b>"));
	}
	
	public void displayLetter() {
		hideTwitchStuff();
		letterLayout.setVisibility(View.VISIBLE);
		currTask = N_BACK + "back";
		if(iterationsThisRound > N_BACK) { // Enough to start asking yes/no
			header.setText("Compare to " + N_BACK + " letters ago");
			nextButtonLayout.setVisibility(View.GONE);
			yesNoButtonsLayout.setVisibility(View.VISIBLE);
		}
		else {
			header.setText("Remember this letter.");
			yesNoButtonsLayout.setVisibility(View.GONE);
			nextButtonLayout.setVisibility(View.VISIBLE);
		}
		letterIndex = getNextLetterIndex();
		prevLetterIndices.add(letterIndex);
		bigLetter.setText("" + letters.charAt(letterIndex));
		iterationsThisRound++;
	}
	
	public void handleNBackAnswer(boolean thinksMatch) {
		endTime = System.currentTimeMillis();
		Integer nBackIndex = prevLetterIndices.poll();
		boolean matchesNback = nBackIndex == letterIndex;
		Log.d("CogStudy", "Guessed same (" + letters.charAt(letterIndex) + ")? " +
				thinksMatch + "; actually? " + letters.charAt(nBackIndex) + ", " + matchesNback);
		logEntry(thinksMatch, matchesNback);
		changeDisplay();
	}
	
	public void displayTwitch() {
		hideLetterStuff();
		hideTwitchStuff();
		currTask = getNextTwitchTask();
		// "SlideToUnlock", "Census/People", "Census/Activity", "Census/Dress", "Census/Energy", "PhotoRanking"
		if(currTask.equals("SlideToUnlock")) {
			header.setText("Slide To Unlock");
			seekEnergy.setProgress(50); // Reset progress bar to start
			slideLayout.setVisibility(View.VISIBLE);
		}
		else if(currTask.equals("Census/People")) {
			header.setText("People around?");
			int pixels = TwitchUtils.getPixelsFromDp(this, 160);
			setupImgButtons(pixels, pixels, peopleImgIDs, false);
		}
		else if(currTask.equals("Census/Activity")) {
			header.setText("Activity?");
			int pixels = TwitchUtils.getPixelsFromDp(this, 130);
			setupImgButtons(pixels, pixels, activityImgIDs, true);
		}
		else if(currTask.equals("Census/Dress")) {
			header.setText("Attire nearby?");
			int pixels = TwitchUtils.getPixelsFromDp(this, 160);
			setupImgButtons(pixels, pixels, dressImgIDs, false);
		}
		else if(currTask.equals("Census/Energy")) {
			header.setText("Energy level?");
			int pixels = TwitchUtils.getPixelsFromDp(this, 160);
			setupImgButtons(pixels, pixels, energyImgIDs, false);
		}
		else if(currTask.equals("PhotoRanking")) {
			header.setText("Tap your favorite photo");
			// Dequeue next two IDs from queue as our pairing
			if(photoIDqueue.isEmpty()) {
				populatePhotoIDqueue();
			}
			pic1.setImageResource(photoIDqueue.poll());
			pic2.setImageResource(photoIDqueue.poll());
			layoutPhotoRanking.setVisibility(View.VISIBLE);
		}
		else {
			Log.d("CogStudy", "Task not found: " + currTask);
		}
	}
	
	private void setupImgButtons(int width, int height, int[] res, boolean showRow3) {
		picRow1.setVisibility(View.VISIBLE);
		picRow2.setVisibility(View.VISIBLE);
		// Set padding between elements
		picRow1.setPadding(5, 0, 5, 0);
		picRow2.setPadding(5, 5, 5, 0);
		
		if(showRow3) { // Census/Activity
			picRow3.setVisibility(View.VISIBLE);
			picRow3.setPadding(5, 5, 5, 0);
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
		for(int i = 0; i < res.length; i++) {
			ImageButton btn = imgButtons[i];
			btn.setLayoutParams(params);
			btn.setImageResource(res[i]);
		}
	}
	
	// End a Census or Photo-ranking task
	@Override
	public void onClick(View view) {
		endTime = System.currentTimeMillis();
		logEntry();
		changeDisplay();
	}
	
	// Returns String of next Twitch task
	private String getNextTwitchTask() {
		if(currTwitchTaskIndex == twitchTaskRound.size()) {
			// End of a round
			resetTwitchTasks();
			currTwitchTaskIndex = 0;
		}
		else if(currTwitchTaskIndex == twitchTaskRound.size() - 1) {
			// Last task of this round
			roundsCompleted++;
			twitchRoundDone = true;
		}
		String task = twitchTaskRound.get(currTwitchTaskIndex);
		Log.d("CogStudy", "Twitch task " + currTwitchTaskIndex + ": " + task);
		currTwitchTaskIndex++;
		return task;
	}
	
	// Clear this round of tasks and replace with a new randomized round-robin
	private void resetTwitchTasks() {
		ArrayList<String> tasks = new ArrayList<String>();
		for(int i = 0; i < 3; i++) tasks.add("SlideToUnlock");
		tasks.add("Census/People");
		tasks.add("Census/Activity");
		tasks.add("Census/Dress");
		tasks.add("Census/Energy");
		tasks.add("PhotoRanking");
		twitchTaskRound.clear();
		while(!tasks.isEmpty()) {
			int randIndex = (int) (Math.random() * tasks.size());
			twitchTaskRound.add(tasks.remove(randIndex));
		}
		
		String nextRoundRobin = "";
		for(String task : twitchTaskRound) {
			nextRoundRobin += task + " ";
		}
		Log.d("CogStudy", "Reset Twitch tasks. Next round robin: " + nextRoundRobin);
		
	}
	
	// Log entry for n-back, with correctness
	private void logEntry(Boolean userGuess, Boolean correctAnswer) {
		int duration = shortestDuration();
		String phoneID = TwitchUtils.getDeviceID(this);
		String condition = N_BACK + "back";
		Log.d("CogStudy", "Entry: currTask[" + currTask + "], prevTask[" +
				prevTask + "], phoneID[" + phoneID + "], number[" +
				iterations + "], duration[" + duration  + ", userGuess[" +
				userGuess + "], correctAnswer[" + correctAnswer + "]");
		TwitchDatabase db = new TwitchDatabase(TwitchCognitiveLoad.this);
		db.open();
		db.createCogStudyEntry(phoneID, iterations, condition, currTask,
				prevTask, duration, userGuess, correctAnswer);
		db.close();
	}
	
	// Log entry for Twitch: nulls for booleans
	private void logEntry() {
		logEntry(null, null);
	}
	
	private int shortestDuration() {
		long durationViaFocus = endTime - startTime; 
		long durationViaScreenOn = endTime - LockScreenBroadcast.startTime;
		// Note: LockScreenBroadcast.startTime only helpful for first unlock
		if(durationViaFocus <= durationViaScreenOn) return (int) durationViaFocus;
		return (int) durationViaScreenOn;
	}

	private int getNextLetterIndex() {
		int randIndex = (int) (Math.random() * letters.length());
		if(iterationsThisRound <= N_BACK + 1) return randIndex;
		int nBack = prevLetterIndices.peek();
		if(Math.random() < SAME_LETTER_CHANCE) {
			return nBack;
		} else {
			// Make sure we don't return the n-back letter if SAME_LETTER_CHANCE fails.
			while(randIndex == nBack) randIndex = (int) (Math.random() * letters.length());
			return randIndex;
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
		final int[] images = {R.drawable.closedlock, R.drawable.openlock};
		if(progress >= 0 && progress < 500) {
			lockImage.setImageResource(images[0]);
		} else if(progress > 500 && progress <= 1000) {
			lockImage.setImageResource(images[1]);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {}

	// End a slide-to-unlock task
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// Could check last known progress to prevent early unlocks.
		endTime = System.currentTimeMillis();
		logEntry();
		changeDisplay();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    if(hasFocus) {
	    	Log.d("WindowFocus", "Focus on"); 
	    	startTime = System.currentTimeMillis();	
	    }	    
	}

	@Override
	public void onBackPressed() {
		// Do nothing when the back button is pressed.
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)
			return true;
		else
			return super.onKeyDown(keyCode, event);
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
			cogStudyDone();
			return true;		
		default: 
			return false; 
		}

	}
	//Menu stuff ends.
}
