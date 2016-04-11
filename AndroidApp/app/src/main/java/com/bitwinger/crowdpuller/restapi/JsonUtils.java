package com.bitwinger.crowdpuller.restapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Created by Nimesh on 25-01-2016.
 */
public class JsonUtils {
    private static Gson gson = new GsonBuilder().serializeNulls().create();
    //
    public JsonUtils(){

    }

    public static <T> T getFromJson(String jsonString, Class<T> classType){
        return gson.fromJson(jsonString, classType);
    }

    public static <T> T getFromJson(String jsonString, Type type){
        return gson.fromJson(jsonString, type);
    }
    public static String toJson(Object objData){
        return gson.toJson(objData);
    }
}
