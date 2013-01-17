package com.kinvey.tutorials.userlogin;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;
import com.textuality.authorized.AuthorizedActivity;
import com.textuality.authorized.Response;
import com.textuality.authorized.ResponseHandler;

public class GoogleLoginActivity extends AuthorizedActivity {
	
    /** OAuth 2.0 scope for writing a moment to the user's Google+ history. */
    static final String SCOPE_STRING = "oauth2:https://www.googleapis.com/auth/plus.me";
    private static final String PLUS_PEOPLE_ME = "https://www.googleapis.com/plus/v1/people/me";

	private static final int GPLAY_REQUEST_CODE = 782049854;

    private String mID;
	private String mAccount;
	private static KCSClient mKinveyClient;

		  
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.google_login);
	
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();

        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, 
                false, null, null, null, null);  
        startActivityForResult(intent, GPLAY_REQUEST_CODE);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPLAY_REQUEST_CODE && resultCode == RESULT_OK) {
            mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            get(PLUS_PEOPLE_ME, mAccount, SCOPE_STRING, 
                    null, new ResponseHandler() {
                        

				@Override
                public void handle(Response response) {
                    if (response.status != 200) {
                        error(response);
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(new String(response.body));
                        mID = json.optString("id");
                        
                        loginGoogleKinveyUser();
                        
                    } catch (JSONException je) {
                        throw new RuntimeException(je);
                    }
                }

				
            });        
        }
    }

	private void loginGoogleKinveyUser() {
		mKinveyClient.loginWithGoogleAuthToken(getAuthToken(), 
				new KinveyCallback<KinveyUser>() {
			
			public void onFailure(Throwable e) {
				Log.e(TAG, "Failed Kinvey login", e);
		        //TextView tv = (TextView) findViewById(R.id.output);
		        String b = new String(e.getMessage());
		        Log.e(AuthorizedActivity.TAG, "Error: " + b);
	//	        tv.setText("DOH!  Great Scott!\nKinvey: " + b);	
			};
			
			@Override
			public void onSuccess(KinveyUser r) {
			//	TextView output = (TextView) findViewById(R.id.output);
				StringBuffer strbuff = new StringBuffer();
				strbuff.append("Google Account ID: "+mID+"\n");
				strbuff.append("Kinvey connection: OK\n");
				strbuff.append("Kinvey username:\n" + r.getUsername());
		//		output.setText(strbuff.toString());
				
			}
		});
	}

	protected void error(Response response) {
      //  TextView tv = (TextView) findViewById(R.id.output);
        String b = new String(response.body);
        Log.d(AuthorizedActivity.TAG, "Error " + response.status + " body: " + b);
//        tv.setText("OUCH!  The Internet never works!\n" + b);		
	}

}