package com.example.swasthpunjab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AppointmentsAdapter extends ArrayAdapter<AppointmentModel> {

    public AppointmentsAdapter(Context context, ArrayList<AppointmentModel> appointments) {
        super(context, 0, appointments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppointmentModel appointment = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_appointment, parent, false);
        }

        TextView tvDoctor = convertView.findViewById(R.id.tvAppDoctor);
        TextView tvDateTime = convertView.findViewById(R.id.tvAppDateTime);
        Button btnJoin = convertView.findViewById(R.id.btnJoinCall);

        tvDoctor.setText("Dr. " + appointment.getDoctorName());
        tvDateTime.setText(appointment.getDate() + " at " + appointment.getTime());

        // Always enable button so user gets feedback on click
        btnJoin.setEnabled(true);
        // Visual cue: Green if valid, Grey (but clickable) if invalid? 
        // Or just keep it Green/Active and check validation on click to avoid confusion.
        // Let's keep distinct colors for current status though.
        
        boolean isValidTime = isTimeForAppointment(appointment.getDate(), appointment.getTime());
        
        if (isValidTime) {
            btnJoin.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            btnJoin.setText("Join Call");
        } else {
            btnJoin.setBackgroundColor(Color.GRAY);
            btnJoin.setText("Join Call"); // Or "Locked"
        }

        btnJoin.setOnClickListener(v -> {
            boolean validNow = isTimeForAppointment(appointment.getDate(), appointment.getTime());
            if (validNow) {
                Intent intent = new Intent(getContext(), WaitingRoomActivity.class);
                intent.putExtra("APPOINTMENT_ID", appointment.getId()); 
                intent.putExtra("DOCTOR_NAME", appointment.getDoctorName());
                getContext().startActivity(intent);
            } else {
                 // Debug Feedback Logic
                 try {
                     SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
                     Date appTime = sdf.parse(appointment.getDate() + " " + appointment.getTime());
                     long diff = new Date().getTime() - appTime.getTime();
                     long mins = diff / (60 * 1000);
                     
                     if (diff < 0) {
                        Toast.makeText(getContext(), "Too Early (" + Math.abs(mins) + " mins left)", Toast.LENGTH_SHORT).show();
                     } else {
                        Toast.makeText(getContext(), "Expired (" + mins + " mins ago)", Toast.LENGTH_SHORT).show();
                     }
                 } catch (Exception e) {
                     Toast.makeText(getContext(), "Time Format Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                 }
            }
        });

        return convertView;
    }

    private boolean isTimeForAppointment(String dateStr, String timeStr) {
        // Format of dateStr: "d/M/yyyy"
        // Format of timeStr: "H:mm"
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy H:mm", Locale.getDefault());
            Date now = new Date();
            long currentTime = now.getTime();
            long fifteenMinutes = 15 * 60 * 1000; // 15 minutes window

            // 1. Check exact parsed time
            Date appTime = sdf.parse(dateStr + " " + timeStr);
            if (checkWindow(currentTime, appTime.getTime(), fifteenMinutes)) {
                return true;
            }

            // 2. Fallback: Handle 12-hour confusion (e.g. user picked "1:45" (AM) but meant "1:45 PM")
            // This happens commonly with the 24h picker if users click '1' instead of '13'
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            
            if (hour < 12) {
                long pmTimeMillis = appTime.getTime() + (12 * 60 * 60 * 1000); // Add 12 hours
                if (checkWindow(currentTime, pmTimeMillis, fifteenMinutes)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkWindow(long current, long start, long duration) {
        // Strict Window: Active only for 15 minutes starting from Appointment Time
        // FROM: Appointment Time
        // TO: Appointment Time + 15 minutes
        return current >= start && current <= (start + duration);
    }
}
