package com.kinvey.sample.signin;

import com.kinvey.android.Client;

import android.app.Application;

/**
 * Global application class.  Instantiates the KCS Client and sets global constants.
 *
 */
public class UserLogin extends Application {
    private Client service;

    // Enter your Kinvey app credentials
    private static final String APP_KEY = "YOUR_APP_KEY";
    private static final String APP_SECRET = "YOUR_APP_SECRET";
    
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
		service = new Client.Builder(APP_KEY, APP_SECRET, this).build();
    }

    public Client getKinveyService() {
        return service;
    }
}
