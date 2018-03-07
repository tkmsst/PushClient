/**
 * FirebaseInstanceIdService called when token is updated.
 */

package org.android.pushclient;;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        final SharedPreferences prefs = getSharedPreferences(getString(R.string.pref_file),
                Context.MODE_PRIVATE);
        String server_url = prefs.getString("server_url", "");

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Persist or remove token at third-party servers.
        new ServerAccess(null).register(server_url, refreshedToken, true);
    }
}
