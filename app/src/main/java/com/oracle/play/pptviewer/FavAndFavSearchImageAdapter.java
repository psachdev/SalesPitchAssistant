package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.oracle.play.pptviewer.SlidesListAdapter.Holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FavAndFavSearchImageAdapter extends BaseAdapter {


	private static String logtag = "FavAndFavSearchImageAdapter";//for use as the tag when logging
	private MainActivity context;
	// references to our images
	private ArrayList<Bitmap> mThumbIds;
	private ArrayList<String> pptNames;
	private ArrayList<Integer> numSlides;
	private ArrayList<Long> numViewers;
	private ArrayList<Integer> docId;

	private static LayoutInflater inflater=null;
	GridView gridView;
	ArrayList<String> searchMatchNames;

	public FavAndFavSearchImageAdapter(MainActivity c, GridView gridview, ArrayList<String> matchedNames){
		context = c;
		gridView = gridview;


		mThumbIds=new ArrayList<Bitmap>();
		pptNames = new ArrayList<String>();
		numSlides = new ArrayList<Integer>();
		numViewers = new ArrayList<Long>();
		docId = new ArrayList<Integer>();
		Integer[] keyArray;


		if(matchedNames==null){
			//For all Fav
			Set<Integer> keys = MainActivity.pptBeanMap.keySet();
			keyArray = keys.toArray(new Integer[0]);
			searchMatchNames = null;

		} else{
			if(matchedNames.size()==0){
				((MainActivity)context).updateInteractionsView(0);
				((MainActivity)context).updateRatingsView(0);
				((MainActivity)context).updateStatsView(0);
			}
			searchMatchNames = new ArrayList<String>(matchedNames);
			Set<Integer> keys = MainActivity.pptBeanMap.keySet();
			Integer[] tempKeyArray = keys.toArray(new Integer[0]);
			keyArray = new Integer[matchedNames.size()];
			int i=0;
			for(Integer tempKey: tempKeyArray){
				PptBean tempBean = (PptBean) MainActivity.pptBeanMap.get(tempKey);
				if((tempBean!=null) && (tempBean.isFav())){
					
					if(matchedNames.contains(tempBean.getPptName())){
						System.out.println("I =  " + i);
						keyArray[i] = tempBean.getDocId();
						i++;

					}
				}
			}


		}
		for(Integer key:keyArray){
			PptBean temp = (PptBean) MainActivity.pptBeanMap.get(key);

			if((temp!=null) && (temp.isFav())){
				//Is a favorite
				String name = temp.getPptName();
				pptNames.add(name);
				String[] thumbnailArray = temp.getThumbnailURL();

				if(thumbnailArray[0]!=null){
					mThumbIds.add(CacheManager.getImage(thumbnailArray[0]));
				}


				numSlides.add(temp.getSlideCount());

				numViewers.add(temp.getNumViewers());
				docId.add(temp.getDocId());
				//				Log.d(logtag," Fav name: " + name);
				//				Log.d(logtag," Fav thumbnail Size: " + thumbnailArray.length);

			}
		}
		inflater = (LayoutInflater)context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		if(mThumbIds != null)
			return mThumbIds.size();
		else 
			return 0;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
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

			PptBean temp = ((PptBean) (MainActivity.pptBeanMap.get(docId.get(position))));

			LinearLayout pane = (LinearLayout) rowView.findViewById(R.id.slidePane);
			if(position!=MainActivity.sharedPosition)
				pane.setBackgroundColor(Color.parseColor("#3399FF"));
			else{


				if(temp!=null){

					pane.setBackgroundColor(Color.parseColor("#CCCCFF"));
					System.out.println("Updating ratings and interactions");
					context.updateRatingsView(docId.get(position));

					// update interactions view
					context.updateInteractionsView(docId.get(position));

					context.updateStatsView(docId.get(position));
				}

			}

			holder.slideName = (TextView) rowView.findViewById(R.id.slideName);              
			holder.slideName.setText(pptNames.get(position));


			holder.numSlides = (TextView) rowView.findViewById(R.id.numSlides);              
			holder.numSlides.setText(numSlides.get(position) + " slides");

			holder.img = (ImageView) rowView.findViewById(R.id.thumbnail);
			holder.img.setImageBitmap(mThumbIds.get(position));
			// holder.img.setTag(pptNames[position]);
			rowView.setTag(docId.get(position));



			holder.numViewers = (TextView) rowView.findViewById(R.id.numViewers);   
			if(temp!=null)
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
			holder.favbutton.setTag(docId.get(position));

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
						/*
						 * This situation will not occur since obviously everything on Fav page will
						 * be a fav but still coding it.
						 */
						//Was not a favorite before. User clicked and has to now become favorite
						//User marked as favorite
						//Edit the local memory
						temp.setFav(true);

						MainActivity.pptBeanMap.put(temp.getDocId(), temp);

						//Change history	
						upTemp.setFav(true);
						MainActivity.uploadBeanMap.put(docId, upTemp);


						//Need to reload page to remove the clicked item
						gridView.setAdapter(new FavAndFavSearchImageAdapter(context, gridView,searchMatchNames));

					} else {
						//User un-marked as favorite
						//Edit the local memory
						temp.setFav(false);

						MainActivity.pptBeanMap.put(temp.getDocId(), temp);


						//Change history
						upTemp.setFav(false);
						MainActivity.uploadBeanMap.put(docId, upTemp);


						//Need to reload page to remove the clicked item
						//Remove it from search List if this is for a search
						if(searchMatchNames!=null){
							searchMatchNames.remove(temp.getPptName());
							System.out.println("Removing " + temp.getPptName());
						}
						gridView.setAdapter(new FavAndFavSearchImageAdapter(context, gridView,searchMatchNames));

					}
				}
			});

		}
		return rowView;
	}




}