package com.bitwinger.crowdpuller.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.FullListView;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.requests.DeletePostRequest;
import com.bitwinger.crowdpuller.restapi.results.PostDetails;
import com.bitwinger.crowdpuller.restapi.results.UserPosts;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that represents the main selection screen for Scrumptious.
 */
public class PostListFragment extends Fragment {
    private static final String TAG = "PostListFragment";
    private static final String DATA_KEY = "POST_DATA";

    private FullListView listView;
    private List<UserPosts> postsData;
    private PostListAdapter listAdapter;
    private MainActivity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.post_list, container, false);

        listView = (FullListView) view.findViewById(R.id.postsList);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putString(DATA_KEY, JsonUtils.toJson(postsData));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null) {
            postsData = JsonUtils.getFromJson(savedInstanceState.getString(DATA_KEY), new TypeToken<List<UserPosts>>() {
            }.getType());
            if (postsData != null) {
                loadListView();
            }
        }
    }

    /**
     * Resets the view to the initial defaults.
     */
    private void loadListView() {
        listAdapter = new PostListAdapter(
                getActivity(),
                R.id.postsList,
                postsData);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openUserPost((String) view.getTag());
            }
        });
        listView.resetHeight();
    }

    private class PostListAdapter extends ArrayAdapter<UserPosts> {
        public PostListAdapter(
                Context context, int resourceId, List<UserPosts> listElements) {
            super(context, resourceId, listElements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserPosts userPost = getItem(position);
            if (userPost != null) {
                if (convertView == null) {
                    LayoutInflater inflater =
                            (LayoutInflater) getActivity().getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.post_list_item, null);
                }
                TextView txtHeader = (TextView) convertView.findViewById(R.id.txtHeader);
                TextView txtCreatedOn = (TextView) convertView.findViewById(R.id.txtCreatedOn);
                ImageView imgEdit = (ImageView) convertView.findViewById(R.id.imgEdit);
                ImageView imgDelete = (ImageView) convertView.findViewById(R.id.imgDelete);

                imgEdit.setTag(position);
                //Set Image View click listener
                imgEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer itemPosition = (Integer) v.getTag();
                        openUserPost(getItem(itemPosition).Id);
                    }
                });
                imgDelete.setTag(position);
                //Set Image View click listener
                imgDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer itemPosition = (Integer) v.getTag();
                        deletePost(itemPosition);
                    }
                });
                convertView.setTag(userPost.Id);
                //
                txtHeader.setText(userPost.hdr);
                txtCreatedOn.setText(userPost.crtdOn);
            }
            return convertView;
        }
    }

    public void getPosts() {
        listView.setAdapter(null);
        if (getApp().isLoggedIn()) {
            String urlPath = getApp().getAPIPath("/member/post/") +
                    getApp().getSessionDetails().memberId;
            RestClient restClient = new RestClient(activity, urlPath,
                    RestClient.RequestMethod.GET, null, null);
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        if (ResponseData != null && !ResponseData.isEmpty()) {
                            Log.i("FeedList", "Feeds: " + ResponseData);
                            postsData = JsonUtils.getFromJson(ResponseData, new TypeToken<List<UserPosts>>() {
                            }.getType());
                            loadListView();
                        }
                    }
                }
            });
            restClient.execute();
        }
    }

    private void openUserPost(String PostId) {
        String urlPath = getApp().getAPIPath("/post/") + PostId;
        RestClient restClient = new RestClient(activity, urlPath,
                RestClient.RequestMethod.GET, null, null);
        restClient.setResultCallback(new RestClient.ResultCallback() {
            @Override
            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                if (ResponseCode == 200) {
                    if (ResponseData != null && !ResponseData.isEmpty()) {
                        Log.i("LoadPosts", "Post: " + ResponseData);
                        PostDetails postDetails = JsonUtils.getFromJson(ResponseData, PostDetails.class);
                        activity.showPostDetailsFragment(postDetails, true);
                        Log.i("LoadPosts", "Address: " + postDetails.post.addr);
                    }
                }
            }
        });
        restClient.execute();
    }

    public void deletePost(final int itemPosition) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Post")
                .setMessage("Would you like to delete this post?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getApp().isLoggedIn()) {
                            String urlPath = getApp().getAPIPath("/post/delete");
                            DeletePostRequest deleteRequest = new DeletePostRequest(postsData.get(itemPosition).Id);
                            Map headers = new HashMap<String, String>();
                            headers.put("AccToken", getApp().getSessionDetails().sessionId);

                            RestClient restClient = new RestClient(activity, urlPath,
                                    RestClient.RequestMethod.PUT, headers, JsonUtils.toJson(deleteRequest));
                            restClient.setResultCallback(new RestClient.ResultCallback() {
                                @Override
                                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                                    if (ResponseCode == 200) {
                                        postsData.remove(itemPosition);
                                        listAdapter.notifyDataSetChanged();
                                        Log.i("Posts", "Post: " + ResponseData);
                                    }
                                }
                            });
                            restClient.execute();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) activity.getApplication());
    }
}