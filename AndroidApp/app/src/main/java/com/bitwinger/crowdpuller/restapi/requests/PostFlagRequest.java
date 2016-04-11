package com.bitwinger.crowdpuller.restapi.requests;

/**
 * Created by Nimesh on 28-01-2016.
 */
public class PostFlagRequest {
    public String pId;
    public String rId;

    public PostFlagRequest(String postId, String responderId) {
        this.pId = postId;
        this.rId = responderId;
    }
}
