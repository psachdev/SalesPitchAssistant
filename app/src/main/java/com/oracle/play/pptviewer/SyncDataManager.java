package com.oracle.play.pptviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class SyncDataManager {
	private static String logtag = "SyncDataManager";//for use as the tag when logging

	public static void UpdateFav(Context context,String pptName, boolean flag){
		JsonParser parser = new JsonParser();

		String fileName = "file.dat";

		/*
		 * Fav would be:
		 * [{name:"Name1", isFav:true},{name:"Name2",isFav:false}]
		 */
		try{

			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
			FileInputStream fis = context.openFileInput(fileName);
			int c;
			String temp = "";
			while ((c=fis.read()) != -1) {
				temp += Character.toString((char)c);
			}
			String data = new String(temp);
			fis.close();
			fos.close();
			
			
			if(data.length()>0){
				System.out.println("Data: " + data);
				Object obj = parser.parse(data);

				//if(!(obj.equals("JsonNull"))){
				JsonArray jsonArray = (JsonArray)obj;

				if(jsonArray!=null){
					Log.d(logtag," Reading current data" + jsonArray.toString());
					int numOfEntries = jsonArray.size();
					boolean matchFound = false;
					for(int i=0;i<numOfEntries;i++){
						JsonObject entry = (JsonObject) jsonArray.get(i);
						String name = entry.get("name").toString();
						if(name.equalsIgnoreCase("\"" + pptName + "\"")){
							//Name match
							matchFound = true;
							Boolean curFlag = entry.get("isFav").getAsBoolean();
							if(curFlag == flag){
								//It is the same, do nothing
							}
							else{
								JsonElement newFlag = new JsonPrimitive(flag);
								entry.remove("isFav");
								entry.add("isFav", newFlag);
								Log.d(logtag," Editing current data");
								break;
							}
						}

					}
					if(!matchFound){

						//Name does not exist. Add it
						Log.d(logtag, "No name found..adding it");
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty("name", pptName);
						jsonObject.addProperty("isFav", flag);
						jsonArray.add(jsonObject);


					}
					//All data has been parsed. Now need to write into the file
					String dataToWrite = jsonArray.toString();
					Log.d(logtag, "Writing to file: " + dataToWrite); 
					context.deleteFile(fileName);

					 fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
					fos.write(dataToWrite.getBytes());
					fos.close();
				}				
			}
			else{
				Log.d(logtag," File is empty");
				//File is empty has no data. Create data
				JsonArray jsonArray = new JsonArray();
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("name", pptName);
				jsonObject.addProperty("isFav", flag);
				jsonArray.add(jsonObject);

				//Write data to file
				String dataToWrite = jsonArray.toString();
				System.out.println("Data to write:" + dataToWrite);
				
				fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				fos.write(dataToWrite.getBytes());
				fos.close();



			}

		}catch(Exception e){
			e.printStackTrace();
		}

	}


}
