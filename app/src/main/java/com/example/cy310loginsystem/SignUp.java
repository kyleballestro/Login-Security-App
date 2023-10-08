package com.example.cy310loginsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SignUp extends AppCompatActivity {

    private Button signUpBtn, cancelBtn;
    private EditText editTextEmailAddress, editTextPassword, editTextConfirmPassword;
    private TextView errorTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpBtn = findViewById(R.id.signUpBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        editTextEmailAddress = findViewById(R.id.editTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        errorTxt = findViewById(R.id.errorTxt);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateData()) {
                    // Store the information in the database
                    String email = String.valueOf(editTextEmailAddress.getText());
                    String password = String.valueOf(editTextPassword.getText());
                    byte[] hashedPassword = null;

                    // Hash the password
                    SecureRandom random = new SecureRandom();
                    byte[] salt = new byte[16];
                    random.nextBytes(salt);
                    try{
                        MessageDigest md = MessageDigest.getInstance("SHA-512");
                        md.update(salt);
                        hashedPassword = md.digest(password.getBytes());
                        md.reset();
                    }
                    catch (NoSuchAlgorithmException ex){
                        System.out.println(ex);
                    }
                    String hashedPasswordString = new String(hashedPassword, StandardCharsets.UTF_8);
                    String saltString = new String(salt, StandardCharsets.UTF_8);

                    // Store the email, hashed password, and salt in the database


                    // Return to login screen
                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Return to login screen
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean validateData(){
        // Get information from all fields
        String email = String.valueOf(editTextEmailAddress.getText());
        String password = String.valueOf(editTextPassword.getText());
        String confirmPassword = String.valueOf(editTextConfirmPassword.getText());

        // If any are empty, throw error text
        if (email.equals("") || password.equals("") || confirmPassword.equals("")) {
            errorTxt.setText("Please fill out all fields");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }
        // If passwords do not match, throw error text
        if (!password.equals(confirmPassword)) {
            errorTxt.setText("Passwords do not match");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }
}