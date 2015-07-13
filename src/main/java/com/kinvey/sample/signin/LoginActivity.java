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

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

/**
 * 
 * This class handles Kinvey login authentication as well as storage of the User in Android's
 * AccountManager.  
 * 
 * The activity extends the Android AccountAuthenticatorActivity and prompts the user for a login (Email Address)
 * and password.  User also has the option to instead authenticate via Facebook or Twitter.  
 */
public class LoginActivity extends AccountAuthenticatorActivity {

	public static final String TAG = LoginActivity.class.getSimpleName();
	
	/**
	 * Configuration parameters for Android's AbstractAuthenticator
	 */
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_AUTHTOKENTYPE="authtokenType";
	
	/**
	 * Configuration parameters for Android's AbstractAuthenticator
	 */
	public static final String PARAM_LOGIN_TYPE_KINVEY = "kinvey";
	
	/**
	 * Android AccountManager object
	 */
	private AccountManager mAccountManager;
	
	private ProgressDialog mProgressDialog = null;
	
	public static final int MIN_USERNAME_LENGTH = 8;
	public static final int MIN_PASSWORD_LENGTH = 4;
	
	private Boolean mConfirmCredentials = false;
	
	/**
	 * KinveyClient
	 */
	protected Client kinveyClient;
	
	protected Button mButtonLogin;
	protected EditText mEditUserEmail;
	protected EditText mEditPassword;
	protected TextView mErrorMessage;
	protected String mUserEmail;
	protected String mPassword;
	
	protected Boolean mRequestNewAccount = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAccountManager = AccountManager.get(this);
		final Intent intent = getIntent();
		mUserEmail = intent.getStringExtra(PARAM_USERNAME);
		
		mRequestNewAccount = (mUserEmail == null);
		mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS,false);
		kinveyClient = ((UserLogin) getApplication()).getKinveyService();

		setContentView(R.layout.activity_login);
		
		mErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		mEditUserEmail = (EditText) findViewById(R.id.etEmailLogin);
		mEditPassword = (EditText) findViewById(R.id.etPassword);
	        	
    	mButtonLogin = (Button) findViewById(R.id.btnLogin);
        mEditUserEmail = (EditText) findViewById(R.id.etEmailLogin);
        mEditPassword = (EditText) findViewById(R.id.etPassword);
	}
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Authenticating...");
		dialog.setIndeterminate(true);
		
		mProgressDialog = dialog;
		return dialog;		
	}
	
	/**
	 * Method to handle Login button clicks - gets Username and Password and calls User Login method.  
	 */
	public void login(View view) {
		if (mRequestNewAccount) {
			mUserEmail = mEditUserEmail.getText().toString();
		}
		mPassword = mEditPassword.getText().toString();
		if (TextUtils.isEmpty(mUserEmail) || TextUtils.isEmpty(mPassword)) {
			mErrorMessage.setText("Please enter a valid username and password.");
		} else {
			showProgress();
			userLogin();
		}
	}
	
	public void launchTwitterLogin(View view) {
		LoginActivity.this.startActivity(new Intent(LoginActivity.this, TwitterLoginActivity.class));
        LoginActivity.this.finish();
	}
	
	public void launchFacebookLogin(View view) {
		LoginActivity.this.startActivity(new Intent(LoginActivity.this, FacebookLoginActivity.class));
        LoginActivity.this.finish();
	}

    public void launchGoogleLogin(View v){
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, GoogleLoginActivity.class));
        LoginActivity.this.finish();
    }

    public void launchLinkedInLogin(View v){
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, LinkedinLoginActivity.class));
        LoginActivity.this.finish();
    }

	public void registerAccount(View v) {
		Intent intent = new Intent(this, RegisterNewAccountActivity.class);
        startActivity(intent);
	}
	
	/**
	 * Called as a result of a Kinvey Authentication if credentials needed to be confirmed 
	 * (needed for Android Account Manager in case credentials change/expire.)
	 */
	private void finishConfirmCredentials(boolean result) {
		final Account account = new Account(mUserEmail, UserLogin.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, mPassword);
		mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_KINVEY);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	/**
	 * Finishes the login process by creating/updating the account with the Android
	 * AccountManager.  
	 */
	private void finishLogin(String authToken) {
		final Account account = new Account(mUserEmail, UserLogin.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			Bundle userData = new Bundle();
			userData.putString(UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_KINVEY);
			mAccountManager.addAccountExplicitly(account, mPassword, userData);
		} else {
			mAccountManager.setPassword(account, mPassword);
			mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_KINVEY);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserEmail);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLogin.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK,intent);
		finish();
	}
	
	/**
	 * Called following a successful KinveyLogin to process the result and persist to AccountManager
	 */
	public void onAuthenticationResult(String authToken) {
		boolean success = ((authToken != null) && (authToken.length()>0));
		hideProgress();
		
		if(success) {
			if (!mConfirmCredentials) {
				finishLogin(authToken);
			} else {
				finishConfirmCredentials(success);
			}
		} else {
			if (mRequestNewAccount) {
				mErrorMessage.setText("Please enter a valid username or password");
			} else {
				mErrorMessage.setText("Please enter a valid password");
			}
			
		}
	}
	
	
	// TODO:  Fix ShowDialog
	@SuppressWarnings("deprecation")
	public void showProgress() {
		showDialog(0);
	}
	
	private void hideProgress() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	/**
	 * 
	 * Method to log the twitter Kinvey user, passing a KinveyCallback.  
	 */
	public void userLogin() {
		kinveyClient.user().login(mEditUserEmail.getText().toString(), mEditPassword.getText().toString(), new KinveyUserCallback() {
            public void onFailure(Throwable t) {
                CharSequence text = "Wrong username or password";
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
                onAuthenticationResult(null);
            }

            public void onSuccess(User u) {
                CharSequence text = "Logged in.";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                onAuthenticationResult(u.getId());
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
                LoginActivity.this.finish();
            }

        });
	}
}