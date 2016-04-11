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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.MapHelper;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.masters.Categories;
import com.bitwinger.crowdpuller.masters.Preferences;
import com.bitwinger.crowdpuller.masters.ResponseTypes;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.results.PostDetails;
import com.bitwinger.pickerfragments.DatePickerFragment;
import com.bitwinger.utils.Constants;
import com.bitwinger.utils.FetchAddressIntentService;
import com.bitwinger.utils.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that represents the main selection screen for Scrumptious.
 */
public class PostDetailsFragment extends Fragment {
    private static final String TAG = "PostDetailsFragment";
    private static final String DATA_KEY = "POST_DETAILS_DATA";

    private MainActivity activity;
    private TextView txtResponses;
    private TextView txtResponsesCount;
    private ImageButton btn_pick_post_location;
    private TextView txt_post_address;
    private EditText edit_title;
    private EditText edit_message;
    private EditText edit_category;
    private ImageButton btn_pick_category;
    private EditText edit_feedback_option;
    private ImageButton btn_pick_feedback_options;
    private EditText edit_expiry_date;
    private ImageButton btn_pick_date;
    private DatePickerFragment datePickerFragment;
    private RadioGroup rg_target_gender;
    private RadioButton rbmale;
    private RadioButton rbfemale;
    private RadioButton rbboth;
    private EditText edit_target_min_age;
    private NumberPicker min_age_picker;
    private ImageButton btn_pick_min_age;
    private EditText edit_target_max_age;
    private NumberPicker max_age_picker;
    private ImageButton btn_pick_max_age;
    private Button btn_save_post;
    private Button btn_cancel_post;
    private ScrollView svpost_details;

    private AddressResultReceiver mResultReceiver;
    private Date dateExpiryMin;
    private Date dateExpiryMax;
    private Date dateExpiry;
    private PostDetails postDetails;
    private MapHelper mapHelper;
    private MapHelper.CallbackHandler callbackHandler;
    private Boolean isNewPost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.post_details, container, false);

        txtResponses = (TextView) view.findViewById(R.id.txtResponses);
        txtResponsesCount = (TextView) view.findViewById(R.id.txtResponsesCount);
        btn_pick_post_location = (ImageButton) view.findViewById(R.id.btn_pick_post_location);
        txt_post_address = (TextView) view.findViewById(R.id.txt_post_address);
        edit_title = (EditText) view.findViewById(R.id.edit_title);
        edit_message = (EditText) view.findViewById(R.id.edit_message);
        edit_category = (EditText) view.findViewById(R.id.edit_category);
        btn_pick_category = (ImageButton) view.findViewById(R.id.btn_pick_category);
        edit_feedback_option = (EditText) view.findViewById(R.id.edit_feedback_options);
        btn_pick_feedback_options = (ImageButton) view.findViewById(R.id.btn_pick_feedback_options);
        edit_expiry_date = (EditText) view.findViewById(R.id.edit_expiry_date);
        btn_pick_date = (ImageButton) view.findViewById(R.id.btn_pick_date);
        rg_target_gender = (RadioGroup) view.findViewById(R.id.rg_target_gender);
        rbmale = (RadioButton) view.findViewById(R.id.rbmale);
        rbfemale = (RadioButton) view.findViewById(R.id.rbfemale);
        rbboth = (RadioButton) view.findViewById(R.id.rbBoth);
        edit_target_min_age = (EditText) view.findViewById(R.id.edit_target_min_age);
        btn_pick_min_age = (ImageButton) view.findViewById(R.id.btn_pick_min_age);
        edit_target_max_age = (EditText) view.findViewById(R.id.edit_target_max_age);
        btn_pick_max_age = (ImageButton) view.findViewById(R.id.btn_pick_max_age);
        btn_save_post = (Button) view.findViewById(R.id.btn_save_post);
        btn_cancel_post = (Button) view.findViewById(R.id.btn_cancel_post);

        callbackHandler = new MapHelper.CallbackHandler() {
            @Override
            public void UpdateLocation(LatLng latLng) {

            }

            @Override
            public void UpdateBounds(LatLngBounds latLngBounds, double rectHeight, double rectWidth) {
                LatLng center = latLngBounds.getCenter();
                if (postDetails.post.lat == null || postDetails.post.lat != (float) center.latitude ||
                        postDetails.post.longi == null || postDetails.post.longi != (float) center.longitude) {
                    postDetails.post.lat = (float) center.latitude;
                    postDetails.post.longi = (float) center.longitude;
                    getAddressByIntentService(center);
                    Log.d(TAG, "UpdateBounds: " + postDetails.post.lat + ":" + postDetails.post.longi);
                }
                postDetails.post.neLat = (float)latLngBounds.northeast.latitude;
                postDetails.post.neLng = (float)latLngBounds.northeast.longitude;
                postDetails.post.swLat = (float)latLngBounds.southwest.latitude;
                postDetails.post.swLng = (float)latLngBounds.southwest.longitude;
                postDetails.post.hghtInKM = rectHeight;
                postDetails.post.wdthInKM = rectWidth;
            }
        };
        mapHelper = new MapHelper(getActivity(), MainActivity.POSTDETAILS);
        //
        btn_pick_post_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });
        //
        View.OnClickListener pickCategoryListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Pick Category")
                        .setItems(Categories.getArrayList(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                postDetails.post.catid = Categories.getCatIdFromArrayIndex(which);
                                edit_category.setText(Categories.getCatCodeFromCatId(postDetails.post.catid));
                                edit_category.setError(null);
                            }
                        })
                        .show();
            }
        };
        btn_pick_category.setOnClickListener(pickCategoryListener);
        edit_category.setOnClickListener(pickCategoryListener);
        //
        View.OnClickListener pickFeedbackOptionsListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Pick Feedback Option Type")
                        .setItems(ResponseTypes.getArrayList(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                postDetails.post.rspType = (which == 0 ? null : which);
                                edit_feedback_option.setText(ResponseTypes.getList().get(which));
                                edit_feedback_option.setError(null);
                            }
                        })
                        .show();
            }
        };
        btn_pick_feedback_options.setOnClickListener(pickFeedbackOptionsListener);
        edit_feedback_option.setOnClickListener(pickFeedbackOptionsListener);
        //
        View.OnClickListener pickdateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (datePickerFragment == null) {
                    datePickerFragment = new DatePickerFragment();
                    datePickerFragment.createPickerDialog(getActivity());
                    datePickerFragment.setCallbackHandler(new DatePickerFragment.CallbackHandler() {
                        @Override
                        public void setDate(DatePicker view, int year, int month, int day) {
                            Calendar cal = Calendar.getInstance();
                            cal.clear();
                            cal.set(Calendar.DAY_OF_MONTH, day);
                            cal.set(Calendar.MONTH, month);
                            cal.set(Calendar.YEAR, year);
                            dateExpiry = cal.getTime();
                            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale);
                            edit_expiry_date.setText(simpleDateFormat2.format(dateExpiry));
                        }
                    });
                    datePickerFragment.getDatePickerDialog().setTitle("Select Expiry Date");
                }
                Calendar cal = Calendar.getInstance();
                if (dateExpiry != null) {
                    cal.setTime(dateExpiry);
                }
                datePickerFragment.getDatePickerDialog().getDatePicker().setMinDate(dateExpiryMin.getTime());
                datePickerFragment.getDatePickerDialog().getDatePicker().setMaxDate(dateExpiryMax.getTime());
                datePickerFragment.getDatePickerDialog().updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                datePickerFragment.show(getFragmentManager(), "datePicker");
            }
        };
        btn_pick_date.setOnClickListener(pickdateListener);
        edit_expiry_date.setOnClickListener(pickdateListener);
        //
        View.OnClickListener prfSexClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postDetails.post.prfSex = (rbmale.isChecked() ? "M" : (rbfemale.isChecked() ? "F" : null));
            }
        };
        rbmale.setOnClickListener(prfSexClickListener);
        rbfemale.setOnClickListener(prfSexClickListener);
        rbboth.setOnClickListener(prfSexClickListener);
        //
        min_age_picker = new NumberPicker(getActivity());
        min_age_picker.setMinValue(18);
        min_age_picker.setMaxValue(100);
        //
        max_age_picker = new NumberPicker(getActivity());
        max_age_picker.setMinValue(18);
        max_age_picker.setMaxValue(100);
        //
        View.OnClickListener pickMinAgeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                min_age_picker.setValue(postDetails.post.prfMinAge == null ? 18 : postDetails.post.prfMinAge);
                if (min_age_picker.getParent() != null) {
                    ((FrameLayout) min_age_picker.getParent()).removeView(min_age_picker);
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("Pick Minimum Age")
                        .setView(min_age_picker)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Integer newVal = min_age_picker.getValue();
                                edit_target_min_age.setText(Integer.toString(newVal) + " Years");
                                postDetails.post.prfMinAge = newVal;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        btn_pick_min_age.setOnClickListener(pickMinAgeListener);
        edit_target_min_age.setOnClickListener(pickMinAgeListener);
        //
        View.OnClickListener pickMaxAgeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                max_age_picker.setValue(postDetails.post.prfMaxAge == null ? 100 : postDetails.post.prfMaxAge);
                if (max_age_picker.getParent() != null) {
                    ((FrameLayout) max_age_picker.getParent()).removeView(max_age_picker);
                }
                new AlertDialog.Builder(getActivity())
                        .setTitle("Pick Maximum Age")
                        .setView(max_age_picker)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Integer newVal = max_age_picker.getValue();
                                edit_target_max_age.setText(Integer.toString(newVal) + " Years");
                                postDetails.post.prfMaxAge = newVal;
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        btn_pick_max_age.setOnClickListener(pickMaxAgeListener);
        edit_target_max_age.setOnClickListener(pickMaxAgeListener);
        //
        btn_save_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost();
            }
        });
        btn_cancel_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPost();
            }
        });
        svpost_details = (ScrollView) view.findViewById(R.id.svpost_details);
        //
        mResultReceiver = new AddressResultReceiver(new Handler());
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        if (this.postDetails != null) {
            saveValueState();
            outState.putString(DATA_KEY, JsonUtils.toJson(postDetails));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            postDetails = JsonUtils.getFromJson(savedInstanceState.getString(DATA_KEY), PostDetails.class);
            if (this.postDetails != null) {
                bindData(this.postDetails);
            }
        }
    }

    public void bindData(PostDetails postDetails) {
        svpost_details.scrollTo(0, 0);
        edit_title.setError(null);
        edit_message.setError(null);
        edit_category.setError(null);
        edit_feedback_option.setError(null);
        //
        isNewPost = (postDetails == null);
        if (postDetails == null) {
            postDetails = new PostDetails();
            postDetails.post.aId = getApp().getSessionDetails().memberId;
            postDetails.post.hghtInKM = Math.sqrt(Preferences.maxCovAreaInKM);
            postDetails.post.wdthInKM = Math.sqrt(Preferences.maxCovAreaInKM);
        }
        this.postDetails = postDetails;
        //
        String ResponseSoFar = "";
        for (int i = 0; i < postDetails.responses.size(); i++) {
            ResponseSoFar += (i > 0 ? ", " : "") + postDetails.responses.get(i).val + " (" + Integer.toString(postDetails.responses.get(i).rspCount) + ")";
        }
        txtResponsesCount.setText(ResponseSoFar);
        if (this.postDetails.post.Id == null || this.postDetails.post.Id.isEmpty() || postDetails.responses.size() == 0) {
            txtResponses.setVisibility(View.GONE);
            txtResponsesCount.setVisibility(View.GONE);
        } else {
            txtResponses.setVisibility(View.VISIBLE);
            txtResponsesCount.setVisibility(View.VISIBLE);
        }
        txt_post_address.setText(postDetails.post.addr);
        edit_title.setText(postDetails.post.hdr);
        edit_message.setText(postDetails.post.msg);
        if (postDetails.post.catid != null && postDetails.post.catid >= 0) {
            edit_category.setText(Categories.getCatCodeFromCatId(postDetails.post.catid));
        } else {
            edit_category.setText(null);
        }
        edit_feedback_option.setText(ResponseTypes.getList().get(postDetails.post.rspType == null ? 0 : postDetails.post.rspType));
        //
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", getResources().getConfiguration().locale);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale);
        Calendar cal = Calendar.getInstance();
        dateExpiryMin = cal.getTime();
        if (postDetails.post.expryDt == null || postDetails.post.expryDt.isEmpty()) {
            cal.add(Calendar.DATE, Preferences.daysToExpirePost);
            dateExpiry = cal.getTime();
            dateExpiryMax = cal.getTime();
            //
            postDetails.post.expryDt = simpleDateFormat2.format(dateExpiry);
            edit_expiry_date.setText(simpleDateFormat2.format(dateExpiry));
        } else {
            dateExpiry = null;
            try {
                dateExpiry = simpleDateFormat.parse(postDetails.post.expryDt);
                edit_expiry_date.setText(simpleDateFormat2.format(dateExpiry));
            } catch (Exception ex) {
                edit_expiry_date.setText(null);
            }
            try {
                cal.setTime(simpleDateFormat.parse(postDetails.post.crtdOn));
                cal.add(Calendar.DATE, Preferences.daysToExpirePost);
                dateExpiryMax = cal.getTime();
            } catch (Exception ex) {

            }
        }
        rbmale.setChecked(postDetails.post.prfSex != null && postDetails.post.prfSex.equalsIgnoreCase("M"));
        rbfemale.setChecked(postDetails.post.prfSex != null && postDetails.post.prfSex.equalsIgnoreCase("F"));
        rbboth.setChecked(postDetails.post.prfSex == null || postDetails.post.prfSex.isEmpty());
        edit_target_min_age.setText(postDetails.post.prfMinAge != null ? postDetails.post.prfMinAge + " Years" : null);
        edit_target_max_age.setText(postDetails.post.prfMaxAge != null ? postDetails.post.prfMaxAge + " Years" : null);
    }

    private void saveValueState(){
        postDetails.post.addr = txt_post_address.getText().toString();
        postDetails.post.hdr = edit_title.getText().toString();
        postDetails.post.msg = edit_message.getText().toString();
        postDetails.post.expryDt = edit_expiry_date.getText().toString();
        postDetails.post.expryDt = postDetails.post.expryDt.isEmpty() ? null : postDetails.post.expryDt;
    }

    private void savePost() {
        svpost_details.scrollTo(0, 0);
        Boolean isError = false;
        if (edit_title.getText().length() == 0) {
            edit_title.setError("Title is required.");
            isError = true;
        }
        if (edit_message.getText().length() == 0) {
            edit_message.setError("Message is required.");
            isError = true;
        }
        if (edit_category.getText().length() == 0) {
            edit_category.setError("Category is required.");
            isError = true;
        }
        if (edit_feedback_option.getText().length() == 0) {
            edit_feedback_option.setError("Feedback option is required.");
            isError = true;
        }
        if (postDetails.post.lat==null){
            isError = true;
            new AlertDialog.Builder(getActivity())
                    .setTitle("Post location")
                    .setMessage("Please select location for this post.")
                    .setNeutralButton("Ok", null)
                    .show();
        }
        if (isError) {
            return;
        }
        saveValueState();
        //
        if (getApp().isLoggedIn()) {
            String urlPath = getApp().getAPIPath("/post");
            Map headers = new HashMap<String, String>();
            headers.put("AccToken", getApp().getSessionDetails().sessionId);

            RestClient restClient = new RestClient(activity, urlPath,
                    (postDetails.post.Id == null || postDetails.post.Id.isEmpty() ? RestClient.RequestMethod.POST : RestClient.RequestMethod.PUT),
                    headers, JsonUtils.toJson(postDetails.post));
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        activity.showPostListFragment(false);
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

    public void cancelPost() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Exit Post")
                .setMessage("Do you want to exit post screen? Doing so, you may loose any information already entered on this screen.")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.showPostListFragment(false);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Save & Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePost();
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
            postDetails.post.addr = resultData.getString(Constants.RESULT_DATA_KEY);
            txt_post_address.setText(postDetails.post.addr);
        }
    }

    private CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) activity.getApplication());
    }

    public MapHelper getMapHelper(){
        return mapHelper;
    }

    public void openMap(){
        Log.d(TAG, "onClick: " + postDetails.post.lat + ":" + postDetails.post.longi);
        mapHelper.setMapSettings(PostDetailsFragment.this.callbackHandler,
                (postDetails.post.lat == null ? null : (new LatLng(postDetails.post.lat, postDetails.post.longi))),
                MapHelper.RECT, postDetails.post.hghtInKM, postDetails.post.wdthInKM);
        activity.showMapFragment(mapHelper, true);
    }
}