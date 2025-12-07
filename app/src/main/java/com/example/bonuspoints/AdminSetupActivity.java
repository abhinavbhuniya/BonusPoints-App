package com.example.bonuspoints;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bonuspoints.database.DatabaseHelper;
import com.example.bonuspoints.ui.LoginActivity;

/**
 * Simple admin setup to create faculty accounts
 * No fancy libraries - just basic UI and database operations
 */
public class AdminSetupActivity extends AppCompatActivity {

    private EditText etAdminPassword, etFacultyUsername, etFacultyPassword, etSubject;
    private Button btnVerifyAdmin, btnAddFaculty, btnFinishSetup;
    private LinearLayout layoutFacultySetup, layoutAddFaculty;
    private TextView tvInstructions, tvFacultyCount;
    private ScrollView scrollView;

    private DatabaseHelper databaseHelper;
    private boolean isAdminVerified = false;
    private int facultyCount = 0;

    // Simple admin password (you can change this)
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create simple layout programmatically (no XML needed)
        createLayout();

        databaseHelper = new DatabaseHelper(this);
        updateFacultyCount();
    }

    private void createLayout() {
        // Create main scroll view
        scrollView = new ScrollView(this);
        scrollView.setPadding(32, 32, 32, 32);

        // Main vertical layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Title
        TextView title = new TextView(this);
        title.setText("Admin Setup - Create Faculty Accounts");
        title.setTextSize(20);
        title.setTextColor(0xFF2196F3);
        title.setPadding(0, 0, 0, 24);
        mainLayout.addView(title);

        // Instructions
        tvInstructions = new TextView(this);
        tvInstructions.setText("Enter admin password to begin faculty setup:");
        tvInstructions.setPadding(0, 0, 0, 16);
        mainLayout.addView(tvInstructions);

        // Admin password section
        etAdminPassword = new EditText(this);
        etAdminPassword.setHint("Admin Password");
        etAdminPassword.setInputType(0x00000081); // textPassword
        mainLayout.addView(etAdminPassword);

        btnVerifyAdmin = new Button(this);
        btnVerifyAdmin.setText("Verify Admin");
        btnVerifyAdmin.setOnClickListener(v -> verifyAdmin());
        mainLayout.addView(btnVerifyAdmin);

        // Faculty setup section (hidden initially)
        layoutFacultySetup = new LinearLayout(this);
        layoutFacultySetup.setOrientation(LinearLayout.VERTICAL);
        layoutFacultySetup.setVisibility(View.GONE);

        TextView facultyTitle = new TextView(this);
        facultyTitle.setText("Add Faculty Members:");
        facultyTitle.setTextSize(16);
        facultyTitle.setPadding(0, 32, 0, 16);
        layoutFacultySetup.addView(facultyTitle);

        tvFacultyCount = new TextView(this);
        tvFacultyCount.setText("Faculty added: 0");
        tvFacultyCount.setPadding(0, 0, 0, 16);
        layoutFacultySetup.addView(tvFacultyCount);

        // Add faculty form
        layoutAddFaculty = new LinearLayout(this);
        layoutAddFaculty.setOrientation(LinearLayout.VERTICAL);

        etFacultyUsername = new EditText(this);
        etFacultyUsername.setHint("Faculty Username (e.g., faculty1)");
        layoutAddFaculty.addView(etFacultyUsername);

        etFacultyPassword = new EditText(this);
        etFacultyPassword.setHint("Faculty Password");
        etFacultyPassword.setInputType(0x00000081); // textPassword
        layoutAddFaculty.addView(etFacultyPassword);

        etSubject = new EditText(this);
        etSubject.setHint("Subject (e.g., Java Programming)");
        layoutAddFaculty.addView(etSubject);

        btnAddFaculty = new Button(this);
        btnAddFaculty.setText("Add Faculty");
        btnAddFaculty.setOnClickListener(v -> addFaculty());
        layoutAddFaculty.addView(btnAddFaculty);

        layoutFacultySetup.addView(layoutAddFaculty);

        // Finish setup button
        btnFinishSetup = new Button(this);
        btnFinishSetup.setText("Finish Setup");
        btnFinishSetup.setOnClickListener(v -> finishSetup());
        btnFinishSetup.setVisibility(View.GONE);
        layoutFacultySetup.addView(btnFinishSetup);

        mainLayout.addView(layoutFacultySetup);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void verifyAdmin() {
        String password = etAdminPassword.getText().toString().trim();

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter admin password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.equals(ADMIN_PASSWORD)) {
            isAdminVerified = true;
            tvInstructions.setText("Admin verified! Add faculty members:");
            layoutFacultySetup.setVisibility(View.VISIBLE);
            btnVerifyAdmin.setVisibility(View.GONE);
            etAdminPassword.setVisibility(View.GONE);
            Toast.makeText(this, "Admin access granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Invalid admin password", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFaculty() {
        if (!isAdminVerified) {
            Toast.makeText(this, "Please verify admin first", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etFacultyUsername.getText().toString().trim();
        String password = etFacultyPassword.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || subject.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = databaseHelper.addFaculty(username, password, subject);

        if (result > 0) {
            facultyCount++;
            updateFacultyCount();
            clearFacultyForm();
            Toast.makeText(this, "Faculty added successfully!", Toast.LENGTH_SHORT).show();

            if (facultyCount >= 1) {
                btnFinishSetup.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Failed to add faculty (username might already exist)", Toast.LENGTH_LONG).show();
        }
    }

    private void updateFacultyCount() {
        facultyCount = databaseHelper.getFacultyCount();
        if (tvFacultyCount != null) {
            tvFacultyCount.setText("Faculty added: " + facultyCount);
        }
    }

    private void clearFacultyForm() {
        etFacultyUsername.setText("");
        etFacultyPassword.setText("");
        etSubject.setText("");
    }

    private void finishSetup() {
        if (facultyCount == 0) {
            Toast.makeText(this, "Please add at least one faculty member", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Complete Setup")
                .setMessage("Setup complete! " + facultyCount + " faculty members added.\n\nThe app is now ready to use.")
                .setPositiveButton("Continue to Login", (dialog, which) -> {
                    databaseHelper.completeAdminSetup();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
