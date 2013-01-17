package com.kinvey.tutorials.userlogin;

import com.kinvey.KCSClient;
import com.kinvey.KinveySettings;

import android.app.Application;

public class UserLoginTutorial extends Application {
    private KCSClient service;

    // Enter your Kinvey app credentials
    private static final String APP_KEY = "kid_TVZbaNqLeJ";
    private static final String APP_SECRET = "9b259536eb2b4ddba874cc933d78a0d4";
    
    // Application Constants
    public static final String AUTHTOKEN_TYPE = "com.kinvey.myapplogin";
    public static final String ACCOUNT_TYPE = "com.kinvey.myapplogin";
    
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
