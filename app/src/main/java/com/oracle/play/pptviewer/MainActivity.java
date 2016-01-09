package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity{
	private static String logtag = "MainActivity";//for use as the tag when logging

	private static String GRID_LOAD = "Load Grid";
	private static String GRID_RELOAD = "Reload Grid";
	private static String FAV_LOAD = "Load Fav Grid";
	private static String FAV_GRID_RELOAD = "Fav Reload Grid";
	private static String SEARCH_LOAD =" Load Search Results Grid";
	private static String SEARCH_FAV_LOAD = "Load Favorite Search Results Grid";
	
	private static int sortbyCreationDate_index = 0;
	private static int sortbyViews_index = 1;
	private static int sortbyRatings_index = 2;
	private static int sortbyFeedback_index = 3;
	private static int currentActionbarItem_checked;

	private static boolean isHome = true;
	private static ArrayList<String> matchedNames = new ArrayList<String>();
	static String username = "";
	//static String displayUsername = "";
	private static int chosenPosition = 0;
	public static int sharedPosition = 0;

	final MainActivity context = this;
	private HorizontalScrollView hv;

	//public String pptClicked = "";
	public int docId = 0;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	public static LinkedHashMap<Integer,Object> pptBeanMap;
	public static HashMap<Integer,UploadBean> uploadBeanMap;

	//public static HashMap<String,Boolean> favHistory = new HashMap<String,Boolean>();
	//Handler for timer thread
	//Handler handler = new Handler();  

	private LinearLayout toolbar;

	private Animation animleft;
	private Animation animright;
	
	private MenuItem actionbarMenuItems[];
	

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main1);

		// get username
		Intent myIntent = getIntent();
		username = myIntent.getStringExtra("username");
		//displayUsername = myIntent.getStringExtra("displayUsername");
		System.out.println("+++ MainActivity onCreate: got username " + username);

		//Fixing username null issue
		if(username!=null){
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit(); 
			editor.putString("userName", username);
		//	editor.putString("displayuserName", displayUsername);
			editor.commit();
			//If username is not null, that means this came from login page so re-initialize the cache here
			CacheManager.reInitInstance();
			addGridView(GRID_RELOAD,0);
		}
		else{
			SharedPreferences preferences = getPreferences(MODE_PRIVATE);
			username = preferences.getString("userName", "John Doe");
		//	displayUsername = preferences.getString("displayuserName", "John Doe");
		}

		int t = myIntent.getIntExtra("docId", -1);
		if(t!=-1){
			docId = t;
		}
		//For new ppt upload
		Boolean flag = myIntent.getBooleanExtra("call_refresh", false);
		if(flag){
			//Call came from new ppt upload
			//Reinitialize the UI
			CacheManager.reInitInstance();
			addGridView(GRID_RELOAD,0);
		}
		else{
			CacheManager.initInstance();
			addGridView(GRID_LOAD,chosenPosition);
		}
		addLogoutButtonListener();

		addHomeButtonListener();
		addFavoriteButtonListener();
		addSearchButtonListener();
		addRefreshButtonListener();
		addSyncButtonListener();
		addInfoButtonListener();

		
		addRatingsButtonListener();

		updateInteractionsView(docId);
		//add_show_hide_toolbar();

		updateStatsView(docId);

		//timer for batch sync    
		//handler.postDelayed(runnable,30000);
		addCloudStuff();
	}

	private void addCloudStuff(){
		animleft = AnimationUtils.loadAnimation(this, R.anim.anim_left);
		animright = AnimationUtils.loadAnimation(this, R.anim.anim_right);
		ImageView homeButton = (ImageView)findViewById(R.id.cloudButton);
		hv = (HorizontalScrollView)findViewById(R.id.cloudview);
		if(hv == null){
			Toast.makeText(context, "Horizontal View is null", Toast.LENGTH_LONG).show();
			return;
		}
		hv.setVisibility(View.GONE);

		homeButton.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = MotionEventCompat.getActionMasked(event);
				switch(action) {
				case (MotionEvent.ACTION_DOWN) :
					Log.d("LAYOUT TUTORIAL","Action was DOWN");
				Toast.makeText(context, "Cloud integration pending ...", Toast.LENGTH_LONG).show();
				hv.clearAnimation();
				hv.setAnimation(animleft);
				hv.getAnimation().start();				
				hv.setVisibility(View.VISIBLE);
				return true;
				case (MotionEvent.ACTION_UP) :
					Log.d("LAYOUT TUTORIAL","Action was UP");
				//Toast.makeText(context, "TOUCH UP", Toast.LENGTH_SHORT).show();
				//hv.clearAnimation();
				//hv.setAnimation(animright);
				//hv.getAnimation().start();
				hv.setVisibility(View.GONE);
				return true;
				default:
					return false;
				}
			}});		
	}

	public void addGridView(String input_option, int chosenPos) {
		sharedPosition = chosenPos;
		final GridView gridview = (GridView) findViewById(R.id.gridView);

		if(input_option.equalsIgnoreCase(GRID_LOAD)){
			gridview.setAdapter(SlidesListAdapter.initInstance(this));
			isHome = true;
		}
		else if (input_option.equalsIgnoreCase(FAV_LOAD)) {
			//This is fav view
			FavAndFavSearchImageAdapter favImageAdapter = new FavAndFavSearchImageAdapter(this, gridview,null);	
			gridview.setAdapter(favImageAdapter);
			isHome = false;

		}
		else if (input_option.equalsIgnoreCase(GRID_RELOAD)) {
			gridview.setAdapter(SlidesListAdapter.reInitInstance(this));
			isHome = true;
		}
		else if (input_option.equalsIgnoreCase(FAV_GRID_RELOAD)) {
			FavAndFavSearchImageAdapter favImageAdapter = new FavAndFavSearchImageAdapter(this, gridview,null);	
			gridview.setAdapter(favImageAdapter);
			isHome = false;
		}
		else if(input_option.equalsIgnoreCase(SEARCH_LOAD)){
			gridview.setAdapter(new SearchImageAdapter(this, matchedNames));

		}
		else if(input_option.equalsIgnoreCase(SEARCH_FAV_LOAD)){
			FavAndFavSearchImageAdapter favSearchImageAdapter = new FavAndFavSearchImageAdapter(this, gridview,matchedNames);
			gridview.setAdapter(favSearchImageAdapter);

		}


		//Adding gesture listener
		final GestureDetector gestureDetector = new GestureDetector(this, new GestureListener());
		final GestureDetector FlingGestureDetector = new GestureDetector(this, new FlingGestureListener());

		gridview.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent me) {

				float currentXPosition = me.getX();
				float currentYPosition = me.getY();
				int position = gridview.pointToPosition((int) currentXPosition, (int) currentYPosition);

				View x = gridview.getChildAt(position);


				if((x!=null) && (x.getTag()!=null)){
					docId = (Integer) x.getTag();
					boolean selectFlag = selectPpt(x, gridview, position);
					chosenPosition = position;
					if(selectFlag){
						return gestureDetector.onTouchEvent(me);
					}
					else
						return false;
				}
				else
					return FlingGestureDetector.onTouchEvent(me);
			}


		});
	}


	private boolean selectPpt(View x, GridView gridview, int position) {
		// System.out.println("Selecting ppt at " + position + ":" + gridview.getChildCount());
		if((x!=null) && (x.getTag()!=null)){
			docId = (Integer) x.getTag();

			if((docId!=0)&&(pptBeanMap!=null)&&((pptBeanMap.get(docId))!=null)){


				//	Toast.makeText(context, "TOUCHED at " + pptClicked, Toast.LENGTH_SHORT).show();

				updateRatingsView(docId);

				// update interactions view
				updateInteractionsView(docId);

				// Hightlight slide deck background while waiting for load
				LinearLayout pane = (LinearLayout) x.findViewById(R.id.slidePane);
				if(pane != null){
					pane.setBackgroundColor(Color.parseColor("#CCCCFF"));
					chosenPosition=position;
					int totalChild = gridview.getChildCount();
					for(int i=0;i<totalChild;i++){
						if(i!=position){
							View temp = gridview.getChildAt(i);
							LinearLayout temp_pane = (LinearLayout) temp.findViewById(R.id.slidePane);
							if(temp_pane!=null){
								temp_pane.setBackgroundColor(Color.parseColor("#3399FF"));
							}
						}
					}
					//pane.setBackgroundResource(R.drawable.color_66b2ff);
				}

				else{

					System.out.println("PANE IS NULL");
				}
				return true;
			}

		}
		return false;
	}

	/*private void addFavButtonListner(ImageButton favButton) {
		if (favButton == null)
			return;

		favButton.setOnClickListener(new OnClickListener() {      
			@Override
			public void onClick(View v) {        
				Toast.makeText(getBaseContext(),  "FavButton clicked for " + docId, Toast.LENGTH_SHORT).show();

				// button tag:  0 (not selected), 1 (selected)
				String s = (String) v.getTag();       
				// System.out.println("+++ tag = " + s);
				boolean selected = s.equals("1");

				// Perform action on clicks, depending on whether it's now checked
				if (!selected) {
					//Was not selected before. User clicked and has to now selected
					//Edit the local memory
					((ImageButton)v).setImageResource(R.drawable.star_10_10_yellow);
					v.setTag("1");

					int seeStars = getStarRatings();
					setStarRatings(seeStars);

					//TODO: Change history        
					// MainActivity.favHistory.put(pptClicked, true);						
				} else {
					//User un-marked as favorite
					//Edit the local memory
					((ImageButton)v).setImageResource(R.drawable.star_icon_10x10);
					v.setTag("0");
					
					int oldStars = getStarRatings();
          setStarRatings(oldStars-1);          

					//TODO: Change history
					// MainActivity.favHistory.put(pptName, false);
				}
			}
		});
	}*/

	

	private int getStarRatings() {

		int stars = 0;
		final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		if(ratingBar!=null){
			return (int) ratingBar.getRating();
		}
		return stars;
	}


	private void addRatingsButtonListener() {
		Log.d(logtag, "addRatingsButtonListener: deck highligted = " + docId);

		// handle stars and comments rating
		final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		final EditText commentStr = (EditText) findViewById(R.id.comments);

		//Disable the buttons
		ratingBar.setIsIndicator(true);
		ratingBar.setStepSize(1);
		
		commentStr.setEnabled(false);
		commentStr.clearFocus();
		commentStr.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					//Toast.makeText(MainActivity.this, "dfd" + docId, Toast.LENGTH_SHORT).show();

					String comments = commentStr.getText().toString();
					//Toast.makeText(getBaseContext(), comments, Toast.LENGTH_LONG).show();

					PptBean temp = (PptBean) pptBeanMap.get(docId);
					if (temp != null) {
						// TextView ratingsView = (TextView) findViewById(R.id.ratingsView);
						// ratingsView.setText("Ratings for " + temp.getPptName() + ":");
						// update star rating
						int stars = getStarRatings();

						Rating rating = new Rating(stars,comments,username);
						ArrayList<Rating> currentRatingList = temp.getRatings();
						currentRatingList.add(rating);
						temp.setRatings(currentRatingList);
						pptBeanMap.put(temp.getDocId(), temp);

						//Add comment to uploadBean
						UploadBean upTemp = (UploadBean) uploadBeanMap.get(docId);
						if(upTemp==null){
							upTemp = new UploadBean();
							upTemp.setDocId(docId);
						}
						ArrayList<Rating> tempCurrentRatingList = upTemp.getRatings();
						tempCurrentRatingList.add(rating);
						upTemp.setRatings(tempCurrentRatingList);

						uploadBeanMap.put(docId, upTemp);
						updateInteractionsView(docId);
						//setStarRatings(0);
						commentStr.setText("", TextView.BufferType.EDITABLE);
						ratingBar.setRating(0);
					}
					return true;
				}

				return false;
			}
		});


	}

	public void updateInteractionsView(int inputDocId) {
		PptBean bean = null;
		int docid = inputDocId;
		final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		final EditText commentStr = (EditText) findViewById(R.id.comments);
		if(docid!=0){
		bean = (PptBean) pptBeanMap.get(docid);
		}
		if (bean != null) {     

			//Enable the button
			ratingBar.setIsIndicator(false);
			commentStr.setEnabled(true);

			ArrayList<Rating> ratings = bean.getRatings();
			if (!ratings.isEmpty()) {
				TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
				table.removeAllViews();
				table.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));			

				LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(context.LAYOUT_INFLATER_SERVICE);
				for(Rating x: ratings){

					String user = x.getUsername();
					String comment = x.getComment();
					int stars = x.getStars();

					TableRow row = new TableRow(this);
					/*TextView t = new TextView(this);					
					String report = " " + user + ": " + stars + " stars, comment = " +
							comment;
					t.setText(report);					
					row.addView(t);					
					table.addView(row);
					 */
					String report = " " + user + ": " + stars + " stars, comment = " +
							comment;

					View newrow = li.inflate(R.layout.interaction_row, row);
					TextView username = (TextView) newrow.findViewById(R.id.iuserName);
					username.setText(user);

					TextView icomment = (TextView) newrow.findViewById(R.id.icomment);
					icomment.setText(comment);

					for(int i = 0; i < stars; i++){
						ImageView starimage = null;
						if(i == 0)
							starimage = (ImageView) newrow.findViewById(R.id.istarButton_0);
						else if( i == 1)
							starimage = (ImageView) newrow.findViewById(R.id.istarButton_1);
						else if( i == 2)
							starimage = (ImageView) newrow.findViewById(R.id.istarButton_2);
						else if( i == 3)
							starimage = (ImageView) newrow.findViewById(R.id.istarButton_3);
						else
							starimage = (ImageView) newrow.findViewById(R.id.istarButton_4);
						starimage.setImageResource(R.drawable.star_10_10_yellow);

					}
					table.addView(newrow);
				}
				//View newrow = li.inflate(R.layout.interaction_row, table);
				//View newrow2 = li.inflate(R.layout.interaction_row, table);
				//table.addView(newrow);
				//HorizontalScrollView samplerow = (HorizontalScrollView)findViewById(R.id.irow);
			} else {
				TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
				table.removeAllViews();
			}

			/* debug
       String report = "PptBeanMap: name=" + pptClicked + " user=" + username + " stars=" + bean.getUserStarRating(username) +
           " comment=" + bean.getUserCommentRating(username);
       Log.d(logtag, "addInteractionsListener "+ report);
       final TextView interactions = (TextView) findViewById(R.id.interactions);

       interactions.setText(report);
			 */
		}
		else{
			//Disable the ok button
			ratingBar.setIsIndicator(true);
			commentStr.setEnabled(false);
			TableLayout table = (TableLayout) findViewById(R.id.tableLayout);
			table.removeAllViews();
		}
	}

	public void updateRatingsView(int inputDocId) {
		// if (docId != 0) {
		// 	final TextView interactions = (TextView) findViewById(R.id.interactions);
		//	interactions.setText(" Ratings for " + ((PptBean)pptBeanMap.get(docId)).getPptName() + ":");
		// }
		
		docId = inputDocId;
		final RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar1);
		final EditText commentStr = (EditText) findViewById(R.id.comments);
		PptBean temp = null;
		
		if(docId!=0)
			temp = (PptBean)pptBeanMap.get(inputDocId);

		if(temp!=null){
			ratingBar.setIsIndicator(false);
			ratingBar.setRating(0);
			commentStr.setText("", TextView.BufferType.EDITABLE);
			commentStr.setEnabled(true);
			
		}
		else{
			ratingBar.setIsIndicator(true);
			commentStr.setEnabled(false);
		}


		/*	PptBean bean = (PptBean) pptBeanMap.get(pptClicked);
		if (bean != null) {
			int stars = bean.getUserStarRating(username);
			setStarRatings(stars);

			String comment = bean.getUserCommentRating(username);
			if (comment != null)
				commentStr.setText(comment, TextView.BufferType.EDITABLE);
			else
				commentStr.setText("", TextView.BufferType.EDITABLE);
		} else {
			setStarRatings(0);
			commentStr.setText("", TextView.BufferType.EDITABLE);
		}*/
	}

	@Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_main1, menu);
    
    // TODO: highlight default menu option
    final MenuItem sortbyCreationDate_item = menu.findItem(R.id.sortby_creationDate);   
    sortbyCreationDate_item.setCheckable(true);
    sortbyCreationDate_item.setChecked(true);  // default
    currentActionbarItem_checked = 0;
    
    final MenuItem sortbyViews_item = menu.findItem(R.id.sortby_views);   
    sortbyViews_item.setCheckable(true);

    final MenuItem sortbyRatings_item = menu.findItem(R.id.sortby_ratings);   
    sortbyRatings_item.setCheckable(true);

    final MenuItem sortbyFeedback_item = menu.findItem(R.id.sortby_feedback);   
    sortbyFeedback_item.setCheckable(true);
    
    actionbarMenuItems = new MenuItem[] {sortbyCreationDate_item, sortbyViews_item, sortbyRatings_item, sortbyFeedback_item};
    
    // render username
    final MenuItem userIdItem = menu.findItem(R.id.userid_label);
    if (username.length() > 0)
      userIdItem.setTitle(username);
  
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // TODO: highlight selected item
    //       perform action
    switch(item.getItemId()) {
    case R.id.sortby_creationDate:
      Toast.makeText(this, "* Sort by Creation Date", Toast.LENGTH_SHORT).show();
      setActionbarItemChecked(sortbyCreationDate_index);
      break;
    case R.id.sortby_views:
      Toast.makeText(this, "Sort by Views", Toast.LENGTH_SHORT).show();
      setActionbarItemChecked(sortbyViews_index);
      break;
    case R.id.sortby_ratings:
      Toast.makeText(this, "Sort by Ratings", Toast.LENGTH_SHORT).show();
      setActionbarItemChecked(sortbyRatings_index);
      break;
    case R.id.sortby_feedback:
      Toast.makeText(this, "Sort by Feedback", Toast.LENGTH_SHORT).show();
      setActionbarItemChecked(sortbyFeedback_index);
      break;
     default:
       break;
    }
    
    return true;
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
		final ImageView homeImage = (ImageView) findViewById(R.id.homeButton);
		final ImageView favoriteImage = (ImageView) findViewById(R.id.starButton);

		homeImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {
				favoriteImage.setImageResource(R.drawable.fav_unselected);
				homeImage.setImageResource(R.drawable.home_selected);
				System.out.println("Home page button clicked..");

				addGridView(GRID_LOAD,0);
				HideSearchIfOpen();
			}
		});
	}


	private void addFavoriteButtonListener() {
		final ImageView favoriteImage = (ImageView) findViewById(R.id.starButton);
		final ImageView homeImage = (ImageView) findViewById(R.id.homeButton);

		favoriteImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {
				favoriteImage.setImageResource(R.drawable.fav_selected);
				homeImage.setImageResource(R.drawable.home_unselected);
				addGridView(FAV_LOAD,0);
				HideSearchIfOpen();
				Toast.makeText(context, "Favorites loaded..", Toast.LENGTH_SHORT).show();			
			}
		});
	}

	private void toggleSearch(SearchView searchview){
		Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_down);
		Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_up);

		if(searchview.getVisibility()==View.GONE){
			searchview.startAnimation(slide_down);
			searchview.setVisibility(View.VISIBLE);

		}
		else{
			searchview.startAnimation(slide_up);
			searchview.setVisibility(View.GONE);
			searchview.setQuery("", false);
			if(isHome){
				matchedNames = SearchPptNames.getPptList("");
				addGridView(SEARCH_LOAD,0);
			}
			else{
				matchedNames = SearchPptNames.getFavPptList("");
				addGridView(SEARCH_FAV_LOAD,0);
			}
		}
	}

	private void addLogoutButtonListener() {
		final ImageView favoriteImage = (ImageView) findViewById(R.id.logoutButton);

		favoriteImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {
				Log.d(logtag, "Application terminated by logout");
				// Toast.makeText(context, "Logging out", Toast.LENGTH_SHORT).show();     

				// TODO: close all out-standing files and save context here
				UploadBeanListWrapper temp = new UploadBeanListWrapper();
				temp.setUploadBeans(uploadBeanMap);
				temp.setUsername(username);
				String dataToSend = new Gson().toJson(temp).toString();
				System.out.println(dataToSend);
				boolean result = false;
				try {
					result = new UploadData().execute(dataToSend).get();
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(logtag, "Sync data failed");
					result = false;
				} 
				if(result){
					Toast.makeText(context, "Sync Successful..Logging out..", Toast.LENGTH_SHORT).show();
					//Sync is successful. Refresh the data
					//Clear the uploadBeanMap
					uploadBeanMap = new HashMap<Integer, UploadBean>();
				}
				else
					Toast.makeText(context, "Sync Failed..Logging out..", Toast.LENGTH_SHORT).show();

				finish();
			}
		});
	}


	private void HideSearchIfOpen(){
		final SearchView searchView = (SearchView) findViewById(R.id.searchView1);

		Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_up);

		if(searchView.getVisibility()==View.VISIBLE){


			searchView.startAnimation(slide_up);
			searchView.setVisibility(View.GONE);
			searchView.setQuery("", false);
			if(isHome){
				matchedNames = SearchPptNames.getPptList("");
				addGridView(SEARCH_LOAD,0);
			}
			else{
				matchedNames = SearchPptNames.getFavPptList("");
				addGridView(SEARCH_FAV_LOAD,0);
			}
		}
	}
	private void addSearchButtonListener() {
		final ImageView searchImage = (ImageView) findViewById(R.id.searchButton);

		searchImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {

				System.out.println("Search bar button clicked..");
				final SearchView searchView = (SearchView) findViewById(R.id.searchView1);
				toggleSearch(searchView);
				searchView.setOnCloseListener(new OnCloseListener() {

					@Override
					public boolean onClose() {
						if(isHome){
							matchedNames = SearchPptNames.getPptList("");
							addGridView(SEARCH_LOAD,0);
						}
						else{
							matchedNames = SearchPptNames.getFavPptList("");
							addGridView(SEARCH_FAV_LOAD,0);
						}
						return true;
					}
				});


				searchView.setOnQueryTextListener(new OnQueryTextListener() { 


					@Override
					public boolean onQueryTextSubmit(String queryString) {
						// TODO Auto-generated method stub
						System.out.println("Query text submitted " + queryString);
						if(isHome){
							matchedNames = SearchPptNames.getPptList(queryString);
							addGridView(SEARCH_LOAD,0);
						}
						else{
							matchedNames = SearchPptNames.getFavPptList(queryString);
							addGridView(SEARCH_FAV_LOAD,0);
						}

						return true;
					}


					@Override
					public boolean onQueryTextChange(String newText) {
						System.out.println("Query text typed " + newText);
						if(isHome){
							matchedNames = SearchPptNames.getPptList(newText);
							addGridView(SEARCH_LOAD,0);
						}
						else{
							matchedNames = SearchPptNames.getFavPptList(newText);
							addGridView(SEARCH_FAV_LOAD,0);
						}

						return true;
					}



				});

				//Close Search View on close
				searchView.setOnCloseListener(new OnCloseListener() {

					@Override
					public boolean onClose() {
						searchView.setVisibility(View.GONE);
						return true;
					}
				});
			}
		});
	}

	private void addRefreshButtonListener() {
		final ImageView refreshImage = (ImageView) findViewById(R.id.refreshButton);

		refreshImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {

			//	Toast.makeText(context, "Refresh started", Toast.LENGTH_SHORT).show();
				//Clearing cache
				CacheManager.reInitInstance();
				//Reload home page
				if(isHome)
					addGridView(GRID_RELOAD,chosenPosition);
				else
					addGridView(FAV_GRID_RELOAD,chosenPosition);
				HideSearchIfOpen();
			//	Toast.makeText(context, "Refresh complete", Toast.LENGTH_SHORT).show();

			}


		});
	}

	private void addInfoButtonListener(){
		final ImageView infoImage = (ImageView) findViewById( R.id.infoButton );

		infoImage.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// custom dialog
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.custom_info);
				dialog.setTitle(" Architecture ");


				ImageView image = (ImageView) dialog.findViewById(R.id.infoimage);
				image.setImageResource(R.drawable.lightbox_app_architecture);

				TextView titletext = (TextView) dialog.findViewById(R.id.infotitle);
				titletext.setText(" The Lightbox Viewer is an app that enables users to view presentations and keep track of analytics data associated with it. ");

				Button dialogButtonneg = (Button) dialog.findViewById(R.id.positiveButtoninfo);
				dialogButtonneg.setText("OK");
				// if button is clicked, close the custom dialog
				dialogButtonneg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});
	}

	private void addSyncButtonListener() {
		final ImageView syncImage = (ImageView) findViewById(R.id.syncButton);

		syncImage.setOnClickListener(new OnClickListener() {        
			@Override
			public void onClick(View view) {

				System.out.println("Sync button clicked..");
				//Toast.makeText(context, "Upload started", Toast.LENGTH_SHORT).show();
				UploadBeanListWrapper temp = new UploadBeanListWrapper();
				temp.setUploadBeans(uploadBeanMap);
				temp.setUsername(username);
				String dataToSend = new Gson().toJson(temp).toString();
				System.out.println(dataToSend);
				boolean result = false;
				try {
					result = new UploadData().execute(dataToSend).get();
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(logtag, "Sync data failed");
					result = false;
				} 
				if(result){
					//Sync is successful. Refresh the data
					Toast.makeText(context, "Upload Successful...Refresh complete", Toast.LENGTH_SHORT).show();
					//Clearing cache
					CacheManager.reInitInstance();
					//Reload home page
					addGridView(GRID_RELOAD,0);
					HideSearchIfOpen();
					
					//Clear the uploadBeanMap
					uploadBeanMap = new HashMap<Integer, UploadBean>();
				}
				else
					Toast.makeText(context, "Upload Failed...", Toast.LENGTH_SHORT).show();
			}


		});
	}

	public void updateStatsView(int inputDocid) {

		// System.out.println("++++++ MainActivity.updateStatsView: pptClicked = " + pptClicked);
		int docid = inputDocid;
		PptBean temp = null;
		TextView stats = (TextView) findViewById(R.id.statsView);
		TextView deckSelected = (TextView) findViewById(R.id.deck_selected);
		if(docid!=0)
			temp = (PptBean) pptBeanMap.get(docid);
		if(temp!=null){

			deckSelected.setText("   " + temp.getPptName());

			float totalViewTime = temp.getTotalViewTime();
			System.out.println("++++++ MainActivity.updateStatsView: totalViewTime from cache is " +
					totalViewTime);
			stats.setText("  Total View time (" + 
					Math.round(totalViewTime*100)/100.0d + " min.)");
		}
		else{
			deckSelected.setText("   ");
			stats.setText("");
		}

	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {


		//event when single tap occurs
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e){
			Log.d(logtag, "PPT Selected " + docId);

			TextView deckSelected = (TextView) findViewById(R.id.deck_selected);
			deckSelected.setText("   " + ((PptBean)pptBeanMap.get(docId)).getPptName());

			// update stats
			updateStatsView(docId);
			/*
			TranslateAnimation animate = new TranslateAnimation(0,-toolbar.getWidth(),0,0);
        	animate.setDuration(500);
        	animate.setFillAfter(true);
        	toolbar.startAnimation(animate);
        	toolbar.setVisibility(View.GONE);
        	show_toolbar.setVisibility(View.VISIBLE);
			 */
			// TODO: show selected slide deck
			//	Toast.makeText(MainActivity.this, "Slide Deck " + pptClicked + " Selected ", Toast.LENGTH_SHORT).show();    


			return true;
		}

		// event when double tap occurs
		@Override
		public boolean onDoubleTap(MotionEvent e) {

			// update stats
			updateStatsView(docId);

			//Increase number of viewers
			PptBean temp = (PptBean) pptBeanMap.get(docId);
			if(temp!=null){
				temp.setNumViewers(temp.getNumViewers()+1);
				pptBeanMap.put(docId, temp);
			}

			UploadBean upTemp = (UploadBean) uploadBeanMap.get(docId);
			if(upTemp == null){
				upTemp = new UploadBean();
				upTemp.setDocId(docId);
			}
			upTemp.setNumViewers(upTemp.getNumViewers()+1);

			/* SLIDE UP MENU */
			String[] urlArray=null;
			int numberOfPpts = 0;
			if( pptBeanMap != null )
				numberOfPpts = pptBeanMap.size();
			if( numberOfPpts > 0 ){
				// System.out.println("++++ MainActivity.onDoubleTap: pptClicked = " + pptClicked);

				urlArray = ((PptBean)pptBeanMap.get(docId)).getThumbnailURL();
				int size = urlArray.length;
				//Toast.makeText(context, "numberOfPpts = " + String.valueOf(size), Toast.LENGTH_SHORT).show();
			}else{
				//Toast.makeText(context, "numberOfPpts = 0", Toast.LENGTH_SHORT).show();
			}

			Log.d(logtag, " Double Tapped at: ("+ docId + ")");
			Intent activityChangeIntent = new Intent(MainActivity.this, ViewSlidesActivity.class);
			if((PptBean) pptBeanMap.get(docId)==null){
				System.out.println("Null");
			}
			activityChangeIntent.putExtra("imagearray",((PptBean)pptBeanMap.get(docId)).getImageURL());
			/* SLIDE UP MENU */
			activityChangeIntent.putExtra("thumbnailurlarray",(String[])urlArray);
			activityChangeIntent.putExtra("clicked", docId);

			//Need to start with slide 0. So commenting the line below
			//activityChangeIntent.putExtra("clicked", (Integer)v.getTag());
			MainActivity.this.startActivity(activityChangeIntent);
			finish();
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				/* SLIDE UP and DOWN for SEARCH */
				if( e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ) {
					Log.d(logtag,"Up swipe on home page");
					final SearchView searchView = (SearchView) findViewById(R.id.searchView1);
					Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_up); 
					if(searchView.getVisibility()==View.VISIBLE){

						searchView.startAnimation(slide_up);
						searchView.setVisibility(View.GONE);
						searchView.setQuery("", false);
						if(isHome){
							matchedNames = SearchPptNames.getPptList("");
							addGridView(SEARCH_LOAD,0);
						}
						else{
							matchedNames = SearchPptNames.getFavPptList("");
							addGridView(SEARCH_FAV_LOAD,0);
						}
					}

				} else if( e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ){
					Log.d(logtag,"Down swipe on home page");
					final SearchView searchView = (SearchView) findViewById(R.id.searchView1);
					Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_down);

					if(searchView.getVisibility()==View.GONE){
						searchView.startAnimation(slide_down);
						searchView.setVisibility(View.VISIBLE);

					}


				}

				return true;
			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

	private class FlingGestureListener extends GestureDetector.SimpleOnGestureListener {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			try {
				/* SLIDE UP and DOWN for SEARCH */
				if( e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ) {
					Log.d(logtag,"Fling Up swipe on home page");
					final SearchView searchView = (SearchView) findViewById(R.id.searchView1);
					Animation slide_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_up); 
					if(searchView.getVisibility()==View.VISIBLE){

						searchView.startAnimation(slide_up);
						searchView.setVisibility(View.GONE);
						searchView.setQuery("", false);
						if(isHome){
							matchedNames = SearchPptNames.getPptList("");
							addGridView(SEARCH_LOAD,0);
						}
						else{
							matchedNames = SearchPptNames.getFavPptList("");
							addGridView(SEARCH_FAV_LOAD,0);
						}
					}

				} else if( e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY ){
					Log.d(logtag," Fling Down swipe on home page");
					final SearchView searchView = (SearchView) findViewById(R.id.searchView1);
					Animation slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.search_down);

					if(searchView.getVisibility()==View.GONE){
						searchView.startAnimation(slide_down);
						searchView.setVisibility(View.VISIBLE);

					}


				}


			} catch (Exception e) {
				// nothing
			}
			return false;
		}
	}

  private void setActionbarItemChecked (int index) {
    if (currentActionbarItem_checked != index) {
      actionbarMenuItems[currentActionbarItem_checked].setChecked(false);
      actionbarMenuItems[index].setChecked(true);
      currentActionbarItem_checked = index;
    }
  }

	/*private Runnable runnable = new Runnable() {
		@Override
		public void run() {

			//Toast.makeText(context, "30 second timer", Toast.LENGTH_SHORT).show();
			Log.d(logtag,"timer............");

			handler.postDelayed(this, 30000);

			UploadBeanListWrapper temp = new UploadBeanListWrapper();
			temp.setUploadBeans(uploadBeanMap);
			temp.setUsername(username);
			String dataToSend = new Gson().toJson(temp).toString();
			System.out.println(dataToSend);
			boolean result = false;
			try {
				result = new UploadData().execute(dataToSend).get();
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(logtag, "Sync data failed");
				result = false;
			} 
			if(result){
				Toast.makeText(context, "Sync Successful...", Toast.LENGTH_SHORT).show();
				//Sync is successful. Refresh the data
				Toast.makeText(context, "Refresh complete", Toast.LENGTH_SHORT).show();
				//Clear the uploadBeanMap
				uploadBeanMap = new HashMap<Integer, UploadBean>();
			}
			else
				Toast.makeText(context, "Sync Failed...", Toast.LENGTH_SHORT).show();
		}
	};*/


}
