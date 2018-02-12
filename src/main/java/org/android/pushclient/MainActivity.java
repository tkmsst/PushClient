/**
 * Main activity.
 */

package org.android.pushclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean launch_app, notif_msg, notif_sound, heads_up, screen_on, end_off;
    private String server_url;
    private TextView mDisplay, mToken;
    private EditText editText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (!checkPlayServices()) {
            Log.i(TAG, "No valid Google Play Services APK found.");
            finish();
        }

        // Grant WRITE_SETTINGS permission.
        setSystemWritePermission();

        mDisplay = (TextView) findViewById(R.id.display);
        mToken = (TextView) findViewById(R.id.token);
        prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);

        getParameters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        checkPlayServices();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void setSystemWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    public void onClick(final View view) {
        ServerAccess serverAccess = new ServerAccess(this);
        if (view == findViewById(R.id.set)) {
            if (setParameters()) {
                mDisplay.setText(getString(R.string.msg_set));
            }
        } else if (view == findViewById(R.id.register)) {
            if (!setParameters()) {
                return;
            }
            String server_url = prefs.getString("server_url", "");
            String token = FirebaseInstanceId.getInstance().getToken();
            serverAccess.register(server_url, token, true);
            mToken.setText(token);
        } else if (view == findViewById(R.id.unregister)) {
            String server_url = prefs.getString("server_url", "");
            String token = FirebaseInstanceId.getInstance().getToken();
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister.");
            }
            serverAccess.register(server_url, token, false);
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
            mToken.setText("");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getParameters() {
        server_url  = prefs.getString("server_url", "");
        launch_app  = prefs.getBoolean("launch_app", true);
        notif_msg   = prefs.getBoolean("notif_msg", true);
        notif_sound = prefs.getBoolean("notif_sound", true);
        heads_up    = prefs.getBoolean("heads_up", false);
        screen_on   = prefs.getBoolean("screen_on", false);
        end_off     = prefs.getBoolean("end_off", true);

        editText  = (EditText) findViewById(R.id.server_url);
        checkBox1 = (CheckBox) findViewById(R.id.launch_app);
        checkBox2 = (CheckBox) findViewById(R.id.notif_msg);
        checkBox3 = (CheckBox) findViewById(R.id.notif_sound);
        checkBox4 = (CheckBox) findViewById(R.id.heads_up);
        checkBox5 = (CheckBox) findViewById(R.id.screen_on);
        checkBox6 = (CheckBox) findViewById(R.id.end_off);

        editText.setText(server_url);
        checkBox1.setChecked(launch_app);
        checkBox2.setChecked(notif_msg);
        checkBox3.setChecked(notif_sound);
        checkBox4.setChecked(heads_up);
        checkBox5.setChecked(screen_on);
        checkBox6.setChecked(end_off);
    }

    private boolean setParameters() {
        server_url  = editText.getText().toString();
        launch_app  = checkBox1.isChecked();
        notif_msg   = checkBox2.isChecked();
        notif_sound = checkBox3.isChecked();
        heads_up    = checkBox4.isChecked();
        screen_on   = checkBox5.isChecked();
        end_off     = checkBox6.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("server_url", server_url);
        editor.putBoolean("launch_app", launch_app);
        editor.putBoolean("notif_msg", notif_msg);
        editor.putBoolean("notif_sound", notif_sound);
        editor.putBoolean("heads_up", heads_up);
        editor.putBoolean("screen_on", screen_on);
        editor.putBoolean("end_off", end_off);

        return editor.commit();
    }
}
