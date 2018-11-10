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

    private MyApplication myApplication;
    private EditText editText;
    private CheckBox[] checkBox = new CheckBox[MyApplication.MAX_FLAG];;
    private TextView[] textView = new TextView[2];;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Grant WRITE_SETTINGS permission.
        setSystemWritePermission();

        myApplication = (MyApplication) getApplication();
        getUiResources();
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
                textView[0].setText(getString(R.string.msg_set));
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
            if (myApplication.server_url.isEmpty()) {
                textView[0].setText(getString(R.string.url_empty));
            } else {
                serverAccess.register(myApplication.server_url,
                        myApplication.regid, true);
            }
            textView[1].setText(myApplication.regid);
        } else if (view == findViewById(R.id.unregister)) {
            setParameters();
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete token.");
            }
            if (myApplication.server_url.isEmpty()) {
                textView[0].setText(getString(R.string.url_empty));
            } else if (myApplication.regid.isEmpty()) {
                textView[0].setText(getString(R.string.token_removed));
            } else {
                serverAccess.register(myApplication.server_url,
                        myApplication.regid, false);
                myApplication.storeRegid("");
            }
        } else if (view == findViewById(R.id.clear)) {
            for(int i = 0; i < textView.length; i++) {
                textView[i].setText("");
            }
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

    private void getUiResources() {
        editText = findViewById(R.id.server_url);
        for(int i = 0; i < checkBox.length; i++) {
            String str = String.valueOf(i + 1);
            checkBox[i] = findViewById(getResources().getIdentifier(
                    "flag" + str, "id", getPackageName()));
        }
        for(int i = 0; i < textView.length; i++) {
            String str = String.valueOf(i + 1);
            textView[i] = findViewById(getResources().getIdentifier(
                    "view" + str, "id", getPackageName()));
        }
    }

    private void getParameters() {
        editText.setText(myApplication.server_url);
        for(int i = 0; i < checkBox.length; i++) {
            checkBox[i].setChecked(myApplication.flag[i]);
        }
    }

    private boolean setParameters() {
        myApplication.server_url = editText.getText().toString();
        for(int i = 0; i < checkBox.length; i++) {
            myApplication.flag[i] = checkBox[i].isChecked();
        }
        return myApplication.storeAll();
    }
}
