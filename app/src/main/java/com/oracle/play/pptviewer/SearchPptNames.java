package com.oracle.play.pptviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Set;

import android.util.Log;

public class SearchPptNames {

	private static String logtag = "SearchPptNames";

	/*This function does the local search
	 *  Input - Context of main activity and search string
	 *  Output - returns a list of names that matched the search string
	 *  
	 *  How to use example: SearchPptNames.getPptList(context, "Tools");
	 */
	public static ArrayList<String> getPptList(String search_string){
		ArrayList<String> matchedPptNames = new ArrayList<String>();
		LinkedHashMap<Integer, Object> tempPptBeanMap = MainActivity.pptBeanMap;
		if(tempPptBeanMap!=null){
			Set<Integer> keys = tempPptBeanMap.keySet();
			Integer[] keyArray = keys.toArray(new Integer[keys.size()]);
			String[] pptNamesArray = new String[keyArray.length];
			for(int i=0;i<keyArray.length;i++){
				pptNamesArray[i] = ((PptBean)tempPptBeanMap.get(keyArray[i])).getPptName();
			}
			if(search_string.equalsIgnoreCase("")){
				//Return all
				for(String key:pptNamesArray){
					matchedPptNames.add(key);
				}
			}
			else{
				for(String key:pptNamesArray){
					if(key.toLowerCase().contains(search_string.toLowerCase())){
						matchedPptNames.add(key);
					}
				}
			}
		}
		Log.d(logtag, "Matched ppt names: " +matchedPptNames);
		return matchedPptNames;
	}

	public static ArrayList<String> getFavPptList(String search_string){
		ArrayList<String> matchedPptNames = new ArrayList<String>();
		LinkedHashMap<Integer, Object> tempPptBeanMap = MainActivity.pptBeanMap;

		if(tempPptBeanMap!=null){
			Set<Integer> keys = tempPptBeanMap.keySet();
			Integer[] keyArray = keys.toArray(new Integer[keys.size()]);
			String[] pptNamesArray = new String[keyArray.length];
			for(int i=0;i<keyArray.length;i++){
				pptNamesArray[i] = ((PptBean)tempPptBeanMap.get(keyArray[i])).getPptName();
				if(search_string.equalsIgnoreCase("")){
					matchedPptNames.add(pptNamesArray[i]);
				}
				else{
					if(pptNamesArray[i].toLowerCase().contains(search_string.toLowerCase())){
						PptBean temp = (PptBean) tempPptBeanMap.get(keyArray[i]);
						if(temp.isFav()){
							//Is a fav. Add to list
							matchedPptNames.add(pptNamesArray[i]);
						}
					}
				}
			}
		}
		Log.d(logtag, "Fav Matched ppt names: " +matchedPptNames);
		return matchedPptNames;
	}

}
