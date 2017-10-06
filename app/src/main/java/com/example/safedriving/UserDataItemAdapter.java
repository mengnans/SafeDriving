package com.example.safedriving;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 *
 * Adapter to bind a UserDataItem List to a view
 * Populates Notification screen with rows of speeding instances
 *
 * @author Daniel Gray, Mengnan Shi, Stanley Sim
 *
 */
public class UserDataItemAdapter extends ArrayAdapter<UserDataItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public UserDataItemAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);

        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final UserDataItem currentItem = getItem(position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);
        }

        row.setTag(currentItem);


        final TextView speedingTickets = (TextView) row.findViewById(R.id.speedingItem);
        speedingTickets.setText(
                currentItem.getDateString() +"\n"
                +"Speed: "+currentItem.getmSpeedString()+"\n"
                +"Limit: "+currentItem.getmLimitString()+"\n"
                +"Street: "+ currentItem.getmStreet()+"\n"
                +"Lat: "+currentItem.getmLatString()+"\n"
                +"Long: " +currentItem.getmLongString());

        return row;
    }
}