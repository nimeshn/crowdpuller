package com.bitwinger.crowdpuller.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.requests.PostFlagRequest;
import com.bitwinger.crowdpuller.restapi.requests.PostResponseRequest;
import com.bitwinger.crowdpuller.restapi.results.FeedDetails;
import com.bitwinger.utils.Utils;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.plus.PlusShare;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that represents the main selection screen for Scrumptious.
 */
public class FeedDetailsFragment extends Fragment {
    private static final String TAG = "FeedDetailsFragment";
    private static final String DATA_KEY = "FEED_DETAILS_DATA";

    private FeedDetails feedDetails;
    private TextView txtHeader;
    private TextView txtMsg;
    private TextView txtPostedCat;
    private TextView txtPostedLoc;
    private TextView txtPostedBy;
    private LinearLayout rg_resp_options;
    private LinearLayout respLayout;
    private ScrollView sv_feed_details;

    private MainActivity activity;
    private ShareDialog shareDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        shareDialog = new ShareDialog(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.feed_details, container, false);

        txtHeader = (TextView) view.findViewById(R.id.txtHeader);
        txtMsg = (TextView) view.findViewById(R.id.txtMsg);
        txtPostedCat = (TextView) view.findViewById(R.id.txtCatValue);
        txtPostedLoc = (TextView) view.findViewById(R.id.txtLocValue);
        txtPostedBy = (TextView) view.findViewById(R.id.txtByValue);
        respLayout = (LinearLayout) view.findViewById(R.id.respLayout);
        rg_resp_options = (LinearLayout) view.findViewById(R.id.rg_resp_options);
        sv_feed_details = (ScrollView) view.findViewById(R.id.sv_feed_details);

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
        if (this.feedDetails != null) {
            outState.putString(DATA_KEY, JsonUtils.toJson(feedDetails));
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            feedDetails = JsonUtils.getFromJson(savedInstanceState.getString(DATA_KEY), FeedDetails.class);
            if (this.feedDetails != null) {
                bindData(this.feedDetails);
            }
        }
    }

    public void bindData(FeedDetails data) {
        sv_feed_details.scrollTo(0, 0);
        this.feedDetails = data;
        txtHeader.setText(feedDetails.post.hdr);
        txtMsg.setText(feedDetails.post.msg);
        txtPostedCat.setText(feedDetails.post.catcode);
        txtPostedLoc.setText(feedDetails.post.addr);

        String postedBy = "Anonymous";
        if (feedDetails.post.shareCI == 1) {
            postedBy = feedDetails.post.FN + "(" + feedDetails.post.emlId + ")";
        }
        postedBy += "on " + feedDetails.post.crtdOn;
        txtPostedBy.setText(postedBy);
        //Set Response Option
        if (feedDetails.responses.size() > 0) {
            //hide all existing
            for (int i = 0; i < rg_resp_options.getChildCount(); i++) {
                rg_resp_options.getChildAt(i).setTag(null);
                rg_resp_options.getChildAt(i).setVisibility(View.GONE);
            }
            for (int i = 0; i < feedDetails.responses.size(); i++) {
                FeedDetails.Responses resp = feedDetails.responses.get(i);
                //
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                RadioButton rb_response = null;
                if (i >= rg_resp_options.getChildCount()) {
                    rb_response = new RadioButton(getActivity());
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        rb_response.setId(Utils.generateViewId());
                    } else {
                        rb_response.setId(View.generateViewId());
                    }
                    rb_response.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveFeedResponse((Integer) v.getTag());
                        }
                    });
                    rb_response.setTextSize(14);
                    rg_resp_options.addView(rb_response, i, layoutParams);
                }
                rb_response = (RadioButton) rg_resp_options.getChildAt(i);
                rb_response.setVisibility(View.VISIBLE);
                rb_response.setTag(resp.id);
                rb_response.setSelected(resp.id == feedDetails.post.rspDtlId);
                rb_response.setChecked(resp.id == feedDetails.post.rspDtlId);
                rb_response.setText(resp.val + " (" + Integer.toString(resp.rspCount) + ")");
            }
            respLayout.setVisibility(View.VISIBLE);
        } else {
            rg_resp_options.removeAllViews();
            respLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void saveFeedResponse(final Integer rspDtlId) {
        feedDetails.post.rspDtlId = rspDtlId;
        String urlPath = getApp().getAPIPath("/post/responses");

        PostResponseRequest responseRequest = new PostResponseRequest(feedDetails.post.Id,
                getApp().getSessionDetails().memberId,
                rspDtlId, null);
        Map headers = new HashMap<String, String>();
        headers.put("AccToken", getApp().getSessionDetails().sessionId);

        RestClient restClient = new RestClient(activity, urlPath,
                RestClient.RequestMethod.POST, headers, JsonUtils.toJson(responseRequest));
        restClient.setResultCallback(new RestClient.ResultCallback() {
            @Override
            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                if (ResponseCode == 200) {
                    if (ResponseCode == 200) {
                        Log.i("FeedResponse", "Feed: " + ResponseData);
                    }
                }
            }
        });
        restClient.execute();
    }

    public void flagFeedPost() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Flag Post as Inappropriate")
                .setMessage("Would you like to flag this post as inappropriate?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String urlPath = getApp().getAPIPath("/post/flag");

                        PostFlagRequest flagRequest = new PostFlagRequest(feedDetails.post.Id,
                                getApp().getSessionDetails().memberId);
                        Map headers = new HashMap<String, String>();
                        headers.put("AccToken", getApp().getSessionDetails().sessionId);

                        RestClient restClient = new RestClient(activity, urlPath,
                                RestClient.RequestMethod.POST, headers, JsonUtils.toJson(flagRequest));
                        restClient.setResultCallback(new RestClient.ResultCallback() {
                            @Override
                            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                                if (ResponseCode == 200) {
                                    if (ResponseCode == 200) {
                                        Log.i("FeedResponse", "Feed: " + ResponseData);
                                        activity.showFeedsFragment(false);
                                    }
                                }
                            }
                        });
                        restClient.execute();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void sharePostOnFB(){
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle(feedDetails.post.hdr)
                    .setContentDescription(feedDetails.post.msg)
                    .setContentUrl(Uri.parse(getApp().getWebURL("/#sharedfeed/" + feedDetails.post.Id)))
                    .setImageUrl(Uri.parse(getApp().getWebURL("/assets/images/logo.png")))
                    .build();
            shareDialog.show(linkContent);
        }
    }

    private void sharePostOnGP(){
        //Sharing on Google Plus
        PlusShare.Builder builder = new PlusShare.Builder(getActivity());
        // Set call-to-action metadata.
        builder.addCallToAction(
                "DISCUSS", /** call-to-action button label */
                Uri.parse(getApp().getWebURL("/sharedpost.php?pid=" + feedDetails.post.Id)), /** call-to-action url (for desktop use) */
                "/post/" + feedDetails.post.Id /** call to action deep-link ID (for mobile use), 512 characters or fewer */);
        // Set the content url (for desktop use).
        builder.setContentUrl(Uri.parse(getApp().getWebURL("/sharedpost.php?pid=" + feedDetails.post.Id)));
        // Set the target deep-link ID (for mobile use).
        builder.setContentDeepLinkId("/post/", null, null, null);
        // Set the share text.
        builder.setText(activity.getString(R.string.app_name) + "-" + feedDetails.post.addr);
        startActivityForResult(builder.getIntent(), 0);
    }

    private void sharePostOnWhatsApp(){
        //sharing on whats app.
        String shareMsg = Uri.parse(getApp().getWebURL("/sharedpost.php?pid=" + feedDetails.post.Id)) +
                System.getProperty("line.separator") +
                System.getProperty("line.separator") +
                "POSTED ON: " +
                activity.getString(R.string.app_name) + " IN " + feedDetails.post.addr +
                System.getProperty("line.separator") +
                System.getProperty("line.separator") +
                "TITLE: " + feedDetails.post.hdr +
                System.getProperty("line.separator") +
                System.getProperty("line.separator") +
                "MESSAGE: " + feedDetails.post.msg;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    public void sharePost() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pick_share_app)
                .setItems(R.array.share_app_list, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0://facebook
                                sharePostOnFB();
                                break;
                            case 1://WhatsApp
                                sharePostOnWhatsApp();
                                break;
                            case 2://Google Plus
                                sharePostOnGP();
                                break;
                        }
                    }
                })
                .show();
    }

    private CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) activity.getApplication());
    }
}