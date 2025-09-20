package com.example.swasthpunjab;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class DoctorListActivity extends AppCompatActivity {

    ListView doctorListView;
    ArrayList<String> doctorNames = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        doctorListView = findViewById(R.id.doctorListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doctorNames);
        doctorListView.setAdapter(adapter);

        // Fetch doctors from SQLite
        MyDBHelper dbHelper = new MyDBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(MyDBHelper.TABLE_DOCTORS, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_NAME));
            String specialty = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_SPECIALTY));
            doctorNames.add(name + " - " + specialty);
        }

        cursor.close();
        db.close();

        // Handle doctor selection
        doctorListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDoctor = doctorNames.get(position);
            Intent intent = new Intent(DoctorListActivity.this, VideoCallWithDoctor.class);
            intent.putExtra("doctorName", selectedDoctor);
            startActivity(intent);
        });
    }
}