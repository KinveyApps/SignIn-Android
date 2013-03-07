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
