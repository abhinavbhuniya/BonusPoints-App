package com.example.bonuspoints.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Student {

    private int id;
    private String registrationNo;
    private String name;
    private int points;
    private String dateAwarded;
    private String facultyName;
    private String subject;
    private String description;

    // Empty constructor
    public Student() {
    }

    // Constructor with all fields including description
    public Student(String registrationNo, String name, int points, String facultyName, String subject, String description) {
        this.registrationNo = registrationNo;
        this.name = name;
        this.points = points;
        this.facultyName = facultyName;
        this.subject = subject;
        this.description = description;
        this.dateAwarded = getCurrentDateTime();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getDateAwarded() {
        return dateAwarded;
    }

    public void setDateAwarded(String dateAwarded) {
        this.dateAwarded = dateAwarded;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Helper method to get formatted date
    public String getFormattedDate() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateAwarded);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateAwarded;
        }
    }

    // Get current date-time
    private String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
