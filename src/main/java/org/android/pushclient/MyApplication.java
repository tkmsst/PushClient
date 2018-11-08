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

    public volatile String server_url;
    public volatile Boolean launch_app;
    public volatile Boolean notif_msg;
    public volatile Boolean notif_sound;
    public volatile Boolean heads_up;
    public volatile Boolean screen_on;
    public volatile Boolean end_off;

    private SharedPreferences prefs;
    private volatile String regid;
    private ConcurrentLinkedQueue<String> queue;
    private AtomicInteger counter;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = getDefaultSharedPreferences(this);
        regid = prefs.getString("regid", "");
        server_url = prefs.getString("server_url", "");
        launch_app = prefs.getBoolean("launch_app", true);
        notif_msg = prefs.getBoolean("notif_msg", true);
        notif_sound = prefs.getBoolean("notif_sound", true);
        heads_up = prefs.getBoolean("heads_up", false);
        screen_on = prefs.getBoolean("screen_on", false);
        end_off = prefs.getBoolean("end_off", true);

        queue = new ConcurrentLinkedQueue<>();
        counter = new AtomicInteger(0);
    }

    public String getRegid() {
        return regid;
    }

    public void storeRegid(String token) {
        if (token != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regid", token);
            editor.apply();
            regid = token;
        }
    }

    public boolean storeAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("regid", regid);
        editor.putString("server_url", server_url);
        editor.putBoolean("launch_app", launch_app);
        editor.putBoolean("notif_msg", notif_msg);
        editor.putBoolean("notif_sound", notif_sound);
        editor.putBoolean("heads_up", heads_up);
        editor.putBoolean("screen_on", screen_on);
        editor.putBoolean("end_off", end_off);
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
