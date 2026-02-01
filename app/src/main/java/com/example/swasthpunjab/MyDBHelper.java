package com.example.swasthpunjab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SwasthPunjab.db";
    public static final int DATABASE_VERSION = 6;

    public static final String TABLE_DOCTORS = "Doctors";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SPECIALTY = "specialty";
    public static final String COLUMN_LICENSE = "license";

    public static final String TABLE_HISTORY = "PatientHistory";
    public static final String COLUMN_PATIENT_NAME = "patient_name";
    public static final String COLUMN_MEDICINE = "medicine";
    public static final String COLUMN_PROBLEM = "problem";
    public static final String COLUMN_DATE = "date";

    // New Appointments Table
    public static final String TABLE_APPOINTMENTS = "Appointments";
    public static final String COL_APP_ID = "id";
    public static final String COL_APP_PATIENT = "patient_name";
    public static final String COL_APP_DOCTOR = "doctor_name";
    public static final String COL_APP_DATE = "date";
    public static final String COL_APP_TIME = "time";
    public static final String COL_APP_EMAIL = "email";
    public static final String COL_APP_STATUS = "status";

    private static final String CREATE_TABLE_DOCTORS =
            "CREATE TABLE " + TABLE_DOCTORS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_SPECIALTY + " TEXT, " +
                    COLUMN_LICENSE + " TEXT);";

    private static final String CREATE_TABLE_HISTORY =
            "CREATE TABLE " + TABLE_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PATIENT_NAME + " TEXT, " +
                    COLUMN_MEDICINE + " TEXT, " +
                    COLUMN_PROBLEM + " TEXT, " +
                    COLUMN_DATE + " TEXT);";

    private static final String CREATE_TABLE_APPOINTMENTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_APPOINTMENTS + " (" +
                    COL_APP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_APP_PATIENT + " TEXT, " +
                    COL_APP_DOCTOR + " TEXT, " +
                    COL_APP_DATE + " TEXT, " +
                    COL_APP_TIME + " TEXT, " +
                    COL_APP_EMAIL + " TEXT, " +
                    COL_APP_STATUS + " TEXT);";

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public MyDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DOCTORS);
        db.execSQL(CREATE_TABLE_HISTORY);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            db.execSQL(CREATE_TABLE_APPOINTMENTS);
        }
    }

    public void insertAppointment(String patientName, String doctorName, String date, String time, String email, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_APP_PATIENT, patientName);
        values.put(COL_APP_DOCTOR, doctorName);
        values.put(COL_APP_DATE, date);
        values.put(COL_APP_TIME, time);
        values.put(COL_APP_EMAIL, email);
        values.put(COL_APP_STATUS, status);
        db.insert(TABLE_APPOINTMENTS, null, values);
        db.close();
    }

    public Cursor getAppointments(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_APPOINTMENTS, null,
                COL_APP_EMAIL + "=?",
                new String[]{email},
                null, null,
                COL_APP_DATE + " DESC, " + COL_APP_TIME + " DESC");
    }

    public int getAppointmentCount(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS + " WHERE " + COL_APP_EMAIL + "=?", new String[]{email});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public void insertPatientHistory(String patientName, String medicine, String problem, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_NAME, patientName);
        values.put(COLUMN_MEDICINE, medicine);
        values.put(COLUMN_PROBLEM, problem);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public Cursor getPatientHistory(String patientName) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_HISTORY, null,
                COLUMN_PATIENT_NAME + "=?",
                new String[]{patientName},
                null, null,
                COLUMN_DATE + " DESC");
    }
}