/**
 * ServerAccess to register or unregister token.
 */

package org.android.pushclient;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerAccess {

    private MainActivity mainActivity;

    public ServerAccess(MainActivity context) {
        mainActivity = context;
    }

    public void register(String server_url, String token, boolean reg) {
        if (server_url == null || token == null) {
            return;
        } else if (server_url.isEmpty() || token.isEmpty()) {
            return;
        }
        String body;
        if (reg) {
            body = "register=";
        } else {
            body = "unregister=";
        }
        body += token;
        new AsyncPost(mainActivity).execute(server_url, body);
    }

    private static class AsyncPost extends AsyncTask<String, Void, String> {
        private static final String PROTOCOL = "http";
        private WeakReference<MainActivity> activityReference;

        // Only retain a weak reference to the activity.
        public AsyncPost(MainActivity mainActivity) {
            activityReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected String doInBackground(String... params) {
            String message;
            String endpoint = params[0];
            if (!endpoint.startsWith(PROTOCOL)) {
                endpoint = PROTOCOL + "://" + endpoint;
            }
            URL url;
            try {
                url = new URL(endpoint);
            } catch (MalformedURLException e) {
                message = "Invalid URL: " + endpoint;
                return message;
            }

            byte bodyByte[] = params[1].getBytes();
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
                con.setFixedLengthStreamingMode(bodyByte.length);
                con.setRequestMethod("POST");
                // Post the request.
                OutputStream out = con.getOutputStream();
                out.write(bodyByte);
                out.flush();
                out.close();
                // Handle the response.
                final int status = con.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStream in = con.getInputStream();
                    byte response[] = new byte[1024];
                    if (in.read(response) > 0) {
                        message = new String(response, "UTF-8");
                    } else {
                        message = "No response from the server.";
                    }
                    in.close();
                } else {
                    message = "The server responded with an error: " + status;
                }
            } catch (IOException e) {
                message ="Failed to connect to the server.";
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
            return message;
        }

        @Override
        protected void onPostExecute(String message) {
            // Get a reference to the activity if it is still there.
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            // Show the message on the activity's UI.
            TextView textView = activity.findViewById(R.id.view1);
            textView.setText(message);
        }
    }
}
