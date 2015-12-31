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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;

import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.textuality.authorized.AuthorizedActivity;

import java.util.Arrays;

/**
 * This class handles Facebook authentication using OAuth 2.  To use this class, an application ID
 * must be set up and obtained from Facebook (see http://devcenter.kinvey.com/android/guides/users#facebook)
 * 
 * The activity extends the Android AccountAuthenticatorActivity and submits an OAuth request using the 
 * Facebook SDK for Android.  Upon Authorization, a new Kinvey user is created.  
 */
public class FacebookLoginActivity extends AccountAuthenticatorActivity {

	/*
	 * Facebook App ID - Specific to the applicaiton making the request.
	 */

	private Boolean mConfirmCredentials = false;
	private Boolean mRequestNewAccount = true;
	private AccountManager mAccountManager;
	
	private ProgressDialog mProgressDialog = null;
	private static final String PARAM_LOGIN_TYPE_FACEBOOK = "facebook";

	private FacebookSdk facebookSdk;
    private CallbackManager callbackManager;

    private ProgressDialog loginProgressDialog;
	/** 
	 * Kinvey Client
	 */

    private FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            if (loginProgressDialog != null && loginProgressDialog.isShowing()) {
                loginProgressDialog.dismiss();
            }
            Toast.makeText(FacebookLoginActivity.this, "Logged in with Facebook.",
                    Toast.LENGTH_LONG).show();

            loginFacebookKinveyUser(loginProgressDialog, loginResult.getAccessToken().getToken());
        }

        @Override
        public void onCancel() {
            Toast.makeText(FacebookLoginActivity.this, "FB login cancelled",
									Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(FacebookException error) {
            Log.i("Kinvey - SignIn",error.getMessage());
        }
    };

	private Client kinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_login);
		
		mAccountManager = AccountManager.get(this);
		
		mAccountManager = AccountManager.get(this);	
		
		kinveyClient = ((UserLogin) getApplication()).getKinveyService();

		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(
				FacebookLoginActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");
		FacebookSdk.sdkInitialize(this);
		doFacebookSso(progressDialog, savedInstanceState);
		callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, facebookCallback);
	}
	
	/**
	 * Facebook SSO Oauth
	 */
    private void doFacebookSso(final ProgressDialog progressDialog, final Bundle savedInstanceState){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    
    /*
     * Login a Kinvey User with Faceook credentials
     */
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {
	
		kinveyClient.user().loginFacebook(accessToken, new KinveyUserCallback() {

            @Override
            public void onFailure(Throwable e) {
                CharSequence text = "Wrong username or password";
                Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }

            @Override
            public void onSuccess(User u) {
                CharSequence text = "Logged in.";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                onAuthenticationResult(u.getId(), u.getAuthToken());
                FacebookLoginActivity.this.startActivity(new Intent(FacebookLoginActivity.this, MainActivity.class));
                FacebookLoginActivity.this.finish();
            }
        });
		
	}
	
	/**
	 * Called as a result of a Kinvey Authentication if credentials needed to be confirmed 
	 * (needed for Android Account Manager in case credentials change/expire.)
	 */
	private void finishConfirmCredentials(boolean result, String authKey, String password) {
		final Account account = new Account(authKey, UserLogin.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, password);
		mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_FACEBOOK);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void registerAccount(View v) {
		Intent intent = new Intent(this, RegisterNewAccountActivity.class);
        startActivity(intent);
	}
	
	/**
	 * Finishes the login process by creating/updating the account with the Android
	 * AccountManager.  
	 */
	private void finishLogin(String authToken, String password) {
		final Account account = new Account(authToken, UserLogin.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			Bundle userData = new Bundle();
			userData.putString(UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_FACEBOOK);
			mAccountManager.addAccountExplicitly(account, password, userData);
		} else {
			mAccountManager.setPassword(account, password);
			mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_FACEBOOK);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, authToken);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLogin.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK,intent);
		finish();
	}
	
	/**
	 * Called following a successful KinveyLogin to process the result and persist to AccountManager
	 */
	public void onAuthenticationResult(String authToken, String password) {
		boolean success = ((authToken != null) && (authToken.length()>0));
		hideProgress();
		
		if(success) {
			if (!mConfirmCredentials) {
				finishLogin(authToken, password);
			} else {
				finishConfirmCredentials(success, authToken, password);
			}
		} else {
			Toast.makeText(this, "Login Failed", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Authenticating...");
		dialog.setIndeterminate(true);
		
		mProgressDialog = dialog;
		return dialog;		
	}
	
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

	protected void error(ProgressDialog progressDialog, String error) {
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
        Log.d(AuthorizedActivity.TAG, "Error " + error);
	}
}
