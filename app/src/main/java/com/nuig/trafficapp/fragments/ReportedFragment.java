package com.nuig.trafficapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import com.nuig.trafficapp.IncidentAdapter;
import com.nuig.trafficappbackend.trafficApp.model.IncidentCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Dylan Toner on 10/02/2016.
 */
public class ReportedFragment extends ListFragment {
    private List<Incident> mItems;
    private TrafficApp trafficAppAPI;
    private Context c;
    private TextView emptyText;
    private String accountName;
    private String displayName;
    private TextView title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources resources = getResources();
        mItems = new ArrayList<Incident>();
        c = getContext();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();

        // Retrieve List of incidents
        new ListOfIncidentsAsyncRetriever().execute();

    }

    public void setList(List<Incident> list)
    {
        mItems = list;
        setListAdapter(new IncidentAdapter(getActivity(), mItems));
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.reported_layout,null);
        SharedPreferences settings = getActivity().getSharedPreferences("TrafficApp", 0);
        accountName = settings.getString("accountName", null);
        displayName = settings.getString("displayName",null);
        emptyText = (TextView) v.findViewById(android.R.id.empty);
        title = (TextView) v.findViewById(android.R.id.title);
        title.append(displayName);

        return v;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        Incident item = mItems.get(position);
        IncidentDetailFragment fragment  = new IncidentDetailFragment();
        fragment.setCurrentIncident(item);
        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
        fragment.show(mFragmentManager,"Details");


    }

    private class ListOfIncidentsAsyncRetriever extends AsyncTask<Void, Void, IncidentCollection> {

        private final Logger log = Logger.getLogger(ListOfIncidentsAsyncRetriever.class.getName());

        @Override
        protected void onPostExecute(final IncidentCollection result) {
            //getActivity().setProgressBarIndeterminateVisibility(false);
            if (result == null || result.getItems() == null
                    || result.getItems().size() < 1) {
                if (result == null) {
                    emptyText.setText(R.string.feed_error);
                } else {
                    emptyText.setText(R.string.feed_empty);
                }
                return;
            }
            List<Incident> temp = result.getItems();
            for(Incident i : temp){
                if(i.getReportedBy().equals(accountName))
                    mItems.add(i);
            }

            setListAdapter(new IncidentAdapter(getActivity(), mItems));
        }

        @Override
        protected IncidentCollection doInBackground(final Void... params) {

            IncidentCollection result;

            try {
                result = trafficAppAPI.incidents().listIncidents().execute();
            } catch (IOException e) {
                String message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
                log.severe("Exception=" + message);
                result = null;
            }
            return result;
        }

    }
}
