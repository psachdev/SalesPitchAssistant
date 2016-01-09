package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

import org.json.JSONArray;
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

public class SearchImageAdapter extends BaseAdapter {
	private static String logtag = "SlidesListAdapter"; //for use as the tag when logging

	private Context context;

	//references to our images
	private Bitmap[] mThumbIds;
	private String[] pptNames;
	private int[] numSlides;
	private int[] numViewers;
	private int[] docId;



	private static LayoutInflater inflater=null;


	public SearchImageAdapter(MainActivity mainActivity, ArrayList<String> matchedNames) {

		context = mainActivity;
		if(matchedNames.size()!=0){
			int numOfPpts = matchedNames.size();
			mThumbIds=new Bitmap[numOfPpts];
			pptNames = new String[numOfPpts];
			numSlides = new int[numOfPpts];
			docId = new int[numOfPpts];
			numViewers = new int[numOfPpts];

			Set<Integer> keys = MainActivity.pptBeanMap.keySet();
			Integer[] keyArray = keys.toArray(new Integer[keys.size()]);
			int i = 0;
			for(int j=0;j<keyArray.length;j++){
				PptBean obj = (PptBean) MainActivity.pptBeanMap.get(keyArray[j]);
				if(matchedNames.contains(obj.getPptName())){
					//This ppt is part of search results
					String name = obj.getPptName();
					pptNames[i] = name;

					String[] thumbnailArray = obj.getThumbnailURL();

					if(thumbnailArray[0]!=null){
						mThumbIds[i]= CacheManager.getImage(thumbnailArray[0]);
					}

					String[] imageArray = obj.getImageURL();

					
					numSlides[i] = obj.getSlideCount();


					boolean isFav = obj.isFav();
					Log.d(logtag,"Fav: " + obj.isFav() + ":" + isFav);

					docId[i] = obj.getDocId();
					//Getting number of viewers
					numViewers[i] = (int) obj.getNumViewers();

					//					PptBean temp = new PptBean(name, thumbnailArray, imageArray, isFav);
					//					temp.setDocId(docId[i]);
					//					MainActivity.pptBeanMap.put(docId[i], temp);

					Log.d(logtag,"name: " + name);
					Log.d(logtag,"thumbnail Size: " + thumbnailArray.length);
					Log.d(logtag,"image Size: " + imageArray.length);
					//Increment i
					i++;
				}
			}

		}
		else{
			Toast.makeText(context, "No results matched Search", Toast.LENGTH_SHORT).show();
			((MainActivity)context).updateInteractionsView(0);
			((MainActivity)context).updateRatingsView(0);
			((MainActivity)context).updateStatsView(0);
		}


		inflater = (LayoutInflater)context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
					((MainActivity) context).updateRatingsView(docId[position]);

					// update interactions view
					((MainActivity) context).updateInteractionsView(docId[position]);
					
					((MainActivity) context).updateStatsView(docId[position]);
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
	        holder.numViewers.setText(temp.getNumViewers() + " viewers");
	         
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
		}

		return rowView;
	}


}
