package com.example.cy310loginsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "login_db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_NAME = "users";
    private static final String EMAIL_COL = "email";
    private static final String PASSWORD_COL = "password";
    private static final String SALT_COL = "salt";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
            EMAIL_COL + " TEXT PRIMARY KEY, " +
            PASSWORD_COL + " TEXT, " +
            SALT_COL + " TEXT)";
        db.execSQL(createTableQuery);
    }

    public void addNewUser(String email, String password, String salt) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(EMAIL_COL, email);
        values.put(PASSWORD_COL, password);
        values.put(SALT_COL, salt);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public boolean userDoesNotExist(String email){
        // Query the database to see if that email is in the database
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the table name and the column you want to search in
        String tableName = "users";
        String columnName = "email";

        // Define the selection and selection arguments
        String selection = columnName + " = ?";
        String[] selectionArgs = {email};

        // Perform the query
        Cursor cursor = db.query(tableName, null, selection, selectionArgs, null, null, null);

        // Check if the cursor contains any results (email exists)
        if (cursor != null && cursor.getCount() > 0) {
            // Email exists in the database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            return false;
        } else {
            // Email doesn't exist in the database
            if (cursor != null) {
                cursor.close();
            }
            db.close();
            return true;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}