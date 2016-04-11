package com.bitwinger.crowdpuller.restapi.results;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nimesh on 25-01-2016.
 */
public class PostDetails {
    public class Post {
        public String Id;
        public String aId;
        public Integer active;
        public String addr;
        public Float angle;
        public Integer catid;
        public String crtdOn;
        public Integer exprd;
        public String expryDt;
        public Integer flggd;
        public Integer flggdRsn;
        public String hdr;
        public Double hghtInKM;
        public Float lat;
        public Float longi;
        public String modOn;
        public String msg;
        public Float neLat;
        public Float neLng;
        public Integer prfMaxAge;
        public Integer prfMinAge;
        public String prfSex;
        public Integer rspType;
        public Float swLat;
        public Float swLng;
        public Double wdthInKM;
    }
    public class PostFlags {
        public Integer flagCnt;
    }
    public class Responses {
        public Integer id;
        public Integer ord;
        public Integer rspCount;
        public String val;
    }
    public Post post;
    public PostFlags postFlags;
    public List<Responses> responses;

    public PostDetails(){
        this.post = new PostDetails.Post();
        this.postFlags = new PostDetails.PostFlags();
        this.responses = new ArrayList<Responses>();
    }
}
