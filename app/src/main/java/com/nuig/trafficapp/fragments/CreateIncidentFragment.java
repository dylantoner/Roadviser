package com.nuig.trafficapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nuig.trafficapp.AccountHelper;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.GeoPt;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Dylan Toner on 29/01/2016.
 */
public class CreateIncidentFragment extends Fragment
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private TrafficApp trafficAppAPI;
    private Context c;
    private String accountName;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng setCoords;
    private final Logger log = Logger
            .getLogger(CreateIncidentFragment.class.getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        c = getContext();
        setCoords = null;
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.create_incident_layout, null);

        SharedPreferences settings = getActivity().getSharedPreferences("TrafficApp", 0);
        accountName = settings.getString("accountName", null);

        if(getArguments()!=null) {
            Bundle args = getArguments();
            if ((args.getParcelable("coords")) != null)
                setCoords = args.getParcelable("coords");
        }

        //Get form elements
        final EditText title = (EditText)v.findViewById(R.id.title_box);
        final EditText description =(EditText) v.findViewById(R.id.description_box);

        //Populate Severity Spinner
        final Spinner severity = (Spinner) v.findViewById(R.id.severity_spinner);
        List<String> severityList = new ArrayList<String>();
        severityList.add("Low");
        severityList.add("Medium");
        severityList.add("High");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, severityList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        severity.setAdapter(adapter);

        final Spinner category = (Spinner) v.findViewById(R.id.category_spinner);
        List<String> categoryList = new ArrayList<String>();
        categoryList.add("Accident");
        categoryList.add("Congestion");
        categoryList.add("Road Works");
        categoryList.add("Weather");
        categoryList.add("Road Closure");
        categoryList.add("Obstruction");

        adapter = new ArrayAdapter<String>(c, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);

        Button button = (Button) v.findViewById(R.id.submit_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Create new incident
                Incident temp = new Incident();
                temp.setTitle(title.getText().toString());
                temp.setDescription(description.getText().toString());
                GeoPt current;
                //set from map
                if(setCoords!=null) {
                    current = new GeoPt()
                            .setLatitude((float) setCoords.latitude)
                            .setLongitude((float) setCoords.longitude);
                }
                //set from current location
                else{
                    current = new GeoPt()
                            .setLatitude((float) mLastLocation.getLatitude())
                            .setLongitude((float) mLastLocation.getLongitude());
                    }
                temp.setLocation(current);
                temp.setSeverity(severity.getSelectedItem().toString());
                temp.setCategory(category.getSelectedItem().toString());
                temp.setReportedBy(accountName);
                if(temp.getTitle()==null||temp.getTitle().toString().equals("")){
                    Toast toast = Toast.makeText(c, "Please enter a title", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(temp.getDescription()==null||temp.getDescription().toString().equals("")){
                    Toast toast = Toast.makeText(c, "Please enter a description", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(temp.getLocation()==null||temp.getLocation().toString().equals("")){
                    Toast toast = Toast.makeText(c, "Please enter a location", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(temp.getSeverity()==null||temp.getSeverity().toString().equals("")){
                    Toast toast = Toast.makeText(c, "Please select severity", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    //Send incident to backend datastore
                    new InsertIncidentTask().execute(temp);
                    //Return to live feed
                    FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                    mFragmentManager.beginTransaction().replace(R.id.containerView, new TabFragment()).commit();
                }
            }
        });

        return v;
    }
    private class InsertIncidentTask extends AsyncTask<Incident, Void, Void> {

        @Override
        protected Void doInBackground(final Incident... params) {

        Incident temp = params [0];
            try {
                trafficAppAPI.incidents().insertIncident(temp).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            AccountHelper ac = new AccountHelper();
            ac.incrementIncidents(accountName);
        }
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch(SecurityException e){
            log.severe("Exception:" +e.getMessage());
        }

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }
    @Override
    public void onConnectionSuspended(int i) {
        // TODO Auto-generated method stub
    }
}
