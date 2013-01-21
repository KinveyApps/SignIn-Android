package com.kinvey.samples.userlogin;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;

import android.app.Application;

/**
 * Global application class.  Instantiates the KCS Client and sets global constants.
 *
 */
public class UserLogin extends Application {
    private KCSClient service;

    // Enter your Kinvey app credentials
    private static final String APP_KEY = your_kinvey_app_key;
    private static final String APP_SECRET = your_kinvey_app_secret;
    
    // Application Constants
    public static final String AUTHTOKEN_TYPE = "com.kinvey.myapplogin";
    public static final String ACCOUNT_TYPE = "com.kinvey.myapplogin";
    public static final String LOGIN_TYPE_KEY = "loginType";
    
    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }
   

    private void initialize() {
		// Enter your app credentials here
		service = KCSClient.getInstance(this.getApplicationContext(), new KinveySettings(APP_KEY, APP_SECRET));
    }

    public KCSClient getKinveyService() {
        return service;
    }
}
