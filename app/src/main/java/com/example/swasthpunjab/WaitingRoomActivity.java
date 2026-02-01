package com.example.swasthpunjab;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WaitingRoomActivity extends AppCompatActivity {

    private String appointmentId;
    private String doctorName;
    private DatabaseReference mRef;
    private ValueEventListener mListener;
    private TextView tvStatus;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        appointmentId = getIntent().getStringExtra("APPOINTMENT_ID");
        doctorName = getIntent().getStringExtra("DOCTOR_NAME");

        TextView tvTitle = findViewById(R.id.tvWaitingTitle);
        tvStatus = findViewById(R.id.tvWaitingStatus);
        btnCancel = findViewById(R.id.btnCancelJoin);

        tvTitle.setText("Waiting for Dr. " + doctorName);
        tvStatus.setText("Requesting to join...");

        if (appointmentId != null) {
            mRef = FirebaseDatabase.getInstance().getReference("appointments").child(appointmentId);
            
            // 1. Send Request
            mRef.child("callStatus").setValue("requested");

            // 2. Listen for Response
            mListener = mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Appointment apt = snapshot.getValue(Appointment.class);
                    if (apt != null) {
                        String status = apt.getCallStatus();
                        
                        if ("active".equals(status)) {
                            // Doctor Accepted!
                            tvStatus.setText("Doctor Accepted! Joining...");
                            launchVideoCall(apt.getDoctorName());
                        } else if ("rejected".equals(status)) {
                            tvStatus.setText("Doctor declined the request.");
                            Toast.makeText(WaitingRoomActivity.this, "Request Declined", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            tvStatus.setText("Waiting for doctor to accept...");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }

        btnCancel.setOnClickListener(v -> {
            if (mRef != null) mRef.child("callStatus").setValue("pending");
            finish();
        });
    }

    private void launchVideoCall(String docName) {
        if (mRef != null && mListener != null) {
            mRef.removeEventListener(mListener);
        }
        
        String roomName = "SwasthPunjab_" + appointmentId;
        String videoUrl = "https://meet.jit.si/" + roomName; 

        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("VIDEO_URL", videoUrl);
        intent.putExtra("DISPLAY_NAME", "Patient"); // Ideally pass real patient name from intent
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If user exits without connecting, reset status? Maybe better not to disrupt flow logic if just rotating screen
        // But if finishing:
        if (isFinishing() && mRef != null && mListener != null) {
            mRef.removeEventListener(mListener);
        }
    }
}
