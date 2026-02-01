package com.example.swasthpunjab;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ConsultationFragment extends Fragment {

    ListView appointmentsListView;
    AppointmentsAdapter adapter;
    ArrayList<AppointmentModel> appointmentList = new ArrayList<>();
    MyDBHelper dbHelper;

    public ConsultationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_consultation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appointmentsListView = view.findViewById(R.id.appointmentsListFrag);
        dbHelper = new MyDBHelper(requireContext());

        loadAppointments();
    }

    private void loadAppointments() {
        // Clear list to avoid duplicates if reloaded
        appointmentList.clear();

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String email = prefs.getString("patient_email", "");

        if (email.isEmpty()) {
            // If no email, maybe show empty state
            return;
        }

        // Fetch from Firebase to ensure we have the ID for the waiting room logic
        com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("appointments");
        ref.orderByChild("email").equalTo(email).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                appointmentList.clear();
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    Appointment apt = ds.getValue(Appointment.class);
                    if (apt != null) {
                        appointmentList.add(new AppointmentModel(apt.getId(), apt.getDoctorName(), apt.getDate(), apt.getTime(), apt.getStatus()));
                    }
                }
                if (getContext() != null) {
                    adapter = new AppointmentsAdapter(requireContext(), appointmentList);
                    appointmentsListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload data when coming back to this fragment
        loadAppointments();
    }
}