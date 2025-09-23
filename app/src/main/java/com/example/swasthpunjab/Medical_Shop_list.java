package com.example.swasthpunjab;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.*;
import java.util.ArrayList;

public class Medical_Shop_list extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;
    private FusedLocationProviderClient locationClient;
    private ListView shopListView;
    private ArrayList<String> shopList;
    private ShopListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_shop_list);

        shopListView = findViewById(R.id.shopListView);
        shopList = new ArrayList<>();
        adapter = new ShopListAdapter(this, shopList);
        shopListView.setAdapter(adapter);

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                loadNearbyMedicalShops(location);
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNearbyMedicalShops(Location location) {
        // Dummy data for now
        shopList.clear();
        shopList.add("Apollo Pharmacy - 1.2 km");
        shopList.add("Ratan Medicos - 0.8 km");
        shopList.add("Max 24 Pharmacy - 2.0 km");
        shopList.add("Absolute Life Care - 1.5 km");
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}