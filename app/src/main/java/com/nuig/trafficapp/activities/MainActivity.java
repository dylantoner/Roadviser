package com.nuig.trafficapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nuig.trafficapp.AccountHelper;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.QuickstartPreferences;
import com.nuig.trafficapp.R;
import com.nuig.trafficapp.RegistrationIntentService;
import com.nuig.trafficapp.fragments.FeedbackDialogFragment;
import com.nuig.trafficapp.fragments.ProfileFragment;
import com.nuig.trafficapp.fragments.ReportedFragment;
import com.nuig.trafficapp.fragments.TabFragment;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import java.util.logging.Logger;

import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;

/**
 * Created by Dylan Toner on 19/01/2016.
 */
public class MainActivity extends FragmentActivity
implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener
{
    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    private ListView incidentsList;
    private TrafficApp trafficAppAPI;
    private Context context;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final String TAG = MainActivity.class.getSimpleName();
    private static final Logger LOG = Logger.getLogger(MainActivity.class.getName());
    private static final String PROPERTY_REG_ID = "registrationId";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private AccountHelper accountHelper;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    GoogleApiClient mGoogleApiClient;
    GoogleSignInAccount acct;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Bundle extras = getIntent().getExtras();
        if(extras!=null)
            acct = extras.getParcelable("acct");

        SharedPreferences settings = getSharedPreferences("TrafficApp", 0);
        String accountName = settings.getString("accountName",null);
        String displayName = settings.getString("displayName",null);
        accountHelper = new AccountHelper();

        accountHelper.addAccount(accountName,displayName);
        incidentsList = (ListView) findViewById(android.R.id.list);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();

        mNavigationView.setNavigationItemSelectedListener(this);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name, R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(QuickstartPreferences.REGISTRATION_COMPLETE)) {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(context);
                    boolean sentToken = sharedPreferences
                            .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                    if (sentToken) {
                        Log.i(TAG, "Token Success");
                    } else {
                        Log.i(TAG, "Token Error");
                    }
                }
                if (intent.getAction().equals(QuickstartPreferences.UPDATE_UI)){
                    TabFragment fragment = (TabFragment) getSupportFragmentManager().findFragmentById(R.id.containerView);
                        if(fragment!=null)
                            fragment.refreshItems();
                }
            }

        };

        if (checkPlayServices()) {

            Log.i(TAG, "Not registered with GCM.");
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
            // Register GCM id in the background
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(GOOGLE_SIGN_IN_API, gso)
                .build();


    }//End OnCreate

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }
    @Override
    protected void onStart() {
        super.onStart();
        //Sign In again if necessary
        if (acct==null){
            acct = reLogin();
        }
    }

    public GoogleSignInAccount reLogin(){
        mGoogleApiClient.connect();
        GoogleSignInAccount ac = null;
        OptionalPendingResult<GoogleSignInResult> opr = GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            if (result != null){
                ac = result.getSignInAccount();
            }
        }
        return ac;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Sign In again if necessary
        if (acct==null){
            acct = reLogin();
        }

    }@Override
     protected void onStop() {
    super.onStop();
    logout();
}

    @Override
    protected void onResume() {
        super.onResume();

        //Sign In again if necessary
        if (acct==null){
            acct = reLogin();
        }

        IntentFilter filter = new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE);
        filter.addAction(QuickstartPreferences.UPDATE_UI);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        //Navigation Items
        if (menuItem.getItemId() == R.id.nav_home) {
            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
            Fragment tab = new TabFragment();
            Fragment currentFragment = mFragmentManager.findFragmentById(R.id.containerView);
            if(!(currentFragment instanceof TabFragment))
                xfragmentTransaction.replace(R.id.containerView,tab).addToBackStack(null).commit();
        }
        else if (menuItem.getItemId() == R.id.nav_profile) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            Fragment profile = new ProfileFragment();
            Bundle args = new Bundle();
            args.putParcelable("acct",acct);
            profile.setArguments(args);
            fragmentTransaction.replace(R.id.containerView, profile).addToBackStack(null).commit();
        }
        else if (menuItem.getItemId() == R.id.nav_reported) {
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerView,new ReportedFragment()).addToBackStack(null).commit();
        }

        //Action Items
        else if (menuItem.getItemId() == R.id.nav_settings) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
        }

        else if (menuItem.getItemId() == R.id.nav_feedback) {
            DialogFragment newFragment = new FeedbackDialogFragment();
            newFragment.show(getSupportFragmentManager(), "feedback");
        }
        else if (menuItem.getItemId() == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logout(){
        LoginActivity.clearCredential();
        mGoogleApiClient.connect();
        if (mGoogleApiClient.isConnected()) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(Status status) {

                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivity(intent);

                        }
                    });
        }
    }
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}