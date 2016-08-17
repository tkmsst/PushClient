/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.android.pushclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessageService";
    private static final int OFF_DURATION = 5000;
    private static final int REGISTER_DURATION = 10000;
    private static final int RING_DURATION = 20000;
    private static final int SHOW_MESSAGES = 6;

    public static int numberMessages = 0;
    private static String[] receivedMessages = new String[SHOW_MESSAGES];

    private SharedPreferences prefs;
    private String pkg_name;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        final boolean launchact = prefs.getBoolean("launch_act", true);
        final boolean sendnotif = prefs.getBoolean("send_notif", true);
        final boolean screenon = prefs.getBoolean("screen_on", false);
        final boolean endoff = prefs.getBoolean("end_off", true);

        // Set screen off timeout.
        PowerManager mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int mScreenTimeout = 0;
        if (endoff && !mPowerManager.isScreenOn()) {
            mScreenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            if (mScreenTimeout > OFF_DURATION) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, OFF_DURATION);
            }
        }

        // Acquire a wake lock.
        int pmFlag = PowerManager.ACQUIRE_CAUSES_WAKEUP;
        if (screenon) {
            pmFlag |= PowerManager.FULL_WAKE_LOCK;
        } else {
            pmFlag |= PowerManager.PARTIAL_WAKE_LOCK;
        }
        PowerManager.WakeLock mWakeLock = mPowerManager.newWakeLock(pmFlag, TAG);
        mWakeLock.acquire();

        // Get push parameters.
        Map<String, String> data = remoteMessage.getData();
        pkg_name = data.get("app");

        // Send a notification.
        if (sendnotif) {
            sendNotification(data.get("msg"));
        }

        // Start the activity.
        boolean isLaunched = false;
        if (launchact) {
            try {
                Intent intent = getPushIntent(
                        Intent.FLAG_ACTIVITY_NO_USER_ACTION |
                        Intent.FLAG_ACTIVITY_NO_ANIMATION |
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                if (intent != null) {
                    startActivity(intent);
                    isLaunched = true;
                    // Wait for registration.
                    Thread.sleep(REGISTER_DURATION);
                }
            } catch (android.content.ActivityNotFoundException e) {
                Log.i(TAG, "Activity not found.");
            } catch (InterruptedException e) {
                Log.d(TAG, "Sleep interrupted.");
            }
        }

        // Release the wake lock.
        mWakeLock.release();

        // Restore the screen timeout setting.
        if (mScreenTimeout > OFF_DURATION) {
            if (isLaunched) {
                try {
                    Thread.sleep(RING_DURATION);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Sleep interrupted.");
                }
            }
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, mScreenTimeout);
        }
    }

    @Override
    public void onDeletedMessages() {
        sendNotification(getString(R.string.msg_deleted));
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        // Set a Pendingintent.
        Intent intent = getPushIntent(0);
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Store messages.
        if (!messageBody.isEmpty()) {
            if (numberMessages < SHOW_MESSAGES) {
                receivedMessages[numberMessages] = messageBody;
            }
            numberMessages++;
        }

        // Build a notification.
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i = 0; i < min(numberMessages, SHOW_MESSAGES) ; i++) {
            inboxStyle.addLine(receivedMessages[i]);
        }
        int moreMessages = numberMessages - SHOW_MESSAGES;
        if (moreMessages > 0) {
           inboxStyle.setSummaryText(getString(R.string.more_msg, moreMessages));
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setStyle(inboxStyle)
                .setCategory(Notification.CATEGORY_CALL)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(getDeleteIntent())
                .setDefaults(Notification.DEFAULT_SOUND |
                             Notification.DEFAULT_LIGHTS);
        if (prefs.getBoolean("heads_up", false)) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 , notificationBuilder.build());
    }

    private Intent getPushIntent(int flags) {
        if (pkg_name == null) {
            return null;
        }

        PackageManager mPackageManager = getPackageManager();
        Intent intent = mPackageManager.getLaunchIntentForPackage(pkg_name);
        if (intent != null) {
            intent.setFlags(flags |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                    Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        return intent;
    }

    private PendingIntent getDeleteIntent() {
        Intent intent = new Intent(this, NotificationReceiver.class);
        return PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private int min(int a, int b) {
        return (a < b) ? a : b;
    }
}
