package com.oracle.play.pptviewer;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
// import android.widget.Toast;

public class ViewSlidesActivity extends Activity {
	private static String logtag = "ViewSlidesActivity";//for use as the tag when logging

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	ImageView image;

	static private int currentSlideNum;
	static private int lastSlideNum;
	static private Date lastAccessDate;
	static private float totalAccessTime = 0;
	String[] slides;

	private int docId = 0;
	private String slidesName;

	// Stats
	Date now = Calendar.getInstance().getTime();
	Date[] accessTimes = null;
	Float[] accessTimeFloats = null;

	/* SLIDE UP MENU */
	private LinearLayout slide_up_menu_layout;
	private Animation animUp;
	private Animation animDown;
	final ViewSlidesActivity context = this;
	private String[] thumbNailUrlArray;

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_slides);

		currentSlideNum = 0;
		lastSlideNum = 0;

		// TODO: show stats on home page
		lastAccessDate = Calendar.getInstance().getTime();     

		//Get URL from called intent
		Intent myIntent = getIntent(); // gets the previously created intent
		slides = myIntent.getStringArrayExtra("imagearray");
		docId = myIntent.getIntExtra("clicked",0);
		PptBean temp = (PptBean)MainActivity.pptBeanMap.get(docId);
		if(temp!=null){
			slidesName = temp.getPptName();
			totalAccessTime = temp.getTotalViewTime();
		}
		/* SLIDE UP MENU */
		thumbNailUrlArray = myIntent.getStringArrayExtra("thumbnailurlarray");
		addSlideUpMenu();

		if (slides != null) {
			image = (ImageView) findViewById(R.id.image);
			image.setImageBitmap(CacheManager.getImage(slides[currentSlideNum]));

			accessTimes = new Date[slides.length];
			for (int i=0; i<slides.length; i++)
				accessTimes[i] = now;

			accessTimeFloats = new Float[slides.length];
			for (int j=0; j<slides.length; j++)
				accessTimeFloats[j] = (float)0.0;
		}
		addHomeButtonListener();
		addGestureListener();
	}

	private void addSlideUpMenu(){
		slide_up_menu_layout = (LinearLayout) findViewById(R.id.slider);
		slide_up_menu_layout.setVisibility(View.GONE);
		animUp = AnimationUtils.loadAnimation(this, R.anim.anim_up);
		animDown = AnimationUtils.loadAnimation(this, R.anim.anim_down);


		//Adding OnClickListener to images / thumbnails 
		final OnClickListener thumbnailClickListner = new OnClickListener() {
			public void onClick(View v) {
				//Toast.makeText(context, "Thumbnail clicked:" + v.getTag(), Toast.LENGTH_SHORT).show();
				Integer slideNumber = (Integer)v.getTag();
				int slideNumInt = Integer.valueOf(slideNumber);
				image = (ImageView) findViewById(R.id.image);
				image.setImageBitmap(CacheManager.getImage(slides[slideNumInt]));
			}
		};

		LinearLayout slide_gallery_list = (LinearLayout)findViewById(R.id.slide_gallery);
		//Populating thumbnails from Cache        
		int numberOfPpts = 0;
		if( thumbNailUrlArray != null )
			numberOfPpts = thumbNailUrlArray.length;
		if( numberOfPpts > 0 ){
			//Toast.makeText(ViewSlidesActivity.this, "Thumbnails Displayed", Toast.LENGTH_SHORT).show();
			for( int i = 0; i < thumbNailUrlArray.length; i++){
				ImageView imageView = new ImageView(context);
				// LayoutParams lp = new LayoutParams(300, 300);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
				params.setMargins(30, 10, 10, 30);
				imageView.setLayoutParams(params);

				//left, top, right, bottom
				//           MarginLayoutParams marginParams = new MarginLayoutParams(imageView.getLayoutParams());
				//           marginParams.setMargins(50, 0, 0, 50);
				//           imageView.setLayoutParams(marginParams);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);

				Bitmap bm = CacheManager.getImage(thumbNailUrlArray[i]);

				if ( bm == null )
					imageView.setImageResource(R.drawable.ic_launcher);
				else
					imageView.setImageBitmap(bm);

				slide_gallery_list.addView(imageView);
			}
		}else{
			Toast.makeText(ViewSlidesActivity.this, "No Thumbnails urls found", Toast.LENGTH_SHORT).show();
		}


		if( slide_gallery_list == null){
			Toast.makeText(context, "slide_gallery is empty", Toast.LENGTH_SHORT).show();

		}else{
			int child_count = slide_gallery_list.getChildCount();
			if( child_count > 0 ){
				//Toast.makeText(ViewSlidesActivity.this, "Adding Image Listener", Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(ViewSlidesActivity.this, "No Images found", Toast.LENGTH_SHORT).show();

			for(int i = 0; i < child_count; i++){
				//Assuming all children are ImageView
				ImageView thumbnail = (ImageView)slide_gallery_list.getChildAt(i);
				thumbnail.setTag(i);
				thumbnail.setOnClickListener( thumbnailClickListner );
			}
		}
	}

	@Override
	protected void onStart() {//activity is started and visible to the user
		Log.d(logtag,"onStart() called");
		super.onStart();
	}

	@Override
	protected void onResume() {//activity was resumed and is visible again
		Log.d(logtag,"onResume() called");
		super.onResume(); 
	}

	@Override
	protected void onPause() { //device goes to sleep or another activity appears
		Log.d(logtag,"onPause() called");//another activity is currently running (or user has pressed Home)
		super.onPause();   
	}

	@Override
	protected void onStop() { //the activity is not visible anymore
		Log.d(logtag,"onStop() called");
		super.onStop();
	}

	@Override
	protected void onDestroy() {//android has killed this activity
		Log.d(logtag,"onDestroy() called");
		super.onDestroy();
	}


	private void addHomeButtonListener() {
		/*
		final ImageView homeImage = (ImageView) findViewById(R.id.homeButton);

		homeImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {
				System.out.println("Home page button clicked..");
				//Saving stats
				saveStats(slidesName, totalAccessTime, accessTimeLongs);
				Intent activityChangeIntent = new Intent(ViewSlidesActivity.this, MainActivity.class);
				ViewSlidesActivity.this.startActivity(activityChangeIntent);

				finish();
			}
		});

		 */
	}

	private void addGestureListener() {
		gestureDetector = new GestureDetector(this, new MyGestureDetector());

		LinearLayout centerpane = (LinearLayout) findViewById(R.id.center_pane);
		centerpane.setOnTouchListener(new View.OnTouchListener() {       
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});

	}

	class MyGestureDetector extends SimpleOnGestureListener {
		/* SINGLE TAP - swipe the slides down */
		//event when single tap occurs
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e){
			//Toast.makeText(ViewSlidesActivity.this, "Single tap - emulate swipe down", Toast.LENGTH_SHORT).show();
			slide_up_menu_layout.startAnimation(animDown);
			slide_up_menu_layout.setVisibility(View.GONE);
			return true;		
		}

		/* DOUBLE TAP - goto home page */
		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			//Toast.makeText(ViewSlidesActivity.this, "Double tap - goto home", Toast.LENGTH_SHORT).show();
			//Saving stats
			updateStats();
			saveStats(slidesName, totalAccessTime, accessTimeFloats);
			Intent activityChangeIntent = new Intent(ViewSlidesActivity.this, MainActivity.class);
			activityChangeIntent.putExtra("docId", docId);
			ViewSlidesActivity.this.startActivity(activityChangeIntent);

			finish();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				/* SLIDE UP MENU */
				/*
              if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                  return false;
				 */
				if( e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ) {
					//Toast.makeText(ViewSlidesActivity.this, "Up Swipe", Toast.LENGTH_SHORT).show();
					slide_up_menu_layout.setVisibility(View.VISIBLE);
					slide_up_menu_layout.startAnimation(animUp);
				} else if( e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ){
					//Toast.makeText(ViewSlidesActivity.this, "Down Swipe", Toast.LENGTH_SHORT).show();
					slide_up_menu_layout.startAnimation(animDown);
					slide_up_menu_layout.setVisibility(View.GONE);
				}

				// left-right swipe for next/previous slides
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;
				// right to left swipe
				if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					//Toast.makeText(ViewSlidesActivity.this, "Left Swipe", Toast.LENGTH_SHORT).show();
					Log.d(logtag, "Swipe Left");
					showNextSlide();
				}  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					//Toast.makeText(ViewSlidesActivity.this, "Right Swipe", Toast.LENGTH_SHORT).show();
					Log.d(logtag, "Swipe Right");
					showPreviousSlide();
				}
			} catch (Exception e) {
				// nothing
			}
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}
	}

	public void showNextSlide() {
		image = (ImageView) findViewById(R.id.image);

		lastSlideNum = currentSlideNum;
		if (currentSlideNum < slides.length-1) {
			currentSlideNum += 1;
		}

		//Changing to bitmap
		image.setImageBitmap(CacheManager.getImage(slides[currentSlideNum]));

		// update stats
		updateStats();
	}

	public void showPreviousSlide() {
		image = (ImageView) findViewById(R.id.image);

		lastSlideNum = currentSlideNum;
		currentSlideNum -= 1;
		if (currentSlideNum < 0) {
			currentSlideNum = 0;
		}

		//Changing to bitmap
		image.setImageBitmap(CacheManager.getImage(slides[currentSlideNum]));

		// update stats
		updateStats();
	}



/*	private String getAccessTimes() {
		String stats = new String();

		for (int i=0; i<accessTimeLongs.length; i++) {
			if (i > 0 && i<=accessTimeLongs.length-1)
				stats += ",";
			stats += accessTimeLongs[i];
		}

		return stats;
	}*/


	private void updateStats() {
		Date endDate = Calendar.getInstance().getTime();

		long diffInMs = endDate.getTime() - accessTimes[lastSlideNum].getTime();
		long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
		
//		long accessInSec = Long.valueOf(accessTimeLongs[lastSlideNum]).longValue() + diffInSec;
//
//		long accessInMin = accessInSec/60;
//		if (accessInSec < 61)
//			accessInMin = 1;

		accessTimeFloats[lastSlideNum] = (float) (diffInSec/(float)60);
		System.out.println("*******************" + accessTimeFloats[lastSlideNum] + ":" + diffInSec);
		accessTimes[currentSlideNum] = endDate;

		for (int i=0; i<accessTimes.length; i++) {
			totalAccessTime += accessTimeFloats[i];
		}

		//saveStats(slidesName, totalAccessTime, accessTimeLongs);
	}

	private void saveStats(String slidesName, float totalAccessTime2, Float[] accessTimeFloats2)  {     
		PptBean temp = null;
		if (MainActivity.pptBeanMap != null){
			temp = (PptBean) MainActivity.pptBeanMap.get(docId);
			if(temp!=null){
				UploadBean upTemp = (UploadBean) MainActivity.uploadBeanMap.get(docId);
				if(upTemp == null){
					upTemp = new UploadBean();
					upTemp.setPptName(slidesName);
				}

				temp.setTotalViewTime(temp.getTotalViewTime() + totalAccessTime2);
				upTemp.setTotalViewTime(upTemp.getTotalViewTime() + totalAccessTime2);

				ArrayList<ArrayList<Float>> accessTimesList = temp.getAccessTimes();
				accessTimesList.add(new ArrayList<Float>(Arrays.asList(accessTimeFloats2)));
				temp.setDocId(docId);
				temp.setAccessTimes(accessTimesList);
				temp.setTotalViewTime(totalAccessTime);
				MainActivity.pptBeanMap.put(docId, temp);

				ArrayList<ArrayList<Float>> accessTimesListUp = upTemp.getAccessTimes();
				accessTimesListUp.add(new ArrayList<Float>(Arrays.asList(accessTimeFloats2)));
				upTemp.setDocId(docId);
				upTemp.setAccessTimes(accessTimesListUp);
				upTemp.setTotalViewTime(totalAccessTime);
				MainActivity.uploadBeanMap.put(docId, upTemp);


			}
		}
	}
}


