package com.kinvey.samples.userlogin;

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

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.samples.userlogin.R;
import com.kinvey.util.KinveyCallback;
import com.textuality.authorized.AuthorizedActivity;

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
	private static final String FB_APP_ID = your_facebook_app_key;
	
	private Boolean mConfirmCredentials = false;
	private Boolean mRequestNewAccount = true;
	private AccountManager mAccountManager;
	
	private ProgressDialog mProgressDialog = null;
	private static final String PARAM_LOGIN_TYPE_FACEBOOK = "facebook";
	
	private static Facebook facebook = new Facebook(FB_APP_ID);
	
	/** 
	 * Kinvey Client
	 */
	private static KCSClient mKinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_login);
		
		mAccountManager = AccountManager.get(this);
		
		mAccountManager = AccountManager.get(this);	
		
		mKinveyClient = ((UserLogin) getApplication()).getKinveyService();

		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(
				FacebookLoginActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");
		
		doFacebookSso(progressDialog);
		
	}
	
	/**
	 * Facebook SSO Oauth
	 */
    private void doFacebookSso(final ProgressDialog progressDialog){
    	
    	facebook.authorize(FacebookLoginActivity.this, 
				new String[] { "publish_stream," , "publish_checkins" },
				new DialogListener() {
					@Override
					public void onComplete(Bundle values) {
						// Close the progress dialog and toast success to the user
						if (progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
						Toast.makeText(FacebookLoginActivity.this, "Logged in with Facebook.", 
								Toast.LENGTH_LONG).show();
						
						loginFacebookKinveyUser(progressDialog, facebook.getAccessToken());
						
					}

					@Override
					public void onFacebookError(FacebookError error) {
						error(progressDialog, error.getMessage());
					}

					@Override
					public void onError(DialogError e) {
						error(progressDialog, e.getMessage());
					}

					@Override
					public void onCancel() {
						Toast.makeText(FacebookLoginActivity.this, "FB login cancelled", 
								Toast.LENGTH_LONG).show();
					}
				});
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    /*
     * Login a Kinvey User with Faceook credentials
     */
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {
	
		mKinveyClient.loginWithFacebookAccessToken(accessToken, new KinveyCallback<KinveyUser>() {
			
			@Override
			public void onFailure(Throwable e) {
				CharSequence text = "Wrong username or password";
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		        toast.show();				
			}
			
			@Override
			public void onSuccess(KinveyUser u) {
				CharSequence text = "Logged in " + u.getUsername() + ".";
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	            onAuthenticationResult(u.getId(), u.getPassword());
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
