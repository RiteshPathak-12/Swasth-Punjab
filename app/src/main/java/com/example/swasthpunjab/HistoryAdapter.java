package com.example.swasthpunjab;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class HistoryAdapter extends ArrayAdapter<String> {

    public HistoryAdapter(Context context, ArrayList<String> history) {
        super(context, 0, history);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String record = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView text = convertView.findViewById(android.R.id.text1);
        text.setText(record);
        return convertView;
    }
}
