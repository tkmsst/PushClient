/**
 * Main activity.
 */

package org.android.pushclient;

import android.content.Intent;
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

    private TextView mDisplay, mToken;
    private EditText editText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    private MyApplication myApplication;

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

        myApplication = (MyApplication) getApplicationContext();
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
            if (setParameters()) {
                String token = FirebaseInstanceId.getInstance().getToken();
                if (token != null) {
                    serverAccess.register(
                            myApplication.manageServerUrl(null), token, true);
                    myApplication.manageRegid(token);
                    mToken.setText(token);
                }
            }
        } else if (view == findViewById(R.id.unregister)) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister.");
            }
            String regid = myApplication.manageRegid(null);
            if (!regid.isEmpty()) {
                serverAccess.register(
                        myApplication.manageServerUrl(null), regid, false);
                myApplication.manageRegid("");
            } else {
                mDisplay.setText(getString(R.string.token_deleted));
            }
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
        mDisplay = (TextView) findViewById(R.id.display);
        mToken   = (TextView) findViewById(R.id.token);

        editText  = (EditText) findViewById(R.id.server_url);
        checkBox1 = (CheckBox) findViewById(R.id.launch_app);
        checkBox2 = (CheckBox) findViewById(R.id.notif_msg);
        checkBox3 = (CheckBox) findViewById(R.id.notif_sound);
        checkBox4 = (CheckBox) findViewById(R.id.heads_up);
        checkBox5 = (CheckBox) findViewById(R.id.screen_on);
        checkBox6 = (CheckBox) findViewById(R.id.end_off);

        editText.setText(myApplication.manageServerUrl(null));
        checkBox1.setChecked(myApplication.get("launch_app"));
        checkBox2.setChecked(myApplication.get("notif_msg"));
        checkBox3.setChecked(myApplication.get("notif_sound"));
        checkBox4.setChecked(myApplication.get("heads_up"));
        checkBox5.setChecked(myApplication.get("screen_on"));
        checkBox6.setChecked(myApplication.get("end_off"));
    }

    private boolean setParameters() {
        myApplication.manageServerUrl(editText.getText().toString());
        myApplication.put("launch_app", checkBox1.isChecked());
        myApplication.put("notif_msg", checkBox2.isChecked());
        myApplication.put("notif_sound", checkBox3.isChecked());
        myApplication.put("heads_up", checkBox4.isChecked());
        myApplication.put("screen_on", checkBox5.isChecked());
        myApplication.put("end_off", checkBox6.isChecked());

        return myApplication.storeAll();
    }
}
