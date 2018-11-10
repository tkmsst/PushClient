/**
 * Application to access common variables.
 */

package org.android.pushclient;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MyApplication extends Application {

    public static final int MAX_FLAG = 6;

    public volatile String regid;
    public volatile String server_url;
    public volatile boolean[] flag = new boolean[MAX_FLAG];

    private SharedPreferences prefs;
    private ConcurrentLinkedQueue<String> queue;
    private AtomicInteger counter;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getDefaultSharedPreferences(this);
        regid = prefs.getString("regid", "");
        server_url = prefs.getString("server_url", "");

        final boolean[] defaultFlag= {true, true, true, false, false, true};
        for(int i = 0; i < MAX_FLAG; i++) {
            String str = String.valueOf(i + 1);
            flag[i] = prefs.getBoolean("flag" + str, defaultFlag[i]);
        }

        queue = new ConcurrentLinkedQueue<>();
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
        for(int i = 0; i < MAX_FLAG; i++) {
            String str = String.valueOf(i + 1);
            editor.putBoolean("flag" + str, flag[i]);
        }
        return editor.commit();
    }

    public ConcurrentLinkedQueue<String> getQueue() {
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
