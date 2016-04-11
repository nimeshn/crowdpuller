package com.bitwinger.crowdpuller.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitwinger.crowdpuller.CrowdPullerApplication;
import com.bitwinger.crowdpuller.MainActivity;
import com.bitwinger.crowdpuller.R;
import com.bitwinger.crowdpuller.restapi.JsonUtils;
import com.bitwinger.crowdpuller.restapi.RestClient;
import com.bitwinger.crowdpuller.restapi.requests.LoginRequest;
import com.bitwinger.crowdpuller.restapi.results.UserSessionDetails;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.HashMap;
import java.util.Map;

public class SplashFragment extends Fragment {
    private static String TAG = "SplashFragment";
    private static final int RC_SIGN_IN = 9001;
    private boolean isResumed = false;

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;

    private SignInButton signInButton;
    private LinearLayout gpSignOutLayout;
    private LinearLayout gpLayout;
    private LinearLayout fbLayout;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private TextView gpStatusTextView;
    private TextView fbStatusTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash, container, false);

        //facebook login
        callbackManager = CallbackManager.Factory.create();
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    Log.d(TAG, "onCurrentProfileChanged: " + currentProfile.getName());
                    fbStatusTextView.setText(getString(R.string.signed_in_fmt, currentProfile.getName()));
                    fbStatusTextView.setVisibility(View.VISIBLE);
                } else {
                    fbStatusTextView.setText(null);
                    fbStatusTextView.setVisibility(View.GONE);
                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken != null) {
                    if (isResumed && !getApp().isLoggedIn()) {
                        signInApplication(currentAccessToken.getToken(), null);
                    }
                } else {
                    if (getApp().getFbToken() != null && getApp().isLoggedIn()) {
                        signOutApplication();
                    }
                }
            }
        };
        fbStatusTextView = (TextView) view.findViewById(R.id.fb_status);
        //
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("email,public_profile");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(getActivity(), "Login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getActivity(), "Login error", Toast.LENGTH_SHORT).show();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_plus_client_id))
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(((MainActivity) getActivity()) /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
                        // be available.
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //
        gpSignOutLayout = (LinearLayout) view.findViewById(R.id.sign_out_and_disconnect);
        gpStatusTextView = (TextView) view.findViewById(R.id.gp_status);
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        signInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpSignIn();
            }
        });
        //
        Button btnSignOut = (Button) view.findViewById(R.id.btn_sign_out);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpSignOut();
            }
        });
        Button btnDisconnect = (Button) view.findViewById(R.id.btn_disconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpRevokeAccess();
            }
        });
        //
        gpLayout = (LinearLayout) view.findViewById(R.id.gp_layout);
        fbLayout = (LinearLayout) view.findViewById(R.id.fb_layout);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    public void onResume() {
        super.onResume();
        isResumed = true;
        if (AccessToken.getCurrentAccessToken() != null) {
            if (!getApp().isLoggedIn()) {
                // if the user already logged in, try to show the selection fragment
                signInApplication(AccessToken.getCurrentAccessToken().getToken(), null);
            }
        } else {
            if (getApp().isLoggedIn()) {
                fbLayout.setVisibility(getApp().getFbToken() != null ? View.VISIBLE : View.GONE);
                gpLayout.setVisibility(getApp().getGpToken() != null ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    private void gpSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void gpSignOut() {
        signOutApplication();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void gpRevokeAccess() {
        signOutApplication();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        updateUI(false);
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();

            Log.d(TAG, "getIdToken:" + acct.getIdToken());
            Log.d(TAG, "handleSignInResult: " + getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //
            if (!getApp().isLoggedIn()) {
                signInApplication(null, acct.getIdToken());
            }
            gpStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getEmail()));
            updateUI(true);
        } else {
            if (getApp().getGpToken() != null && getApp().isLoggedIn()) {
                signOutApplication();
            }
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            signInButton.setVisibility(View.GONE);
            gpSignOutLayout.setVisibility(View.VISIBLE);
            gpStatusTextView.setVisibility(View.VISIBLE);
        } else {
            gpStatusTextView.setText(null);
            signInButton.setVisibility(View.VISIBLE);
            gpSignOutLayout.setVisibility(View.GONE);
            gpStatusTextView.setVisibility(View.GONE);
        }
    }

    private void signInApplication(final String fbToken, final String gpToken) {
        getApp().setFbToken(fbToken);
        getApp().setGpToken(gpToken);
        LoginRequest loginRequest = new LoginRequest(fbToken, gpToken);
        RestClient restClient = new RestClient((MainActivity) getActivity(),
                getApp().getAPIPath("/signin"),
                RestClient.RequestMethod.POST, null, JsonUtils.toJson(loginRequest));
        restClient.setResultCallback(new RestClient.ResultCallback() {
            @Override
            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                if (ResponseCode == 200) {
                    if (ResponseData != null && !ResponseData.isEmpty()) {
                        Log.i("MainActivity", "Login Result: " + ResponseData);
                        fbLayout.setVisibility(fbToken != null ? View.VISIBLE : View.GONE);
                        gpLayout.setVisibility(gpToken != null ? View.VISIBLE : View.GONE);
                        UserSessionDetails userSessionDetails = JsonUtils.getFromJson(ResponseData, UserSessionDetails.class);
                        getApp().setSessionDetails(userSessionDetails);
                        if (userSessionDetails.NewSignUp == 1) {//if its new sign up, then show the profile first
                            ((MainActivity) getActivity()).showProfileFragment(false);
                        } else if (getApp().getDeepLink() != null) {//if its deep link URL, then take us to the feed detail
                            ((MainActivity) getActivity()).showFeedDetailsFragment(getApp().getDeepLink(), true);
                            getApp().setDeepLink(null);
                        } else {
                            ((MainActivity) getActivity()).showFeedsFragment(false);
                        }
                    }
                } else
                {
                    Log.d(TAG, "onCallComplete: " + ResponseData);
                }
            }
    });
        restClient.execute();
    }

    private void signOutApplication() {
        Map headers = new HashMap<String, String>();
        headers.put("AccToken", getApp().getSessionDetails().sessionId);
        RestClient restClient = new RestClient((MainActivity) getActivity(),
                getApp().getAPIPath("/signout"),
                RestClient.RequestMethod.POST, headers, null);
        restClient.setResultCallback(new RestClient.ResultCallback() {
            @Override
            public void onCallComplete(int ResponseCode, String ResponseMessage, String ResponseData) {
                if (ResponseCode == 200) {
                    getApp().logOutUser();
                    fbLayout.setVisibility(View.VISIBLE);
                    gpLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        restClient.execute();
    }

    private CrowdPullerApplication getApp() {
        return ((CrowdPullerApplication) getActivity().getApplication());
    }

    public void retryLogin() {
        if (!getApp().isLoggedIn()) {
            signInApplication(getApp().getFbToken(), getApp().getGpToken());
        }
    }
}