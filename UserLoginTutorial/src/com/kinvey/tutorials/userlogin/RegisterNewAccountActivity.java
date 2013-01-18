package com.kinvey.tutorials.userlogin;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.util.KinveyCallback;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterNewAccountActivity extends Activity {

	public static final String TAG = LoginActivity.class.getSimpleName();

	protected KCSClient mKinveyClient;
	protected EditText mEditFirstName;
	protected EditText mEditLastName;
	protected EditText mEditEmailAddress;
	protected EditText mEditPassword;
	protected EditText mEditPasswordConfirm;
	protected Button mRegisterAccount;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView(R.layout.activity_register);
		
		mEditFirstName = (EditText) findViewById(R.id.etFirstName);
		mEditLastName = (EditText) findViewById(R.id.etLastName);
		mEditEmailAddress = (EditText) findViewById(R.id.etEmailAddress);
		mEditPassword = (EditText) findViewById(R.id.etPassword);
		mEditPasswordConfirm = (EditText) findViewById(R.id.etPasswordConfirm);
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();
	}
	
	public void registerAccount(View view) {
		if (validateFields()) {
			if (validatePasswordMatch()) {
				processSignup(view);
			} else {
				Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
			} 
		} else {
			Toast.makeText(this, "Fields not filled in", Toast.LENGTH_SHORT).show();
		}
	}
	
	// TODO:  Implement Text Listeners to handle this
	private boolean validateFields() {
		if (mEditFirstName.getText().length()>0 && mEditLastName.getText().length()>0 
				&& mEditEmailAddress.length()>0 && mEditPassword.getText().length()>0 
				&& mEditPasswordConfirm.getText().length()>0) {
			return true;
		} else {
			return false;
		}
	}

	private boolean validatePasswordMatch() {
		if (mEditPassword.getText().toString().equals(mEditPasswordConfirm.getText().toString())) {
			return true;
		} else {
			return false;
		}
	}

	public void processSignup(View view) {
		Toast.makeText(this, "Creating user...", Toast.LENGTH_SHORT).show();
	    mKinveyClient.createUserWithUsername(mEditEmailAddress.getText().toString(), mEditPassword.getText().toString(), new KinveyCallback<KinveyUser>() {
	        public void onFailure(Throwable t) {
	            CharSequence text = "Could not sign up.";
	            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	        }
	        public void onSuccess(KinveyUser u) {
	            CharSequence text = "Welcome," + u.getUsername() + ".  Your account has been registered.  Please login to confirm your credentials.";
	            u.setEmail(u.getUsername());
	            u.setGivenName(mEditFirstName.getText().toString());
	            u.setSurName(mEditLastName.getText().toString());
	            
	           Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	           RegisterNewAccountActivity.this.startActivity(new Intent(RegisterNewAccountActivity.this, LoginActivity.class));
	           RegisterNewAccountActivity.this.finish();
	           
	        }
	    });
	}
}
