/**
 * Main activity.
 */

package org.android.pushclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends Activity {

    private static final String TAG = "PC:MainActivity";

    private TextView textView1, textView2;
    private EditText editText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5, checkBox6;

    private MyApplication myApplication;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grant WRITE_SETTINGS permission.
        setSystemWritePermission();

        myApplication = (MyApplication) getApplication();
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
                textView1.setText(getString(R.string.msg_set));
            } else {
                Log.e(TAG, "Failed to set parameters.");
            }
        } else if (view == findViewById(R.id.register)) {
            setParameters();
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(Task<InstanceIdResult> task) {
                            if (task != null && task.isSuccessful()) {
                                String token = task.getResult().getToken();
                                myApplication.storeRegid(token);
                            } else {
                                Log.e(TAG, "Failed to get token.");
                            }
                        }
                    });
            String regid = myApplication.getRegid();
            if (myApplication.server_url.isEmpty()) {
                textView1.setText(getString(R.string.url_empty));
            } else {
                serverAccess.register(myApplication.server_url, regid, true);
            }
            textView2.setText(regid);
        } else if (view == findViewById(R.id.unregister)) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                Log.e(TAG, "Failed to unregister.");
            }
            String regid = myApplication.getRegid();
            if (myApplication.server_url.isEmpty()) {
                textView1.setText(getString(R.string.url_empty));
            } else if (regid.isEmpty()) {
                textView1.setText(getString(R.string.token_removed));
            } else {
                serverAccess.register(myApplication.server_url, regid, false);
                myApplication.storeRegid("");
            }
        } else if (view == findViewById(R.id.clear)) {
            textView1.setText("");
            textView2.setText("");
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
        textView1 = findViewById(R.id.message);
        textView2 = findViewById(R.id.token);

        editText = findViewById(R.id.server_url);
        checkBox1 = findViewById(R.id.launch_app);
        checkBox2 = findViewById(R.id.notif_msg);
        checkBox3 = findViewById(R.id.notif_sound);
        checkBox4 = findViewById(R.id.heads_up);
        checkBox5 = findViewById(R.id.screen_on);
        checkBox6 = findViewById(R.id.end_off);

        editText.setText(myApplication.server_url);
        checkBox1.setChecked(myApplication.launch_app);
        checkBox2.setChecked(myApplication.notif_msg);
        checkBox3.setChecked(myApplication.notif_sound);
        checkBox4.setChecked(myApplication.heads_up);
        checkBox5.setChecked(myApplication.screen_on);
        checkBox6.setChecked(myApplication.end_off);
    }

    private boolean setParameters() {
        myApplication.server_url = editText.getText().toString();
        myApplication.launch_app = checkBox1.isChecked();
        myApplication.notif_msg = checkBox2.isChecked();
        myApplication.notif_sound = checkBox3.isChecked();
        myApplication.heads_up = checkBox4.isChecked();
        myApplication.screen_on = checkBox5.isChecked();
        myApplication.end_off = checkBox6.isChecked();

        return myApplication.storeAll();
    }
}
