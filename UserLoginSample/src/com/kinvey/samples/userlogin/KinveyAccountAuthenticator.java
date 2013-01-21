package com.kinvey.samples.userlogin;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;
import com.kinvey.KinveyUser;
import com.kinvey.exception.KinveyException;
import com.kinvey.util.KinveyCallback;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


/**
 * 
 * KinveyAccountAuthenticator class extends the Android AbstractAccountAuthenticator. 
 * Two methods are implemented from the base class:  addAccount and getAuthToken.
 * 
 * These implementations are required for the Kinvey credentials to be stored in 
 * 
 * addAccount:  Creates an Intent to LoginActivity when a new Account needs to be added
 * getAuthToken:  Attempts login to Kinvey service to retrieve an Auth token for an app
 *
 */
public class KinveyAccountAuthenticator extends AbstractAccountAuthenticator {

	private KCSClient mKinveyClient; 
	private static final String TAG = KinveyAccountAuthenticator.class.getSimpleName();
	private final Context mContext;
	
	// Enter your Kinvey app credentials
    private static final String APP_KEY = "kid_TVZbaNqLeJ";
    private static final String APP_SECRET = "9b259536eb2b4ddba874cc933d78a0d4";
	
	public KinveyAccountAuthenticator(Context context) {
		super(context);
		mContext = context;
		mKinveyClient = KCSClient.getInstance(mContext, new KinveySettings(APP_KEY, APP_SECRET));
		// TODO:  Need to solve how to pass KinveyService context through the AccountAuthenticator,
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		
		final Intent intent = new Intent(mContext, LoginActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		
		if (!authTokenType.equals("com.kinvey.myapplogin")) {
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}
		final AccountManager am = AccountManager.get(mContext);
		final String password = am.getPassword(account);
		
		if (password !=null) {
			final KinveyUser ku = login(account.name,password);
			
			if (ku != null) {
				final Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.kinvey.myapplogin");
				result.putString(AccountManager.KEY_AUTHTOKEN, ku.getId());
				return result;
			}
			
		}
		
		// No valid user
		final Intent intent = new Intent(mContext, LoginActivity.class);
		
		// TODO:  Remove comments on putExtras
		//intent.putExtra(LoginActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		//intent.putExtra(LoginActivity.PARAM_AUTHENTICATOR_RESPONSE, response);
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private KinveyUser login(String username, String password) {
			try {
				return mKinveyClient.loginWithUsername(username, password);
			} catch (KinveyException ex) {
				return null;
			}
	}
}
