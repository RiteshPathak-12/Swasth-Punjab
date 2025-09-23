package com.example.swasthpunjab;

import android.content.Context;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class ShopListAdapter extends ArrayAdapter<String> {

    public ShopListAdapter(Context context, ArrayList<String> shops) {
        super(context, 0, shops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String shop = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView shopName = convertView.findViewById(android.R.id.text1);
        shopName.setText(shop);
        return convertView;
    }
}