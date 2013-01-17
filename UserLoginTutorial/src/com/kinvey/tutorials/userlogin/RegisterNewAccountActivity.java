package com.kinvey.tutorials.userlogin;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.tutorials.userlogin.authentication.LoginActivity;
import com.kinvey.util.KinveyCallback;

import android.os.Bundle;
import android.app.Activity;
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
	
	public void register(View view) {
		if (validateFields()) {
			if (validatePasswordMatch()) {
				processSignup(view);
			} else {
				// Passwords don't match
			} 
		} else {
			// Fields not filled in
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
		if (mEditPassword.equals(mEditPasswordConfirm)) {
			return true;
		} else {
			return false;
		}
	}

	public void processSignup(View view) {
	    mKinveyClient.createUserWithUsername(mEditEmailAddress.getText().toString(), mEditPassword.getText().toString(), new KinveyCallback<KinveyUser>() {
	        public void onFailure(Throwable t) {
	            CharSequence text = "Could not sign up.";
	            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	        }
	        public void onSuccess(KinveyUser u) {
	            CharSequence text = "Welcome back," + u.getUsername() + ".";
	            u.setEmail(u.getUsername());
	            u.setGivenName(mEditFirstName.getText().toString());
	            u.setSurName(mEditLastName.getText().toString());
	            
	           // Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	           // LoginActivity.this.startActivity(new Intent(LoginActivity.this, SessionsActivity.class));
	           // LoginActivity.this.finish();
	        }
	    });
	}
}
