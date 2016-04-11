package com.bitwinger.crowdpuller.restapi.requests;

/**
 * Created by Nimesh on 28-01-2016.
 */
public class DeletePostRequest {
    public String Id;

    public DeletePostRequest(String PostId) {
        this.Id = PostId;
    }
}
