package com.example.cy310loginsystem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.mindrot.jbcrypt.BCrypt;
import android.util.Patterns;

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
        DBHandler dbhandler = new DBHandler(SignUp.this);

        // Store the information in the database
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateData()) {
                    String email = String.valueOf(editTextEmailAddress.getText());
                    String password = String.valueOf(editTextPassword.getText());

                    // Hash the password
                    String salt = BCrypt.gensalt();
                    String hashedPassword = BCrypt.hashpw(password, salt);

                    // Store the email, hashed password, and salt in the database
                    if (dbhandler.userDoesNotExist(email)){
                        dbhandler.addNewUser(email, hashedPassword, salt);
                        // Return to login screen
                        Intent intent = new Intent(SignUp.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        errorTxt.setText("Account with that email already exists");
                        errorTxt.setVisibility(View.VISIBLE);
                    }
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

        // If email is not an email, throw error text
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            errorTxt.setText("Not a valid email");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // If password doesn't contain an uppercase letter or number, or does contain a space, throw error text
        boolean containsUpper = false, containsNumber = false, containsSpace = false;
        for (int i = 0; i < password.length(); i++){
            char ch = password.charAt(i);
            if (Character.isUpperCase(ch)){
                containsUpper = true;
            }
            if (Character.isDigit(ch)){
                containsNumber = true;
            }
            if (Character.isSpaceChar(ch)){
                containsSpace = true;
            }
        }
        if (containsUpper == false){
            errorTxt.setText("Password must contain at least one uppercase letter");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }
        if (containsNumber == false){
            errorTxt.setText("Password must contain at least one number");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }
        if (containsSpace == true){
            errorTxt.setText("Password cannot contain any spaces");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        // If password shorter than 15 characters, throw error text
        if (password.length() < 15){
            errorTxt.setText("Password must be at least 15 characters");
            errorTxt.setVisibility(View.VISIBLE);
            return false;
        }

        return true;
    }
}