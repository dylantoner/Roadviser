package com.nuig.trafficapp;

import android.content.Context;
import android.os.AsyncTask;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.UserAccount;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Created by Dylan Toner on 20/02/2016.
 */
public class AccountHelper {
    private TrafficApp trafficAppAPI;
    private final Logger log = Logger.getLogger(AccountHelper.class.getName());
    //Constructor
    public AccountHelper(){
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
    }


    public void addAccount(String email,String dispName){
        ParamList params = new ParamList(email,dispName);

        new InsertUserAccountTask().execute(params);

    }

    public void removeAccount(String email){

        new DeleteUserAccountTask().execute(email);

    }
    public UserAccount getAccount(String email){
        UserAccount usr = null;
        try {
            usr = new GetAccountTask().execute(email).get();
        }
        catch (Exception e){

        }
        return usr;
    }
    public void incrementReputation(String email)
    {
        new IncrementReputationTask().execute(email);
    }

    public void incrementIncidents(String email)
    {
        new IncrementIncidentsTask().execute(email);
    }

    private class InsertUserAccountTask extends AsyncTask<ParamList, Void, Void> {

        @Override
        protected Void doInBackground(final ParamList... params) {

            ParamList paramList = params [0];
            String email = paramList.email;
            String dispName = paramList.dispName;
            try {
                trafficAppAPI.userAccounts().addUserAccount(email,dispName).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return null;
        }
    }
    private class DeleteUserAccountTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {

            String email = params [0];

            try {
                trafficAppAPI.userAccounts().removeUserAccount(email).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return null;
        }
    }
    private class IncrementIncidentsTask extends AsyncTask<String, Void, UserAccount> {

        @Override
        protected UserAccount doInBackground(final String... params) {

            String email = params [0];
            UserAccount temp = null;
            try {
                temp = trafficAppAPI.userAccounts().getUserAccount(email).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return temp;
        }

        @Override
        protected void onPostExecute(final UserAccount result) {
            result.setNumIncidents((result.getNumIncidents() + 1));
            new UpdateAccountTask().execute(result);
        }
    }
    private class IncrementReputationTask extends AsyncTask<String, Void, UserAccount> {

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
                log.severe("Exception=" + message);
            }
            return temp;
        }

        @Override
        protected void onPostExecute(final UserAccount result) {
            if(result!=null) {
                result.setReputation((result.getReputation() + 1));
                new UpdateAccountTask().execute(result);
            }
        }
    }

    private class UpdateAccountTask extends AsyncTask<UserAccount, Void, Void> {

        @Override
        protected Void doInBackground(final UserAccount... params) {
            UserAccount temp = params [0];
            try {
                trafficAppAPI.userAccounts().updateUserAcount(temp).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return null;
        }
    }

    private class GetAccountTask extends AsyncTask<String, Void, UserAccount> {

        @Override
        protected UserAccount doInBackground(final String... params) {

            String email = params [0];
            UserAccount temp = null;
            try {
                temp = trafficAppAPI.userAccounts().getUserAccount(email).execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
            }
            return temp;
        }
    }

    private class ParamList{
        public String email;
        public String dispName;

        public ParamList(String email,String name){
            this.email = email;
            this.dispName = name;
        }

    }
}
