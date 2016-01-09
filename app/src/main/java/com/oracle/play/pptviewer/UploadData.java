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

import android.os.AsyncTask;
import android.util.Log;


public class UploadData extends AsyncTask<String, Void, Boolean> {

	private static String logtag = "UploadData";//for use as the tag when logging
	private static String strurl = "http://slcai764.us.oracle.com:8081/PPTRestService/rest/syncData";
	
	//This function makes the rest call to retrieve the json object that has all the ppt lists
	@Override
	protected Boolean doInBackground(String... params) {
		try{
			String jsonData = params[0];
			List<NameValuePair> listparams = new LinkedList<NameValuePair>();
			listparams.add(new BasicNameValuePair("syncData", jsonData));
			String paramString = URLEncodedUtils.format(listparams, "utf-8");
			String finalUrl = strurl + "?" + paramString;
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
			String response = output.toString();
			Log.d(logtag,"Upload Data response " + response);
			br.close();
			if(response.equalsIgnoreCase("SUCCESS"))
				return true;
		}catch(Exception e){
			e.printStackTrace();
		}

		return false;
	}


}
