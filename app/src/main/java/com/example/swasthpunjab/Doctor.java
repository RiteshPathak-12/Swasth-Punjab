package com.example.swasthpunjab;

public class Doctor {
    private String name;
    private String specialty;
    private String license;
    private String experience;
    private String email;
    private String password;

    // Required empty constructor for Firebase
    public Doctor() {
    }

    public Doctor(String name, String specialty, String license, String experience, String email, String password) {
        this.name = name;
        this.specialty = specialty;
        this.license = license;
        this.experience = experience;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getLicense() { return license; }
    public String getExperience() { return experience; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
