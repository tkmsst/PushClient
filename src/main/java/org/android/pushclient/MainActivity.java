/**
 * Main activity.
 */

package org.android.pushclient;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends Activity {

    private static final String TAG = "PC:MainActivity";

    private NotificationChannel channel;
    private String pkg;
    private MyApplication myApplication;
    private ServerAccess serverAccess;
    private EditText editText;
    private Spinner spinner;
    private CheckBox[] checkBox;
    private TextView[] textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the NotificationChannel.
        myApplication = (MyApplication) getApplication();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        channel = notificationManager.getNotificationChannel(MyFirebaseMessagingService.CHANNEL_ID);
        if (channel == null) {
            channel = new NotificationChannel(MyFirebaseMessagingService.CHANNEL_ID,
                    getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            myApplication.changeable = true;
        } else {
            myApplication.changeable = false;
        }

        // Grant system permissions.
        pkg = getPackageName();
        setSystemPermission();

        // Prepare resources.
        serverAccess = new ServerAccess(this);
        getUiResources();
        getParameters();
    }

    public void onClick(final View view) {
        if (view == findViewById(R.id.set)) {
            if (setParameters()) {
                textView[0].setText(getString(R.string.msg_set));
            } else {
                Log.e(TAG, "Failed to set parameters.");
            }
        } else if (view == findViewById(R.id.register)) {
            setParameters();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful()) {
                                // Get the Instance ID token.
                                final String token = task.getResult();
                                myApplication.storeRegid(token);
                                textView[1].setText(token);
                                // Send the token to the server.
                                if (myApplication.server_url.isEmpty()) {
                                    textView[0].setText(getString(R.string.url_empty));
                                } else {
                                    serverAccess.register(myApplication.server_url, token, true);
                                }
                            } else {
                                textView[0].setText(getString(R.string.no_token));
                            }
                        }
                    });
        } else if (view == findViewById(R.id.unregister)) {
            setParameters();
            if (myApplication.server_url.isEmpty()) {
                textView[0].setText(getString(R.string.url_empty));
            } else if (myApplication.regid.isEmpty()) {
                textView[0].setText(getString(R.string.token_removed));
            } else {
                serverAccess.register(myApplication.server_url, myApplication.regid, false);
                myApplication.storeRegid("");
            }
        } else if (view == findViewById(R.id.clear)) {
            for (TextView t : textView) {
                t.setText("");
            }
        }
    }

    private void setSystemPermission() {
        Uri pkg_uri = Uri.parse("package:" + pkg);
        PowerManager powerManager = getSystemService(PowerManager.class);
        if (!powerManager.isIgnoringBatteryOptimizations(pkg)) {
            Intent intent = new Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, pkg_uri);
            startActivity(intent);
        }

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkg_uri);
            startActivity(intent);
        }

        if (!Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, pkg_uri);
            startActivity(intent);
        }
    }

    private void getUiResources() {
        Resources res = getResources();

        editText = findViewById(R.id.server_url);

        spinner = findViewById(R.id.importance);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.importance, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setEnabled(myApplication.changeable);
        spinner.setAdapter(adapter);

        checkBox = new CheckBox[MyApplication.MAX_FLAG];
        for (int i = 0; i < checkBox.length; i++) {
            String str = String.valueOf(i + 1);
            checkBox[i] = findViewById(res.getIdentifier("flag" + str, "id", pkg));
        }

        textView = new TextView[2];
        for (int i = 0; i < textView.length; i++) {
            String str = String.valueOf(i + 1);
            textView[i] = findViewById(res.getIdentifier("view" + str, "id", pkg));
        }
    }

    private void getParameters() {
        editText.setText(myApplication.server_url);
        spinner.setSelection(channel.getImportance());
        for (int i = 0; i < checkBox.length; i++) {
            checkBox[i].setChecked(myApplication.flag[i]);
        }
    }

    private boolean setParameters() {
        if (myApplication.changeable) {
            createNotificationChannel(spinner.getSelectedItemPosition());
            myApplication.changeable = false;
            spinner.setEnabled(false);
        }

        myApplication.server_url = editText.getText().toString();
        for (int i = 0; i < checkBox.length; i++) {
            myApplication.flag[i] = checkBox[i].isChecked();
        }
        return myApplication.storeAll();
    }

    private void createNotificationChannel(int importance) {
        channel.enableLights(true);
        channel.setImportance(importance);
        channel.setShowBadge(false);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
