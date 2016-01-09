package com.oracle.play.pptviewer;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class ImageRetriever extends AsyncTask<String, Void, Bitmap> {

	/**
	 * @param args
	 */
	 private static String logtag = "ImageRetriever";//for use as the tag when logging
	 
	 //This function takes the image url as input and returns the bitmap as output
	 
	@Override
	protected Bitmap doInBackground(String... strUrl) {
		Bitmap bitmap = null;
		Log.d(logtag, "Begin do in background");
		try{
			Log.d(logtag, "URL :" + strUrl[0]);
			URL url = new URL(strUrl[0]);
			URLConnection connection = url.openConnection();
			connection.setUseCaches(true);
			//Object response = connection.getContent();			
			InputStream in = connection.getInputStream();
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
			
			//}
			Log.d(logtag, "Bitmap received");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return bitmap;
	}
}
