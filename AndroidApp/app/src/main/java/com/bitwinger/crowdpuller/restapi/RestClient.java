package com.bitwinger.crowdpuller.restapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.bitwinger.crowdpuller.Preprocessor;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by Nimesh on 27-01-2016.
 */
public class RestClient {
    public enum RequestMethod {
        GET(1),
        POST(2),
        PUT(3),
        PATCH(4);
        private int value;

        private RequestMethod(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String toString() {
            if (this.value == GET.value) {
                return "GET";
            } else if (this.value == POST.value) {
                return "POST";
            } else if (this.value == PUT.value) {
                return "PUT";
            } else if (this.value == PATCH.value) {
                return "PATCH";
            } else return null;
        }
    }

    public interface ResultCallback {
        void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData);
    }

    private static String TAG = "RestClient";
    private ResultCallback resultCallback = null;
    private Activity callingActivity = null;
    private String urlPath = null;
    private RequestMethod requestType = RequestMethod.GET;
    private String requestData = null;
    private int responseCode = -1;
    private String responseData = null;
    private String responseError = null;
    private String responseMessage = null;
    private Map<String, String> requestHeaders = null;
    private static Boolean HttpsOptionSet = false;
    private Boolean connectTimedOut = false;

    public static void setHttpsURLConnectionOptions(Context caller) {
        if (!HttpsOptionSet) {
            if (Preprocessor.DEBUG) {
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostName, SSLSession sslSession) {
                        return true;
                    }
                });
            }
            HttpsOptionSet = true;
        }
    }

    public void setResultCallback(ResultCallback callback) {
        this.resultCallback = callback;
    }

    public RestClient(Activity Caller, String URLPath, RequestMethod RequestType, Map<String, String> RequestHeaders, String RequestData) {
        callingActivity = Caller;
        urlPath = URLPath;
        requestType = RequestType;
        requestData = RequestData;
        requestHeaders = RequestHeaders;
        RestClient.setHttpsURLConnectionOptions(Caller);
    }

    public void execute() {
        new LongOperation().execute(urlPath);
    }

    // Class with extends AsyncTask class
    private class LongOperation extends AsyncTask<String, Void, Void> {
        // Required initialization
        private String exceptionMessage = null;
        private ProgressDialog Dialog = new ProgressDialog(callingActivity);
        String data = "";
        String uiUpdate = null;
        String jsonParsed = null;
        int sizeData = 0;
        String serverText = null;

        protected void onPreExecute() {
            // NOTE: You can call UI Element here.
            //Start Progress Dialog (Message)
            Dialog.setMessage("Please wait..");
            Dialog.show();
            try {
                // Set Request parameter
                data += "&" + URLEncoder.encode("data", "UTF-8") + "=" + serverText;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        private String getStringFromInputStream(InputStream in) {
            InputStream bin = null;
            String result = "";
            try {
                bin = new BufferedInputStream(in);
                // byte array to store input
                byte[] contents = new byte[1024];
                int bytesRead = 0;
                while ((bytesRead = bin.read(contents)) != -1) {
                    result = result + new String(contents, 0, bytesRead);
                }
            } catch (Exception ex) {
                exceptionMessage = ex.getMessage();
                ex.printStackTrace();
            } finally {
                try {
                    if (bin != null) {
                        bin.close();
                    }
                } catch (Exception ex) {
                }
            }
            return result;
        }

        // Call after onPreExecute method
        protected Void doInBackground(String... urls) {
            HttpsURLConnection urlConnection = null;
            // Send data
            try {
                Log.i("RestClient", "Starting");
                URL url = new URL(urls[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                if (Preprocessor.DEBUG) {
                    urlConnection.setSSLSocketFactory(CustomTrustManager.getSSLSocketFactory(callingActivity));
                }
                urlConnection.setUseCaches(false);
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                urlConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                if (requestHeaders != null) {
                    for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                        urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                switch (requestType) {
                    case GET:
                        //Do nothing here
                        break;
                    case POST:
                    case PUT:
                    case PATCH:
                        urlConnection.setRequestMethod(requestType.toString());
                        urlConnection.setDoInput(true);
                        urlConnection.setDoOutput(true);
                        //
                        Log.i("RestClient", "RequestData:" + requestData);
                        if (requestData != null) {
                            urlConnection.setFixedLengthStreamingMode(requestData.getBytes().length);
                            DataOutputStream out = null;
                            try {
                                out = new DataOutputStream(urlConnection.getOutputStream());
                                out.write(requestData.getBytes());
                                //out.writeChars(URLEncoder.encode(requestData, "UTF-8"));
                            } finally {
                                if (out != null) {
                                    out.flush();
                                    out.close();
                                }
                            }
                        }
                        urlConnection.connect();
                        break;
                    default:
                        throw new Exception("This request type is not supported.");
                }
                //
                responseCode = urlConnection.getResponseCode();
                responseMessage = urlConnection.getResponseMessage();
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    responseData = getStringFromInputStream(urlConnection.getInputStream());
                    Log.i("RestClient", "ResponseData:" + responseData);
                } else {
                    responseError = getStringFromInputStream(urlConnection.getErrorStream());
                    Log.i("RestClient", "ResponseError:" + responseError);
                }
                Log.i("RestClient", "ResponseCode:" + Integer.toString(responseCode));
                Log.i("RestClient", "ResponseMessage:" + responseMessage);
            } catch (java.net.SocketTimeoutException ex) {
                connectTimedOut = true;
                exceptionMessage = ex.getMessage();
                ex.printStackTrace();
            } catch (Exception ex) {
                exceptionMessage = ex.getMessage();
                ex.printStackTrace();
            } finally {
                try {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                } catch (Exception ex) {
                }
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            // NOTE: You can call UI Element here.
            // Close progress dialog
            Dialog.dismiss();
            if (exceptionMessage != null) {
                if (connectTimedOut) {
                    Toast.makeText(callingActivity, "Application could not connect to the server. " +
                            "Please check your network connectivity and retry.", Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, "onPostExecute : " + exceptionMessage);
            } else {
                // Show Response Json On Screen (activity)
                uiUpdate = responseData;
                if (resultCallback != null) {
                    resultCallback.onCallComplete(responseCode, responseMessage, (responseData != null ? responseData : responseError));
                }
            }
        }
    }
}