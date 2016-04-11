package com.bitwinger.crowdpuller.restapi.requests;

/**
 * Created by Nimesh on 28-01-2016.
 */
public class PostResponseRequest {
    public String pId;
    public String rId;
    public int rspDtlId;
    public String notes;

    public PostResponseRequest(String postId, String responderId, int responseId, String Notes) {
        this.pId = postId;
        this.rId = responderId;
        this.rspDtlId = responseId;
        this.notes = Notes;
    }
}