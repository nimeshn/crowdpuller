package com.bitwinger.crowdpuller;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bitwinger.crowdpuller.fragments.FeedDetailsFragment;
import com.bitwinger.crowdpuller.fragments.FeedListFragment;
import com.bitwinger.crowdpuller.fragments.PostDetailsFragment;
import com.bitwinger.crowdpuller.fragments.PostListFragment;
import com.bitwinger.crowdpuller.fragments.ProfileFragment;
import com.bitwinger.crowdpuller.fragments.SplashFragment;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.results.FeedDetails;
import com.bitwinger.crowdpuller.restapi.results.PostDetails;
import com.bitwinger.slidingmenu.adapter.NavDrawerListAdapter;
import com.bitwinger.slidingmenu.model.NavDrawerItem;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;

import bolts.AppLinks;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CURRENT_FRAGMENT_INDEX = "current_fragment_index";
    private static final String MAP_PARENT_INDEX = "map_parent_index";

    public static final int FEEDLIST = 0;
    public static final int POSTLIST = 1;
    public static final int PROFILE = 2;
    public static final int SPLASH = 3;
    public static final int FEEDDETAILS = 4;
    public static final int POSTDETAILS = 5;
    public static final int MAP = 6;
    private static final int FRAGMENT_COUNT = MAP + 1;
    private int currentFragmentIndex = -1;
    private int mapParentIndex = -1;

    private FeedListFragment feedListFragment = null;
    private PostListFragment postListFragment = null;
    private ProfileFragment profileFragment = null;
    private SplashFragment splashFragment = null;
    private FeedDetailsFragment feedDetailsFragment = null;
    private PostDetailsFragment postDetailsFragment = null;
    private MapFragment mapFragment = null;
    private PlaceAutocompleteFragment autocompleteFragment = null;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];

    /*Start for Drawer Menu*/
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    private CharSequence mDrawerTitle;
    // used to store app title
    private CharSequence mTitle;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    /*End for Drawer Menu*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            currentFragmentIndex = savedInstanceState.getInt(CURRENT_FRAGMENT_INDEX);
            mapParentIndex = savedInstanceState.getInt(MAP_PARENT_INDEX);
        }
        //App Target Link
        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            String appLinkData = targetUrl.toString();
            Log.d(TAG, "App Link Target URL: " + targetUrl.toString());
            Log.d(TAG, "onCreate: " + AppLinks.getAppLinkData(getIntent()));
            if (appLinkData.length() > 18 && appLinkData.indexOf("CrowdPuller://pid/") == 0) {
                getApp().setDeepLink(appLinkData.substring(18));
            }
        }

        FragmentManager fm = getFragmentManager();
        fragments[FEEDLIST] = feedListFragment = (FeedListFragment) fm.findFragmentById(R.id.feedListFragment);
        fragments[POSTLIST] = postListFragment = (PostListFragment) fm.findFragmentById(R.id.postListFragment);
        fragments[PROFILE] = profileFragment = (ProfileFragment) fm.findFragmentById(R.id.profileFragment);
        fragments[SPLASH] = splashFragment = (SplashFragment) fm.findFragmentById(R.id.splashFragment);
        fragments[FEEDDETAILS] = feedDetailsFragment = (FeedDetailsFragment) fm.findFragmentById(R.id.feedDetailsFragment);
        fragments[POSTDETAILS] = postDetailsFragment = (PostDetailsFragment) fm.findFragmentById(R.id.postDetailsFragment);
        fragments[MAP] = mapFragment = (MapFragment) fm.findFragmentById(R.id.mapFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
        //
        autocompleteFragment = (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                if (mapParentIndex == PROFILE) {
                    profileFragment.getMapHelper().drawMapShape(place.getLatLng(), true);
                }else if (mapParentIndex == POSTDETAILS) {
                    postDetailsFragment.getMapHelper().drawMapShape(place.getLatLng(), true);
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        //Set App Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(myToolbar);

        /*Drawer Menu Related Code*/
        mTitle = mDrawerTitle = getTitle();

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();

        for (int i = 0; i < navMenuTitles.length; i++) {
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], navMenuIcons.getResourceId(i, -1)));
        }
        // Recycle the typed array
        navMenuIcons.recycle();

        // load slide menu titles
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_titles);

        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(adapter);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, myToolbar,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            showSplashFragment(false);
        }
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            handleOptionsClick(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        //if its a map we will need to reload it.
        if (currentFragmentIndex == MAP) {
            if (mapParentIndex == PROFILE) {
                profileFragment.openMap();
            } else {
                postDetailsFragment.openMap();
            }
        } else {
            showFragment(currentFragmentIndex, true, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_FRAGMENT_INDEX, currentFragmentIndex);
        outState.putInt(MAP_PARENT_INDEX, mapParentIndex);
    }

    @Override
    public void onBackPressed() {
        //if Drawer is open then close on backpress
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        //
        if (currentFragmentIndex == FEEDDETAILS) {
            currentFragmentIndex = FEEDLIST;
        } else if (currentFragmentIndex == POSTDETAILS) {
            postDetailsFragment.cancelPost();
            return;
        } else if (currentFragmentIndex == PROFILE) {
            profileFragment.cancelProfile();
            return;
        } else if (currentFragmentIndex == MAP) {
            if (mapParentIndex >= 0) {
                showFragment(mapParentIndex, true, false);
            }
            mapParentIndex = -1;
            return;
        }
        //
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            updateDrawerList(currentFragmentIndex);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Application")
                    .setMessage("Do you want to exit application?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    public void showProfileFragment(boolean addToBackStack) {
        if (promptForLogin()) {
            profileFragment.getProfileData();
            showFragment(PROFILE, true, addToBackStack);
        }
    }

    public void showSplashFragment(boolean addToBackStack) {
        showFragment(SPLASH, true, addToBackStack);
    }

    public void showFeedsFragment(boolean addToBackStack) {
        if (promptForLogin()) {
            feedListFragment.getFeeds();
            showFragment(FEEDLIST, true, addToBackStack);
        }
    }

    public void showFeedDetailsFragment(String PostId, final boolean addToBackStack) {
        if (promptForLogin()) {
            String urlPath = getApp().getAPIPath("/feed/post/") + PostId +
                    "/" + getApp().getSessionDetails().memberId;
            RestClient restClient = new RestClient(MainActivity.this, urlPath,
                    RestClient.RequestMethod.GET, null, null);
            restClient.setResultCallback(new RestClient.ResultCallback() {
                @Override
                public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                    if (ResponseCode == 200) {
                        if (ResponseData != null && !ResponseData.isEmpty()) {
                            FeedDetails feedDetails = JsonUtils.getFromJson(ResponseData, FeedDetails.class);
                            feedDetailsFragment.bindData(feedDetails);
                            showFragment(FEEDDETAILS, true, addToBackStack);
                        }
                    }
                }
            });
            restClient.execute();
        }
    }

    public void showPostListFragment(boolean addToBackStack) {
        if (promptForLogin()) {
            postListFragment.getPosts();
            showFragment(POSTLIST, true, addToBackStack);
        }
    }

    public void showPostDetailsFragment(PostDetails postDetails, boolean addToBackStack) {
        if (promptForLogin()) {
            postDetailsFragment.bindData(postDetails);
            showFragment(POSTDETAILS, true, addToBackStack);
        }
    }

    public void showMapFragment(MapHelper mapHelper, boolean addToBackStack) {
        if (promptForLogin()) {
            autocompleteFragment.setText(null);
            autocompleteFragment.setHint(mapParentIndex == PROFILE ? "Select Feed Location" : "Select Post Region");
            mapParentIndex = mapHelper.getParentFragmentIndex();
            mapFragment.getMapAsync(mapHelper);
            showFragment(MAP, true, addToBackStack);
        }
    }

    private void clearFMBackStack() {
        FragmentManager manager = getFragmentManager();
        int backStackSize = manager.getBackStackEntryCount();
        for (int i = 0; i < backStackSize; i++) {
            manager.popBackStack();
        }
    }

    private void showFragment(int fragmentIndex, boolean clearBackStack, boolean addToBackStack) {
        if (getApp().isLoggedIn() &&
                getApp().getSessionDetails().NewSignUp == 1 &&
                !(fragmentIndex == PROFILE || (fragmentIndex == MAP && mapParentIndex == PROFILE))
                ) {
            new AlertDialog.Builder(this)
                    .setTitle("New User")
                    .setMessage("Welcome to Crowdpuller. Please select your current location and fill in profile details to start using application.")
                    .show();
            return;
        }
        if (clearBackStack)
            clearFMBackStack();
        currentFragmentIndex = -1;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                currentFragmentIndex = fragmentIndex;
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        updateDrawerList(fragmentIndex);
        if (currentFragmentIndex == MAP) {
            getSupportActionBar().hide();
        }else{
            getSupportActionBar().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        prepareContextMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_fav_filter:
                return true;
            case R.id.action_cat_filter:
                feedListFragment.showCategoryFilter();
                return true;
            case R.id.action_refresh_feed:
                feedListFragment.getFeeds();
                return true;
            case R.id.action_flag_post:
                feedDetailsFragment.flagFeedPost();
                return true;
            case R.id.action_fb_share_post:
                feedDetailsFragment.sharePost();
                return true;
            case R.id.action_refresh_posts:
                postListFragment.getPosts();
                return true;
            case R.id.action_new_post:
                showPostDetailsFragment(null, true);
                return true;
            case R.id.action_retry_login:
                splashFragment.retryLogin();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        prepareContextMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void prepareContextMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        boolean feedsOpen = (currentFragmentIndex == FEEDLIST);
        boolean feedPostOpen = (currentFragmentIndex == FEEDDETAILS);
        boolean postListOpen = (currentFragmentIndex == POSTLIST);
        boolean postOpen = (currentFragmentIndex == POSTDETAILS);
        boolean splashOpen = (currentFragmentIndex == SPLASH);
        //menu.findItem(R.id.action_fav_filter).setVisible(feedsOpen && !drawerOpen);
        menu.findItem(R.id.action_fav_filter).setVisible(false);
        menu.findItem(R.id.action_cat_filter).setVisible(feedsOpen && !drawerOpen);
        menu.findItem(R.id.action_refresh_feed).setVisible(feedsOpen && !drawerOpen);
        menu.findItem(R.id.action_flag_post).setVisible(feedPostOpen && !drawerOpen);
        menu.findItem(R.id.action_fb_share_post).setVisible(feedPostOpen && !drawerOpen);
        menu.findItem(R.id.action_refresh_posts).setVisible(postListOpen && !drawerOpen);
        menu.findItem(R.id.action_new_post).setVisible(postListOpen && !drawerOpen);
        menu.findItem(R.id.action_retry_login).setVisible(splashOpen && !drawerOpen);
    }

    private void updateDrawerList(int position) {
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        setTitle(navMenuTitles[position]);
        invalidateOptionsMenu();
    }

    private Boolean handleOptionsClick(int position) {
        switch (position) {
            case FEEDLIST:
                // User chose the "Feeds" item, show the feeds list
                showFeedsFragment(false);
                break;

            case POSTLIST:
                // User chose the "Posts" action, Show the Posts created by user
                // as a favorite...
                showPostListFragment(false);
                break;

            case PROFILE:
                // User chose the "Profile" action, Show user profile details
                // as a favorite...
                showProfileFragment(false);
                break;

            case SPLASH:
                // User chose the "logout" action, logout and close the app
                // as a favorite...
                showSplashFragment(false);
                break;
            default:
                return false;
        }
        updateDrawerList(position);
        mDrawerLayout.closeDrawer(mDrawerList);
        return true;
    }

    public CrowdPullerApplication getApp(){
        return ((CrowdPullerApplication) getApplication());
    }

    public Boolean promptForLogin(){
        if (!getApp().isLoggedIn()){
            new AlertDialog.Builder(this)
                    .setTitle("SignIn Application")
                    .setMessage("Please sign in before using application?")
                    .show();
        }
        return getApp().isLoggedIn();
    }
}