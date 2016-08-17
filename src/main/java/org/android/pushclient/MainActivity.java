/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.android.pushclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static TextView mDisplay, mToken;
    private EditText editText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5;
    private SharedPreferences prefs;

    private String serverurl;
    private boolean launchact, sendnotif, headsup, screenon, endoff;

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
        if (view == findViewById(R.id.set)) {
            if (setParameters()) {
                mDisplay.setText(getString(R.string.msg_set));
            }
        } else if (view == findViewById(R.id.register)) {
            if (!setParameters()) {
                return;
            }
            String serverurl = prefs.getString("server_url", "");
            String token = FirebaseInstanceId.getInstance().getToken();
            ServerAccess.register(serverurl, token);
            mToken.setText(token);
        } else if (view == findViewById(R.id.unregister)) {
            String serverurl = prefs.getString("server_url", "");
            String token = FirebaseInstanceId.getInstance().getToken();
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                Log.e(TAG, "Failed to unregister.");
            }
            ServerAccess.unregister(serverurl, token);
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
        serverurl = prefs.getString("server_url", "");
        launchact = prefs.getBoolean("launch_act", true);
        sendnotif = prefs.getBoolean("send_notif", true);
        headsup   = prefs.getBoolean("heads_up", false);
        screenon  = prefs.getBoolean("screen_on", false);
        endoff    = prefs.getBoolean("end_off", true);

        editText  = (EditText) findViewById(R.id.serverurl);
        checkBox1 = (CheckBox) findViewById(R.id.launchact);
        checkBox2 = (CheckBox) findViewById(R.id.sendnotif);
        checkBox3 = (CheckBox) findViewById(R.id.headsup);
        checkBox4 = (CheckBox) findViewById(R.id.screenon);
        checkBox5 = (CheckBox) findViewById(R.id.endoff);

        editText.setText(serverurl);
        checkBox1.setChecked(launchact);
        checkBox2.setChecked(sendnotif);
        checkBox3.setChecked(headsup);
        checkBox4.setChecked(screenon);
        checkBox5.setChecked(endoff);
    }

    private boolean setParameters() {
        serverurl = editText.getText().toString();
        launchact = checkBox1.isChecked();
        sendnotif = checkBox2.isChecked();
        headsup   = checkBox3.isChecked();
        screenon  = checkBox4.isChecked();
        endoff    = checkBox5.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("server_url", serverurl);
        editor.putBoolean("launch_act", launchact);
        editor.putBoolean("send_notif", sendnotif);
        editor.putBoolean("heads_up", headsup);
        editor.putBoolean("screen_on", screenon);
        editor.putBoolean("end_off", endoff);

        return editor.commit();
    }
}
