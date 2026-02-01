package com.example.swasthpunjab;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MyAppointmentsActivity extends AppCompatActivity {

    ListView appointmentsListView;
    AppointmentsAdapter adapter;
    ArrayList<AppointmentModel> appointmentList = new ArrayList<>();
    MyDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        appointmentsListView = findViewById(R.id.appointmentsListView);
        dbHelper = new MyDBHelper(this);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String email = prefs.getString("patient_email", "");

        if (email.isEmpty()) {
            Toast.makeText(this, "No registered email found.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadAppointments(email);
    }

    private void loadAppointments(String email) {
        Cursor cursor = dbHelper.getAppointments(email);
        
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No booked appointments.", Toast.LENGTH_SHORT).show();
            return;
        }

        while (cursor.moveToNext()) {
            String doctorName = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COL_APP_DOCTOR));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COL_APP_DATE));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COL_APP_TIME));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(MyDBHelper.COL_APP_STATUS));

            // IMPORTANT: Using Doctor Name as ID for now because local DB doesn't store Firebase Key easily without schema change.
            // In a full implementation, you should store the Firebase Key in SQLite or fetch entirely from Firebase here.
            // For now, passing ID = doctorName will work IF we use doctorName as logic key, but the WaitingRoom expects Firebase ID.
            // FIX: We need to fetch from Firebase to get the real ID, OR rely on the fact that we need to migrate MyAppointments to Firebase.
            // Temporary Workaround: Creating a dummy ID or using doctor name, but the WaitingRoom activity will fail if ID is wrong.
            // Recommendation: Migrate MyAppointments to Firebase or update Local DB to store ID.
            
            // To prevent breaking, I will fetch from Firebase dynamically in this specific activity or allow the Adapter to find it.
            // But since I cannot rewrite the whole Sync logic now, I'll pass a placeholder.
            // NOTE: The user MUST migrate this list to Firebase for the ID to be available.
            
            // Assuming for this request that we want the FEATURE to work, I will switch this Activity to use Firebase.
        }
        
        loadAppointmentsFromFirebase(email);
    }
    
    private void loadAppointmentsFromFirebase(String email) {
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("appointments");
        ref.orderByChild("email").equalTo(email).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
             @Override
             public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                 appointmentList.clear();
                 for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                     Appointment apt = ds.getValue(Appointment.class);
                     if (apt != null) {
                         appointmentList.add(new AppointmentModel(apt.getId(), apt.getDoctorName(), apt.getDate(), apt.getTime(), apt.getStatus()));
                     }
                 }
                 if (adapter == null) {
                     adapter = new AppointmentsAdapter(MyAppointmentsActivity.this, appointmentList);
                     appointmentsListView.setAdapter(adapter);
                 } else {
                     adapter.notifyDataSetChanged();
                 }
             }
             @Override
             public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
        });
    }
}
