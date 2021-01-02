package com.revinad.iwantadelivery.Service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {

    private static final String TAG = "FirebaseInstanceIdService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String refreshedToken) {
        Log.d("TOKEN", "sendRegistrationToServer: TOKEN " + refreshedToken);
    }
}