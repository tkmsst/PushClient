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
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
            URL url;
            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                message = "Invalid URL: " + params[0];
                return message;
            }

            disableSSLCertificateChecking();
            HttpURLConnection con = null;
            try {
                byte[] bodyByte = params[1].getBytes();
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
                    byte[] response = new byte[1024];
                    if (in.read(response) > 0) {
                        message = new String(response, StandardCharsets.UTF_8);
                    } else {
                        message = "No response from the server.";
                    }
                    in.close();
                } else {
                    message = "The server responded with an error: " + status;
                }
            } catch (IOException e) {
                message = "Failed to connect to the server.";
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

    private static void disableSSLCertificateChecking() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        TrustManager[] tm = {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        try {
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, tm, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        } catch (Exception e) {
        }
    }
}
