/**
 * FirebaseMessagingService called when a notification is received.
 */

package org.android.pushclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.LinkedList;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static int numberMessages = 0;
    public static LinkedList<String> receivedMessages = new LinkedList<>();

    private static final String TAG = "FirebaseMessageService";
    private static final int TIMEOUT_PERIOD = 10000;
    private static final int LAUNCH_DURATION = 15000;
    private static final int MAX_DELAY= 30000;
    private static final int MAX_MESSAGES = 6;

    private boolean notif_msg = true;
    private boolean notif_sound = true;
    private boolean heads_up =false;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Set variables.
        final SharedPreferences prefs = getSharedPreferences(
                getString(R.string.pref_file),Context.MODE_PRIVATE);
        final boolean launch_app = prefs.getBoolean("launch_app", true);
        final boolean screen_on  = prefs.getBoolean("screen_on", false);
        final boolean end_off    = prefs.getBoolean("end_off", true);

        notif_msg   = prefs.getBoolean("notif_msg", true);
        notif_sound = prefs.getBoolean("notif_sound", true);
        heads_up    = prefs.getBoolean("heads_up", false);

        // Acquire a wake lock.
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        int pmFlag = PowerManager.ACQUIRE_CAUSES_WAKEUP;
        if (screen_on) {
            pmFlag |= PowerManager.FULL_WAKE_LOCK;
        } else {
            pmFlag |= PowerManager.PARTIAL_WAKE_LOCK;
        }
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(pmFlag, TAG);
        wakeLock.acquire(LAUNCH_DURATION);

        // Get push parameters.
        Map<String, String> data = remoteMessage.getData();

        // Get the app intent.
        Intent intent = getPushIntent(data.get("app"));

        // Send a notification.
        if (notif_msg || notif_sound || heads_up) {
            sendNotification(data.get("msg"), intent);
        }

        if (!launch_app) {
            return;
        }

        // Set screen off timeout.
        int screenTimeout = 0;
        if (end_off && !powerManager.isInteractive()) {
            screenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            if (screenTimeout > TIMEOUT_PERIOD) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, TIMEOUT_PERIOD);
            }
        }

        // Start the activity.
        if (intent != null) {
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Log.i(TAG, "Activity not found.");
            }
        }

        // Restore the screen timeout setting.
        if (screenTimeout > TIMEOUT_PERIOD) {
            scheduleJob(screenTimeout);
        }
    }

    @Override
    public void onDeletedMessages() {
        sendNotification(getString(R.string.msg_deleted), null);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, Intent intent) {
        // Set defaults.
        int defaultsFlag = Notification.DEFAULT_LIGHTS;
        if (notif_sound) {
            defaultsFlag |= Notification.DEFAULT_SOUND;
        }

        // Build a notification.
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setCategory(Notification.CATEGORY_CALL)
                .setDeleteIntent(getDeleteIntent())
                .setDefaults(defaultsFlag);

        // Set notification messages.
        if (notif_msg) {
            if (!messageBody.isEmpty()) {
                if (receivedMessages.size() == MAX_MESSAGES) {
                    receivedMessages.remove();
                }
                receivedMessages.add(messageBody);
                numberMessages++;
            }
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            for (String s : receivedMessages) {
                inboxStyle.addLine(s);
            }
            int moreMessages = numberMessages - MAX_MESSAGES;
            if (moreMessages > 0) {
                inboxStyle.setSummaryText(getString(R.string.more_msg, moreMessages));
            }
            notificationBuilder
                    .setContentText(messageBody)
                    .setStyle(inboxStyle);
        }

        // Set the heads-up notification.
        if (heads_up) {
            notificationBuilder.setFullScreenIntent(PendingIntent.getActivity(this,
                    0, new Intent(), 0), true);
        }

        // Set a Pendingintent.
        if (intent != null) {
            notificationBuilder.setContentIntent(PendingIntent.getActivity(this,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        // Notify.
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    private Intent getPushIntent(String pkg_name) {
        if (pkg_name == null) {
            return null;
        }

        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkg_name);
        if (intent != null) {
            intent.setFlags(
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

    /**
     * Schedule a job using JobScheduler.
     */
    private void scheduleJob(int screenTimeout) {
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, MyJobService.class);
        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putInt("TIMEOUT", screenTimeout);
        JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                .setExtras(persistableBundle)
                .setOverrideDeadline(MAX_DELAY)
                .setRequiresDeviceIdle(true)
                .build();
        scheduler.schedule(jobInfo);
    }
}
