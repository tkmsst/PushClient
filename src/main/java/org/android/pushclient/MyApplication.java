/**
 * Application to access common variables.
 */

package org.android.pushclient;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class MyApplication extends Application {

    private SharedPreferences prefs;
    private String server_url;
    private String regid;
    private Map<String, Boolean> map;
    private ConcurrentLinkedQueue<String> queue;
    private AtomicInteger counter;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getDefaultSharedPreferences(this);
        server_url = prefs.getString("server_url", "");
        regid = prefs.getString("regid", "");
        map = new HashMap<>();
        queue = new ConcurrentLinkedQueue<>();
        counter = new AtomicInteger(0);

        putAll();
    }

    private void putAll() {
        map.put("launch_app", prefs.getBoolean("launch_app", true));
        map.put("notif_msg", prefs.getBoolean("notif_msg", true));
        map.put("notif_sound", prefs.getBoolean("notif_sound", true));
        map.put("heads_up", prefs.getBoolean("heads_up", false));
        map.put("screen_on", prefs.getBoolean("screen_on", false));
        map.put("end_off", prefs.getBoolean("end_off", true));
    }

    public synchronized String manageServerUrl(String url) {
        if (url != null) {
            server_url = url;
        }
        return server_url;
    }

    public synchronized String manageRegid(String token) {
        if (token != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("regid", token);
            editor.apply();
            regid = token;
        }
        return regid;
    }

    public Boolean get(String key) {
        return map.get(key);
    }

    public void put(String key, Boolean value) {
        map.put(key, value);
    }

    public boolean storeAll() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("server_url", manageServerUrl(null));
        editor.putString("regid", manageRegid(null));
        for (String s : map.keySet()) {
            editor.putBoolean(s, map.get(s));
        }
        return editor.commit();
    }

    public ConcurrentLinkedQueue<String> getQueue() {
        return queue;
    }

    public int getCounter() {
        return counter.intValue();
    }

    public void setCounter(int i) {
        counter.set(i);
    }

    public void incrementCounter() {
        counter.getAndIncrement();
    }
}
