package com.twitch.homescreenlock;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.VelocityTrackerCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// Twitch images comparison app
public class HomeScreenLockMain extends Activity implements OnTouchListener{

	private static final String DEBUG_TAG = "Velocity";
	private VelocityTracker mVelocityTracker = null;

	static final int CAPTURE_IMAGE = 1;
	private static Uri fileUri;

	private String mCurrentPhotoPath;
	public String topImageURL; public int randomRowTop; 
	public String bottomImageURL; public int randomRowBottom; 

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	String state = Environment.getExternalStorageState(); //To find out whether USB/External storage is accessible.
	static long startTime;
	long endTime; 
	String flagToast = "NotShown"; String flagToastDynamicImages = "NotDynamic"; 


	@Override
	public void onBackPressed() {
		//Handles no action upon back button press
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_home_screen_lock);
		ImageButton buttonToUnlock = (ImageButton) findViewById(R.id.button_Camera);

		PackageManager pm = getPackageManager(); //Enables the activity to be home.
		ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
		pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);

		TwitchDatabase imageURLsInDB = new TwitchDatabase(HomeScreenLockMain.this);
		imageURLsInDB.open();

		boolean databaseExists = doesDatabaseExist(this,"Twitch_db"); 
		Log.d("Database Existance", "Database exists= " + databaseExists); 

		buttonToUnlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				startCameraIntent(); 		

			}
		});

		ImageView myView1 = (ImageView) findViewById(R.id.imageView1);
		myView1.setOnTouchListener(this); 
		ImageView myView2 = (ImageView) findViewById(R.id.imageView2);
		myView2.setOnTouchListener(this);		

		boolean flagPromtingDownloadImagesWhenInternetMediaOnButLessImages = false;
		//Implementing the logic of image generator below (starting with checking Internet connectivity).
		File imagesFolderRepository = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TwitchAlbumRepository");
		imagesFolderRepository.mkdirs(); //Creates the TwitchAlbumRepository


		if(TwitchUtils.isOnline(this) && Environment.MEDIA_MOUNTED.equals(state)) //ON INTERNET AND IF SDCARD IS ACCESSIBLE.
		{
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;			
			Log.d("Internet", "ON"); 			
			List<String> listOfURLs = new ArrayList<String>();

			try {
				AssetManager assetManager;
				String lineOfURL = null;
				//List<String> listOfURLs = new ArrayList<String>();
				assetManager = getAssets();
				InputStream is = assetManager
						.open("ImageNetURLs.txt"); //Image-Net.org often or slow, so saving locally. 
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				int numRowsinImageURLTable = imageURLsInDB.getNumberOfRows("ImageURLs_tb");
				int numSelectedRowsinImageURLTable = imageURLsInDB.getNumberOfSelectedRows("ImageURLs_tb");	
				if(numSelectedRowsinImageURLTable == 0)
				{
					while ((lineOfURL = br.readLine()) != null) {					
						if(lineOfURL.toLowerCase(Locale.getDefault()).contains("flickr".toLowerCase()))
						{
							Log.d("IMAGENET", lineOfURL);
							listOfURLs.add(lineOfURL); //All URLs added in this list. 
							imageURLsInDB.createEntryURLS(lineOfURL, "NotSelected", 0, 0, "NotDeleted"); //All URLs added in the database. 
						}
					}
				}
				if(numSelectedRowsinImageURLTable == numRowsinImageURLTable)
				{					
					Log.d("For looping Table", "Order update"); 
					imageURLsInDB.updateURLNotSelectedFlag(); 
					int numSelectedRowsinImageURLTableOnLoop = imageURLsInDB.getNumberOfSelectedRows("ImageURLs_tb");	
					Log.d("Table loop", "New loop of table: Number of Selected rows= " + numSelectedRowsinImageURLTableOnLoop);					

				}

				br.close();
				Log.d("IMAGENET", "Size: " + listOfURLs.size());
			} catch (IOException e) {
				e.printStackTrace();
			}		

			/*try {
				String urlList = getURLlist();
				Log.d("IMAGENETURL",urlList );
			} catch (Exception e) {
				e.printStackTrace();
			} */

			String pathOfTwitchRepository = Environment.getExternalStorageDirectory().toString()+"/dcim/" + "TwitchAlbumRepository";
			Log.d("Files", "Path of the Twitch Rep: " + pathOfTwitchRepository);
			File f = new File(pathOfTwitchRepository);        
			File filesInTwitchRepository[] = f.listFiles(); //Adds all the files in the folder in an array. 
			Log.d("Files", "Number of files in Twitch Rep: "+ filesInTwitchRepository.length); 

			if(filesInTwitchRepository.length <= 2)
			{
				flagPromtingDownloadImagesWhenInternetMediaOnButLessImages = true; 				
			}

			if(flagPromtingDownloadImagesWhenInternetMediaOnButLessImages == false)
			{
				flagToastDynamicImages = "Dynamic";
				Random randomURLs = new Random(); 
				List<Integer> listToPreventDublicateRandomNumber = new ArrayList<Integer>();
				listToPreventDublicateRandomNumber = imageURLsInDB.getListOfSelectedRows(); //So that already selected rows are not selected again.
				Log.d("ListOfSelectedRows", "Size of selected rows= " + listToPreventDublicateRandomNumber.size() ); 
				final int[] imageViews = {R.id.imageView1, R.id.imageView2}; //For 2 Image views.

				for (int j = 0; j < 2; j++) //Number of URLs to be chosen randomly. 
				{
					while(true)
					{
						int numRowsinImageURLTable = imageURLsInDB.getNumberOfRows("ImageURLs_tb");
						int randomRow = randomURLs.nextInt((numRowsinImageURLTable+1) -1) +1 ;
						if (!listToPreventDublicateRandomNumber.contains(randomRow))
						{
							listToPreventDublicateRandomNumber.add(randomRow);
							Log.d("Rows", "Total number of rows= " + numRowsinImageURLTable + " Random row number= " + randomRow); 
							imageURLsInDB.updateURLSelectedFlag(randomRow); //Updating the flag to selected for the random rows chosen/selected. 
							String urlInThatRandomRow = imageURLsInDB.getImageURL(randomRow); 
							Log.d("URL", "Random URL= " + urlInThatRandomRow);
							new downloadImages().execute(urlInThatRandomRow, Integer.toString(randomRow));
							//int xx = imageURLsInDB.getNumberOfSelectedRows("ImageURLs_tb");
							//Log.d("number", "xxnumber= " + xx); 

							Bitmap myBitmap = BitmapFactory.decodeFile(filesInTwitchRepository[j].getAbsolutePath());
							ImageView iv = (ImageView)findViewById(imageViews[j]);                    
							iv.setImageBitmap(myBitmap); 
							break;
						}	
					}
				}
				String nameOfTopFile = filesInTwitchRepository[0].getName();				 
				randomRowTop = Integer.parseInt(nameOfTopFile.substring(0, nameOfTopFile.length()-4));//to chop off .jpg
				topImageURL = imageURLsInDB.getImageURL(randomRowTop);				

				String nameOfBottomFile = filesInTwitchRepository[1].getName(); 
				randomRowBottom = Integer.parseInt(nameOfBottomFile.substring(0, nameOfBottomFile.length()-4));
				bottomImageURL = imageURLsInDB.getImageURL(randomRowBottom);				

				Log.d("TopBottomImageTrack", "Top= " + randomRowTop + "Bottom= " + randomRowBottom + "TopURL= " + topImageURL + "BottomURL= " + bottomImageURL); 
				imageURLsInDB.createEntryImages("Top", topImageURL, bottomImageURL, Integer.toString(randomRowTop), Integer.toString(randomRowBottom), 0 ); //Top is by default. 
			}

		}
		else //NO INTERNET or SD CARD REQUIRED
		{
			Log.d("Internet", "OFF"); 
			final int[] imageViews = {R.id.imageView1, R.id.imageView2};
			final int[] images = {R.drawable.nature1, R.drawable.nature2, R.drawable.nature3, R.drawable.nature4}; //Can add more images by default, when no Internet connection. 
			Random rng = new Random(); 
			List<Integer> generated = new ArrayList<Integer>();
			for (int j = 0; j < 2; j++)
			{
				while(true)
				{
					Integer next = rng.nextInt(images.length) ;
					if (!generated.contains(next)) //So that same image is not repeated. 
					{
						generated.add(next);                    
						ImageView iv = (ImageView)findViewById(imageViews[j]);
						iv.setImageResource(images[next]);
						break;
					}
				}
			}
		}

		if(flagPromtingDownloadImagesWhenInternetMediaOnButLessImages == true)
		{
			Log.d("Internet", "Not off, yet backup to download images when they are less than 2");
			//Downloading two images when Internet is on or/and SD is working but no images. 
			Random randomURLs = new Random();
			List<Integer> listToPreventDublicateRandomNumber = new ArrayList<Integer>();
			listToPreventDublicateRandomNumber = imageURLsInDB.getListOfSelectedRows(); //So that already selected rows are not selected again.
			Log.d("ListOfSelectedRowsBackup", "Size of selected rows= " + listToPreventDublicateRandomNumber.size() ); 
			for (int j = 0; j < 2; j++) //Number of URLs to be chosen randomly. 
			{	
				while(true)
				{
					int numRowsinImageURLTable = imageURLsInDB.getNumberOfRows("ImageURLs_tb");
					int randomRow = randomURLs.nextInt((numRowsinImageURLTable+1) -1) +1 ;
					if (!listToPreventDublicateRandomNumber.contains(randomRow))
					{
						listToPreventDublicateRandomNumber.add(randomRow);
						Log.d("Rows Backup", "Total number of rows= " + numRowsinImageURLTable + " Random row number= " + randomRow); 
						String urlInThatRandomRow = imageURLsInDB.getImageURL(randomRow);
						imageURLsInDB.updateURLSelectedFlag(randomRow); //Updating the flag to selected for the random rows chosen/selected. 
						new downloadImages().execute(urlInThatRandomRow, Integer.toString(randomRow));
						break; 
					}
				}

			}

			//In the meantime, showing the images in drawable as NO INTERNET or SD CARD REQUIRED. 
			final int[] imageViews = {R.id.imageView1, R.id.imageView2};
			final int[] images = {R.drawable.nature1, R.drawable.nature2, R.drawable.nature3, R.drawable.nature4}; //Can add more images by default, when no Internet connection. 
			Random rng = new Random(); 
			List<Integer> generated = new ArrayList<Integer>();
			for (int j = 0; j < 2; j++)
			{
				while(true)
				{
					Integer next = rng.nextInt(images.length) ;
					if (!generated.contains(next)) //So that same image is not repeated. 
					{
						generated.add(next);                    
						ImageView iv = (ImageView)findViewById(imageViews[j]);
						iv.setImageResource(images[next]);
						break;
					}
				}
			}
		}

		imageURLsInDB.close();
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

	private void refreshFolder(File f) { //After picture is downloaded, it refreshes the folder.  
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File ff = new File(f.getAbsolutePath());
		Uri contentUri = Uri.fromFile(ff);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
		sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
	}

	//This causes the downloading of images in background, while main activity works normally.<input, progress, result>
	public class downloadImages extends AsyncTask<String, Void, Void>
	{    	
		@Override
		protected Void doInBackground(String... sUrl) {
			try {   	                        

				File dir = new File (Environment.getExternalStorageDirectory() + "/dcim/" + "TwitchAlbumRepository");
				if(dir.exists()==false) {
					dir.mkdirs(); //Create directory if it does not exists. 
				}

				URL url = new URL(sUrl[0]); //URL of the image(s). 
				File file = new File(dir, String.valueOf(sUrl[1]) + ".jpg"); //Naming the file. 

				long startTime = System.currentTimeMillis();
				Log.d("DownloadManager", "download begining");
				Log.d("DownloadManager", "download url:" + url);
				Log.d("DownloadManager", "downloaded file name:" + sUrl[1] + ".jpg");

				//Open a connection to that URL. 
				URLConnection ucon = url.openConnection();

				//Define InputStreams to read from the URLConnection.    	            
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);

				//Read bytes to the Buffer until there is nothing more to read(-1).    	            
				ByteArrayBuffer baf = new ByteArrayBuffer(5000);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}

				//Convert the Bytes read to a String. 
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(baf.toByteArray());
				fos.flush();
				fos.close();
				Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

			} catch (IOException e) {
				Log.d("DownloadManager", "Error: " + e);
			}
			return null; 
		}
	}    


	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {	   
		case CAPTURE_IMAGE:
			if (resultCode == Activity.RESULT_OK) {
				Log.d("Picture taken successfully","OK"); 
				PackageManager pm = getPackageManager(); //Disables the activity to be home.
				ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
				pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
				finish();
			}
		}
	}

	private void startCameraIntent() {	   

		//camera stuff
		Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		//folder stuff
		File imagesFolder = new File(Environment.getExternalStorageDirectory() + "/dcim/" + "TwitchAlbum");
		imagesFolder.mkdirs();

		File image = new File(imagesFolder, "QR_" + timeStamp + ".jpg");
		Uri uriSavedImage = Uri.fromFile(image);
		mCurrentPhotoPath = image.getAbsolutePath();
		imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
		galleryAddPic();
		startActivityForResult(imageIntent, CAPTURE_IMAGE);

	}

	private void galleryAddPic() { //After the picture is taken, this function helps in getting that picture to the gallery. 
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {		

		switch(view.getId()){
		case R.id.imageView1:
			Log.d("View1 touched","ImageView1");
			return onTouchGesture(event, "Top" ); //true

		case R.id.imageView2:
			Log.d("View2 touched","ImageView2");
			return onTouchGesture(event, "Bottom"); //true

		}
		return true; //true
	}

	public boolean onTouchGesture(MotionEvent event, String whichImageView) {//User defined function 
		int index = event.getActionIndex();
		int action = event.getActionMasked();
		int pointerId = event.getPointerId(index);
		float initialPositionX = 0; //Useful to calculate distance
		float initialPositionY = 0;
		float finalPositionX = 0;
		float finalPositionY = 0;
		boolean success = true; 
		String toastText = null;
		endTime = System.currentTimeMillis();

		// Get your custom_toast.xml layout
		LayoutInflater inflater = getLayoutInflater();

		View layout = inflater.inflate(R.layout.twitch_census_toast,
				(ViewGroup) findViewById(R.id.custom_toast_layout_id));


		switch(action) {
		case MotionEvent.ACTION_DOWN:
			if(mVelocityTracker == null) {
				// Retrieve a new VelocityTracker object to watch the velocity of a motion.
				mVelocityTracker = VelocityTracker.obtain();
				initialPositionX = event.getX(); 
				initialPositionY = event.getY();
			}
			else {
				// Reset the velocity tracker back to its initial state.
				mVelocityTracker.clear();
			}
			// Add a user's movement to the tracker.
			mVelocityTracker.addMovement(event);
			break;
		case MotionEvent.ACTION_UP:
			finalPositionX = event.getX(); 
			finalPositionY = event.getY();
			break; 
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.addMovement(event);
			// When you want to determine the velocity, call 
			// computeCurrentVelocity(). Then call getXVelocity() 
			// and getYVelocity() to retrieve the velocity for each pointer ID. 
			mVelocityTracker.computeCurrentVelocity(1000);
			// Log velocity of pixels per second
			// Best practice to use VelocityTrackerCompat where possible.
			Log.d("", "X velocity: " +  VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId));
			Log.d("", "Y velocity: " +  VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId));
			if ((VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointerId) > 100.0))
			{
				try{

					String pathOfTwitchRepository = Environment.getExternalStorageDirectory().toString()+"/dcim/" + "TwitchAlbumRepository";	
					File f = new File(pathOfTwitchRepository);        
					File filesInTwitchRepository[] = f.listFiles(); //Adds all the files in the folder in an array. 

					//Updating the database (including updating the duration)
					TwitchDatabase entry = new TwitchDatabase(HomeScreenLockMain.this);
					entry.open();
					//Calculating the duration of time spent on the lock screen.		
					long durationInMiliSecondsViaFocus = endTime - startTime; 
					long durationInMiliSecondsViaScreenOn = endTime - LockScreenBroadcast.startTime;
					Log.d("DurationForImages", "Duration in milisecs via Focus= " + durationInMiliSecondsViaFocus); 
					Log.d("DurationForImages", "Duration in milisecs via ScreenOn= " + durationInMiliSecondsViaScreenOn);
					long durationInMiliSeconds = 0; 
					if(durationInMiliSecondsViaFocus < durationInMiliSecondsViaScreenOn)
					{
						durationInMiliSeconds = durationInMiliSecondsViaFocus; 
					}
					else if(durationInMiliSecondsViaScreenOn < durationInMiliSecondsViaFocus)
					{
						durationInMiliSeconds = durationInMiliSecondsViaScreenOn; 
					}
					Log.d("DurationForImages", "Duration in milisecs= " + durationInMiliSeconds); 
					
					entry.updateTopBottom(whichImageView, Integer.toString(randomRowTop), Integer.toString(randomRowBottom), durationInMiliSeconds); 

					// To set an image on Toast
					ImageView image = (ImageView) layout.findViewById(R.id.imageToast);
					if(whichImageView == "Top")
					{
						Bitmap myBitmap = BitmapFactory.decodeFile(pathOfTwitchRepository + "/" + randomRowTop + ".jpg");						                   
						image.setImageBitmap(myBitmap);
					}
					else if(whichImageView == "Bottom")
					{
						Bitmap myBitmap = BitmapFactory.decodeFile(pathOfTwitchRepository + "/" + randomRowBottom + ".jpg");						                   
						image.setImageBitmap(myBitmap);
					}
					//Deleting the images shown on the screen.					
					Log.d("Images to be deleted", "Top delete= " + randomRowTop + ".jpg" + " Bottom delete = " + randomRowBottom + ".jpg");

					File fileToBeDeletedTop = new File(pathOfTwitchRepository + "/" + randomRowTop + ".jpg");
					fileToBeDeletedTop.delete();
					File fileToBeDeletedBottom = new File(pathOfTwitchRepository + "/" + randomRowBottom + ".jpg");
					fileToBeDeletedBottom.delete();					

					//Deleting left over images upon looping. 
					int numRowsinImageURLTable = entry.getNumberOfRows("ImageURLs_tb");
					int numSelectedRowsinImageURLTable = entry.getNumberOfSelectedRows("ImageURLs_tb");	
					if(numSelectedRowsinImageURLTable == numRowsinImageURLTable)
					{
						Log.d("For looping Table", "Order delete");
						for(int i=0; i<filesInTwitchRepository.length; i++)
						{
							File fileToBeDeleted = new File(filesInTwitchRepository[i].getAbsolutePath());
							fileToBeDeleted.delete();
						}
						Log.d("Files in rep", "Number of files after new table loop= " + filesInTwitchRepository.length); 

					}
					entry.close();

					TwitchDatabase aggregate = new TwitchDatabase(HomeScreenLockMain.this);
					aggregate.open();
					Log.d("PhotoRanking", "Checking for aggregates");
					aggregate.checkAggregates(this);
					//Updating the delete flag in the Table(may be required in future-Not yet implemented). 

				}
				catch(Exception e){
					success = false; 
					e.printStackTrace(); 
				}
				finally{
					if(success){
						Log.d("DATABASE ENTRY IMAGES", "SUCCESS"); 
					}				
				}
				Log.d("Swipe right", whichImageView);
				PackageManager pm = getPackageManager(); //Disables the activity to be home.
				ComponentName compName = new ComponentName(getApplicationContext(), HomeScreenLockActivity.class);
				pm.setComponentEnabledSetting(compName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);

				// Set a message
				Random rngToast = new Random();		
				Integer nextToastPercentage = rngToast.nextInt(80-50) + 50;
				Integer nextToastPercentageOf = rngToast.nextInt(500-100) + 100;
				toastText = String.valueOf(nextToastPercentage);
				TextView text = (TextView) layout.findViewById(R.id.textToast);		
				text.setText(toastText + "% ");
				TextView textPercentOf = (TextView) layout.findViewById(R.id.textToastPercentOf);
				textPercentOf.setText(" of " + nextToastPercentageOf +" people ");

				// Toast
				Log.d("flagToast", "flagToast= " + flagToast);
				if(TwitchUtils.isOnline(this) && Environment.MEDIA_MOUNTED.equals(state) && flagToast == "NotShown" && flagToastDynamicImages == "Dynamic") //ON INTERNET AND IF SDCARD IS ACCESSIBLE.
				{
					// We can read and write the media
					mExternalStorageAvailable = mExternalStorageWriteable = true;
					final Toast toast = new Toast(getApplicationContext());
					toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
					toast.setDuration(Toast.LENGTH_SHORT);
					toast.setView(layout);
					toast.show();
					flagToast = "Shown";

					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							toast.cancel(); 
						}
					}, 1250);
				}
				
				finish(); 
			}
			break;	            
		case MotionEvent.ACTION_CANCEL:
			// Return a VelocityTracker object back to be re-used by others.
			mVelocityTracker.recycle();
			break;
		}
		return true;
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

	private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
		File dbFile=context.getDatabasePath(dbName);
		return dbFile.exists();
	}
}



