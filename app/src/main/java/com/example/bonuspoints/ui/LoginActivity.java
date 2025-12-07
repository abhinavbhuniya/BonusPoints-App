package com.example.bonuspoints.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bonuspoints.AdminSetupActivity;
import com.example.bonuspoints.MainActivity;
import com.example.bonuspoints.R;
import com.example.bonuspoints.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        // Check if admin setup is needed
        if (!databaseHelper.isAdminSetupComplete()) {
            Intent intent = new Intent(this, AdminSetupActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        try {
            etUsername = findViewById(R.id.etUsername);
            etPassword = findViewById(R.id.etPassword);
            btnLogin = findViewById(R.id.btnLogin);
            progressBar = findViewById(R.id.progressBar);

            // Fix forgot password click
            TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
            if (tvForgotPassword != null) {
                tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
            }

            btnLogin.setOnClickListener(v -> attemptLogin());

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing login", Toast.LENGTH_LONG).show();
        }
    }

    // Add this method to LoginActivity
    private void showForgotPasswordDialog() {
        StringBuilder message = new StringBuilder();
        message.append("Demo Credentials:\n\n");
        message.append("Admin Setup Password: admin123\n\n");
        message.append("After admin setup, you can login with the faculty accounts you created.\n\n");
        message.append("Note: In production, contact your system administrator for password reset.");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Demo Credentials")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }


    private void attemptLogin() {
        try {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty()) {
                Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }
            btnLogin.setEnabled(false);

            // Authenticate using database
            String facultySubject = databaseHelper.authenticateFaculty(username, password);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            btnLogin.setEnabled(true);

            if (facultySubject != null) {
                saveLoginSession(username, facultySubject);
                Toast.makeText(this, "Welcome! Logged in to " + facultySubject, Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                etPassword.setText("");
            }

        } catch (Exception e) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            btnLogin.setEnabled(true);
            Toast.makeText(this, "Login error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoginSession(String username, String facultySubject) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("is_logged_in", true);
        editor.putString("faculty_name", username);
        editor.putString("faculty_subject", facultySubject);
        editor.putLong("login_time", System.currentTimeMillis());

        editor.apply();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
