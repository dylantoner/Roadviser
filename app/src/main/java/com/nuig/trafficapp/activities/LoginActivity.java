package com.nuig.trafficapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.nuig.trafficapp.Constants;
import com.nuig.trafficapp.R;

import static com.google.android.gms.auth.api.Auth.*;

/**
 * Created by Dylan Toner on 18/02/2016.
 */
public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private static final int RC_GET_TOKEN = 9002;
    private static GoogleAccountCredential credential;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private GoogleSignInAccount acct;
    private static final int REQUEST_GET_ACCOUNTS = 1;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Accounts permission has not been granted.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_GET_ACCOUNTS);
        }
        else {
            credential = GoogleAccountCredential.usingAudience(this, Constants.AUDIENCE_ANDROID_CLIENT_ID);
        }

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.WEB_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());

    }

    public static GoogleAccountCredential getCredential() {
        return credential;
    }
    public static void clearCredential(){
        if(credential!=null)
            credential.setSelectedAccountName("");
    }

    @Override
    public void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            if (result != null){
                credential.setSelectedAccountName(result.getSignInAccount().getEmail());
                handleSignInResult(result);
             }
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GET_TOKEN) {
            GoogleSignInResult result = GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            updateUI(true);
            credential = GoogleAccountCredential.usingAudience(this, Constants.AUDIENCE_ANDROID_CLIENT_ID);
            String email = acct.getEmail();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Accounts permission has not been granted.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS},
                        REQUEST_GET_ACCOUNTS);
            }
            else {
                credential.setSelectedAccountName(email);
            }


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Location permission has not been granted.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_ACCESS_FINE_LOCATION);
                } else {
                SharedPreferences settings = getSharedPreferences("TrafficApp", 0);
                String accountName = acct.getEmail();
                String displayName = acct.getDisplayName();

                SharedPreferences.Editor editor = settings.edit();
                editor.putString("accountName",accountName);
                editor.putString("displayName",displayName);
                editor.apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("acct", acct);
                    startActivity(intent);
                }
            }
         else {
            // Signed out, show unauthenticated UI.
            updateUI(false);
        }

    }
    // [END handleSignInResult]
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PERMISSION IS ALLOWED.
                    credential = GoogleAccountCredential.usingAudience(this, Constants.AUDIENCE_ANDROID_CLIENT_ID);
                } else {
                    Toast.makeText(LoginActivity.this, "This app requires access to phone's accounts to login.", Toast.LENGTH_SHORT).show();
                    if(acct!=null)
                        credential.setSelectedAccountName(acct.getEmail());
                }
                return;
            }
            case REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // PERMISSION IS ALLOWED.
                    SharedPreferences settings = getSharedPreferences("TrafficApp", 0);
                    String accountName = acct.getEmail();
                    String displayName = acct.getDisplayName();

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("accountName",accountName);
                    editor.putString("displayName",displayName);
                    editor.apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("acct", acct);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "Location permission is required to use this app", Toast.LENGTH_SHORT).show();
                    updateUI(false);
                }
                return;
            }
        }
    }

    private void signIn() {
        Intent signInIntent = GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    private void revokeAccess() {
        GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading");
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
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }
}
