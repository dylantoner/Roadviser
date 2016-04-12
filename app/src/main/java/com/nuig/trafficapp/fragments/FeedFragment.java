package com.nuig.trafficapp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.List;


/**
 * Created by Dylan Toner on 21/01/2016.
 */
public class FeedFragment extends ListFragment {
    private List<Incident> mItems;
    private TrafficApp trafficAppAPI;
    private Context c;
    private TextView emptyText;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        c = getContext();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        refreshItems();
}

    public void setList(List<Incident> list)
    {
        mItems = list;
        if(mItems!=null)
            setListAdapter(new IncidentAdapter(getActivity(), mItems));
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_layout,null);
        emptyText = (TextView) v.findViewById(android.R.id.empty);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                mFragmentManager.beginTransaction().replace(R.id.containerView, new CreateIncidentFragment()).addToBackStack(null).commit();
            }
        });
        refreshItems();
        return v;
    }

public void refreshItems(){
    TabFragment fragment = (TabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.containerView);
    fragment.refreshItems();
}
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Incident item = mItems.get(position);
        IncidentDetailFragment fragment  = new IncidentDetailFragment();
        fragment.setCurrentIncident(item);
        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
        fragment.show(mFragmentManager, "Details");
    }
}
