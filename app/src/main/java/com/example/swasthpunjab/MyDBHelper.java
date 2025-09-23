package com.example.swasthpunjab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SwasthPunjab.db";
    public static final int DATABASE_VERSION = 1;

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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTORS);
        onCreate(db);
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