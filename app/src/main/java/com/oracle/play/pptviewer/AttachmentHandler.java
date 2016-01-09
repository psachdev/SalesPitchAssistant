package com.oracle.play.pptviewer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.*;
import android.provider.MediaStore;

public class AttachmentHandler extends Activity {

	private AttachmentHandler context = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		//setContentView(R.layout.activity_attachment__handler);
		String errormsg=null;
		Uri data = getIntent().getData();
		if(data!=null) {
			getIntent().setData(null);
			try {
				importData(data);
			} catch (Exception e) {
				// warn user about bad data here
				errormsg = e.getMessage();
			} finally{        	
				if(errormsg != null){
					createErrorDiaglog(errormsg);
				}else{
					createStartActivityDiaglog();
				}	  	    	
			}
		}
	}

	private void importData(Uri data) {
		final String scheme = data.getScheme();	
		final String filename = this.getContentName(this.getContentResolver(), data);

		if(ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			try {
				ContentResolver cr = this.getContentResolver();
				InputStream is = cr.openInputStream(data);
				if(is == null) return;

				StringBuffer buf = new StringBuffer();			
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				if((this.isExternalStorageReadable()) && (this.isExternalStorageWritable())){
					File publicdir = this.getAlbumStorageDir("sample");
					String publicdirpath = publicdir.getAbsolutePath();
					String filepath = publicdirpath + "/" + filename;
					OutputStream outputStream = new FileOutputStream(new File(filepath));        	 
					int read = 0;
					byte[] bytes = new byte[1024];
					while ((read = is.read(bytes)) != -1) {
						outputStream.write(bytes, 0, read);
					}
					outputStream.close();
					//new UploadFileTask().execute(filepath);
					//Uploading data
					final String filepathonphone = filepath;
					File myFile = new File(filepathonphone);
					RequestParams params = new RequestParams();
					try {
					    params.put("file", myFile);
					} catch(FileNotFoundException e) {
						e.printStackTrace();
					}					
					AsyncHttpClient client = new AsyncHttpClient();
					client.post("http://slcai764:8081//PPTRestService/rest/pptUpload", params, new AsyncHttpResponseHandler() {
					    @Override
					    public void onStart() {
					        // called before request is started
					    	
					    }
					    
					    @Override
					    public void onRetry(int retryNo) {
					        // called when request is retried
					    }

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
							// TODO Auto-generated method stub	
							Log.e("LAYOUT_TUTORIAL", "Data sending failed");
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
							// TODO Auto-generated method stub			
							Log.e("LAYOUT_TUTORIAL", "Data sent successfully");
						}
					});
				}
				is.close();
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				return;
			}
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File getAlbumStorageDir(String albumName) {
		// Get the directory for the user's public pictures directory. 
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), albumName);
		if (!file.mkdirs()) {
			Log.e("LAYOUT_TUTORIAL", "Directory not created");
		}
		return file;
	}
	
	private void createStartActivityDiaglog(){
		// custom dialog
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.custom);
		dialog.setTitle("UPLOAD STATUS");
		

		//ImageView image = (ImageView) dialog.findViewById(R.id.dialogimage);
		//image.setImageResource(R.drawable.oracle_logo_50_50);
		
		//TextView titletext = (TextView) dialog.findViewById(R.id.title);
		//titletext.setText("");		

		// set the custom dialog components - text, image and button
		TextView dialogtext = (TextView) dialog.findViewById(R.id.dialogtext);
		dialogtext.setText("Data upload complete. Do you want to launch LightBox Viewer?");

		Button dialogButtonpos = (Button) dialog.findViewById(R.id.positiveButton);
		dialogButtonpos.setText("YES");
		// if button is clicked, close the custom dialog
		dialogButtonpos.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				Intent activityChangeIntent = new Intent(context, MainActivity.class);
				activityChangeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				activityChangeIntent.putExtra("call_refresh", true);
				startActivity(activityChangeIntent);
				
				context.finish();
			}
		});
		
		Button dialogButtonneg = (Button) dialog.findViewById(R.id.negativeButton);
		dialogButtonneg.setText("DISMISS");
		// if button is clicked, close the custom dialog
		dialogButtonneg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				context.finish();
			}
		});

		dialog.show();
	}

	private void createErrorDiaglog(String message){
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Data Upload Failure");
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "DISMISS", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				context.finish();
			}
		});
		alertDialog.setIcon(R.drawable.oracle_logo_50_50);
		alertDialog.show();
	}
	
	
	/* http://developer.android.com/guide/topics/providers/content-provider-basics.html */
	private String getContentName(ContentResolver resolver, Uri uri){
		Cursor cursor = resolver.query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
		cursor.moveToFirst();
		int nameIndex = cursor.getColumnIndex(cursor.getColumnNames()[0]);
		if (nameIndex >= 0) {
			return cursor.getString(nameIndex);
		} else {
			return null;
		}
	}
}
