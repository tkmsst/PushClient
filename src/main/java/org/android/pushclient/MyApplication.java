/**
 * Application to access common variables.
 */

package org.android.pushclient;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MyApplication extends Application {

    public static final int MAX_FLAG = 3;

    public volatile String regid;
    public volatile String server_url;
    public volatile int importance;
    public volatile boolean[] flag = new boolean[MAX_FLAG];

    private SharedPreferences prefs;
    private ConcurrentLinkedDeque<String> queue;
    private AtomicInteger counter;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getDefaultSharedPreferences(createDeviceProtectedStorageContext());
        regid = prefs.getString("regid", "");
        server_url = prefs.getString("server_url", "");
        importance = prefs.getInt("importance", 3);

        final boolean[] defaultFlag = {true, false, true};
        for (int i = 0; i < MAX_FLAG; i++) {
            final String str = String.valueOf(i + 1);
            flag[i] = prefs.getBoolean("flag" + str, defaultFlag[i]);
        }

        queue = new ConcurrentLinkedDeque<>();
        counter = new AtomicInteger(0);
    }

    public void storeRegid(String token) {
        if (token != null) {
            regid = token;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regid", token);
            editor.apply();
        }
    }

    public boolean storeAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regid", regid);
        editor.putString("server_url", server_url);
        editor.putInt("importance", importance);
        for (int i = 0; i < MAX_FLAG; i++) {
            final String str = String.valueOf(i + 1);
            editor.putBoolean("flag" + str, flag[i]);
        }
        return editor.commit();
    }

    public ConcurrentLinkedDeque<String> getQueue() {
        return queue;
    }

    public int getCounter() {
        return counter.get();
    }

    public void setCounter(int i) {
        counter.set(i);
    }

    public void incrementCounter() {
        counter.getAndIncrement();
    }
}
