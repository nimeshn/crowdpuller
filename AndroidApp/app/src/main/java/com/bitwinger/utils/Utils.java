package com.bitwinger.utils;

import android.app.Activity;
import android.app.AlertDialog;

import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.results.ErrorData;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Nimesh on 05-02-2016.
 */
public class Utils {
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate a value suitable for use in {@link #setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void showErrorDialog(Activity activity, Integer ErrorNo, String ErrorData){
        if (ErrorNo == 404) {
            ErrorData errorData = JsonUtils.getFromJson(ErrorData, ErrorData.class);
            new AlertDialog.Builder(activity)
                    .setTitle("Application Error")
                    .setItems(errorData.errors.toArray(new CharSequence[errorData.errors.size()]), null)
                    .setIcon(android.R.drawable.stat_notify_error)
                    .setNeutralButton("Ok", null)
                    .show();
        }
        else if (ErrorNo == 500){
            new AlertDialog.Builder(activity)
                    .setTitle("Application Error")
                    .setMessage("Application could not process your request due to a server problem." +
                            " Please try again in few minutes.")
                    .setIcon(android.R.drawable.stat_notify_error)
                    .setNeutralButton("Ok", null)
                    .show();
        }
        else if (ErrorNo == 401){
            new AlertDialog.Builder(activity)
                    .setTitle("Application Error")
                    .setMessage("You are not logged in or your login session might have timedout." +
                            " You will need to relogin to continue.")
                    .setIcon(android.R.drawable.stat_notify_error)
                    .setNeutralButton("Ok", null)
                    .show();
        }
    }
}