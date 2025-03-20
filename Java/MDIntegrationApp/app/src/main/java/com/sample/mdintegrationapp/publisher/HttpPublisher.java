package com.sample.mdintegrationapp.publisher;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class HttpPublisher {
    private static final String TAG = HttpPublisher.class.getSimpleName();
    private static final String SSL_PROTOCOL = "TLSv1.2";
    private static final SSLContext sslContext;
    private static PublisherSettings publisherSettings;
    private static String authCredentials = null;

    static {
        try {
            sslContext = SSLContext.getInstance(SSL_PROTOCOL);
            sslContext.init(null, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPublisherSettings(PublisherSettings settings) {
        publisherSettings = settings;
        if (publisherSettings.getNumRetries() < 0)
            publisherSettings.setNumRetries(0);
        if (!publisherSettings.getUsername().isEmpty() && !publisherSettings.getPassword().isEmpty())
            //Base64-encode username and password
            authCredentials = "Basic " + java.util.Base64.getEncoder().encodeToString(
                    (publisherSettings.getUsername() + ":" + publisherSettings.getPassword()).getBytes());
        else
            authCredentials = null;
    }

    /**
     * Send HTTP request to endpoint
     *
     * @param request  the HTTP request
     * @param listener the listener to receive the response
     */
    public static void sendRequest(HttpRequest request, HttpRequestResponseListener listener) {
        new Thread(() -> {
            HttpResponse response = sendRequest(request, publisherSettings.getNumRetries());
            listener.onHttpRequestResponse(response);
        }).start();
    }

    /**
     * Send HTTP request to endpoint
     *
     * @param request the HTTP request
     * @param @return HttpResponse
     */
    public static HttpResponse sendRequest(HttpRequest request, int numRetries) {
        HttpResponse response = sendRequest(request);
        //Get Response code (200-299 = success)
        if ((response.getResponseCode() >= 200 && response.getResponseCode() <= 299) || numRetries == 0)
            return response;
        try {
            Thread.sleep(publisherSettings.getRetryDelay());
        } catch (InterruptedException ignored) {
        }
        return sendRequest(request, --numRetries);
    }

    private static HttpResponse sendRequest(HttpRequest request) {
        if (request.getData() != null)
            Log.d(TAG, "sendRequest() to " + request.getUrl() + " with data: " + request.getData());
        else
            Log.d(TAG, "sendRequest() to " + request.getUrl());
        HttpResponse response = new HttpResponse(request);
        URL obj = null;
        try {
            obj = new URL(request.getUrl());
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid URL " + request.getUrl(), e);
            response.setResponse("Invalid URL");
            return response;
        }
        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) obj.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Couldn't open connection to URL " + request.getUrl(), e);
            response.setResponse("Couldn't open connection");
            return response;
        }
        try (AutoCloseable ignored = con::disconnect) {
            //Send data to host
            if (con instanceof HttpsURLConnection)
                ((HttpsURLConnection) con).setSSLSocketFactory(sslContext.getSocketFactory());
            con.setConnectTimeout(publisherSettings.getConnectTimeout());
            con.setReadTimeout(publisherSettings.getReadTimeout());
            if (authCredentials != null)
                con.setRequestProperty("Authorization", authCredentials);
            if (request.getData() != null) {
                if (request.getContentType() != null)
                    con.setRequestProperty("Content-Type", request.getContentType());
                con.setDoOutput(true);
                con.setRequestMethod("POST");
                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.writeBytes(request.getData());
                    wr.flush();
                }
            }

            //Get Response code (200-299 = success)
            int responseCode = con.getResponseCode();
            response.setResponseCode(responseCode);
            Log.d(TAG, con.getRequestMethod() + " response: " + responseCode);

            //Fetch result
            try (BufferedReader in = (responseCode >= 400 ? new BufferedReader(new InputStreamReader(con.getErrorStream())) : new BufferedReader(new InputStreamReader(con.getInputStream())))) {
                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    Log.w(TAG, inputLine);
                    sb.append(inputLine);
                }
                response.setResponse(sb.toString());
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to send request", e);
            response.setResponse("Exception " + e);
            response.setResponseCode(0);
        }
        return response;
    }
}