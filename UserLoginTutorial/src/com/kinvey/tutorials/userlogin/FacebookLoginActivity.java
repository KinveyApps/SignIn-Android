package com.kinvey.tutorials.userlogin;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.persistence.mapping.MappedEntity;
import com.kinvey.persistence.mapping.MappedField;
import com.kinvey.util.KinveyCallback;
import com.kinvey.util.ScalarCallback;
import com.textuality.authorized.AuthorizedActivity;

public class FacebookLoginActivity extends Activity {

	private static final String FB_APP_ID = "";
	
	private static Facebook facebook = new Facebook(FB_APP_ID);
	
	private static KCSClient mKinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_login);
		
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();

		
		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(
				FacebookLoginActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");
		
		doFacebookSso(progressDialog);
		
	}
	

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
    
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {
	
		mKinveyClient.loginWithFacebookAccessToken(accessToken, new KinveyCallback<KinveyUser>() {
			
			@Override
			public void onFailure(Throwable e) {
				error(progressDialog, "Kinvey: " + e.getMessage());				
			}
			
			@Override
			public void onSuccess(KinveyUser r) {
				//TextView output = (TextView) findViewById(R.id.output);
				StringBuffer strbuff = new StringBuffer();
				strbuff.append("Kinvey connection: OK\n");
				strbuff.append("Kinvey username:\n" + r.getUsername());
				//output.setText(strbuff.toString());
				
				saveSomeData();
			}
		});
		
	}

	public static class MyData implements MappedEntity {
		private String name;
		private String id;
		
		public MyData() {}
		
		@Override
		public List<MappedField> getMapping() {
			return Arrays.asList(new MappedField[] {new MappedField("id", "_id"), new MappedField("name", "name")});
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
		

	}

	private void saveSomeData() {
		MyData mdata = new MyData();
		mdata.setName("vanhugel");
		mKinveyClient.mappeddata(MyData.class, "hills").save(mdata, new ScalarCallback<MyData>() {

			@Override
			public void onFailure(Throwable e) {
				Log.e("FacebookLogin", "failed to save", e);
			}
			
			@Override
			public void onSuccess(MyData arg0) {
				Log.d("FacebookLogin", "Worked!");
			}
		});
	}


	protected void error(ProgressDialog progressDialog, String error) {
    	if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
        
		
     //   TextView tv = (TextView) findViewById(R.id.output);
        String b = new String(error);
        Log.d(AuthorizedActivity.TAG, "Error " + error);
    //    tv.setText("OUCH!  The Internet never works!\n" + b);
	}
}
