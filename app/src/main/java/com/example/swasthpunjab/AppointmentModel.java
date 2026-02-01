package com.example.swasthpunjab;

public class AppointmentModel {
    private String id;
    private String doctorName;
    private String date;
    private String time;
    private String status;

    public AppointmentModel(String id, String doctorName, String date, String time, String status) {
        this.id = id;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId() { return id; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
}
