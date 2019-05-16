// TopicArrayAdapter.java
// An ArrayAdapter for displaying a List<Topic>'s elements in a ListView
package com.morrill.swviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopicArrayAdapter extends ArrayAdapter<Topic> {

    private static class ViewHolder {
        TextView nameTextView;
    }

    // stores already downloaded bitmaps for reuse
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    // constructor to initialize superclass inherited members
    public TopicArrayAdapter(Context context, List<Topic> forecast) {
        super(context, -1, forecast);
    }

    // creates the custom views for the ListView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get Topic object for this specified ListView position
        Topic name = getItem(position);

        ViewHolder viewHolder; // object that reference's list item's views

        // check for reusable ViewHolder from a ListView item that scrolled
        // offscreen; otherwise, create a new ViewHolder
        if(convertView == null) { // no reusable ViewHolder, so create one
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView =
                    inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.nameTextView =
                    (TextView) convertView.findViewById(R.id.nameTextView);
            convertView.setTag(viewHolder);
        }
        else { // reuse existing ViewHolder stored as the list item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }


        // get other data from Topic object and place into views
        Context context = getContext(); // for loading String resources
        viewHolder.nameTextView.setText(context.getString(
                R.string.name_description, name.name));

        return convertView; // return completed list item to display
    }
}
