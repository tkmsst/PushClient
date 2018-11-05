/**
 * Main activity.
 */

package org.android.pushclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mDisplay, mToken;
    private EditText editText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    private MyApplication myApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grant WRITE_SETTINGS permission.
        setSystemWritePermission();

        myApplication = (MyApplication) getApplicationContext();
        getParameters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Grant WRITE_SETTINGS permission.
        setSystemWritePermission();
    }

    public void onClick(final View view) {
        ServerAccess serverAccess = new ServerAccess(this);
        if (view == findViewById(R.id.set)) {
            if (setParameters()) {
                mDisplay.setText(getString(R.string.msg_set));
            } else {
                Log.e(TAG, "Failed to set parameters.");
            }
        } else if (view == findViewById(R.id.register)) {
            setParameters();
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful()) {
                                String token = task.getResult().getToken();
                                myApplication.manageRegid(token);
                            } else {
                                Log.e(TAG, "Failed to get token.");
                            }
                        }
                    });
            String regid = myApplication.manageRegid(null);
            String server_url = myApplication.manageServerUrl(null);
            if (server_url.isEmpty()) {
                mDisplay.setText(getString(R.string.url_empty));
            } else if (!regid.isEmpty()) {
                serverAccess.register(server_url, regid, true);
            }
            mToken.setText(regid);
        } else if (view == findViewById(R.id.unregister)) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister.");
            }
            String regid = myApplication.manageRegid(null);
            String server_url = myApplication.manageServerUrl(null);
            if (server_url.isEmpty()) {
                mDisplay.setText(getString(R.string.url_empty));
            } else if (!regid.isEmpty()) {
                serverAccess.register(server_url, regid, false);
                myApplication.manageRegid("");
            } else {
                mDisplay.setText(getString(R.string.token_removed));
            }
        } else if (view == findViewById(R.id.clear)) {
            mDisplay.setText("");
            mToken.setText("");
        }
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

    private void getParameters() {
        mDisplay = findViewById(R.id.display);
        mToken   = findViewById(R.id.token);

        editText  = findViewById(R.id.server_url);
        checkBox1 = findViewById(R.id.launch_app);
        checkBox2 = findViewById(R.id.notif_msg);
        checkBox3 = findViewById(R.id.notif_sound);
        checkBox4 = findViewById(R.id.heads_up);
        checkBox5 = findViewById(R.id.screen_on);
        checkBox6 = findViewById(R.id.end_off);

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
