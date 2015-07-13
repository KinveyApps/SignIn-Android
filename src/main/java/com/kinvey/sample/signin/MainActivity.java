/** 
 * Copyright (c) 2014 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.kinvey.sample.signin;

import com.google.api.client.http.HttpTransport;

import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launch Activity checks to see if a KinveyUser exists as a
 * in the Android native AccountManager.  If user exists,  
 * a welcome message is displayed.  Otherwise, user is 
 * re-directed to the login screen.
 * 
 * Kinvey user credentials can be created by creating an 
 * account and logging in directly, logging in via 
 * Twitter, Facebook, or Google.  
 * 
 * Once a user successfully authenticates, the credentials
 * are saved in the AccountManager.  
 */
public class MainActivity extends Activity {
	private TextView tvHello;

    private static final Level LOGGING_LEVEL = Level.FINEST;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        Logger.getLogger(HttpTransport.class.getName()).setLevel(LOGGING_LEVEL);



        tvHello = (TextView) findViewById(R.id.tvHello);
		if (loggedIn()) {
			tvHello.setText("Hello!  You are logged in!");
		} else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
		
	}
	
	private boolean loggedIn() {
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(UserLogin.ACCOUNT_TYPE);
		
		if (accounts.length > 0) {
			return true;
		} else {
			return false;
		}
	}
}
