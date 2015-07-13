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

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountAuthenticatorActivity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent; 
import android.net.Uri;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/**
 * 
 * This class handles Twitter authentication using OAuth 1a.  To use this class, an app 
 * Consumer Key and Secret must be provided (see http://devcenter.kinvey.com/android/guides/users#twitter)
 * 
 * The activity extends the Android AccountAuthenticatorActivity and submits an OAuth request using the 
 * Signpost OAuth API.  Once a request token is received, the user is redirected to a Twitter 
 * Authorization URL to confirm the Authorization.  Upon Authorization, the Twitter API creates a callback
 * to the Activity, which is intercepted and a new Kinvey user is created.  
 */

public class TwitterLoginActivity extends AccountAuthenticatorActivity {
	
	private static final String TAG = TwitterLoginActivity.class.getSimpleName();
	/**
	 * Twitter Consumer Key and Secret - Specific to the application making the request
	 */
	private static final String TWITTER_CONSUMER_KEY = "your_twitter_consumer_key";
	private static final String TWITTER_CONSUMER_SECRET = "your_twitter_consumer_secret";
	/**
	 * The Callback URL is used by Twitter OAuth to return the authorization to this Activity.  The URL must be 
	 * set as an intent in the Application manifest as follows:
	 * 
	 * <intent-filter> 
     *           <action android:name="android.intent.action.VIEW"/> 
     *           <category android:name="android.intent.category.DEFAULT"/> 
     *           <category android:name="android.intent.category.BROWSABLE"/> 
     *          <data android:scheme="kinveysociallogin" android:host="twitteractivity"/> 
	 * </intent-filter>
	 * 
	 * The scheme and host should be modified to identify the specific application.  
	 * 
	 */
	private final String CALLBACKURL = "kinveysociallogin://twitteractivity"; 
	private static final String  CALLBACK_SCHEME = "kinveysociallogin";
	
	/**
	 * URLs for accessing Twitter OAuth
	 */
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";
	
	/**
	 * Configuration parameters for Android's AbstractAuthenticator
	 */
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_AUTHTOKENTYPE="authtokenType";

	/**
	 * Defaults for Authenticator
	 */
	private Boolean mConfirmCredentials = false;
	private Boolean mRequestNewAccount = true;
	
	/**
	 * Android AccountManager object
	 */
	private AccountManager mAccountManager;
	 
	/**
	 * Configuration parameters for Android's AbstractAuthenticator
	 */
	private static final String PARAM_LOGIN_TYPE_TWITTER = "twitter";

	private ProgressDialog mProgressDialog = null;
	
	/**
	 * Signpost Consumer and Provider
	 */
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	
	/** 
	 * Kinvey Client
	 */
	private static Client kinveyClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the Android Account Manager
		mAccountManager = AccountManager.get(this);
		
		// Get an instance of the Kinvey client
		kinveyClient = ((UserLogin) getApplication()).getKinveyService();
		
		setContentView(R.layout.activity_twitter_login);

		try {
			// Sets Consumer and Provider for OAuth
			this.consumer = new CommonsHttpOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
			this.provider = new CommonsHttpOAuthProvider(REQUEST_URL,ACCESS_URL,AUTHORIZE_URL);
		} catch (Exception e) {
			String message = "Error creating consumer / provider";
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			Log.e(TAG, message,e);
		}	
			Log.i(TAG, "Starting task to retrieve request token.");
			
			showProgress();
			
			new OAuthRequestTokenTask(this,consumer,provider).execute();

	}
	
	/**
	 * Called following a successful KinveyLogin to process the result and persist to AccountManager
	 */
	private void onAuthenticationResult(String authToken, String password) {
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
	
	
	/**
	 * Called as a result of a Kinvey Authentication if credentials needed to be confirmed 
	 * (needed for Android Account Manager in case credentials change/expire.)
	 */
	private void finishConfirmCredentials(boolean result, String authKey, String password) {
		final Account account = new Account(authKey, UserLogin.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, password);
		mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_TWITTER);
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setResult(RESULT_OK, intent);
	}
	
	/**
	 * Finishes the login process by creating/updating the account with the Android
	 * AccountManager.  
	 */
	private void finishLogin(String authToken, String password) {
		final Account account = new Account(authToken, UserLogin.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			Bundle userData = new Bundle();
			userData.putString(UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_TWITTER);
			mAccountManager.addAccountExplicitly(account, password, userData);
		} else {
			mAccountManager.setPassword(account, password);
			mAccountManager.setUserData(account, UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_TWITTER);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, authToken);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLogin.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK,intent);
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
	
	/**
	 * Intercepts new Intents and retrieves the Access Token if the intent uses the Callback Uri
	 */
    @Override 
    protected void onNewIntent(Intent intent) { 

    	super.onNewIntent(intent); 
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(consumer,provider).execute(uri);
		}
    } 
	
    /**
     * AsyncTask to retrieve the Request Token
     */
	private class OAuthRequestTokenTask extends AsyncTask<Void, Void, Void> {
		
		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		
		public OAuthRequestTokenTask(Context context,OAuthConsumer consumer,OAuthProvider provider) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
		}
		
		 @Override
	     protected Void doInBackground(Void... params) {

			try {
				Log.i(TAG, "Retrieving request token from Google servers");
				final String url = provider.retrieveRequestToken(consumer, CALLBACKURL);
				Log.i(TAG, "Popping a browser with the authorize URL : " + url);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
				context.startActivity(intent);
			} catch (Exception e) {
				Log.e(TAG, "Error during OAUth retrieve request token", e);
			}
			return null;

		 }
	 }
	 
	/**
	 * AsyncTask to retrieve the Access Token
	 */
	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private OAuthProvider provider;
		private OAuthConsumer consumer;

		public RetrieveAccessTokenTask(OAuthConsumer consumer,OAuthProvider provider) {
			this.consumer = consumer;
			this.provider = provider;
		}


		@Override
		protected Void doInBackground(Uri...params) {
			final Uri uri = params[0];
			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);
	
				loginTwitterKinveyUser(consumer.getToken(),consumer.getTokenSecret());

				Log.i(TAG, "OAuth - Access Token Retrieved");

			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}
			return null;
		}
		/**
		 * 
		 * Method to log the twitter Kinvey user, passing a KinveyCallback.  
		 */
		private void loginTwitterKinveyUser(String accessToken, String accessSecret) {
				
				kinveyClient.user().loginTwitter(accessToken, accessSecret, TWITTER_CONSUMER_KEY,
                        TWITTER_CONSUMER_SECRET, new KinveyUserCallback() {

                    public void onFailure(Throwable e) {
                        CharSequence text = "Wrong username or password";
                        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                        toast.show();
                    }

                    ;

                    @Override
                    public void onSuccess(User u) {
                        CharSequence text = "Logged in " + u.get("username") + ".";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                        onAuthenticationResult(u.getId(), u.getAuthToken());
                        TwitterLoginActivity.this.startActivity(new Intent(TwitterLoginActivity.this, MainActivity.class));
                        TwitterLoginActivity.this.finish();
                    }
                });
			}
	}	
	
}
