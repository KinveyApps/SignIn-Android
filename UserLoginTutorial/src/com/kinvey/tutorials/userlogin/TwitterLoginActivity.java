package com.kinvey.tutorials.userlogin;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider; 
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer; 
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;
import com.textuality.authorized.AuthorizedActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent; 
import android.net.Uri; 

public class TwitterLoginActivity extends Activity {
	
	private static final String TWITTER_CONSUMER_KEY = "YOvzeuvsi3NvBRErFBBzCA";
	private static final String TWITTER_CONSUMER_SECRET = "IC5OdCiIO4Be1TSdKKpcmproDVJLEFByxkK2l8T7Mo";
	private final String CALLBACKURL = "kinveysociallogin://twitteractivity"; 
	
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";
	
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_AUTHTOKENTYPE="authtokenType";

	private Boolean mConfirmCredentials = false;
	private Boolean mRequestNewAccount = false;
	private AccountManager mAccountManager;
	 
	final public static String  CALLBACK_SCHEME = "kinveysociallogin";

	private final String TAG = "TwitterActivity";
	private ProgressDialog mProgressDialog = null;
	
	private CommonsHttpOAuthConsumer consumer; 
	private OAuthProvider provider; 
	
	private static KCSClient mKinveyClient;
	
	String mUserEmail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAccountManager = AccountManager.get(this);
		final Intent intent = getIntent();
		mUserEmail = intent.getStringExtra(PARAM_USERNAME);
		
		mRequestNewAccount = (mUserEmail == null);
		mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS,false);
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();
		
		
		setContentView(R.layout.activity_twitter_login);
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();

		mAccountManager = AccountManager.get(this);


		try {
			this.consumer = new CommonsHttpOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
			this.provider = new CommonsHttpOAuthProvider(REQUEST_URL,ACCESS_URL,AUTHORIZE_URL);
		} catch (Exception e) {
			        Log.e(TAG, "Error creating consumer / provider",e);
		}
			Log.i(TAG, "Starting task to retrieve request token.");
			
			showProgress();
			
			new OAuthRequestTokenTask(this,consumer,provider).execute();

	}
	
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
				Toast.makeText(this, "Please enter a valid username or password.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Please enter a valid password.", Toast.LENGTH_SHORT).show();
			}
			
		}
	}
	
	private void finishConfirmCredentials(boolean result) {
		final Account account = new Account(mUserEmail, UserLoginTutorial.ACCOUNT_TYPE);
		mAccountManager.setUserData(account, "loginType", "kinvey");
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void finishLogin(String authToken) {
		final Account account = new Account(mUserEmail, UserLoginTutorial.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			mAccountManager.addAccountExplicitly(account, null, null);
		} else {
			mAccountManager.setPassword(account, mPassword);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserEmail);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLoginTutorial.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK,intent);
		finish();
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Authenticating...");
		dialog.setIndeterminate(true);
		
		mProgressDialog = dialog;
		return dialog;		
	}
	
	public void showProgress() {
		showDialog(0);
	}
	
	private void hideProgress() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
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
		
		 private void loginTwitterKinveyUser(String accessToken, String accessSecret) {
				
				mKinveyClient.loginWithTwitterAccessToken(accessToken, accessSecret, TWITTER_CONSUMER_KEY, 
						TWITTER_CONSUMER_SECRET, new KinveyCallback<KinveyUser>() {
					
					public void onFailure(Throwable e) {
						Log.e(TAG, "Failed Kinvey login", e);
				        //TextView tv = (TextView) findViewById(R.id.output);
				        String b = new String(e.getMessage());
				        Log.e(AuthorizedActivity.TAG, "Error: " + b);
			//	        tv.setText("DOH!  Great Scott!\nKinvey: " + b);	
					};
					
					@Override
					public void onSuccess(KinveyUser r) {
						//TextView output = (TextView) findViewById(R.id.output);
						StringBuffer strbuff = new StringBuffer();
						strbuff.append("Kinvey connection: OK\n");
						strbuff.append("Kinvey username:\n" + r.getUsername());
						
						//output.setText(strbuff.toString());
						
					}
				});
			}
	}	
	
}
