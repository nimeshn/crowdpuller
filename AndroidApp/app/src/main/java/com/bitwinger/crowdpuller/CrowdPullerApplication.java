package com.bitwinger.crowdpuller;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.bitwinger.crowdpuller.masters.Categories;
import com.bitwinger.crowdpuller.masters.FlagReasons;
import com.bitwinger.crowdpuller.masters.ResponseTypes;
import com.bitwinger.crowdpuller.restapi.results.UserSessionDetails;
import com.facebook.FacebookSdk;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Nimesh on 24-01-2016.
 */
public class CrowdPullerApplication extends Application {
    private String deepLink = null;
    private String fbToken = null;
    private String gpToken = null;
    private UserSessionDetails sessionDetails = null;

    public String getDeepLink(){
        return deepLink;
    }

    public void setDeepLink(String value){
        deepLink = value;
    }

    public String getGpToken(){
        return gpToken;
    }

    public void setGpToken(String value){
        gpToken = value;
    }

    public String getFbToken(){
        return fbToken;
    }

    public void setFbToken(String value){
        fbToken = value;
    }

    public UserSessionDetails getSessionDetails() {
        return sessionDetails;
    }

    public void setSessionDetails(UserSessionDetails sessionDetails) {
        this.sessionDetails = sessionDetails;
    }

    public Boolean isLoggedIn() {
        return (sessionDetails != null && sessionDetails.sessionId != null && !sessionDetails.sessionId.isEmpty());
    }

    public void logOutUser() {
        sessionDetails = null;
        gpToken = null;
        fbToken = null;
    }

    public String getAPIPath(String path) {
        return (Preprocessor.DEBUG ? getString(R.string.crowdpuller_debug_api_server) : getString(R.string.crowdpuller_release_api_server)) +
                getString(R.string.crowdpuller_api_path) +
                path;
    }

    public String getWebURL(String path) {
        return (Preprocessor.DEBUG ? getString(R.string.crowdpuller_debug_web_url) : getString(R.string.crowdpuller_release_web_url)) +
                path;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        //
        Categories.fillData();
        FlagReasons.populate();
        ResponseTypes.populate();
        //
        getAppKeyHash();
    }

    public String getAppKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.bitwinger.crowdpuller",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashkey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash:", hashkey);
                return hashkey;
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        return null;
    }
}