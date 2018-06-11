/**
 * BroadcastReceiver called when the notification is cleared.
 */

package org.android.pushclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.android.pushclient.MyApplication;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyApplication myApplication = (MyApplication) context.getApplicationContext();
        myApplication.getQueue().clear();
        myApplication.setCounter(0);
    }
}
