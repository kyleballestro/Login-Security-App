package com.example.cy310loginsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import org.mindrot.jbcrypt.BCrypt;
import android.util.Patterns;

public class MainActivity extends AppCompatActivity {

    private TextView signUpTxt, errorTxt;
    private EditText editTextEmailAddress, editTextPassword;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signUpTxt = findViewById(R.id.signUpTxt);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.loginBtn);
        errorTxt = findViewById(R.id.errorTxt);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check that all fields are entered
                if (validateData()){
                    Intent intent = new Intent(MainActivity.this, Entry.class);
                    startActivity(intent);
                }
            }
        });

        signUpTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

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
}