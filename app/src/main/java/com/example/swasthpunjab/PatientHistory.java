package com.example.swasthpunjab;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class PatientHistory extends AppCompatActivity {

    ListView historyListView;
    HistoryAdapter adapter;
    ArrayList<String> historyItems = new ArrayList<>();
    MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);

        historyListView = findViewById(R.id.historyListView);
        dbHelper = new MyDBHelper(this);

        String patientName = "Ritesh"; // You can pass this dynamically
        Cursor cursor = dbHelper.getPatientHistory(patientName);

        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No history found for " + patientName, Toast.LENGTH_SHORT).show();
        }

        while (cursor.moveToNext()) {
            String medicine = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_MEDICINE));
            String problem = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_PROBLEM));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COLUMN_DATE));
            historyItems.add(date + "\nMedicine: " + medicine + "\nProblem: " + problem);
        }

        adapter = new HistoryAdapter(this, historyItems);
        historyListView.setAdapter(adapter);
    }
}