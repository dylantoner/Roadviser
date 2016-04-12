package com.nuig.trafficapp.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nuig.trafficapp.CloudEndpointBuilderHelper;
import com.nuig.trafficapp.R;
import com.nuig.trafficappbackend.trafficApp.TrafficApp;
import com.nuig.trafficappbackend.trafficApp.model.GeoPt;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Dylan Toner on 21/01/2016.
 */
public class MapFragment extends Fragment
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener{
    private GoogleMap map;
    private LocationRequest lr;
    private GoogleApiClient mGoogleApiClient;
    private com.google.android.gms.maps.MapFragment mapFragment;
    private ImageView iv;
    private Location mCurrentLocation;
    private Location mLastLocation;
    private static View view;
    private List<Incident> mItems;
    private TrafficApp trafficAppAPI;
    Marker posMarker;
    private final Logger log = Logger.getLogger(MapFragment.class.getName());


    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = container;
            if (parent != null)
                parent.removeView(view);
        }

        try {
            view = inflater.inflate(R.layout.map_layout, container, false);

        }
        catch (InflateException e) {
            Toast.makeText(getActivity(), "Problems inflating the view !", Toast.LENGTH_LONG).show();
            TabFragment fragment = (TabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.containerView);
            List<Fragment> content = fragment.getChildFragmentManager().getFragments();
            fragment.getChildFragmentManager().beginTransaction().remove(content.get(1));
            onCreateView(inflater,container,savedInstanceState);
        }
        catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Google Play Services missing !", Toast.LENGTH_LONG).show();
        }
        //refreshItems();
        return view;
    }

    public void refreshItems(List<Incident> list){
        mItems = list;
        updateUI(mCurrentLocation);
    }

    public void initializeMap() {
        mapFragment = ((com.google.android.gms.maps.MapFragment) this.getActivity().getFragmentManager().findFragmentById(R.id.map));
        iv = (ImageView) view.findViewById(R.id.iv);

        map = mapFragment.getMap();
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        try{
        map.setMyLocationEnabled(true);}
        catch(SecurityException e){
            log.severe("Exception: " + e.getMessage());
        }

        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnInfoWindowClickListener(this);
        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
        MapsInitializer.initialize(this.getActivity());
    }

    @Override
    public void onMapClick(final LatLng latLng)
    {
        if (posMarker != null) {
            posMarker.remove();
        }
        posMarker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_icon_large))
                .title("New Incident"));
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if(marker.getTitle().equals("New Incident")){
            FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
            Fragment newIncident = new CreateIncidentFragment();
            Bundle args = new Bundle();
            LatLng latLng = marker.getPosition();
            args.putParcelable("coords", latLng);
            newIncident.setArguments(args);
            mFragmentManager.beginTransaction().replace(R.id.containerView,newIncident ).addToBackStack(null).commit();
        }
        else
            marker.showInfoWindow();

        return true;
    }
    @Override
    public void onInfoWindowClick(final Marker marker) {
        for (Incident i: mItems) {
            if (marker.getTitle().equals(i.getTitle().toString())) {
                IncidentDetailFragment fragment = new IncidentDetailFragment();
                fragment.setCurrentIncident(i);
                FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                fragment.show(mFragmentManager,"Details");
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lr = LocationRequest.create();
        trafficAppAPI = CloudEndpointBuilderHelper.getEndpoints();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onLocationChanged(Location loc) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 15);
        map.animateCamera(cameraUpdate);
        this.mCurrentLocation = loc;
        updateUI(mCurrentLocation);
        TabFragment fragment = (TabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.containerView);
        fragment.setLocation(mCurrentLocation);
    }

    private void updateUI(Location loc)
    {
        if (loc != null && map!=null) {
            LatLng lat = new LatLng(loc.getLatitude(), loc.getLongitude());

            map.clear();
            if(mItems!=null) {
                for (Incident i : mItems) {
                    GeoPt g = i.getLocation();
                    LatLng latLng = new LatLng(g.getLatitude(), g.getLongitude());
                    MarkerOptions marker = new MarkerOptions().position(latLng).title(i.getTitle().toString());
                    marker.icon(BitmapDescriptorFactory.fromResource(chooseIcon(i)));
                    map.addMarker(marker);
                }
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(lat).zoom(14).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }
    private int chooseIcon (Incident i){
        int icon= R.drawable.ic_hazard_yellow;
        if(i.getSeverity().equals("Low"))
        {
            if(i.getCategory().equals("Accident"))
                icon = R.drawable.yellow_accident;
            else if(i.getCategory().equals("Congestion"))
                icon = R.drawable.yellow_congestion;
            else if(i.getCategory().equals("Road Works"))
                icon = R.drawable.yellow_road_works;
            else if(i.getCategory().equals("Weather"))
                icon = R.drawable.yellow_flood;
            else if(i.getCategory().equals("Road Closure"))
                icon = R.drawable.yellow_road_closed;
            else if(i.getCategory().equals("Obstruction"))
                icon = R.drawable.yellow_blockage;
        }
        else if(i.getSeverity().equals("Medium")){
            if(i.getCategory().equals("Accident"))
                icon = R.drawable.amber_accident;
            else if(i.getCategory().equals("Congestion"))
                icon = R.drawable.amber_congestion;
            else if(i.getCategory().equals("Road Works"))
                icon = R.drawable.amber_road_works;
            else if(i.getCategory().equals("Weather"))
                icon = R.drawable.amber_flood;
            else if(i.getCategory().equals("Road Closure"))
                icon = R.drawable.amber_road_closed;
            else if(i.getCategory().equals("Obstruction"))
                icon = R.drawable.amber_blockage;
        }
        else if(i.getSeverity().equals("High")){
            if(i.getCategory().equals("Accident"))
                icon = R.drawable.red_accident;
            else if(i.getCategory().equals("Congestion"))
                icon = R.drawable.red_congestion;
            else if(i.getCategory().equals("Road Works"))
                icon = R.drawable.red_road_works;
            else if(i.getCategory().equals("Weather"))
                icon = R.drawable.red_flood;
            else if(i.getCategory().equals("Road Closure"))
                icon = R.drawable.red_road_closed;
            else if(i.getCategory().equals("Obstruction"))
                icon = R.drawable.red_blockage;
        }
        return icon;
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            startLocationUpdates();
            TabFragment fragment = (TabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.containerView);
            fragment.setLocation(mLastLocation);
            updateUI(mLastLocation);}
        catch(SecurityException e){
            log.severe("Exception: " + e.getMessage());
            }

    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        initializeMap();
        super.onStart();
    }
    @Override
    public void onStop(){
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            if(mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, lr, this);
        }
        catch(SecurityException e){
            log.severe("Exception: " + e.getMessage());
        }

    }
    protected void stopLocationUpdates() {
        if(mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapFragment = ((com.google.android.gms.maps.MapFragment) this.getActivity().getFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null)
            this.getActivity().getFragmentManager().beginTransaction().remove(mapFragment).commit();
    }
}