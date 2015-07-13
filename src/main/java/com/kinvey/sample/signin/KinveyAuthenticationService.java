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


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Service for KinveyAccountAuthenticator Requests
 */
public class KinveyAuthenticationService extends Service {
	private static final String TAG = "AccountAuthenticatorService";
	private static KinveyAccountAuthenticator sAccountAuthenticator = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		IBinder ret = null;
		if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
			ret = new KinveyAccountAuthenticator(this).getIBinder();
		}
		return ret;	
	}
	
	private KinveyAccountAuthenticator getAuthenticator()
	{
		if (sAccountAuthenticator == null) {
			sAccountAuthenticator = new KinveyAccountAuthenticator(this);
		}
		
	return sAccountAuthenticator;
	}

}
