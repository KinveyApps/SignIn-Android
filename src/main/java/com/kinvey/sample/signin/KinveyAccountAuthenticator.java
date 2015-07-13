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
import com.kinvey.java.User;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;


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

	private Client kinveyClient;
	private static final String TAG = KinveyAccountAuthenticator.class.getSimpleName();
	private final Context mContext;

	public KinveyAccountAuthenticator(Context context) {
		super(context);
		mContext = context;
		kinveyClient = new Client.Builder(mContext).build();
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
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
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
            User ku;
            try {
			    ku = kinveyClient.user().loginBlocking(account.name, password).execute();
                // TODO make async
            } catch (IOException ex) {ku=null;}
			
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
		
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		return null;
	}

}
