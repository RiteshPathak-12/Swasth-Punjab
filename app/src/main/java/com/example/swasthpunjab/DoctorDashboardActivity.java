package com.example.swasthpunjab;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;

public class DoctorDashboardActivity extends AppCompatActivity {

    private RecyclerView rvLiveRequests, rvAppointments, rvPastAppointments;
    private TextView tvNoRequests, tvLiveStatus, tvDoctorTitle;
    private SwitchCompat switchStatus;
    
    // Data Lists
    private ArrayList<Appointment> liveRequestList = new ArrayList<>();
    private ArrayList<Appointment> scheduleList = new ArrayList<>();
    private ArrayList<Appointment> pastList = new ArrayList<>();
    
    // Adapters
    private LiveRequestAdapter requestAdapter;
    private ScheduleAdapter scheduleAdapter;
    private ScheduleAdapter pastAdapter;
    
    private DatabaseReference ref;
    private String doctorNameLog = "Dr. Sanchit"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Bind Views
        rvLiveRequests = findViewById(R.id.rvLiveRequests);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvPastAppointments = findViewById(R.id.rvPastAppointments); // New RecyclerView
        tvNoRequests = findViewById(R.id.tvNoRequests);
        tvLiveStatus = findViewById(R.id.tvLiveStatus);
        switchStatus = findViewById(R.id.switchStatus);
        tvDoctorTitle = findViewById(R.id.tvDoctorTitle);

        // Setup Toolbar/Header
        if (getIntent().hasExtra("DOCTOR_NAME")) {
            doctorNameLog = getIntent().getStringExtra("DOCTOR_NAME");
        }
        tvDoctorTitle.setText("Dashboard: " + doctorNameLog);

        // Setup RecyclerViews
        rvLiveRequests.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvPastAppointments.setLayoutManager(new LinearLayoutManager(this));

        requestAdapter = new LiveRequestAdapter(liveRequestList);
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        pastAdapter = new ScheduleAdapter(pastList); // Reuse same adapter design

        rvLiveRequests.setAdapter(requestAdapter);
        rvAppointments.setAdapter(scheduleAdapter);
        rvPastAppointments.setAdapter(pastAdapter);

        // Firebase
        ref = FirebaseDatabase.getInstance().getReference("appointments");

        setupStatusSwitch();
        listenForRequests();
    }

    private void setupStatusSwitch() {
        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                tvLiveStatus.setText("Online");
                tvLiveStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                Toast.makeText(this, "You are now ONLINE", Toast.LENGTH_SHORT).show();
            } else {
                tvLiveStatus.setText("Offline");
                tvLiveStatus.setTextColor(Color.GRAY);
                Toast.makeText(this, "You are now OFFLINE", Toast.LENGTH_SHORT).show();
            }
        });
    }



// ...

    private void listenForRequests() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                liveRequestList.clear();
                scheduleList.clear();
                pastList.clear();

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                    SimpleDateFormat fullFormat = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
                    Date today = getZeroTimeDate(new Date());
                    long currentTimeMillis = System.currentTimeMillis();
                    long fifteenMinutesMillis = 15 * 60 * 1000;

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Appointment apt = ds.getValue(Appointment.class);
                        if (apt != null) {
                            if (doctorNameLog.equals(apt.getDoctorName()) || true) { 
                                String status = apt.getCallStatus();
                                String aptDateStr = apt.getDate();
                                String aptTimeStr = apt.getTime();
                                
                                boolean isLive = false;
                                
                                if ("requested".equals(status) || "active".equals(status)) {
                                    // CHECK EXPIRATION:
                                    // If current time > (Appointment Time + 15 mins), treat as expired/schedule
                                    try {
                                        Date aptDateTime = fullFormat.parse(aptDateStr + " " + aptTimeStr);
                                        if (aptDateTime != null) {
                                            long expirationTime = aptDateTime.getTime() + fifteenMinutesMillis;
                                            
                                            // If NOT expired yet, stay in waiting room
                                            if (currentTimeMillis <= expirationTime) {
                                                liveRequestList.add(apt);
                                                isLive = true;
                                            }
                                        }
                                    } catch (Exception ex) {
                                        // Fallback if parsing failed: keep in live if status matches
                                        liveRequestList.add(apt);
                                        isLive = true;
                                    }
                                }

                                if (!isLive) {
                                    // Add to Schedule or Past based on Date
                                    Date aptDate = dateFormat.parse(aptDateStr);
                                    if (aptDate != null) {
                                        if (aptDate.compareTo(today) == 0) {
                                            scheduleList.add(apt);
                                        } else if (aptDate.before(today)) {
                                            pastList.add(apt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                requestAdapter.notifyDataSetChanged();
                scheduleAdapter.notifyDataSetChanged();
                pastAdapter.notifyDataSetChanged();
                
                if (liveRequestList.isEmpty()) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                    rvLiveRequests.setVisibility(View.GONE);
                } else {
                    tvNoRequests.setVisibility(View.GONE);
                    rvLiveRequests.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private Date getZeroTimeDate(Date date) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void joinCall(String id) {
        String videoUrl = "https://meet.jit.si/SwasthPunjab_" + id;
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("VIDEO_URL", videoUrl);
        intent.putExtra("DISPLAY_NAME", doctorNameLog);
        startActivity(intent);
        
        Toast.makeText(this, "Opening Secure Line...", Toast.LENGTH_LONG).show();
    }

    // --- Inner Adapter Classes for Simplicity ---

    // 1. Live Request Adapter (Cards)
    class LiveRequestAdapter extends RecyclerView.Adapter<LiveRequestAdapter.ReqViewHolder> {
        ArrayList<Appointment> list;
        public LiveRequestAdapter(ArrayList<Appointment> list) { this.list = list; }

        @NonNull @Override
        public ReqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_request, parent, false);
            return new ReqViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ReqViewHolder holder, int position) {
            Appointment apt = list.get(position);
            holder.name.setText(apt.getPatientName());
            holder.time.setText("Time: " + apt.getTime());
            holder.purpose.setText("Reason: " + apt.getPurpose());
            
            holder.btnAccept.setOnClickListener(v -> {
                // 1. Admit Patient
                ref.child(apt.getId()).child("callStatus").setValue("active");
                // 2. Join Not Immediately? Or join now.
                joinCall(apt.getId());
            });

            holder.btnReject.setOnClickListener(v -> {
                 ref.child(apt.getId()).child("callStatus").setValue("rejected");
                 Toast.makeText(DoctorDashboardActivity.this, "Request Rejected", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ReqViewHolder extends RecyclerView.ViewHolder {
            TextView name, time, purpose;
            Button btnAccept, btnReject;
            public ReqViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tvRequestPatientName);
                time = itemView.findViewById(R.id.tvRequestTime);
                purpose = itemView.findViewById(R.id.tvRequestPurpose);
                btnAccept = itemView.findViewById(R.id.btnAcceptRequest);
                btnReject = itemView.findViewById(R.id.btnRejectRequest);
            }
        }
    }

    // 2. Schedule Adapter (Simple List)
    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.SlotViewHolder> {
        ArrayList<Appointment> list;
        public ScheduleAdapter(ArrayList<Appointment> list) { this.list = list; }

        @NonNull @Override
        public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment_slot, parent, false);
            return new SlotViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
            Appointment apt = list.get(position);
            holder.time.setText(apt.getTime());
            holder.name.setText(apt.getPatientName());
            holder.status.setText(apt.getDate() + " • " + apt.getStatus());

            holder.btnDetails.setOnClickListener(v -> {
                // Show Quick Details
                new AlertDialog.Builder(DoctorDashboardActivity.this)
                    .setTitle("Patient Details")
                    .setMessage("Name: " + apt.getPatientName() + "\nSymptoms: " + apt.getPurpose() + "\nEmail: " + apt.getEmail())
                    .setNeutralButton("Prescribe", (dialog, which) -> {
                         Intent intent = new Intent(DoctorDashboardActivity.this, PrescriptionActivity.class);
                         intent.putExtra("APPOINTMENT_ID", apt.getId());
                         intent.putExtra("PATIENT_NAME", apt.getPatientName());
                         intent.putExtra("PATIENT_EMAIL", apt.getEmail()); // Added Email
                         startActivity(intent);
                    })
                    .setPositiveButton("Close", null)
                    .show();
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class SlotViewHolder extends RecyclerView.ViewHolder {
            TextView time, name, status;
            View btnDetails;
            public SlotViewHolder(@NonNull View itemView) {
                super(itemView);
                time = itemView.findViewById(R.id.tvSlotTime);
                name = itemView.findViewById(R.id.tvSlotPatient);
                status = itemView.findViewById(R.id.tvSlotStatus);
                btnDetails = itemView.findViewById(R.id.btnViewDetails);
            }
        }
    }
}
