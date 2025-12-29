package com.nla.bookstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Button btnLogin, btnSignUp;
    private EditText txtEmail, txtPassword;
    private TextView btnForgotPassword;

    @Override
    protected void onStart() {
        super.onStart();
        // REQUIREMENT: Session Management (Auto-login)
        if (mAuth.getCurrentUser() != null) {
            goToDashboard();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtEmail = findViewById(R.id.etEmail);
        txtPassword = findViewById(R.id.etPassword);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // PART A-2: Login
        btnLogin.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            if (validateInputs(email, password)) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                goToDashboard();
                            } else {
                                Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // PART A-1: Signup
        btnSignUp.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            if (validateInputs(email, password)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "User Registered!", Toast.LENGTH_SHORT).show();
                                goToDashboard();
                            } else {
                                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // PART A-3: Forgot Password
        btnForgotPassword.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                showResetPopup();
            } else {
                sendResetEmail(email);
            }
        });
    }

    private void showResetPopup() {
        EditText resetInput = new EditText(this);
        resetInput.setHint("Enter your email");
        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setView(resetInput)
                .setPositiveButton("Send Link", (d, w) -> sendResetEmail(resetInput.getText().toString().trim()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendResetEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Reset email sent to " + email, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Invalid Email");
            return false;
        }
        if (password.length() < 6) {
            txtPassword.setError("Min 6 characters");
            return false;
        }
        return true;
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}