package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SlidesListAdapter extends BaseAdapter {
	private static String logtag = "SlidesListAdapter"; //for use as the tag when logging
	private static SlidesListAdapter slideListAdapterInstance;
	private MainActivity context;

	//references to our images
	private Bitmap[] mThumbIds;
	private String[] pptNames;
	private int[] numSlides;
	private int[] numViewers;
	private int[] docId;



	private static LayoutInflater inflater=null;

	//Init the instance
	public static ListAdapter initInstance(MainActivity c){
		if (slideListAdapterInstance == null)
		{
			// Create the instance
			slideListAdapterInstance = new SlidesListAdapter(c);				
		}

		return slideListAdapterInstance;
	}

	//Reinstantitate
	public static ListAdapter reInitInstance(MainActivity c){

		// Create the instance
		slideListAdapterInstance = new SlidesListAdapter(c);
		return slideListAdapterInstance;
	}

	public SlidesListAdapter(MainActivity mainActivity) {
		// TODO Auto-generated constructor stub
		context = mainActivity;

		//Contact the restURL and get the JSON array
		try {
			new PptListRetriever(context,SlidesListAdapter.this).execute(MainActivity.username);      

		} catch(Exception e){
			e.printStackTrace();
		}


		inflater = (LayoutInflater)context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void pptListRetrieved(JSONArray pptList){
		try{
			if(pptList!=null){
				int numOfPpts = pptList.length();
				mThumbIds=new Bitmap[numOfPpts];
				pptNames = new String[numOfPpts];
				MainActivity.pptBeanMap = new LinkedHashMap<Integer,Object>();
				MainActivity.uploadBeanMap = new HashMap<Integer,UploadBean>();

				numSlides = new int[numOfPpts];
				numViewers = new int[numOfPpts];
				docId = new int[numOfPpts];

				for(int i=0;i<numOfPpts;i++){
					ArrayList<Rating> ratingArrayList = new ArrayList<Rating>();
					JSONObject obj = pptList.getJSONObject(i);
					String name = obj.getString("name");
					pptNames[i] = name;
					JSONArray thumbnailJsonArray = (JSONArray)obj.get("thumbnailUrlList");
					String[] thumbnailArray = new String[thumbnailJsonArray.length()];
					for(int j =0;j<thumbnailJsonArray.length();j++){
						thumbnailArray[j] = (String) thumbnailJsonArray.get(j);
					}
					System.out.println(thumbnailArray[0] + obj.getInt("docId"));
					if(thumbnailArray[0]!=null){
						mThumbIds[i]= CacheManager.getImage(thumbnailArray[0]);
					}
					JSONArray imageJsonArray = (JSONArray)obj.get("imageUrlList");
					String[] imageArray = new String[imageJsonArray.length()];
					for(int j =0;j<imageJsonArray.length();j++){
						imageArray[j] = (String) imageJsonArray.get(j);
					}

					JSONArray ratingsJsonArray = (JSONArray)obj.get("feedbackList");
					int numOfRatings = ratingsJsonArray.length();
					for(int j=0;j<numOfRatings;j++){
						JSONObject ratingObject = (JSONObject) ratingsJsonArray.get(j);
						int starRatings = 0;
						String comment = "";
						String createdBy = "";
						try{
							starRatings = ratingObject.getInt("starRating");
						}catch(JSONException e){
							e.printStackTrace();
						}
						try{
							comment = ratingObject.getString("comments");
						}catch(JSONException e){
							e.printStackTrace();
						}
						try{
							createdBy = ratingObject.getString("createdBy");
						}catch(JSONException e){
							e.printStackTrace();
						}

						Rating rating = new Rating(starRatings,comment,createdBy);
						ratingArrayList.add(rating);         	
					}

					Log.d(logtag, "put imageArray of size " +
							imageArray.length + " for " + name);


					numSlides[i] = obj.getInt("slideCount");


					boolean isFav = obj.getBoolean("isFavorite");
					Log.d(logtag,"Fav: " + obj.getBoolean("isFavorite") + ":" + isFav);

					//Getting number of viewers
					numViewers[i] = obj.getInt("viewCount");

					//Getting docid
					docId[i] = obj.getInt("docId");

					PptBean temp = new PptBean(name, thumbnailArray, imageArray, isFav,numSlides[i]);
					temp.setRatings(ratingArrayList);
					temp.setNumViewers(numViewers[i]);
					temp.setDocId(docId[i]);


					//Changing key to docid
					MainActivity.pptBeanMap.put(docId[i], temp);

					Log.d(logtag,"name: " + name);
					Log.d(logtag,"thumbnail Size: " + thumbnailArray.length);
					Log.d(logtag,"image Size: " + imageArray.length);
				}
				slideListAdapterInstance.notifyDataSetChanged();
			}
			else{
				Toast.makeText(context, "PPT List Null", Toast.LENGTH_SHORT).show();
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	@Override
	public int getCount() {
		if(mThumbIds != null)
			return mThumbIds.length;
		else 
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		// return position;
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public class Holder
	{
		TextView slideName;
		TextView numSlides;
		TextView numViewers;
		ImageView img;
		ImageView favbutton;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder=new Holder();
		View rowView;

		if (convertView != null) {
			rowView = convertView;
		} else {
			rowView = inflater.inflate(R.layout.slides_list, null);

			PptBean temp = ((PptBean) (MainActivity.pptBeanMap.get(docId[position])));
			LinearLayout pane = (LinearLayout) rowView.findViewById(R.id.slidePane);
			if(position!=MainActivity.sharedPosition)
				pane.setBackgroundColor(Color.parseColor("#3399FF"));
			else{


				if(temp!=null){
					pane.setBackgroundColor(Color.parseColor("#CCCCFF"));
					System.out.println("Updating ratings and interactions");
					context.updateRatingsView(docId[position]);

					// update interactions view
					context.updateInteractionsView(docId[position]);

					context.updateStatsView(docId[position]);
				}

			}


			holder.slideName = (TextView) rowView.findViewById(R.id.slideName);              
			holder.slideName.setText(pptNames[position]);

			holder.numSlides = (TextView) rowView.findViewById(R.id.numSlides);              
			holder.numSlides.setText(numSlides[position] + " slides");

			holder.img = (ImageView) rowView.findViewById(R.id.thumbnail);
			holder.img.setImageBitmap(mThumbIds[position]);
			// holder.img.setTag(pptNames[position]);
			rowView.setTag(docId[position]);





			holder.numViewers = (TextView) rowView.findViewById(R.id.numViewers);
			if(temp!=null){
				holder.numViewers.setText(temp.getNumViewers() + " viewers");
			}
			holder.favbutton = (ImageView) rowView.findViewById(R.id.favButton);
			if((temp!=null) && (temp.isFav())){       	 
				holder.favbutton.setImageResource(R.drawable.fav_selected);
				holder.favbutton.setTag(R.string.key_1,true);
			}
			else{       	
				holder.favbutton.setImageResource(R.drawable.fav_unselected);
				holder.favbutton.setTag(R.string.key_1,false);
			}  
			holder.favbutton.setTag(docId[position]);
			//Set onclick listener?
			holder.favbutton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					int docId = (Integer) v.getTag();

					PptBean temp = new PptBean();
					temp =(PptBean)MainActivity.pptBeanMap.get(docId);
					UploadBean upTemp = (UploadBean) MainActivity.uploadBeanMap.get(docId);
					if(upTemp == null){
						upTemp = new UploadBean();
						upTemp.setDocId(docId);
					}
					boolean flag = false;
					if ((v.getTag(R.string.key_1)) != null){
						flag = ((Boolean) v.getTag(R.string.key_1));
					}
					// Perform action on clicks, depending on whether it's now checked
					if (!flag) {
						//Was not a favorite before. User clicked and has to now become favorite
						//User marked as favorite
						//Edit the local memory
						temp.setFav(true);
						((ImageView)v).setImageResource(R.drawable.fav_selected);
						v.setTag(R.string.key_1,true);

						MainActivity.pptBeanMap.put(temp.getDocId(), temp);

						//Change history		
						upTemp.setFav(true);
						MainActivity.uploadBeanMap.put(docId, upTemp);

					} else {
						//User un-marked as favorite
						//Edit the local memory
						temp.setFav(false);
						((ImageView)v).setImageResource(R.drawable.fav_unselected);
						v.setTag(R.string.key_1,false);
						MainActivity.pptBeanMap.put(temp.getDocId(), temp);


						//Change history
						upTemp.setFav(false);
						MainActivity.uploadBeanMap.put(docId, upTemp);

					}
				}
			});
			// 	    if(position==0){
			// 	    	  context.select0Ppt();
			// 	      }
		}

		return rowView;
	}


}
