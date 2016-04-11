package com.bitwinger.crowdpuller.masters;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nimesh on 06-02-2016.
 */
public class ResponseTypes {
    private static Map<Integer, String> list = new LinkedHashMap<Integer, String>();

    public static Map<Integer, String> getList() {
        return list;
    }

    public static void populate() {
        if (list.size() == 0) {
            list.put(0, "None");
            list.put(1, "Yes/No/Don't Know");
            list.put(2, "Ratings (1-5 *)");
            list.put(3, "Like/Dislike/Neutral");
        }
    }

    public static CharSequence[] getArrayList(){
        return list.values().toArray(new CharSequence[list.size()]);
    }
}