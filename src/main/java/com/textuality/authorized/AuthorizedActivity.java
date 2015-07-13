/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.textuality.authorized;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

/**
 * Provides GET (and eventually POST) methods that are authorized using an OAuth2 token requested on 
 *  behalf of a specified Google account.  
 * To use it you have to subclass it, which gives you a get() method, see below.  It's asynchronous,
 *  so you also have to pass in a ResponseHandler object.  
 * The OAuth processes may require firing off one or more Activities to get user approval, so this class
 *  needs its OnActivityResult to be called.  So if you implement OnActivityResult, you have to start with 
 *  @Override
 *  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 *      super.onActivityResult(requestCode, resultCode, data);
 */
public class AuthorizedActivity extends AccountAuthenticatorActivity {
    private static final int REQUEST_CODE = new Random().nextInt(1000000000);
    public static final String TAG = "AuthorizedActivity";

    // This is required because you might have to dispatch off to another activity and get a token back in
    //  onActivityResult, and you can�t serialize the ResponseHandler argument in Intent extras.
    // It means that you can�t really have more than one request at a time in play. 
    private Request mRequest;
    
    private boolean mSecondTry = false;
	private String mAuthToken;
            
    /**
     * Perform an HTTP GET on a resource with OAuth2 authorization.  
     *  This is asynchronous; results will be returned via the handler.
     * 
     * @param uriString The URI to GET, as a string.
     * @param email The email address associated with a Google account on the device, for authorization
     * @param scope The scope which identifies the resource, for authorization
     * @param headers Any HTTP headers which are to be transmitted with the GET request
     * @param handler This object's "handle" method will be called when the GET completes, succesfully or not
     */
    public void get(String uriString, String email, String scope, Map<String, List<String>> headers, ResponseHandler handler) {
        try {
            URL uri = new URL(uriString);
            get(uri, email, scope, headers, handler);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Perform an HTTP GET on a resource with OAuth2 authorization.  
     *  This is asynchronous; results will be returned via the handler.
     * 
     * @param uri The URI to GET
     * @param email The email address associated with a Google account on the device, for authorization
     * @param scope The scope which identifies the resource, for authorization
     * @param headers Any HTTP headers which are to be transmitted with the GET request
     * @param handler This object's "handle" method will be called when the GET completes, succesfully or not
     */
    public void get(URL uri, String email, String scope, Map<String, List<String>> headers, ResponseHandler handler) {
        mRequest = Request.create(uri, headers, email, scope, handler);
        new Runner().execute();
    }

    // has to be done in the background because there are multiple network round-trips involved
    private class Runner extends AsyncTask<Void, Void, Response> {
        
        @Override
        protected Response doInBackground(Void... params) {
            return authenticateAndGo(new Backoff());
        }
        
        @Override
        protected void onPostExecute(Response result) {
            super.onPostExecute(result);

            // if the result is null, that means we're off in another activity doing oauth stuff. 
            //  When that finishes, it'll eventually end up calling the handler
            if (result != null) {
                mRequest.handler().handle(result);
            }
        }
    }

    private Response authenticateAndGo(Backoff backoff) {
        Response response;
        try {
            mAuthToken = GoogleAuthUtil.getToken(this, mRequest.email(), mRequest.scope());
            response = doGet(mAuthToken, this);

        } catch (UserRecoverableAuthException userAuthEx) {
            // This means that the app hasn't been authorized by the user for access to the scope, so we're going to have
            //  to fire off the (provided) Intent to arrange for that
            // But we only want to do this once. Multiple attempts probably mean the user said no.
            if (!mSecondTry) {
                startActivityForResult(userAuthEx.getIntent(), REQUEST_CODE);
                response = null;
            } else {
                response = new Response(-1, null, "Multiple approval attempts");
            }
            
        }  catch (IOException ioEx) {
            // Something is stressed out; the auth servers are by definition high-traffic and you can't count on
            //  100% success. But it would be bad to retry instantly, so back off
            if (backoff.shouldRetry()) {
                backoff.backoff(); 
                response = authenticateAndGo(backoff);
            } else {
                response = new Response(-1, null, "No response from authorization server.");
            }

        }  catch (GoogleAuthException fatalAuthEx)  {
            Log.d(TAG, "Fatal Authorization Exception");
            response = new Response(-1, null, "Fatal authorization exception: " + fatalAuthEx.getLocalizedMessage());
        }

        return response;
    }

    // Come here if user intervention was required for authentication
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mSecondTry = true;
                new Runner().execute();        
            } else {            
                mRequest.handler().handle(new Response(-1, null, "Authorization denied by user"));
            }
        }
    }
    
    private void addHeaders(HttpURLConnection connection, String token, Map<String, List<String>> headers) {
        connection.addRequestProperty("Authorization",  "OAuth " + token);
        if (headers != null) {
            for (String header : headers.keySet()) {
                for (String value : headers.get(header)) {
                    connection.addRequestProperty(header, value);
                }
            }
        }
    }

    private Response doGet(String token, Activity activity) {
        HttpURLConnection conn = null;
        Response response = new Response(-1, null, "Unknown problem");
        try {
            conn = (HttpURLConnection) mRequest.uri().openConnection();
            addHeaders(conn, token, mRequest.headers());

            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            int status = conn.getResponseCode();

            String lastRedirect = null;
            while (status == 302) {
                String redirect = conn.getHeaderFields().get("Location").get(0);
                if (!redirect.equals(lastRedirect)) {
                    lastRedirect = redirect;
                    URL redirectURI = null;
                    try { 
                        redirectURI = new URL(redirect);
                        conn.disconnect();
                        conn = (HttpURLConnection) redirectURI.openConnection();
                        addHeaders(conn, token, mRequest.headers());
                        in = new BufferedInputStream(conn.getInputStream());
                        status = conn.getResponseCode();
                    } catch (MalformedURLException e) {
                        response = new Response(-1, null, "Malformed redirect URI: " + redirect);
                    }
                }
            }

            if ((status / 100) == 2) {
                response = new Response(status, conn.getHeaderFields(), readStream(in));
            } else {
                // apparently 401/403 never come here
                BufferedInputStream err = new BufferedInputStream(conn.getErrorStream());
                response = new Response(status, conn.getHeaderFields(), readStream(err));
            }
        } catch (Exception e) {
            try {
                GoogleAuthUtil.invalidateToken(activity, token);

                int status  = conn.getResponseCode();
                if (status == 401) {
                    // they gave us a new token, but it 401'ed. This could be getting unlucky with the
                    //  expiry, or it could be that access was actually revoked.  So we'll recurse. If 
                    //  it was just expiry, everything should work fine.  If the token was revoked,
                    //  the getToken will fail next time around.
                    response = authenticateAndGo(new Backoff());
                } else {
                    BufferedInputStream err = new BufferedInputStream(conn.getErrorStream());
                    response = new Response(status, conn.getHeaderFields(), readStream(err));
                }

            } catch (IOException ee) {
                response = new Response(-1, null, ee.getLocalizedMessage().getBytes());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response;
    }

    private static byte[] readStream(InputStream in) 
            throws IOException {
        final byte[] buf = new byte[1024];
        int count = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        while ((count = in.read(buf)) != -1) {
            out.write(buf, 0, count);
        }
        in.close();
        return out.toByteArray();
    }

    public String getAuthToken() {
		return mAuthToken;
	}

	static class Backoff {

        private static final long INITIAL_WAIT = 1000 + new Random().nextInt(1000);
        private static final long MAX_BACKOFF = 1800 * 1000;

        private long mWaitInterval = INITIAL_WAIT;
        private boolean mBackingOff = true;

        public boolean shouldRetry() {
            return mBackingOff;
        }

        private void noRetry() {
            mBackingOff = false;
        }

        public void backoff() {
            if (mWaitInterval > MAX_BACKOFF) {
                noRetry();
            } else if (mWaitInterval > 0) {
                try {
                    Thread.sleep(mWaitInterval);
                } catch (InterruptedException e) {
                    // life's a bitch, then you die
                }
            }

            mWaitInterval = (mWaitInterval == 0) ? INITIAL_WAIT : mWaitInterval * 2;
        }
    }
}