package com.bitwinger.utils;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.bitwinger.crowdpuller.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nimesh on 07-02-2016.
 */
public class FetchAddressIntentService  extends IntentService {

    protected ResultReceiver resultReceiver;
    private static final String TAG = "FetchAddyIntentService";

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        resultReceiver = intent.getParcelableExtra(Constants.RECEIVER);
        LatLng location = intent.getParcelableExtra(
                Constants.LOCATION_DATA_EXTRA);

        try {
            addresses = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1);
        } catch (IOException ioException) {
            errorMessage = "Service Not Available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            errorMessage = "Invalid Latitude or Longitude Used";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.latitude + ", Longitude = " +
                    location.longitude, illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "Not Found";
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage, null);
        } else {
            for (Address address : addresses) {
                String outputAddress = "";
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    outputAddress += " --- " + address.getAddressLine(i);
                }
            }
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getString(R.string.address_found));
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(",", addressFragments), address);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        resultReceiver.send(resultCode, bundle);
    }
}