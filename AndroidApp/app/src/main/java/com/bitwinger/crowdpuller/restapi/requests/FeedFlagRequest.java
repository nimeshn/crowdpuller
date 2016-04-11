package com.bitwinger.crowdpuller.restapi.requests;

/**
 * Created by Nimesh on 28-01-2016.
 */
public class FeedFlagRequest {
    public String pId;
    public String mId;
    public int flag;
    public FeedFlagRequest(String postId, String responderId, int Flag) {
        this.pId = postId;
        this.mId = responderId;
        this.flag = Flag;
    }
}
