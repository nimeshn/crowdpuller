package com.bitwinger.crowdpuller.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.MapHelper;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.results.UserProfile;
import com.bitwinger.utils.Constants;
import com.bitwinger.utils.FetchAddressIntentService;
import com.bitwinger.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A Fragment that displays a Login/Logout button as well as the user's
 * profile picture and name when logged in.
 */
public final class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String DATA_KEY = "PROFILE_DATA";

    private ImageButton btn_pick_user_location;
    private TextView txt_user_address;
    private EditText edit_full_name;
    private EditText edit_email;
    private EditText edit_birth_year;
    private ImageButton btn_pick_birth_year;
    private RadioButton rb_profile_male;
    private RadioButton rb_profile_female;
    private CheckBox chk_share_contact;
    private Button btn_save_profile;
    private Button btn_cancel_profile;
    private ScrollView svprofile_details;
    private NumberPicker birth_year_picker;
    private AddressResultReceiver mResultReceiver;

    private UserProfile userProfile;
    private MainActivity activity;
    private MapHelper mapHelper;
    private MapHelper.CallbackHandler callbackHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile, container, false);

        btn_pick_user_location = (ImageButton) view.findViewById(R.id.btn_pick_user_location);
        txt_user_address = (TextView) view.findViewById(R.id.txt_user_address);
        edit_full_name = (EditText) view.findViewById(R.id.edit_full_name);
        edit_email = (EditText) view.findViewById(R.id.edit_email);
        edit_birth_year = (EditText) view.findViewById(R.id.edit_birth_year);
        btn_pick_birth_year = (ImageButton) view.findViewById(R.id.btn_pick_birth_year);
        rb_profile_male = (RadioButton) view.findViewById(R.id.rb_profile_male);
        rb_profile_female = (RadioButton) view.findViewById(R.id.rb_profile_female);
        chk_share_contact = (CheckBox) view.findViewById(R.id.chk_share_contact);
        btn_save_profile = (Button) view.findViewById(R.id.btn_save_profile);
        btn_cancel_profile = (Button) view.findViewById(R.id.btn_cancel_profile);
        svprofile_details = (ScrollView) view.findViewById(R.id.svprofile_details);

        callbackHandler = new MapHelper.CallbackHandler() {
            @Override
            public void UpdateLocation(LatLng latLng) {
                getAddressByIntentService(latLng);
                userProfile.lat = (float) latLng.latitude;
                userProfile.longi = (float) latLng.longitude;
                Log.d(TAG, "UpdateLocation: " + userProfile.lat + ":" + userProfile.longi);
            }

            @Override
            public void UpdateBounds(LatLngBounds latLngBounds, double rectHeight, double rectWidth) {

            }
        };
        mapHelper = new MapHelper(getActivity(), MainActivity.PROFILE);
        //
        btn_pick_user_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        final int currYear = Calendar.getInstance().get(Calendar.YEAR);
        birth_year_picker = new NumberPicker(getActivity());
        birth_year_picker.setMinValue(currYear - 100);
        birth_year_picker.setMaxValue(currYear - 16);
        View.OnClickListener pickBirthYearListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                birth_year_picker.setValue(userProfile.bYr == null ? currYear - 16 : userProfile.bYr);
                if (birth_year_picker.getParent() != null) {
                    ((FrameLayout) birth_year_picker.getParent()).removeView(birth_year_picker);
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("Pick Minimum Age")
                        .setView(birth_year_picker)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Integer newVal = birth_year_picker.getValue();
                                edit_birth_year.setText(Integer.toString(newVal));
                                edit_birth_year.setError(null);
                                userProfile.bYr = newVal;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        btn_pick_birth_year.setOnClickListener(pickBirthYearListener);
        edit_birth_year.setOnClickListener(pickBirthYearListener);
        //
        View.OnClickListener genderClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userProfile.sex = (rb_profile_male.isChecked() ? "M" : (rb_profile_female.isChecked() ? "F" : null));
            }
        };
        rb_profile_male.setOnClickListener(genderClickListener);
        rb_profile_female.setOnClickListener(genderClickListener);
        //
        btn_save_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();
            }
        });
        btn_cancel_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelProfile();
            }
        });
        //
        mResultReceiver = new AddressResultReceiver(new Handler());
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        if (this.userProfile != null) {
            saveValueState();
            outState.putString(DATA_KEY, JsonUtils.toJson(userProfile));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            userProfile = JsonUtils.getFromJson(savedInstanceState.getString(DATA_KEY), UserProfile.class);
            if (userProfile != null) {
                bindData(this.userProfile);
            }
        }
    }

    private void bindData(UserProfile userProfile) {
        this.userProfile = userProfile;
        //
        svprofile_details.scrollTo(0, 0);
        edit_full_name.setError(null);
        edit_email.setError(null);
        edit_birth_year.setError(null);

        txt_user_address.setText(userProfile.addr);
        edit_full_name.setText(userProfile.FN);
        edit_email.setText(userProfile.emlId);
        if (userProfile.bYr != null) {
            edit_birth_year.setText(Integer.toString(userProfile.bYr));
        }
        rb_profile_male.setChecked(userProfile.sex.equalsIgnoreCase("M"));
        rb_profile_female.setChecked(userProfile.sex.equalsIgnoreCase("F"));
        chk_share_contact.setChecked(userProfile.shareCI == 1);
        if (getApp().getSessionDetails().NewSignUp == 1) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("New User")
                    .setMessage("Welcome to Crowdpuller. Please select your current location and fill in profile details to start using application.")
                    .show();
        }
    }

    public void getProfileData() {
        String urlPath = getApp().getAPIPath("/member/") +
                getApp().getSessionDetails().memberId;
        RestClient restClient = new RestClient(activity, urlPath,
                RestClient.RequestMethod.GET, null, null);
        restClient.setResultCallback(new RestClient.ResultCallback() {
            @Override
            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                if (ResponseCode == 200) {
                    if (ResponseData != null && !ResponseData.isEmpty()) {
                        Log.i("getProfileData", "profile: " + ResponseData);
                        bindData(JsonUtils.getFromJson(ResponseData, UserProfile.class));
                        Log.i("getProfileData", "Address: " + userProfile.addr);
                    }
                }
            }
        });
        restClient.execute();
    }

    private void saveValueState(){
        userProfile.addr = txt_user_address.getText().toString();
        userProfile.FN = edit_full_name.getText().toString();
        userProfile.emlId = edit_email.getText().toString();
        userProfile.shareCI = chk_share_contact.isChecked() ? 1 : 0;
    }

    private void saveProfile() {
        Boolean isError = false;
        if (edit_full_name.getText().length() == 0) {
            edit_full_name.setError("Name is required.");
            isError = true;
        }
        if (edit_email.getText().length() == 0) {
            edit_email.setError("EmailId is required.");
            isError = true;
        }
        if (edit_birth_year.getText().length() == 0) {
            edit_birth_year.setError("Birth year is required.");
            isError = true;
        }
        if (isError) {
            return;
        }
        //
        saveValueState();
        if (getApp().isLoggedIn()) {
            String urlPath = getApp().getAPIPath("/member");
            Map headers = new HashMap<String, String>();
            headers.put("AccToken", getApp().getSessionDetails().sessionId);

            RestClient restClient = new RestClient(activity, urlPath,
                    RestClient.RequestMethod.PUT,
                    headers, JsonUtils.toJson(userProfile));
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        getApp().getSessionDetails().NewSignUp = 0;
                        getApp().getSessionDetails().FN = userProfile.FN;
                        getApp().getSessionDetails().memberAddr = userProfile.addr;
                        //
                        if (getApp().getDeepLink() != null) {//if its deep link URL
                            (activity).showFeedDetailsFragment(getApp().getDeepLink(), true);
                            getApp().setDeepLink(null);
                        } else {
                            activity.showFeedsFragment(false);
                        }
                    } else {
                        Utils.showErrorDialog(getActivity(), ResponseCode, ResponseData);
                        if (ResponseCode == 401) {
                            activity.showSplashFragment(false);
                        }
                    }
                }
            });
            restClient.execute();
        }
    }

    public void cancelProfile() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Exit Profile")
                .setMessage("Do you want to exit profile screen? Doing so, you may loose any information already entered on this screen.")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.showFeedsFragment(false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Save & Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveProfile();
                    }
                })
                .show();
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void getAddressByIntentService(LatLng location) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(activity, FetchAddressIntentService.class);
        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        activity.startService(intent);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            // Display the address string or an error message sent from the intent service.
            Log.d(TAG, "onReceiveResult: " + userProfile.addr);
            userProfile.addr = resultData.getString(Constants.RESULT_DATA_KEY);
            txt_user_address.setText(userProfile.addr);
        }
    }

    public CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) activity.getApplication());
    }

    public MapHelper getMapHelper(){
        return mapHelper;
    }

    public void openMap(){
        mapHelper.setMapSettings(ProfileFragment.this.callbackHandler,
                (userProfile.lat == null ? null : (new LatLng(userProfile.lat, userProfile.longi))),
                MapHelper.MARKER, 0, 0);
        activity.showMapFragment(mapHelper, true);
    }
}