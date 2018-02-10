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
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.LinkedList;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessageService";
    private static final int OFF_DURATION = 5000;
    private static final int REGISTER_DURATION = 10000;
    private static final int RING_DURATION = 20000;
    private static final int MAX_MESSAGES = 6;

    public static int numberMessages = 0;
    public static LinkedList<String> receivedMessages = new LinkedList<>();

    private String pkg_name;
    private SharedPreferences prefs;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        prefs = getSharedPreferences(getString(R.string.pref_file), Context.MODE_PRIVATE);
        final boolean launch_app = prefs.getBoolean("launch_app", true);
        final boolean send_notif = prefs.getBoolean("send_notif", true);
        final boolean screen_on = prefs.getBoolean("screen_on", false);
        final boolean end_off = prefs.getBoolean("end_off", true);

        // Get push parameters.
        Map<String, String> data = remoteMessage.getData();
        pkg_name = data.get("app");

        // Send a notification.
        if (send_notif) {
            sendNotification(data.get("msg"));
        }

        if (!launch_app && !screen_on) {
            return;
        }

        // Set screen off timeout.
        int screenTimeout = 0;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (launch_app && end_off && !powerManager.isInteractive()) {
            screenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            if (screenTimeout > OFF_DURATION) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, OFF_DURATION);
            }
        }

        // Acquire a wake lock.
        int pmFlag = PowerManager.ACQUIRE_CAUSES_WAKEUP;
        if (screen_on) {
            pmFlag |= PowerManager.FULL_WAKE_LOCK;
        } else {
            pmFlag |= PowerManager.PARTIAL_WAKE_LOCK;
        }
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(pmFlag, TAG);
        wakeLock.acquire(REGISTER_DURATION);

        // Start the activity.
        boolean isLaunched = false;
        if (launch_app) {
            Intent intent = getPushIntent(
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION |
                            Intent.FLAG_ACTIVITY_NO_ANIMATION |
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (intent != null) {
                try {
                    startActivity(intent);
                    isLaunched = true;
                } catch (android.content.ActivityNotFoundException e) {
                    Log.i(TAG, "Activity not found.");
                }
            }
        } else {
            return;
        }

        // Restore the screen timeout setting.
        if (screenTimeout > OFF_DURATION) {
            if (isLaunched) {
                try {
                    Thread.sleep(REGISTER_DURATION + RING_DURATION);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Sleep interrupted.");
                }
            }
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, screenTimeout);
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
            if (receivedMessages.size() == MAX_MESSAGES) {
                receivedMessages.remove();
            }
            receivedMessages.add(messageBody);
            numberMessages++;
        }

        // Build a notification.
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        for (String s : receivedMessages) {
            inboxStyle.addLine(s);
        }
        int moreMessages = numberMessages - MAX_MESSAGES;
        if (moreMessages > 0) {
            inboxStyle.setSummaryText(getString(R.string.more_msg, moreMessages));
        }

        int nbFlag = Notification.DEFAULT_LIGHTS;
        if (prefs.getBoolean("notif_sound", true)) {
            nbFlag |= Notification.DEFAULT_SOUND;
        }

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setStyle(inboxStyle)
                .setCategory(Notification.CATEGORY_CALL)
//                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(getDeleteIntent())
                .setDefaults(nbFlag);
        if (prefs.getBoolean("heads_up", false)) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private Intent getPushIntent(int flags) {
        if (pkg_name == null) {
            return null;
        }

        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkg_name);
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
}
