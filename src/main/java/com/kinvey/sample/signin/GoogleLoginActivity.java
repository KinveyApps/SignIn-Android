/*
 * Copyright (c) 2014 Kinvey Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.kinvey.sample.signin;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.AccountPicker;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
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
    private static Client mKinveyClient;
    private AccountManager mAccountManager;


    private static final String PARAM_LOGIN_TYPE_GOOGLE = "google";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_login);

        mKinveyClient = ((UserLogin) getApplication()).getKinveyService();
        mAccountManager = AccountManager.get(this);

        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                new String[] { "com.google" }, false, null, null, null, null);
        startActivityForResult(intent, GPLAY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPLAY_REQUEST_CODE && resultCode == RESULT_OK) {
            mAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            get(PLUS_PEOPLE_ME, mAccount, SCOPE_STRING, null,
                    new ResponseHandler() {

                        @Override
                        public void handle(Response response) {
                            if (response.status != 200) {
                                error(response);
                                return;
                            }
                            try {
                                JSONObject json = new JSONObject(new String(
                                        response.body));
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

        mKinveyClient.user().loginGoogle(getAuthToken(),
                new KinveyUserCallback() {

                    public void onFailure(Throwable e) {
                        Log.e(TAG, "Failed Kinvey login", e);
                        // TextView tv = (TextView) findViewById(R.id.output);
                        String b = new String(e.getMessage());
                        Log.e(AuthorizedActivity.TAG, "Error: " + b);
                        // tv.setText("DOH!  Great Scott!\nKinvey: " + b);
                    };

                    @Override
                    public void onSuccess(User r) {
                        CharSequence text = "Logged in.";
                        Toast.makeText(getApplicationContext(), text,
                                Toast.LENGTH_LONG).show();
                        GoogleLoginActivity.this.startActivity(new Intent(
                                GoogleLoginActivity.this, MainActivity.class));
                        GoogleLoginActivity.this.finish();

                    }
                });
    }

    private void finishLogin(String authToken, String password) {
        final Account account = new Account(authToken, UserLogin.ACCOUNT_TYPE);
        Bundle userData = new Bundle();
        userData.putString(UserLogin.LOGIN_TYPE_KEY, PARAM_LOGIN_TYPE_GOOGLE);
        mAccountManager.addAccountExplicitly(account, password, userData);

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, authToken);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, UserLogin.ACCOUNT_TYPE);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void error(Response response) {
        // TextView tv = (TextView) findViewById(R.id.output);
        String b = new String(response.body);
        Log.d(AuthorizedActivity.TAG, "Error " + response.status + " body: "
                + b);
        // tv.setText("OUCH!  The Internet never works!\n" + b);
    }

}