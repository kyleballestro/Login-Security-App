package com.example.cy310loginsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import org.mindrot.jbcrypt.BCrypt;
import android.util.Patterns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView signUpTxt, errorTxt;
    private EditText editTextEmailAddress, editTextPassword;
    private Button loginBtn;
    private int timeoutCounter = 0; // Number of failed login attempts
    private long timeUntilLogin; // Time the user can login again

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpTxt = findViewById(R.id.signUpTxt);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.loginBtn);
        errorTxt = findViewById(R.id.errorTxt);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        timeUntilLogin = sharedPreferences.getLong("timeUntilLogin", 0);
        timeoutCounter = sharedPreferences.getInt("timeoutCounter", 0);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check that the current time has passed the timeout time
                if (System.currentTimeMillis() >= timeUntilLogin){
                    // If data is valid, grant entry
                    if (validateData()){
                        timeoutCounter = 0;
                        Intent intent = new Intent(MainActivity.this, Entry.class);
                        startActivity(intent);
                    }
                    // If data isn't valid, update timeoutCounter (counts failed attempts)
                    else{
                        timeoutCounter++;
                        // If there has been 5 failed attempts, time the user out for 5 minutes
                        if (timeoutCounter >= 5){
                            // Get current time, add 5 minutes to it. Set timeUntilLogin to that new time.
                            long currentTimeMillis = System.currentTimeMillis();
                            timeUntilLogin = currentTimeMillis + TimeUnit.MINUTES.toMillis(5);

                            // Display error text (for the first time when they've reached 5 attempts)
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            Date nextAllowedLoginTime = new Date(timeUntilLogin);
                            String formattedTime = timeFormat.format(nextAllowedLoginTime);
                            errorTxt.setText("Maximum attempts reached. Try again at " + formattedTime + ".");
                            errorTxt.setVisibility(View.VISIBLE);

                            // Reset timeoutCounter variable
                            timeoutCounter = 0;
                        }
                    }
                }
                else{
                    // Display error text (for anytime they press login while they're timed out)
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    Date nextAllowedLoginTime = new Date(timeUntilLogin);
                    String formattedTime = timeFormat.format(nextAllowedLoginTime);
                    errorTxt.setText("Maximum attempts reached. Try again at " + formattedTime + ".");
                    errorTxt.setVisibility(View.VISIBLE);
                }
            }
        });

        // Takes the user to the sign up screen
        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    // Validates the email and password given by the user
    public boolean validateData(){
        String passFromDB = null;
        String saltFromDB = null;

        // Get information from all fields
        String email = String.valueOf(editTextEmailAddress.getText());
        String password = String.valueOf(editTextPassword.getText());

        // If any are empty, throw error text
        if (email.equals("") || password.equals("")) {
            errorTxt.setText("Please fill out all fields");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // If there isn't a user with that email, throw error text
        DBHandler dbhandler = new DBHandler(MainActivity.this);
        SQLiteDatabase database = dbhandler.getReadableDatabase();
        if (dbhandler.userDoesNotExist(email)) {
            errorTxt.setText("No account with that email");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // Perform the query to retrieve the password
        database = dbhandler.getReadableDatabase();
        String[] passCol = {"password"};
        String[] emailArgs = {email};
        Cursor passwordCursor = database.query("users", passCol,
                "email = ?", emailArgs, null, null, null);
        int columnIndex = passwordCursor.getColumnIndex("password");
        if (passwordCursor.moveToFirst()) {
            passFromDB = passwordCursor.getString(columnIndex);
        } else {
            System.out.println("Column password was not found in the cursor");
        }

        // If the entered password doesn't match the password in database, throw error text
        if (!BCrypt.checkpw(password, passFromDB)){
            errorTxt.setText("Incorrect password");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        // Don't go anywhere. This ensures that reentry into the app isn't allowed without login.
    }

    // This saves the timeUntilLogin and timeoutCounter whenever the user pauses the activity
    // (goes to a new screen, closes the app, turns off phone, etc)
    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("timeUntilLogin", timeUntilLogin);
        editor.putInt("timeoutCounter", timeoutCounter);

        editor.apply();
    }
}