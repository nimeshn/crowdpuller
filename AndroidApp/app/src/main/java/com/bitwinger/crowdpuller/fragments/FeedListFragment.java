package com.bitwinger.crowdpuller.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.FullListView;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.masters.Categories;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.requests.FeedFlagRequest;
import com.bitwinger.crowdpuller.restapi.results.Feed;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedListFragment extends Fragment {
    private static final String TAG = "FeedListFragment";
    private static final String DATA_KEY = "FEED_DATA";

    private Integer catId;
    private String catCode;
    private FullListView listView;
    private List<Feed> feedsData;
    private FeedListAdapter listAdapter;
    private MainActivity activity;

    private TextView txtLocation;
    private TextView txt_category_filter;
    private ImageButton ib_clear_cat_filter;
    private LinearLayout layout_category;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.feed_list, container, false);

        listView = (FullListView) view.findViewById(R.id.feedsList);
        txtLocation = (TextView) view.findViewById(R.id.txtLocation);
        txt_category_filter = (TextView) view.findViewById(R.id.txt_category_filter);
        ib_clear_cat_filter = (ImageButton) view.findViewById(R.id.ib_clear_cat_filter);
        ib_clear_cat_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCategoryFilter();
            }
        });
        layout_category = (LinearLayout) view.findViewById(R.id.layout_category);
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
        outState.putString(DATA_KEY, JsonUtils.toJson(feedsData));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            feedsData = JsonUtils.getFromJson(savedInstanceState.getString(DATA_KEY), new TypeToken<List<Feed>>() {
            }.getType());
            if (feedsData != null) {
                loadListView();
            }
        }
    }

    /**
     * Resets the view to the initial defaults.
     */
    private void loadListView() {
        txtLocation.setText("Location:" +
                getApp().getSessionDetails().memberAddr);
        if (catCode != null) {
            txt_category_filter.setText("Filtered by category:" + catCode);
            layout_category.setVisibility(View.VISIBLE);
        } else {
            layout_category.setVisibility(View.GONE);
        }
        listAdapter = new FeedListAdapter(getActivity(), R.id.feedsList, feedsData);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                activity.showFeedDetailsFragment((String) view.getTag(), true);
            }
        });
        listView.resetHeight();
    }

    private class FeedListAdapter extends ArrayAdapter<Feed> {
        public FeedListAdapter(
                Context context, int resourceId, List<Feed> listElements) {
            super(context, resourceId, listElements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Feed feedPost = getItem(position);
            if (feedPost != null) {
                if (convertView == null) {
                    LayoutInflater inflater =
                            (LayoutInflater) getActivity().getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.feed_list_item, null);
                }
                TextView txtHeader = (TextView) convertView.findViewById(R.id.txtHeader);
                TextView txtCreatedOn = (TextView) convertView.findViewById(R.id.txtCreatedOn);
                ImageView imgFavOn = (ImageView) convertView.findViewById(R.id.imgFavOn);
                ImageView imgFavOff = (ImageView) convertView.findViewById(R.id.imgFavOff);
                ImageView imgHidePost = (ImageView) convertView.findViewById(R.id.imgHideFlag);

                imgFavOn.setTag(position);
                imgFavOff.setTag(position);
                imgHidePost.setTag(position);
                convertView.setTag(feedPost.Id);
                //Set Image View click listener
                imgFavOn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        togglePostFav((Integer) v.getTag(), 1);
                    }
                });
                //Set Image View click listener
                imgFavOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        togglePostFav((Integer) v.getTag(), 0);
                    }
                });
                //Set Image View click listener
                imgHidePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hidePost((Integer) v.getTag());
                    }
                });
                //
                txtHeader.setText(feedPost.hdr);
                txtCreatedOn.setText(feedPost.crtdOn);
                if (feedPost.flag == 0) {
                    imgFavOn.setVisibility(View.VISIBLE);
                    imgFavOff.setVisibility(View.GONE);
                } else if (feedPost.flag == 1) {
                    imgFavOn.setVisibility(View.GONE);
                    imgFavOff.setVisibility(View.VISIBLE);
                }
            }
            return convertView;
        }
    }

    private void setFeedFlag(final int itemPosition) {
        if (getApp().isLoggedIn()) {
            String urlPath = getApp().getAPIPath("/feed/flag");
            FeedFlagRequest flagRequest = new FeedFlagRequest(feedsData.get(itemPosition).Id,
                    getApp().getSessionDetails().memberId,
                    feedsData.get(itemPosition).flag);
            Map headers = new HashMap<String, String>();
            headers.put("X-HTTP-Method-Override", "PATCH");
            headers.put("AccToken", getApp().getSessionDetails().sessionId);

            RestClient restClient = new RestClient(activity, urlPath,
                    RestClient.RequestMethod.POST, headers, JsonUtils.toJson(flagRequest));
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        //if the flag is 2 then remove from feed_list
                        if (feedsData.get(itemPosition).flag == 2) {
                            feedsData.remove(itemPosition);
                        }
                        listAdapter.notifyDataSetChanged();
                        Log.i("FeedList", "Feeds: " + ResponseData);
                    }
                }
            });
            restClient.execute();
        }
    }

    private void togglePostFav(final int itemPosition, final int flag) {
        feedsData.get(itemPosition).flag = flag;
        setFeedFlag(itemPosition);
    }

    private void hidePost(final int itemPosition) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Hide Post")
                .setMessage("Do you want to hide this post from the feed?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        feedsData.get(itemPosition).flag = 2;
                        setFeedFlag(itemPosition);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void getFeeds() {
        txtLocation.setText(null);
        listView.setAdapter(null);
        //
        if (getApp().isLoggedIn()) {
            String urlPath = getApp().getAPIPath("/feed/list/") +
                    getApp().getSessionDetails().memberId + (catId == null ? "" : "/" + catId);
            RestClient restClient = new RestClient(activity, urlPath,
                    RestClient.RequestMethod.GET, null, null);
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        if (ResponseData != null && !ResponseData.isEmpty()) {
                            Log.i("FeedList", "Feeds: " + ResponseData);
                            feedsData = JsonUtils.getFromJson(ResponseData, new TypeToken<List<Feed>>() {
                            }.getType());
                        } else {
                            feedsData = new ArrayList<Feed>();
                        }
                        loadListView();
                    }
                }
            });
            restClient.execute();
        }
    }

    private CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) activity.getApplication());
    }

    public void showCategoryFilter() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Filter By Category")
                .setItems(Categories.getArrayList(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        catId = Categories.getCatIdFromArrayIndex(which);
                        catCode = Categories.getCatCodeFromCatId(catId);
                        getFeeds();
                    }
                })
                .show();
    }

    public void clearCategoryFilter() {
        catId = null;
        catCode = null;
        getFeeds();
    }
}