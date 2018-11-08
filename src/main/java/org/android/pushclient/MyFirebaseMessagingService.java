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
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "PC:MessagingService";

    private static final int TIMEOUT_PERIOD = 10000;
    private static final int LAUNCH_DURATION = 15000;
    private static final int MAX_DELAY= 30000;
    private static final int MAX_MESSAGES = 5;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        MyApplication myApplication = (MyApplication) getApplication();

        // Get push parameters.
        Map<String, String> data = remoteMessage.getData();

        // Get the app intent.
        Intent intent = getAppIntent(data.get("app"));

        // Send a notification.
        if (myApplication.notif_msg ||
            myApplication.notif_sound ||
            myApplication.heads_up) {
            sendNotification(data, intent);
        }

        if (!myApplication.launch_app && !myApplication.screen_on) {
            return;
        }

        // Set screen off timeout.
        int screenTimeout = 0;
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (myApplication.end_off && !powerManager.isInteractive()) {
            screenTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, 0);
            if (screenTimeout > TIMEOUT_PERIOD) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SCREEN_OFF_TIMEOUT, TIMEOUT_PERIOD);
            }
        }

        // Acquire a wake lock.
        int pmFlag = PowerManager.ACQUIRE_CAUSES_WAKEUP;
        if (myApplication.screen_on) {
            pmFlag |= PowerManager.FULL_WAKE_LOCK;
        } else {
            pmFlag |= PowerManager.PARTIAL_WAKE_LOCK;
        }
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(pmFlag, TAG);
        wakeLock.acquire(LAUNCH_DURATION);

        // Start the activity.
        if (myApplication.launch_app && intent != null) {
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS |
                    Intent.FLAG_ACTIVITY_NO_ANIMATION |
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION);
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

    /**
     * Called when message is deleted.
     */
    @Override
    public void onDeletedMessages() {
        MyApplication myApplication = (MyApplication) getApplication();

        Map<String, String> data = new HashMap<>();
        data.put("title", getString(R.string.app_name));
        data.put("msg", getString(R.string.msg_deleted));
        sendNotification(data, null);
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        MyApplication myApplication = (MyApplication) getApplication();

        Log.i(TAG, "Refreshed token: " + token);

        // Get old token.
        String regid = myApplication.getRegid();

        // Persist and remove token at third-party servers.
        ServerAccess serverAccess = new ServerAccess(null);
        if (!regid.isEmpty()) {
            serverAccess.register(myApplication.server_url, regid, false);
        }
        serverAccess.register(myApplication.server_url, token, true);
        myApplication.storeRegid(token);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param data FCM data object received.
     */
    private void sendNotification(Map<String, String> data, Intent intent) {
        MyApplication myApplication = (MyApplication) getApplication();

        // Set title and message.
        String title = data.get("title");
        String message = data.get("msg");

        // Set defaults.
        int defaultsFlag = Notification.DEFAULT_LIGHTS;
        if (myApplication.notif_sound) {
            defaultsFlag |= Notification.DEFAULT_SOUND;
        }

        // Build a notification.
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setCategory(Notification.CATEGORY_CALL)
                .setDeleteIntent(getDeleteIntent())
                .setDefaults(defaultsFlag);

        // Set notification title.
        if (title != null) {
            notificationBuilder.setContentTitle(title);
        }

        // Set notification messages.
        if (myApplication.notif_msg && message != null) {
            ConcurrentLinkedQueue<String> messageQueue = myApplication.getQueue();
            if (!message.isEmpty()) {
                notificationBuilder.setContentText(message);
                if (messageQueue.size() == MAX_MESSAGES) {
                    messageQueue.remove();
                    myApplication.incrementCounter();
                }
                messageQueue.add(message);
            }
            if (messageQueue.size() > 0) {
                Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
                for (String s : messageQueue) {
                    inboxStyle.addLine(s);
                }
                int messageCounter = myApplication.getCounter();
                if (messageCounter > 0) {
                    inboxStyle.setSummaryText(getString(R.string.more_msg, messageCounter));
                }
                notificationBuilder.setStyle(inboxStyle);
            }
        }

        // Set the heads-up notification.
        if (myApplication.heads_up) {
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

    private Intent getAppIntent(String pkg_name) {
        if (pkg_name == null) {
            return null;
        }

        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(pkg_name);
        if (intent != null) {
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
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
