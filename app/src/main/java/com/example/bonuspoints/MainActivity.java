package com.example.bonuspoints;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bonuspoints.adapter.StudentAdapter;
import com.example.bonuspoints.database.DatabaseHelper;
import com.example.bonuspoints.model.Student;
import com.example.bonuspoints.ui.LoginActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private List<Student> studentList;
    private DatabaseHelper databaseHelper;
    private FloatingActionButton fabAddPoints;
    private TextView tvEmptyState;

    private String facultyName;
    private String facultySubject;

    // Registration number validation pattern
    private static final Pattern REG_NO_PATTERN = Pattern.compile("^\\d{2}[A-Z]{3}\\d{4}$");

    // Track existing students by registration number
    private Map<String, String> existingStudents = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initializeViews();
            getFacultyInfo();
            setupToolbar();
            databaseHelper = new DatabaseHelper(this);
            setupRecyclerView();
            loadStudents();
            setupFabClickListener();

        } catch (Exception e) {
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
        fabAddPoints = findViewById(R.id.fabAddPoints);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void getFacultyInfo() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        facultyName = sharedPreferences.getString("faculty_name", "Faculty");
        facultySubject = sharedPreferences.getString("faculty_subject", "Subject");

        if (facultySubject == null || facultySubject.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            logout();
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bonus Points - " + facultySubject);
            getSupportActionBar().setSubtitle("Faculty: " + facultyName);
        }
    }

    private void setupRecyclerView() {
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);
    }

    private void setupFabClickListener() {
        fabAddPoints.setOnClickListener(v -> showAddPointsDialog());
    }

    private void loadStudents() {
        try {
            studentList.clear();
            existingStudents.clear();

            List<Student> students = databaseHelper.getAllStudentsBySubject(facultySubject);
            studentList.addAll(students);

            // Build existing students map
            for (Student student : students) {
                existingStudents.put(student.getRegistrationNo(), student.getName());
            }

            studentAdapter.notifyDataSetChanged();

            // Show/hide empty state
            if (studentList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading students: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddPointsDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_points, null);
            EditText etRegistrationNo = dialogView.findViewById(R.id.etRegistrationNo);
            EditText etStudentName = dialogView.findViewById(R.id.etStudentName);
            EditText etPoints = dialogView.findViewById(R.id.etPoints);
            EditText etDescription = dialogView.findViewById(R.id.etDescription);

            // Set up registration number validation
            etRegistrationNo.setFilters(new InputFilter[]{
                    new InputFilter.LengthFilter(9),
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end,
                                                   Spanned dest, int dstart, int dend) {
                            String input = source.toString();
                            if (input.matches("[0-9A-Z]*")) {
                                return null;
                            }
                            return "";
                        }
                    }
            });

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Award Bonus Points")
                    .setView(dialogView)
                    .setPositiveButton("Award Points", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    try {
                        String regNo = etRegistrationNo.getText().toString().trim().toUpperCase();
                        String name = etStudentName.getText().toString().trim();
                        String pointsStr = etPoints.getText().toString().trim();
                        String descriptionInput = etDescription.getText().toString().trim();

                        // Description is now optional - make it final for lambda
                        final String description = descriptionInput.isEmpty() ? "No description provided" : descriptionInput;

                        // Validation
                        if (!validateInput(regNo, name, pointsStr)) {
                            return;
                        }

                        int points = Integer.parseInt(pointsStr);

                        // Check if registration number already exists
                        if (existingStudents.containsKey(regNo)) {
                            String existingName = existingStudents.get(regNo);
                            if (!existingName.equalsIgnoreCase(name)) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Registration Number Exists")
                                        .setMessage("Registration number " + regNo + " is already registered with name: " + existingName +
                                                "\n\nDo you want to use the existing name?")
                                        .setPositiveButton("Use Existing Name", (d, w) -> {
                                            awardPointsToStudent(regNo, existingName, points, description);
                                            dialog.dismiss();
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                                return;
                            }
                        }

                        // Award points and close dialog
                        awardPointsToStudent(regNo, name, points, description);
                        dialog.dismiss();

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open dialog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(String regNo, String name, String pointsStr) {
        if (regNo.isEmpty() || name.isEmpty() || pointsStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Description is now optional - validation removed

        // Validate registration number format
        if (!REG_NO_PATTERN.matcher(regNo).matches()) {
            Toast.makeText(this, "Invalid registration format!\nUse: YearSubjectCode4Digits\nExample: 24BCE1731", Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            int points = Integer.parseInt(pointsStr);
            if (points <= 0) {
                Toast.makeText(this, "Points must be greater than 0", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (points > 100) {
                Toast.makeText(this, "Points cannot exceed 100", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid points", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void awardPointsToStudent(String regNo, String name, int points, String description) {
        try {
            Student student = new Student(regNo, name, points, facultyName, facultySubject, description);
            long result = databaseHelper.addStudent(student);

            if (result > 0) {
                Toast.makeText(this, "Points awarded successfully!", Toast.LENGTH_SHORT).show();
                loadStudents();
            } else {
                Toast.makeText(this, "Failed to award points", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error awarding points: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStudentClick(Student student, int position) {
        showStudentOptionsDialog(student);
    }

    @Override
    public void onStudentLongClick(Student student, int position) {
        showStudentOptionsDialog(student);
    }

    private void showStudentOptionsDialog(Student student) {
        String message = String.format(
                "Registration No: %s\n" +
                        "Name: %s\n" +
                        "Points: %d\n" +
                        "Date: %s\n" +
                        "Faculty: %s\n" +
                        "Description: %s",
                student.getRegistrationNo(),
                student.getName(),
                student.getPoints(),
                student.getFormattedDate(),
                student.getFacultyName(),
                student.getDescription() != null && !student.getDescription().isEmpty() ? student.getDescription() : "No description"
        );

        new AlertDialog.Builder(this)
                .setTitle("Student Details")
                .setMessage(message)
                .setPositiveButton("Edit", (dialog, which) -> showEditDialog(student))
                .setNegativeButton("Delete", (dialog, which) -> confirmDelete(student))
                .setNeutralButton("Close", null)
                .show();
    }

    private void showEditDialog(Student student) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_points, null);
        EditText etRegistrationNo = dialogView.findViewById(R.id.etRegistrationNo);
        EditText etStudentName = dialogView.findViewById(R.id.etStudentName);
        EditText etPoints = dialogView.findViewById(R.id.etPoints);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Pre-fill with existing data
        etRegistrationNo.setText(student.getRegistrationNo());
        etRegistrationNo.setEnabled(false); // Don't allow changing reg no
        etStudentName.setText(student.getName());
        etStudentName.setEnabled(false); // Don't allow changing name
        etPoints.setText(String.valueOf(student.getPoints()));
        etDescription.setText(student.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Edit Bonus Points")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    try {
                        String pointsStr = etPoints.getText().toString().trim();

                        if (pointsStr.isEmpty()) {
                            Toast.makeText(this, "Please enter points", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int newPoints = Integer.parseInt(pointsStr);
                        if (newPoints <= 0 || newPoints > 100) {
                            Toast.makeText(this, "Points must be between 1-100", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int result = databaseHelper.updateStudentPoints(student.getId(), newPoints);
                        if (result > 0) {
                            Toast.makeText(this, "Points updated successfully!", Toast.LENGTH_SHORT).show();
                            loadStudents();
                        } else {
                            Toast.makeText(this, "Failed to update points", Toast.LENGTH_SHORT).show();
                        }

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter valid points", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDelete(Student student) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Record")
                .setMessage("Are you sure you want to delete this bonus point record for " + student.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteStudent(student.getId());
                    Toast.makeText(this, "Record deleted", Toast.LENGTH_SHORT).show();
                    loadStudents();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_view_totals) {
            showTotalPointsDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showTotalPointsDialog() {
        List<Student> totalsList = databaseHelper.getTotalPointsByStudent(facultySubject);

        if (totalsList.isEmpty()) {
            Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Total Points by Student:\n\n");

        int rank = 1;
        for (Student student : totalsList) {
            message.append(String.format("%d. %s (%s): %d points\n",
                    rank++,
                    student.getName(),
                    student.getRegistrationNo(),
                    student.getPoints()));
        }

        new AlertDialog.Builder(this)
                .setTitle("Total Points Leaderboard")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear();
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
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
