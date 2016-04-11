package com.bitwinger.crowdpuller.restapi.requests;

/**
 * Created by Nimesh on 28-01-2016.
 */
public class LoginRequest {
    public String fbToken;
    public String gpToken;

    public LoginRequest(String FBToken, String GPToken) {
        this.fbToken = (FBToken == null ? "" : FBToken);
        this.gpToken = (GPToken == null ? "" : GPToken);
    }
}
