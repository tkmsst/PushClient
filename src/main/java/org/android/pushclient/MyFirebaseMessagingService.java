/**
 * FirebaseMessagingService called when a notification is received.
 */

package org.android.pushclient;

import android.app.Notification;
import android.app.NotificationChannel;
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

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "default_channel";
    private static final String TAG = "PC:MessagingService";
    private static final int TIMEOUT_PERIOD = 10000;
    private static final int LAUNCH_DURATION = 10000;
    private static final int MAX_DELAY = 30000;
    private static final int MAX_MESSAGES = 5;

    /*
        flags[0] = launch_app;
        flags[1] = screen_on;
        flags[2] = end_off;
     */

    /**
     * Called when a message is received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        MyApplication myApplication = (MyApplication) getApplication();

        // Get push parameters.
        Map<String, String> data = remoteMessage.getData();

        // Send a notification.
        if (myApplication.importance > 0) {
            sendNotification(data);
        } else {
            return;
        }

        // Acquire a wake lock.
        int pmFlag;
        if (myApplication.flag[1]) {
            pmFlag = PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
        } else {
            pmFlag = PowerManager.PARTIAL_WAKE_LOCK;
        }
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(pmFlag, TAG);
        wakeLock.acquire(LAUNCH_DURATION);

        // Set screen off timeout.
        int screenTimeout = 0;
        if (myApplication.flag[2] && !powerManager.isInteractive()) {
            screenTimeout = Settings.System.getInt(
                    getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);
            if (screenTimeout > TIMEOUT_PERIOD) {
                Settings.System.putInt(
                        getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, TIMEOUT_PERIOD);
            } else {
                screenTimeout = 0;
            }
        }

        // Start the activity.
        Intent intent = getAppIntent(data.get("app"));
        if (myApplication.flag[0] && intent != null) {
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
     * Called when the message is deleted.
     */
    @Override
    public void onDeletedMessages() {
        Map<String, String> data = new HashMap<>();
        data.put("title", getString(R.string.app_name));
        data.put("msg", getString(R.string.msg_deleted));
        sendNotification(data);
    }

    /**
     * Called if InstanceID token is updated.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.i(TAG, "Refreshed token: " + token);

        // Persist and remove token at third-party servers.
        MyApplication myApplication = (MyApplication) getApplication();
        ServerAccess serverAccess = new ServerAccess(null);
        if (!myApplication.regid.isEmpty()) {
            serverAccess.register(myApplication.server_url, myApplication.regid, false);
        }
        serverAccess.register(myApplication.server_url, token, true);
        myApplication.storeRegid(token);
    }

    /**
     * Create and show a notification containing the received FCM message.
     */
    private void sendNotification(Map<String, String> data) {
        MyApplication myApplication = (MyApplication) getApplication();

        // Build a notification.
        Notification.Builder notificationBuilder =
                new Notification.Builder(this, MyFirebaseMessagingService.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setDeleteIntent(getDeleteIntent());

        // Set the title.
        final String title = data.get("title");
        if (title != null) {
            notificationBuilder.setContentTitle(title);
        }

        // Set the messages.
        final String message = data.get("msg");
        ConcurrentLinkedQueue<String> messageQueue = myApplication.getQueue();
        if (message != null) {
            if (!message.isEmpty()) {
                if (messageQueue.size() == MAX_MESSAGES) {
                    messageQueue.remove();
                    myApplication.incrementCounter();
                }
                messageQueue.add(message);
            }
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

        // Set a PendingIntent.
        Intent intent = getAppIntent(data.get("app"));
        if (intent != null) {
            notificationBuilder.setContentIntent(PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        // Notify.
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setImportance(myApplication.importance);
        channel.setShowBadge(false);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
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
        return PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
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
