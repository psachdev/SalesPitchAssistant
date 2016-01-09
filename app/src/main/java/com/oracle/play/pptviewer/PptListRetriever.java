package com.oracle.play.pptviewer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;

import android.app.ActionBar.LayoutParams;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class PptListRetriever extends AsyncTask<String, Void, JSONArray> {

	private static String logtag = "PPTListRetriever";//for use as the tag when logging
	private static String strurl = "http://slcai764.us.oracle.com:8081/PPTRestService/rest/pptList_new/";
	//private static String strurl = "http://slcai764.us.oracle.com:8081/pptList.txt";
	//This function makes the rest call to retrieve the json object that has all the ppt lists

	Context context;
	SlidesListAdapter slidesListAdapter;
	//private ProgressDialog mDialog;
	ProgressDialog progDailog;

	public PptListRetriever(Context context, SlidesListAdapter slidesListAdapter) {
		this.slidesListAdapter = slidesListAdapter;
		this.context = context;
	}

	@Override
	protected JSONArray doInBackground(String... username) {
		try{
			URL url = new URL(strurl + username[0]);
			System.out.println(strurl + username[0]);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Accept", "application/json");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(connection.getInputStream())));
			StringBuffer output = new StringBuffer();
			System.out.println("Output from Server .... \n");
			String line;
			while ((line = br.readLine()) != null) {
				output.append(line);
			}
			String response = output.toString();
			JSONArray pptList = new JSONArray(response);
			Log.d(logtag,"Received json object " + pptList);
			br.close();
			return pptList;
		}catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPreExecute() 
	{
		Log.d(logtag,"Pre execute");
		super.onPreExecute();
		
		progDailog = new ProgressDialog(context,R.style.ProgressDialogScheme);
        progDailog.setMessage("");
        progDailog.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
        
        progDailog.getWindow().setGravity(Gravity.LEFT);
        android.view.WindowManager.LayoutParams params = progDailog.getWindow().getAttributes();
        params.x = 900;
        progDailog.getWindow().setAttributes(params);
        
        progDailog.show();
	}

	@Override
	protected void onPostExecute(JSONArray result) 
	{
		super.onPostExecute(result);
		if(progDailog!=null && progDailog.isShowing())
		{
			progDailog.dismiss();
		}
		Log.d(logtag,"Post execute");
		slidesListAdapter.pptListRetrieved(result);
		
	}

}
