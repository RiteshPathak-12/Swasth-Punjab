package com.example.swasthpunjab;

public class Appointment {
    private String id;
    private String patientName;
    private String doctorName;
    private String date;
    private String time;
    private String email;
    private String purpose;
    private String status; // Booked, Cancelled, etc.
    private String callStatus; // pending, requested, active, ended
    private long timestamp;

    public Appointment() {
        // Required for Firebase
    }

    public Appointment(String id, String patientName, String doctorName, String date, String time, String email, String purpose, String status, long timestamp) {
        this.id = id;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.email = email;
        this.purpose = purpose;
        this.status = status;
        this.timestamp = timestamp;
        this.callStatus = "pending";
    }

    public String getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getEmail() { return email; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public String getCallStatus() { return callStatus != null ? callStatus : "pending"; }
    public long getTimestamp() { return timestamp; }
}
