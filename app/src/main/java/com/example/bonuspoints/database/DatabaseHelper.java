package com.example.bonuspoints.database;
import com.example.bonuspoints.model.Student;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bonus_points.db";
    private static final int DATABASE_VERSION = 3; // INCREASED VERSION

    // Table names
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_FACULTY = "faculty";
    private static final String TABLE_ADMIN = "admin_setup";

    // Student table columns
    private static final String KEY_ID = "id";
    private static final String KEY_REGISTRATION_NO = "registration_no";
    private static final String KEY_NAME = "name";
    private static final String KEY_POINTS = "points";
    private static final String KEY_DATE_AWARDED = "date_awarded";
    private static final String KEY_FACULTY_NAME = "faculty_name";
    private static final String KEY_SUBJECT = "subject";
    private static final String KEY_DESCRIPTION = "description"; // NEW COLUMN

    // Faculty table columns
    private static final String KEY_FACULTY_ID = "faculty_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD_HASH = "password_hash";
    private static final String KEY_FACULTY_SUBJECT = "subject";

    // Admin setup table
    private static final String KEY_ADMIN_ID = "admin_id";
    private static final String KEY_IS_SETUP = "is_setup";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Students table WITH description
        String createStudentsTable = "CREATE TABLE " + TABLE_STUDENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_REGISTRATION_NO + " TEXT NOT NULL,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_POINTS + " INTEGER DEFAULT 0,"
                + KEY_DATE_AWARDED + " TEXT NOT NULL,"
                + KEY_FACULTY_NAME + " TEXT NOT NULL,"
                + KEY_SUBJECT + " TEXT NOT NULL,"
                + KEY_DESCRIPTION + " TEXT" + ")";

        // Create Faculty table
        String createFacultyTable = "CREATE TABLE " + TABLE_FACULTY + "("
                + KEY_FACULTY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT UNIQUE NOT NULL,"
                + KEY_PASSWORD_HASH + " TEXT NOT NULL,"
                + KEY_FACULTY_SUBJECT + " TEXT NOT NULL" + ")";

        // Create Admin setup table
        String createAdminTable = "CREATE TABLE " + TABLE_ADMIN + "("
                + KEY_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_IS_SETUP + " INTEGER DEFAULT 0" + ")";

        db.execSQL(createStudentsTable);
        db.execSQL(createFacultyTable);
        db.execSQL(createAdminTable);

        // Insert initial admin setup record
        ContentValues adminValues = new ContentValues();
        adminValues.put(KEY_IS_SETUP, 0);
        db.insert(TABLE_ADMIN, null, adminValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add description column if upgrading from version 1 or 2
            try {
                db.execSQL("ALTER TABLE " + TABLE_STUDENTS + " ADD COLUMN " + KEY_DESCRIPTION + " TEXT");
            } catch (Exception e) {
                // Column might already exist, ignore
            }

            // Create admin table if not exists
            String createAdminTable = "CREATE TABLE IF NOT EXISTS " + TABLE_ADMIN + "("
                    + KEY_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + KEY_IS_SETUP + " INTEGER DEFAULT 0" + ")";
            db.execSQL(createAdminTable);

            // Insert initial record if not exists
            ContentValues adminValues = new ContentValues();
            adminValues.put(KEY_IS_SETUP, 0);
            db.insert(TABLE_ADMIN, null, adminValues);
        }
    }

    // Check if admin setup is complete
    public boolean isAdminSetupComplete() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ADMIN, new String[]{KEY_IS_SETUP},
                null, null, null, null, null);

        boolean isSetup = false;
        if (cursor.moveToFirst()) {
            isSetup = cursor.getInt(0) == 1;
        }
        cursor.close();
        db.close();
        return isSetup;
    }

    // Mark admin setup as complete
    public void completeAdminSetup() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_SETUP, 1);
        db.update(TABLE_ADMIN, values, null, null);
        db.close();
    }

    // Add a faculty member
    public long addFaculty(String username, String password, String subject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD_HASH, hashPassword(password));
        values.put(KEY_FACULTY_SUBJECT, subject);

        long result = db.insert(TABLE_FACULTY, null, values);
        db.close();
        return result;
    }

    // Authenticate faculty login
    public String authenticateFaculty(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        String[] columns = {KEY_FACULTY_SUBJECT};
        String selection = KEY_USERNAME + " = ? AND " + KEY_PASSWORD_HASH + " = ?";
        String[] selectionArgs = {username, hashedPassword};

        Cursor cursor = db.query(TABLE_FACULTY, columns, selection,
                selectionArgs, null, null, null);

        String subject = null;
        if (cursor.moveToFirst()) {
            subject = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return subject;
    }

    // Simple password hashing using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((password + "bonus_salt_2024").getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(password.hashCode());
        }
    }

    // Get count of faculty members
    public int getFacultyCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FACULTY, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // Add student WITH description
    public long addStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_REGISTRATION_NO, student.getRegistrationNo());
        values.put(KEY_NAME, student.getName());
        values.put(KEY_POINTS, student.getPoints());
        values.put(KEY_DATE_AWARDED, student.getDateAwarded());
        values.put(KEY_FACULTY_NAME, student.getFacultyName());
        values.put(KEY_SUBJECT, student.getSubject());
        values.put(KEY_DESCRIPTION, student.getDescription()); // ADD DESCRIPTION

        long result = db.insert(TABLE_STUDENTS, null, values);
        db.close();
        return result;
    }

    // Get all students by subject WITH description
    public List<Student> getAllStudentsBySubject(String subject) {
        List<Student> studentList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STUDENTS +
                " WHERE " + KEY_SUBJECT + " = ? ORDER BY " + KEY_DATE_AWARDED + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{subject});

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.setId(cursor.getInt(0));
                student.setRegistrationNo(cursor.getString(1));
                student.setName(cursor.getString(2));
                student.setPoints(cursor.getInt(3));
                student.setDateAwarded(cursor.getString(4));
                student.setFacultyName(cursor.getString(5));
                student.setSubject(cursor.getString(6));
                student.setDescription(cursor.getString(7)); // GET DESCRIPTION

                studentList.add(student);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return studentList;
    }

    // Get total points by student
    public List<Student> getTotalPointsByStudent(String subject) {
        List<Student> studentList = new ArrayList<>();
        String selectQuery = "SELECT " + KEY_REGISTRATION_NO + ", " + KEY_NAME +
                ", SUM(" + KEY_POINTS + ") as total_points FROM " + TABLE_STUDENTS +
                " WHERE " + KEY_SUBJECT + " = ? GROUP BY " + KEY_REGISTRATION_NO +
                ", " + KEY_NAME + " ORDER BY total_points DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{subject});

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.setRegistrationNo(cursor.getString(0));
                student.setName(cursor.getString(1));
                student.setPoints(cursor.getInt(2));
                student.setSubject(subject);

                studentList.add(student);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return studentList;
    }

    // Update student points
    public int updateStudentPoints(int studentId, int newPoints) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_POINTS, newPoints);

        int result = db.update(TABLE_STUDENTS, values,
                KEY_ID + " = ?", new String[]{String.valueOf(studentId)});
        db.close();
        return result;
    }

    // Delete student
    public void deleteStudent(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STUDENTS, KEY_ID + " = ?",
                new String[]{String.valueOf(studentId)});
        db.close();
    }

    // Get student count
    public int getStudentCount(String subject) {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_STUDENTS +
                " WHERE " + KEY_SUBJECT + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, new String[]{subject});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }
}
