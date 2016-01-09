package com.oracle.play.pptviewer;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;


import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


public class UserNameRetriever extends AsyncTask<Object, Void, Void> {

	private static String logtag = "UserName Retriever";//for use as the tag when logging
	private static String strurl = "http://slcai764.us.oracle.com:8081/PPTRestService/rest/getUser/";
	private static String response;
	private LoginActivity context;
	private static String username;
	
	//This function makes the rest call to retrieve the json object that has all the ppt lists
	@Override
	protected Void doInBackground(Object... params) {
		try{
			username = (String)params[0];
			context = (LoginActivity) params[1];
			String finalUrl = strurl + username;
			Log.d(logtag, "Final Sync URL:" + finalUrl);
			URL url = new URL(finalUrl);
			URLConnection connection = url.openConnection();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(connection.getInputStream())));
			StringBuffer output = new StringBuffer();
			System.out.println("Output from Server .... \n");
			String line;
			while ((line = br.readLine()) != null) {
				output.append(line);
			}
			response = output.toString();
			Log.d(logtag,"User name retrieved " + response);
			br.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	 protected void onPostExecute(Void result) {
		Intent activityChangeIntent = new Intent(context, MainActivity.class);
        activityChangeIntent.putExtra("displayUsername",response);   
        activityChangeIntent.putExtra("username",username);   
        context.startActivity(activityChangeIntent);
        context.finish();
       
    }

}
