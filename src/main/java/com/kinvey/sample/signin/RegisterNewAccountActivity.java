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
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity for registering new accounts.  This prompts a user for a user name (email address),
 * First and Last name, and a password. After validation, it creates a new KinveyUser and redirects 
 * the user back to the login page for Authentication.
 */
public class RegisterNewAccountActivity extends Activity {

	public static final String TAG = LoginActivity.class.getSimpleName();

	protected Client kinveyClient;
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
		kinveyClient = ((UserLogin) getApplication()).getKinveyService();
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
	
	/**
	 * Calls the createUserWithUsername method of the KinveyClient to create a new user.
	 * After sign-up is successful, the First and Last name are added to the user, and
	 * an Intent is instantiated to bring the user back to the login page to confirm
	 * authentication.  
	 * 
	 * Note that a user is not Authorized here to the Android Account Manager, but rather
	 * is added to AccountManager upon successful authentication.  
	 */
	public void processSignup(View view) {
		Toast.makeText(this, "Creating user...", Toast.LENGTH_SHORT).show();
	    kinveyClient.user().create(mEditEmailAddress.getText().toString(), mEditPassword.getText().toString(), new KinveyUserCallback() {
            public void onFailure(Throwable t) {
                CharSequence text = "Could not sign up -> " + t.getMessage();
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Sign-up error", t);
            }

            public void onSuccess(User u) {
                CharSequence text = "Welcome," + u.get("username") + ".  Your account has been registered.  Please login to confirm your credentials.";
                u.put("email", u.get("username"));
                u.put("firstname", mEditFirstName.getText().toString());
                u.put("lastname", mEditLastName.getText().toString());

                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                RegisterNewAccountActivity.this.startActivity(new Intent(RegisterNewAccountActivity.this, LoginActivity.class));
                RegisterNewAccountActivity.this.finish();

            }
        });
	}
}
