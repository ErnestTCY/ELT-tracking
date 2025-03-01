package com.example.grpasg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BmiDatabaseHelper extends SQLiteOpenHelper {
    // Database Name and Version
    private static final String DATABASE_NAME = "BMISQL";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_BMI = "bmi";

    // Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_BMI_VALUE = "bmi_value";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // SQL to create the table
    private static final String CREATE_TABLE_BMI = "CREATE TABLE " + TABLE_BMI + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_BMI_VALUE + " REAL, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_TIMESTAMP + " TEXT);";

    public BmiDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BMI);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BMI);
        onCreate(db);
    }
    // Insert or Update BMI
    public void saveBMI(float bmiValue, String category, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete existing BMI record (if any)
        db.delete(TABLE_BMI, null, null);

        // Insert new BMI record
        ContentValues values = new ContentValues();
        values.put(COLUMN_BMI_VALUE, bmiValue);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_TIMESTAMP, timestamp);

        db.insert(TABLE_BMI, null, values);
        db.close();
    }

    // Retrieve BMI
    public Cursor getBMI() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BMI, null, null, null, null, null, null);
    }
}
