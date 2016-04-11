package com.bitwinger.crowdpuller.restapi.results;

import java.util.List;

/**
 * Created by Nimesh on 25-01-2016.
 */
public class FeedDetails {
    public class Post {
        public String FN;
        public String Id;
        public String addr;
        public String catcode;
        public String crtdOn;
        public String emlId;
        public String hdr;
        public String msg;
        public Integer rsnFlggd;
        public Integer rspDtlId;
        public Integer rspType;
        public Integer shareCI;
    }
    public class PostFlags{
        public Integer flagCnt;
    }
    public class Responses{
        public Integer id;
        public Integer ord;
        public Integer rspCount;
        public String val;
    }
    public Post post;
    public PostFlags postFlags;
    public List<Responses> responses;
}
