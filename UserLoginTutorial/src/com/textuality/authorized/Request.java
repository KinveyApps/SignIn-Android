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

import java.net.URL;
import java.util.List;
import java.util.Map;

public class Request {
    public final URL mURI;
    public final Map<String, List<String>> mHeaders;
    public final byte[] mBody; // non-null if POST
    public String mEmail;
    public String mScope;
    public final ResponseHandler mHandler;
    
    public URL uri() {
        return mURI;
    }
    public Map<String, List<String>> headers() {
        return mHeaders;
    }
    public ResponseHandler handler() {
        return mHandler;
    }
    public String email() {
        return mEmail;
    }
    public String scope() {
        return mScope;
    }

    private Request(URL uri, Map<String, List<String>> headers, byte[] body, String email, String scope, ResponseHandler handler) {
        mURI = uri;
        mHeaders = headers;
        mBody = body;
        mEmail = email;
        mScope = scope;
        mHandler = handler;
    }
    
    public static Request create(URL uri, Map<String, List<String>> headers, String email, String scope, ResponseHandler handler) {
        return new Request(uri, headers, null, email, scope, handler);
    }

}
