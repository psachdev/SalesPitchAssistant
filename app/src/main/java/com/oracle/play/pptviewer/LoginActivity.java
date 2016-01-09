package com.oracle.play.pptviewer;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import android.view.View.OnClickListener;


public class LoginActivity extends Activity{
	private Button login;
	private EditText username;
	private EditText password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		login=(Button)findViewById(R.id.Loginbutton);
		username=(EditText)findViewById(R.id.username);
		password=(EditText)findViewById(R.id.password);

		username.setText("Sales_admin");
		password.setText("Welcome1");
		login.setFocusable(true);
		login.setFocusableInTouchMode(true);
		login.requestFocus();

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {            
				int k=check();
				if(k==1)                
				{
					Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
					Intent activityChangeIntent = new Intent(LoginActivity.this, MainActivity.class);
					activityChangeIntent.putExtra("username",(String)username.getText().toString());   
					LoginActivity.this.startActivity(activityChangeIntent);
					finish();
					
//					try {
//						new UserNameRetriever().execute(username.getText().toString(),LoginActivity.this).get();
//					} catch (Exception e) {
//						e.printStackTrace();
//					} 
				}
				else
				{
					username.setText("");
					password.setText("");
					Toast.makeText(LoginActivity.this, "Authentication Failed...", Toast.LENGTH_SHORT).show();
				}   
			}
		});
	}

	private int check() {
		// read in username and password
		String a = username.getText().toString();
		String b = password.getText().toString();      

		// Always succeeds, until REST interface to check authentication is available
		return 1;
	}
}