package com.nuig.trafficapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.GeoPt;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import com.nuig.trafficappbackend.trafficApp.model.IncidentCollection;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Dylan Toner on 19/01/2016.
 */
public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 2 ;
    private TrafficApp trafficAppAPI;
    private Context c;
    private List<Incident> mItems;
    private Location location;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getContext();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        location = new Location("Default");
        // Retrieve List of incidents
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
        refreshItems();

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.tab_layout,null);
        tabLayout = (TabLayout) v.findViewById(R.id.tabs);
        viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });
        refreshItems();
        return v;

    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            Fragment feed = new FeedFragment();
            Fragment map = new MapFragment();
            switch (position){
              case 0 : return feed;
              case 1 : return map;

            }
            return null;
        }

        @Override
        public int getCount() {
            return int_items;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0 :
                    return "Live Feed";
                case 1 :
                    return "Map";
            }
            return null;
        }
    }

    public void setLocation(Location loc){
        this.location = loc;
    }

    public void refreshItems()
    {
        new ListOfIncidentsAsyncRetriever().execute();
    }

    //Vincenty's formula
    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double dist = earthRadius * c;

        return dist;
    }
    private class ListOfIncidentsAsyncRetriever extends AsyncTask<Void, Void, IncidentCollection> {

        private final Logger log = Logger.getLogger(ListOfIncidentsAsyncRetriever.class.getName());

        @Override
        protected void onPostExecute(final IncidentCollection result) {
            //getActivity().setProgressBarIndeterminateVisibility(false);
            if (result == null || result.getItems() == null
                    || result.getItems().size() < 1) {
                return;
            }
            List<Incident> temp = result.getItems();
            Collections.sort(temp, new Comparator<Incident>() {
                @Override
                public int compare(Incident lhs, Incident rhs) {
                    if (lhs.getTimestamp().getValue() < rhs.getTimestamp().getValue())
                        return 1;
                    else if (lhs.getTimestamp().getValue() > rhs.getTimestamp().getValue())
                        return -1;
                    return 0;
                }
            });
            double preferedDist  = sharedPreferences.getInt("radius",40);
            mItems = new ArrayList<Incident>();
            for(Incident i: temp){
                if(i.getLocation()!=null) {
                    GeoPt dest = i.getLocation();
                    if (distFrom(dest.getLatitude(), dest.getLongitude(),
                            location.getLatitude(), location.getLongitude()) <= preferedDist) {
                        mItems.add(i);
                    }
                }
            }

            List<Fragment> content = getChildFragmentManager().getFragments();
            FeedFragment feed;
            MapFragment map;
            if(content!=null) {
                if (content.get(0) instanceof FeedFragment)
                    feed = (FeedFragment) content.get(0);
                else
                    feed = (FeedFragment) content.get(1);

                if (content.get(1) instanceof MapFragment)
                    map = (MapFragment) content.get(1);
                else
                    map = (MapFragment) content.get(0);

                feed.setList(mItems);
                map.refreshItems(mItems);
            }
        }

        @Override
        protected IncidentCollection doInBackground(final Void... params) {

            IncidentCollection result;

            try {
                //Call to backend method
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
