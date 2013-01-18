package com.kinvey.tutorials.userlogin;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;

import com.kinvey.KCSClient;
import com.kinvey.KinveyUser;
import com.kinvey.tutorials.userlogin.R;
import com.kinvey.tutorials.userlogin.R.id;
import com.kinvey.tutorials.userlogin.R.layout;
import com.kinvey.util.KinveyCallback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SyncStateContract.Constants;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class LoginActivity extends AccountAuthenticatorActivity {

	public static final String TAG = LoginActivity.class.getSimpleName();
	
	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
	public static final String PARAM_USERNAME = "username";
	public static final String PARAM_AUTHTOKENTYPE="authtokenType";
	
	private AccountManager mAccountManager;
	private ProgressDialog mProgressDialog = null;
	
	public static final int MIN_USERNAME_LENGTH = 8;
	public static final int MIN_PASSWORD_LENGTH = 4;
	
	private Boolean mConfirmCredentials = false;
	private final Handler mHandler = new Handler();

	protected KCSClient mKinveyClient;
	protected Button mButtonLogin;
	protected EditText mEditUserEmail;
	protected EditText mEditPassword;
	protected TextView mErrorMessage;
	protected String mUserEmail;
	protected String mPassword;
	
	protected Boolean mRequestNewAccount = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mAccountManager = AccountManager.get(this);
		final Intent intent = getIntent();
		mUserEmail = intent.getStringExtra(PARAM_USERNAME);
		
		mRequestNewAccount = (mUserEmail == null);
		mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRM_CREDENTIALS,false);
		mKinveyClient = ((UserLoginTutorial) getApplication()).getKinveyService();
		
		setContentView(R.layout.activity_login);
		
		mErrorMessage = (TextView) findViewById(R.id.tvErrorMessage);
		mEditUserEmail = (EditText) findViewById(R.id.etEmailLogin);
		mEditPassword = (EditText) findViewById(R.id.etPassword);
	        	
    	mButtonLogin = (Button) findViewById(R.id.btnLogin);
        mEditUserEmail = (EditText) findViewById(R.id.etEmailLogin);
        mEditPassword = (EditText) findViewById(R.id.etPassword);
	}
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage("Authenticating...");
		dialog.setIndeterminate(true);
		
		mProgressDialog = dialog;
		return dialog;		
	}
	
	public void login(View view) {
		if (mRequestNewAccount) {
			mUserEmail = mEditUserEmail.getText().toString();
		}
		mPassword = mEditPassword.getText().toString();
		if (TextUtils.isEmpty(mUserEmail) || TextUtils.isEmpty(mPassword)) {
			mErrorMessage.setText("Please enter a valid username and password.");
		} else {
			showProgress();
			userLogin();
		}
	}
	
	private void finishConfirmCredentials(boolean result) {
		final Account account = new Account(mUserEmail, UserLoginTutorial.ACCOUNT_TYPE);
		mAccountManager.setPassword(account, mPassword);
		mAccountManager.setUserData(account, "loginType", "kinvey");
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void registerAccount(View v) {
		Intent intent = new Intent(this, RegisterNewAccountActivity.class);
        startActivity(intent);
	}
	
	private void finishLogin(String authToken) {
		final Account account = new Account(mUserEmail, UserLoginTutorial.ACCOUNT_TYPE);
		if (mRequestNewAccount) {
			mAccountManager.addAccountExplicitly(account, mPassword, null);
		} else {
			mAccountManager.setPassword(account, mPassword);
		}
		final Intent intent = new Intent();
		intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUserEmail);
		intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLoginTutorial.ACCOUNT_TYPE);
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK,intent);
		finish();
	}
	
	public void onAuthenticationResult(String authToken) {
		boolean success = ((authToken != null) && (authToken.length()>0));
		hideProgress();
		
		if(success) {
			if (!mConfirmCredentials) {
				finishLogin(authToken);
			} else {
				finishConfirmCredentials(success);
			}
		} else {
			if (mRequestNewAccount) {
				mErrorMessage.setText("Please enter a valid username or password");
			} else {
				mErrorMessage.setText("Please enter a valid password");
			}
			
		}
	}
	
	
	// TODO:  Fix ShowDialog
	public void showProgress() {
		showDialog(0);
	}
	
	private void hideProgress() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	public void userLogin() {
		mKinveyClient.loginWithUsername(mEditUserEmail.getText().toString(), mEditPassword.getText().toString(), new KinveyCallback<KinveyUser>() {
	        public void onFailure(Throwable t) {
	            CharSequence text = "Wrong username or password";
	            Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
	            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
	            toast.show();
	            onAuthenticationResult(null);
	        }
	
	        public void onSuccess(KinveyUser u) {
	            CharSequence text = "Logged in " + u.getUsername() + ".";
	            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	            onAuthenticationResult(u.getId());
	            LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
	            LoginActivity.this.finish();
	        }

		});
	}
}