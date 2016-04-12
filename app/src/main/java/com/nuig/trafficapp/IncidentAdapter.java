package com.nuig.trafficapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nuig.trafficappbackend.trafficApp.model.Incident;
import java.util.List;

/**
 * Created by Dylan Toner on 19/01/2016.
 */
public class IncidentAdapter extends ArrayAdapter<Incident> {
    private Context context;

    public IncidentAdapter(Context context, List<Incident> items) {
        super(context, R.layout.incident_item, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.incident_item, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        Incident item = getItem(position);
        //viewHolder.ivIcon.setImageDrawable(item.icon);

        //Set icon colour based on severity
        String severity = item.getSeverity();
        if (severity.equals("Low")){
            viewHolder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_hazard_yellow));
        }
        else if (severity.equals("Medium")){
            viewHolder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_hazard_amber));
        }
        else if (severity.equals("High")){
            viewHolder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_hazard_red));
        }
        else{
            viewHolder.ivIcon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_hazard_yellow));
        }
        viewHolder.tvTitle.setText(item.getTitle());
        viewHolder.tvDescription.setText(item.getDescription());

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvDescription;
    }
}

