SignIn-Android
==============

This application presets a sample login screen / account registration flow.  It allows a user to create an account, log in, and stores the credentials in the Android Account Manager.  

In particular this sample application highlights the following key backend tasks:

* Allow users to sign up and log in
* Store Credentials in Android AccountManager
* Integrate Login with Twitter or Facebook OAuth


## Set up Sign-In Project

1. Clone the project repository.
2. Download the latest Kinvey library (zip) and extract the downloaded zip file, from: http://devcenter.kinvey.com/android/downloads

###Android Studio

1. In Android Studio, go to **File &rarr; New &rarr; Import Project**
2. **Browse** to the extracted zip from step 1, and click **OK**
3. Click **Next** and **Finish**.
4. Copy all jars in the **libs/** folder of the Kinvey Android library zip to the **lib/** folder at the root of the project
5.  Click the **play** button to start a build, if you still see compilation errors ensure the versions are correctly defined in the dependencies list


###Finally, for all IDEs

6. Specify your app key and secret in `assets/kinvey.properties` constant variables
![key and secret]()


```java
app.key=MY_APP_KEY

app.secret=MY_APP_SECRET
```

##Twitter login

1.  Visit [Twitter's developer console](www.dev.twitter.com), login, and create a new application.
2.  Set the `Callback URL` to be anything, as long as it is not empty.  For example, `http://koauthtest.com`
3.  Open the file `TwitterLoginActivity` and set the following two values to your unique keys.

```java
private static final String TWITTER_CONSUMER_KEY = "your_twitter_consumer_key";
private static final String TWITTER_CONSUMER_SECRET = "your_twitter_consumer_secret";
```	

##Facebook login

1.  Visit [Facebook's developer console](https://developers.facebook.com/), login, and create a new appication.
2.  Check the box for `Native Android App`, and enter your Package name and Class name.
3.  Visit [Facebook's getting started guide](https://developers.facebook.com/docs/getting-started/facebook-sdk-for-android/3.0/) and follow their `Step 4` to generate your `Key Hash`, and set that value in your Facebook's app's configuration page.
4.  In this sample project, open the file `/res/values/strings.xml` and set following value with your unique key.

```java
<string name="facebook_app_id">my_facebook_app_id</string>
```

##Google+ Login

1.  Visit [Google's developer console](https://developers.google.com/+/mobile/android/getting-started) and follow `Prerequisites`, `Step 1`, and `Step 2` to enable the Google+ API and import the `library project` (Not a JAR!) into your IDE.

## Required Libraries
The following libraries are required to run this sample: 
* [Kinvey Android Library](http://devcenter.kinvey.com/android/downloads)
* [Facebook SDK for Android](https://developers.facebook.com/android/)
* Google Play Services (Available in the *Android SDK Manager*)
* [Action Bar Sherlock](http://actionbarsherlock.com/)


## License

Copyright (c) 2014 Kinvey, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.





