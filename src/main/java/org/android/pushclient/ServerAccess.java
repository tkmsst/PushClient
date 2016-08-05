/**
 * Server access for registration tokens.
 */

package org.android.pushclient;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ServerAccess {

    private static final String PROTOCOL = "http";

    public static void register(String serverurl, String token) {
        if (!serverurl.isEmpty()) {
            String body = "register=" + token;
            AsyncPost(serverurl, body);
        }
    }

    public static void unregister(String serverurl, String token) {
        if (!serverurl.isEmpty()) {
            String body = "unregister=" + token;
            AsyncPost(serverurl, body);
        }
    }

    private static void AsyncPost(String serverurl, String body) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                String message = "";
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
                    con.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset=UTF-8");
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
                MainActivity.mDisplay.setText(message);
            }
        }.execute(serverurl, body);
    }
}
