package com.nuig.trafficapp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.nuig.trafficapp.AccountHelper;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficapp.activities.LoginActivity;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.UserAccount;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;

/**
 * Created by Dylan Toner on 10/02/2016.
 */
public class ProfileFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{

    private TrafficApp trafficAppAPI;
    private UserAccount userAccount;
    private TextView name;
    private TextView dateJoined;
    private TextView incidents;
    private TextView trust;
    private ImageView profilePic;
    private GoogleSignInAccount acct;
    private String photoUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.profile_layout,null);
        Bundle args = getArguments();
        if(args!=null)
            acct=args.getParcelable("acct");
        if(acct!=null)
            new GetUserAccountTask().execute(acct.getEmail());
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        name = (TextView) v.findViewById(R.id.name);
        dateJoined = (TextView) v.findViewById(R.id.dateJoined);
        incidents = (TextView) v.findViewById(R.id.incidents);
        trust = (TextView) v.findViewById(R.id.trust);
        profilePic = (ImageView) v.findViewById(R.id.profilePic);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void updateUI(UserAccount usr){
        if(usr!=null && acct!=null) {

            name.append(acct.getDisplayName());
            Date d = new Date(usr.getDateJoined().getValue());
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            String date = format.format(d);
            if(acct.getPhotoUrl()!=null)
                photoUrl = acct.getPhotoUrl().toString();
            dateJoined.append(date);
            trust.append(usr.getReputation().toString());
            incidents.append(usr.getNumIncidents().toString());
            if(photoUrl!=null)
                new LoadProfileImage(profilePic).execute(photoUrl);
        }
    }
    private class GetUserAccountTask extends AsyncTask<String, Void, UserAccount> {

        @Override
        protected UserAccount doInBackground(final String... params) {
            if(trafficAppAPI==null)
                trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
            String email = params [0];
            UserAccount temp = null;
            try {
                temp = trafficAppAPI.userAccounts().getUserAccount(email).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
            }
            userAccount = temp;
            return temp;
        }
        @Override
        protected void onPostExecute(final UserAccount result) {
            updateUI(result);
        }
    }

    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
