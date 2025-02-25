/**
 * Application to access common variables.
 */

package org.android.pushclient;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class MyApplication extends Application {

    public volatile boolean changeable;
    public volatile String reg_id;
    public volatile String server_url;
    public volatile boolean[] flags = {true, false, true};

    private SharedPreferences prefs;
    private ConcurrentLinkedDeque<String> queue;
    private AtomicInteger counter;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(createDeviceProtectedStorageContext());
        reg_id = prefs.getString("regid", "");
        server_url = prefs.getString("server_url", "");
        for (int i = 0; i < flags.length; i++) {
            final String str = String.valueOf(i + 1);
            flags[i] = prefs.getBoolean("flag" + str, flags[i]);
        }

        queue = new ConcurrentLinkedDeque<>();
        counter = new AtomicInteger(0);
    }

    public void storeRegid(String token) {
        if (token != null) {
            reg_id = token;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regid", token);
            editor.apply();
        }
    }

    public boolean storeAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regid", reg_id);
        editor.putString("server_url", server_url);
        for (int i = 0; i < flags.length; i++) {
            final String str = String.valueOf(i + 1);
            editor.putBoolean("flag" + str, flags[i]);
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
