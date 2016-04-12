package com.nuig.trafficapp.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.GeoPt;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import com.nuig.trafficappbackend.trafficApp.model.UserAccount;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Dylan Toner on 29/01/2016.
 */
public class IncidentDetailFragment extends DialogFragment implements TextToSpeech.OnInitListener {
    private Incident currentIncident;
    private Context c;
    private String accountName;
    private TrafficApp trafficAppAPI;
    private TextToSpeech engine;
    private String strAddress;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        c = getContext();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        SharedPreferences settings = getActivity().getSharedPreferences("TrafficApp", 0);
        accountName = settings.getString("accountName", null);
        engine = new TextToSpeech(c, this);
    }
    public void setCurrentIncident(Incident incident)
    {
        this.currentIncident = incident;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            engine.setLanguage(Locale.UK);
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.incident_detail_layout, null);

        TextView timestamp = (TextView) v.findViewById(R.id.incident_time);
        if(currentIncident.getTimestamp()!=null) {
            Date d = new Date(currentIncident.getTimestamp().getValue());
            SimpleDateFormat format = new SimpleDateFormat("HH:mm a, dd MMM");
            String date = format.format(d);
            timestamp.setText(date);
        }
        else
            timestamp.setText("Unknown");

        TextView submittedBy = (TextView) v.findViewById(R.id.submittedBy);
        if(currentIncident.getReportedBy()!=null)
            submittedBy.setText(currentIncident.getReportedBy());

        TextView severity = (TextView) v.findViewById(R.id.severity);
        if(currentIncident.getSeverity()!=null)
            severity.setText(currentIncident.getSeverity());

        TextView category = (TextView) v.findViewById(R.id.category);
        if(currentIncident.getCategory()!=null)
            category.setText(currentIncident.getCategory());

        TextView details = (TextView) v.findViewById(R.id.details);
        if(currentIncident.getDescription()!=null)
            details.setText(currentIncident.getDescription());

        TextView trustRating = (TextView) v.findViewById(R.id.incident_trust);
        if(currentIncident.getTrustScore()!=null)
            if(currentIncident.getTrustScore()<0)
                trustRating.setText(R.string.admin_confirmed);
            else if(currentIncident.getTrustScore()<25){
                trustRating.setText(R.string.status_unconfirmed);
                trustRating.append(" ("+currentIncident.getTrustScore().toString()+")");}
            else if(currentIncident.getTrustScore()>=25 && currentIncident.getTrustScore()<50){
                trustRating.setText(R.string.status_semi_confirmed);
                trustRating.append(" ("+currentIncident.getTrustScore().toString()+")");}
            else if(currentIncident.getTrustScore()>50){
                trustRating.setText(R.string.status_confirmed);
                trustRating.append(" ("+currentIncident.getTrustScore().toString()+")");}


        Geocoder gc = new Geocoder(c);
        TextView location = (TextView) v.findViewById(R.id.location);
        if(gc.isPresent()){
            try {
                GeoPt temp = currentIncident.getLocation();
                List<Address> list = gc.getFromLocation(temp.getLatitude(), temp.getLongitude(), 1);
                Address address = list.get(0);

                StringBuffer str = new StringBuffer();
                if(address.getThoroughfare()!=null)
                    str.append(address.getThoroughfare() + ", ");

                if(address.getLocality()!=null)
                    str.append(address.getLocality());
                else
                    str.append(address.getAdminArea());
                strAddress = str.toString();
                location.setText(strAddress);
            }catch (IOException e){

            }
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        //Cannot verify own incident
        if(currentIncident.getReportedBy().equals(accountName)){
            builder.setView(v)
                    // Add action buttons
                    .setTitle(currentIncident.getTitle())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            IncidentDetailFragment.this.getDialog().cancel();
                        }
                    }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new DeleteIncidentTask().execute(currentIncident.getIncidentId());
                    //TabFragment fragment = (TabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.containerView);
                    //fragment.refreshItems();
                    Toast.makeText(c, "Incident Deleted", Toast.LENGTH_SHORT).show();
                }
            }).setNeutralButton("Speak", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String toSpeak = currentIncident.getTitle()+ ".. " + currentIncident.getDescription() + ".. at. " + strAddress;
                    if(Build.VERSION.SDK_INT >= 21)
                        engine.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });
        }
        //Cant verify if already confirmed by admin
        else if(currentIncident.getTrustScore()<0){
            builder.setView(v)
                    // Add action buttons
                    .setTitle(currentIncident.getTitle())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            IncidentDetailFragment.this.getDialog().cancel();
                        }
                    }).setNeutralButton("Speak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String toSpeak = currentIncident.getTitle()+ ".. " + currentIncident.getDescription() + ".. at. " + strAddress;
                    if(Build.VERSION.SDK_INT >= 21)
                        engine.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                }

            });
        }
        else {
            builder.setView(v)
                    // Add action buttons
                    .setTitle(currentIncident.getTitle())
                    .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new VerifyTask().execute(currentIncident);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            IncidentDetailFragment.this.getDialog().cancel();
                        }
                    }).setNeutralButton("Speak", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    String toSpeak = currentIncident.getTitle()+ ".. " + currentIncident.getDescription() + ".. at. " + strAddress;

                    if(Build.VERSION.SDK_INT >= 21)
                        engine.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });
        }
        return builder.create();
    }
    private class VerifyTask extends AsyncTask<Incident, Void, Void> {

        @Override
        protected Void doInBackground(final Incident... params) {

            Incident inc =  params [0];
            String email = inc.getReportedBy();
            try {
                UserAccount temp = trafficAppAPI.userAccounts().getUserAccount(email).execute();
                int rep = temp.getReputation();
                int weight = 1;
                if (rep <= 10)
                    weight = 5;
                else if (rep > 10 && rep <= 20)
                    weight = 10;
                else if (rep > 20 && rep <= 30)
                    weight = 15;
                else if (rep > 30 && rep <= 40)
                    weight = 20;
                else if (rep > 40 && rep <= 50)
                    weight = 25;
                else if (rep > 50)
                    weight = 30;

                List<String> verified = inc.getVerifiedBy();
                if(verified==null || !(verified.contains(accountName))) {
                    inc.setTrustScore(inc.getTrustScore()+weight);
                    temp.setReputation(rep + 1);
                    trafficAppAPI.incidents().updateIncident(inc).execute();
                    trafficAppAPI.userAccounts().updateUserAcount(temp).execute();
                    //Toast.makeText(getContext(), "Incident Verified", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                //log.severe("Exception=" + message);
            }
            return null;
        }

    }
    private class DeleteIncidentTask extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(final Long... params) {
            Long incidentId = params [0];

            try {
                trafficAppAPI.incidents().removeIncident(incidentId).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }

            }
            return null;
        }
    }
}
